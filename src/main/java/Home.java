
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home {

    public enum Command{
        SOMEBODY_BUYS(Pattern.compile("(tworzy zlecenia kupna \\([0-9]{0,}\\) za [0-9]{0,99},[0-9]{0,99}zł)"), Signal.SELL),
        UNRECOGNIZED_COMMAND(null, null),
        SOMEBODY_SELLS(Pattern.compile("(wystawia ten przedmiot na sprzedaż za [0-9]{0,99},[0-9]{0,99}[a-zA-Z]{0,99})"), Signal.BUY);

        Pattern pattern;
        Signal signal;

        Command(Pattern pattern, Signal signal) {this.pattern = pattern; this.signal = signal;}

        public Pattern getPattern() {return this.pattern;}
        public Signal getSignal() {return this.signal;}

        public static Command calculate(String text){
            Pattern buy_pattern = SOMEBODY_BUYS.getPattern();
            Pattern sell_pattern = SOMEBODY_SELLS.getPattern();

            Matcher matcher = buy_pattern.matcher(text);
            if(matcher.find()){
                return SOMEBODY_BUYS;
            }else{
                matcher = sell_pattern.matcher(text);
                if(matcher.find()){
                    return SOMEBODY_SELLS;
                }
            }
            return UNRECOGNIZED_COMMAND;
        }

    }

    public static class HTMLUtils{
        private static Map<String, String> escapes = Map.of("&quot;","", "&amp;","&","&lt;","<","&gt;",">");

        public static String escapeHTML(String html){
            for(Map.Entry<String, String> entry: escapes.entrySet()){
                String escapeCharacter = entry.getKey();
                boolean containsEscapeCharacter = html.contains(escapeCharacter);
                if(containsEscapeCharacter){
                    html = html.replaceAll(escapeCharacter, entry.getValue());
                }
            }
            return html;
        }

        public static String cleanSlashesFromHTML(String html){
            return html.replaceAll("\\\\", "");
        }
    }
    private static class DeserializeSuccessFromIntToBoolean extends StdDeserializer<Boolean> {
        public DeserializeSuccessFromIntToBoolean(){
            this(null);
        }
        public DeserializeSuccessFromIntToBoolean(Class<?> vc) {
            super(vc);
        }

        @Override
        public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            int successValue = jsonParser.getIntValue();
            return switch (successValue) {
                case 1 -> true;
                case 0 -> false;
                default -> throw new IllegalStateException("Unexpected value: " + successValue);
            };
        }
    }

    private static class DeserializeListOfPriceTicks extends StdDeserializer<Set<BuyOrderTick>> {

        public DeserializeListOfPriceTicks(Class<?> vc) {
            super(vc);
        }

        public DeserializeListOfPriceTicks(){
            this(null);
        }

        @Override
        public Set<BuyOrderTick> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
            final JsonNode node = (JsonNode) mapper.readTree(jsonParser);

            final String nodeString = node.toString();
            final String clearedNodeString = nodeString.replaceFirst("\\[", "");
            final String finalCleared = clearedNodeString.substring(1, clearedNodeString.length()-2);

            final String[] ticks = finalCleared.split("],\\[");

            final Set<BuyOrderTick> buyOrderTicks = new HashSet<>();

            for(final String tick: ticks){
                final String[] tickPartsArray = tick.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                final int ticks_size = tickPartsArray.length;

                if(ticks_size == 3){
                    final double price = Double.parseDouble(tickPartsArray[0]);
                    final int sold_amount = Integer.parseInt(tickPartsArray[1]);
                    final String tick_additional_info = tickPartsArray[2];
                    final BuyOrderTick buyOrderTick = new BuyOrderTick.BuyOrderTickBuilder()
                            .price(price)
                            .sold_amount(sold_amount)
                            .sold_info(tick_additional_info)
                            .build();
                    buyOrderTicks.add(buyOrderTick);
                }else{
                    throw new RuntimeException("Invalid number of tick parts during deserialization. Expected: [price, amount, information_string]");
                }
            }
            return buyOrderTicks;
        }
    }

    public static class ActivitiesDeserializer extends StdDeserializer<Element>{

        public ActivitiesDeserializer(){
            this(null);
        }

        public ActivitiesDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Element deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode node = objectMapper.readTree(jsonParser);
            if(node.isArray()){
                List<Element> activities = new ArrayList<>();
                for(final JsonNode array_filed: node){
                    String arr_field_str = array_filed.toString();
                    Element activity_html_element = Jsoup.parse(arr_field_str);
                    activities.add(activity_html_element);
                    return activities.get(0);
                }
            }else{
                throw new RuntimeException("ActivitiesDeserializer have to be changed because steam is not sending array of activities");
            }

            return null;
        }
    }


    @ToString
    @Getter
    @Setter
    @Builder
    public static class BuyOrderTick{
        private double price;
        private int sold_amount;
        private String sold_info;
    }

    enum Game{
        COUNTER_STRIKE(730);
        private final int game_id;
        Game(int game_id) {this.game_id = game_id;}
        public int getGameId() {return this.game_id;}
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ActivityForItem{
        @JsonProperty("success")
        @JsonDeserialize(using = DeserializeSuccessFromIntToBoolean.class)
        private boolean success;
        @JsonProperty("activity")
        @JsonDeserialize(using = ActivitiesDeserializer.class)
        private Element activities;
        @JsonProperty("timestamp")
        private long activityTimestamp;

        public static BuySellSignal extractBuySellSignalFromHTML(Element element, long activity_time){
                Optional<Element> elementChecker = Optional.ofNullable(element);

                if(elementChecker.isEmpty()){
                    return null;
                }
                Element body = elementChecker.get().getElementsByTag("body").get(0);

                String escape = HTMLUtils.escapeHTML(body.toString());
                String cleaned = HTMLUtils.cleanSlashesFromHTML(escape);

                Element div = Jsoup.parse(cleaned).body();

                String author = div.getElementsByClass("market_ticker_name").get(0).text();
                String message = div.getElementsByClass("market_activity_line_item").get(0).getElementsByTag("span").get(0).text();

                Pattern pattern = Pattern.compile("([0-9]{1,},[0-9]{0,})");
                Matcher matcher = pattern.matcher(message);
                double price = 0;
                if(matcher.find()){
                    String[] pricePart = matcher.group(1).split(",");
                    if(pricePart.length == 2){
                        price = Double.parseDouble(pricePart[0]) + (Double.parseDouble(pricePart[1])/100);
                    }else{
                        price = Double.parseDouble(pricePart[0]);
                    }
                }
                Command command = Command.calculate(message);

                if(!Command.UNRECOGNIZED_COMMAND.equals(command)){
                    return new BuySellSignal.BuySellSignalBuilder()
                            .author(author)
                            .msg(message)
                            .price(price)
                            .timeStamp(activity_time)
                            .signal(command.getSignal())
                            .build();
                }else{
                    return null;
                }
        }
    }

    interface ActivityNotificationStrategy{
        void handleNotification(BuySellSignal activityForItem);
    }

    public static class EmailNotificationStrategy implements ActivityNotificationStrategy{

        @Override
        public void handleNotification(BuySellSignal event) {
            System.out.println(event);
        }

    }

    public static class Notifier{
        private final ActivityNotificationStrategy eventNotificationStrategy;

        public Notifier(ActivityNotificationStrategy eventNotificationStrategy){
            this.eventNotificationStrategy = eventNotificationStrategy;
        }

        public void notifyMe(BuySellSignal notificationObject){
            this.eventNotificationStrategy.handleNotification(notificationObject);
        }
    }

    enum Signal{
        SELL,
        BUY
    }

    @ToString
    @Getter
    @Setter
    @Builder
    private static class BuySellSignal{
        private String author;
        private double price;
        private String msg;
        private Signal signal;
        private long timeStamp;
    }


    @Builder
    @Getter
    @Setter
    public static class SnipeCriteria{
        private Predicate<BuySellSignal> activityCallback;
        private double requiredSellPrice;
        private double requiredBuyPrice;
    }

    @ToString
    @Getter
    @Setter
    public static class ItemPriceHistogram{
        @JsonDeserialize(using = DeserializeSuccessFromIntToBoolean.class)
        private boolean success;
        @JsonProperty("buy_order_graph")
        @JsonDeserialize(using = DeserializeListOfPriceTicks.class)
        private Set<BuyOrderTick> historicalPriceData;
        @JsonProperty("highest_buy_order")
        private double highestPrice;
        @JsonProperty("lowest_sell_order")
        private double lowestPrice;
    }

    public enum ResponseStatusCompartment{
        SUCCESS(200, 300), SERVER_ERROR(500,600),
        CLIENT_ERROR(400, 500), REDIRECT_ERROR(300, 400);
        private final int fromInclusive,toExclusive;
        ResponseStatusCompartment(int fromInclusive, int toExclusive) {this.toExclusive = toExclusive; this.fromInclusive = fromInclusive;}
        public int getFromInclusive(){return this.fromInclusive;}
        public int getToExclusive(){return this.toExclusive;}
        public boolean checkIfStatusEquals(int status ){
            int min = this.getFromInclusive();
            int max = this.getToExclusive();
            return status >= min && status < max;
        }
    }

    public static class SteamEndpoints{
        public static final String ITEM_ENDPOINT="https://steamcommunity.com/market/listings/{gameId}";
        public static final String ITEM_ACTIVITY="https://steamcommunity.com/market/itemordersactivity";
    }

    public static class ItemOrderHistogramConstants{
        public static final String COUNTRY = "country";
        public static final String LANGUAGE = "language";
        public static final String CURRENCY = "currency";
        public static final String ITEM_ID = "item_nameid";
    }

    public static class URLBuilder{
        private Map<String, Object> params = new LinkedHashMap<>();
        private StringBuilder baseURL;

        public URLBuilder addParam(String paramKey, Object paramValue){
            params.putIfAbsent(paramKey, paramValue);
            return this;
        }
        public URLBuilder baseUrl(String baseURL){
            this.baseURL = new StringBuilder(baseURL);
            return this;
        }
        public URLBuilder addPathVariable(String pathVariable){
            this.baseURL.append("/".concat(pathVariable));
            return this;
        }
        private String build(){
            StringBuilder urlBuilder = new StringBuilder(this.baseURL);
            urlBuilder.append('?');
            Iterator<Map.Entry<String, Object>> paramsIterator = this.params.entrySet().iterator();
            while(paramsIterator.hasNext()){
                Map.Entry<String, Object> paramEntry = paramsIterator.next();
                urlBuilder.append(paramEntry.getKey()).append('=').append(paramEntry.getValue()).append(paramsIterator.hasNext()?'&':"");
            }
            return urlBuilder.toString();
        }
        public URI buildUri(){

            return URI.create(this.build());
        }
        public String buildStringUrl(){
            return this.build();
        }
    }

    @Getter
    @Setter
    @Builder
    private static class RequestObject{
        private String countryCode;
        private String language;
        private int currency;
        private int itemNameId;

        enum Currency{
            PL(6), USD(1), EUR(3);
            private final int code;
            Currency(int code) {this.code = code;}
            public int getCurrencyCode() {return code;}
        }

        enum Country{
            PL("PL","polish"), US("US", "english");
            private final String countryCode;
            private final String countryLanguage;
            Country(String countryCode, String countryLanguage){this.countryCode = countryCode; this.countryLanguage = countryLanguage;}
            String getCountryCode() {return this.countryCode;} String getLanguageForCountry(){return this.countryLanguage;}
        }

        public static RequestObject buildDefaultRequestObjectForCountry(Country country, int itemNameId){
            // TODO default build for EUR
            return switch (country){
                case PL -> new RequestObjectBuilder()
                        .countryCode(Country.PL.getCountryCode())
                        .currency(Currency.PL.getCurrencyCode())
                        .language(Country.PL.getLanguageForCountry())
                        .itemNameId(itemNameId)
                        .build();
                case US -> new RequestObjectBuilder()
                        .countryCode(Country.US.getCountryCode())
                        .currency(Currency.USD.getCurrencyCode())
                        .language(Country.US.getLanguageForCountry())
                        .itemNameId(itemNameId)
                        .build();
            };
        }

    }

    private static class EndpointUtils{
        public static URI buildURIForRequestObject(RequestObject requestObject, String base_url){
            return new URLBuilder()
                    .baseUrl(base_url)
                    .addParam(ItemOrderHistogramConstants.COUNTRY, requestObject.getCountryCode())
                    .addParam(ItemOrderHistogramConstants.LANGUAGE, requestObject.getLanguage())
                    .addParam(ItemOrderHistogramConstants.CURRENCY, requestObject.getCurrency())
                    .addParam(ItemOrderHistogramConstants.ITEM_ID, requestObject.getItemNameId())
                    .buildUri();
        }
        public static String encodeStringToUrlStandard(String str){

            Map<String, String> character_code = new HashMap<>();
            character_code.put(" ", "%20");
            character_code.put("!", "%21");
            character_code.put("#", "%23");
            character_code.put("$","%24");
            character_code.put("%", "%25");
            character_code.put("&", "%26");
            character_code.put("(", "%28");
            character_code.put(")","%29");
            character_code.put("*", "%2A");
            character_code.put("+", "%2B");
            character_code.put(",", "%2C");
            character_code.put("/", "%2F");
            character_code.put(":", "%3A");
            character_code.put(";", "%3B");
            character_code.put("=", "%3D");
            character_code.put("?", "%3F");
            character_code.put("@", "%40");
            character_code.put("[", "%5B");
            character_code.put("]", "%5D");
            character_code.put("'", "%27");
            character_code.put("|","%7C");

            StringBuilder encodedBuilder = new StringBuilder();

            int str_len = str.length();

            for(int i = 0; i < str_len; ++i){
                char current_character = str.charAt(i);

                String current_character_str = String.valueOf(current_character);
                boolean should_be_encoded =  character_code.containsKey(current_character_str);
                if(should_be_encoded){
                    String encoded_character = character_code.get(current_character_str);
                    encodedBuilder.append(encoded_character);
                }else{
                    encodedBuilder.append(current_character);
                }
            }
            return encodedBuilder.toString();
        }
    }

    public static class ObjectMapperConfig{
        public static ObjectMapper getObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
            return objectMapper;
        }
    }

    public static class ObservedItems{
        private static Map<String, SnipeCriteria> itemNameIdSnipeCriteria = new HashMap<>();

        public static void loadDataFromFile(File configFile){
            ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
            try{
                Map<String, SnipeCriteria> config = objectMapper.readValue(configFile, new TypeReference<Map<String, SnipeCriteria>>() {});
                itemNameIdSnipeCriteria.putAll(config);
            }catch (java.io.IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Getter
    @Setter
    @Builder
    private static class ItemSnipingData{
        private int breakTimeBetweenRequestsInMillis;
        private int itemNameId;
        private SnipeCriteria snipeCriteria;
    }

    @Getter
    private static class Fetcher{
        private ConcurrentHashMap<Integer, Thread> itemThread = new ConcurrentHashMap<>();
        private CopyOnWriteArrayList<ItemSnipingData> snipedItems = new CopyOnWriteArrayList<>();
        private RequestObject.Country country;

        private final HttpClient client;

        public Fetcher(HttpClient client) {
            this.client = client;
        }

        public void setFetchingCountry(RequestObject.Country country){
            this.country = country;
        }

        public Optional<ItemPriceHistogram> getItemPriceHistogram(RequestObject requestObject){
            HttpClient client = this.client;
            URI histogramEndpoint = EndpointUtils.buildURIForRequestObject(requestObject, SteamEndpoints.ITEM_ENDPOINT);

            HttpRequest itemPriceHistogramRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(histogramEndpoint)
                    .build();
            try{
                HttpResponse<String> itemPriceHistogramJsonResponse = client.send(  itemPriceHistogramRequest,
                        HttpResponse.BodyHandlers.ofString());
                int responseStatus = itemPriceHistogramJsonResponse.statusCode();
                if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){
                    String jsonBody = itemPriceHistogramJsonResponse.body();
                    ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
                    ItemPriceHistogram priceHistogram = objectMapper.readValue(jsonBody, ItemPriceHistogram.class);
                    return Optional.ofNullable(priceHistogram);
                }
                return Optional.empty();
            }catch (InterruptedException | IOException e){
                e.printStackTrace();
                return Optional.empty();
            }
        }
        public Optional<Integer> getItemNameIdByItemName(String itemName, Game game){
            String item_name_hash = EndpointUtils.encodeStringToUrlStandard(itemName);
            String item_endpoint = SteamEndpoints.ITEM_ENDPOINT.replace("{gameId}",String.valueOf(game.getGameId()));

            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.baseUrl(item_endpoint);
            urlBuilder.addPathVariable(item_name_hash);

            String str_resource_address = urlBuilder.buildStringUrl();
            try{
                System.out.println(str_resource_address);
                Document itemPage = Jsoup.connect(str_resource_address)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36")
                        .get();
                List<Element> scripts = itemPage.body().getElementsByTag("script");
                Element lastScript = scripts.get(scripts.size()-1);
                String script_text = lastScript.toString();
                Pattern pattern = Pattern.compile("ItemActivityTicker.Start\\( ([0-9]*) \\);");
                Matcher matcher = pattern.matcher(script_text);
                if(matcher.find()){
                    String identifier = matcher.group(1);
                    return Optional.of(Integer.parseInt(identifier));
                }
            }catch (java.io.IOException e){
                e.printStackTrace();
            }
            return Optional.empty();
        }

        public BuySellSignal tryToGetSignal(RequestObject requestObject){
            URI endpoint_uri = EndpointUtils.buildURIForRequestObject(requestObject, SteamEndpoints.ITEM_ACTIVITY);

            HttpRequest hitEndpointForCheckingActions = HttpRequest.newBuilder()
                    .GET()
                    .uri(endpoint_uri)
                    .build();
            try{
                HttpResponse<String> response = this.client.send(hitEndpointForCheckingActions, HttpResponse.BodyHandlers.ofString());
                int responseStatus = response.statusCode();

                if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){

                    String jsonBody = response.body();

                    ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
                    ActivityForItem activityForItem = objectMapper.readValue(jsonBody, ActivityForItem.class);

                    long activityTimestamp = activityForItem.getActivityTimestamp();

                    return ActivityForItem.extractBuySellSignalFromHTML(activityForItem.getActivities(),activityTimestamp);

                }else{
                    throw new RuntimeException("server responded with "+responseStatus+" status");
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }

        public void tryToStopSnipingItem(int itemNameId){
            boolean itemPresentInMap = this.itemThread.containsKey(itemNameId);
            if(itemPresentInMap){
                Thread itemThread = this.itemThread.get(itemNameId);
                itemThread.interrupt();
                this.itemThread.remove(itemNameId);
                this.snipedItems.removeIf(new Predicate<ItemSnipingData>() {
                    @Override
                    public boolean test(ItemSnipingData itemSnipingData) {
                        return itemSnipingData.getItemNameId() == itemNameId;
                    }
                });
            }else{
                throw new RuntimeException("Item not present in map");
            }
        }

        public Runnable tryToGetSignalDistinct(RequestObject requestObject, SnipeCriteria snipeCriteria, Notifier notifier) {
            return () -> {
                long prevResponseTimestamp = 0;
                itemThread.putIfAbsent(requestObject.getItemNameId(), Thread.currentThread());

                while (!Thread.currentThread().isInterrupted()) {
                    try{
                        Thread.sleep(300);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    BuySellSignal buySellSignal = this.tryToGetSignal(requestObject);
                    if(buySellSignal == null){
                        continue;
                    }
                    if (prevResponseTimestamp == 0) {
                        prevResponseTimestamp = buySellSignal.getTimeStamp();
                    } else {
                        long currentDateResponse = buySellSignal.getTimeStamp();
                        if (currentDateResponse == prevResponseTimestamp) {
                            continue;
                        }
                        prevResponseTimestamp = buySellSignal.getTimeStamp();
                    }
                    boolean should_notify = snipeCriteria.getActivityCallback()
                            .test(buySellSignal);
                    if (should_notify) {
                        notifier.notifyMe(buySellSignal);
                    }
                }
            };
        }

        public void addSnipedItem(ItemSnipingData itemSnipingData){
            this.snipedItems.add(itemSnipingData);
        }

        public void addSnipedItems(List<ItemSnipingData> items){
            this.snipedItems.addAll(items);
        }

        public void startSnipingItem(ExecutorService executorService,  Notifier notifier){
            int listSize = this.snipedItems.size();
            ItemSnipingData lastAddedSnipedItem = this.snipedItems.get(listSize-1);
            // create request object for this item
            RequestObject requestObjectForNewItem = RequestObject.buildDefaultRequestObjectForCountry(this.country, lastAddedSnipedItem.getItemNameId());
            executorService.submit(this.tryToGetSignalDistinct( requestObjectForNewItem,
                                                                lastAddedSnipedItem.getSnipeCriteria(),
                                                                notifier));
        }

        public void startSniping(ExecutorService executorService,  Notifier notifier){
            for(ItemSnipingData snipe: this.snipedItems){
                int itemNameId = snipe.getItemNameId();
                RequestObject requestObject = RequestObject.buildDefaultRequestObjectForCountry(this.country, itemNameId);
                executorService.submit(this.tryToGetSignalDistinct(requestObject, snipe.getSnipeCriteria(), notifier));
            }
        }

    }


    public static void main(String[] args) {
        HttpClient simpleClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        Fetcher fetcher = new Fetcher(simpleClient);

        fetcher.setFetchingCountry(RequestObject.Country.PL);
        fetcher.addSnipedItem(new ItemSnipingData.ItemSnipingDataBuilder()
                .itemNameId(14962905)
                .breakTimeBetweenRequestsInMillis(100)
                .snipeCriteria(new SnipeCriteria.SnipeCriteriaBuilder()
                        .activityCallback(new Predicate<BuySellSignal>() {
                            @Override
                            public boolean test(BuySellSignal buySellSignal) {
                                return true;
                            }
                        })
                        .build())
                .build());

        Notifier notifier = new Notifier(new EmailNotificationStrategy());

        fetcher.startSniping(Executors.newSingleThreadExecutor(), notifier);


    }
}
