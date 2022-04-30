import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home {

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

    public static class ActivitiesDeserializer extends StdDeserializer<List<Element>>{

        public ActivitiesDeserializer(){
            this(null);
        }

        public ActivitiesDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public List<Element> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode node = objectMapper.readTree(jsonParser);

            if(node.isArray()){
                List<Element> activities = new ArrayList<>();
                for(final JsonNode array_filed: node){
                    String arr_field_str = array_filed.toString();
                    Element activity_html_element = Jsoup.parse(arr_field_str);
                    activities.add(activity_html_element);
                    return activities;
                }
            }else{
                throw new RuntimeException("ActivitiesDeserializer have to be changed because steam is not sending array of activities");
            }

            return Collections.emptyList();
        }
    }

    public static class TimeStampToLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime>{

        public TimeStampToLocalDateTimeDeserializer(){
            this(null);
        }

        public TimeStampToLocalDateTimeDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            String timestamp = jsonParser.getText().trim();
            long milliseconds = Long.parseLong(timestamp);
            return Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
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
        private List<Element> activities;
        @JsonProperty("timestamp")
        @JsonDeserialize(using = TimeStampToLocalDateTimeDeserializer.class)
        private LocalDateTime activity_time;

        public static BuySellSignal extractBuySellSignalFromHTML(List<Element> activities){
            for(Element element: activities){
                String author=element.getElementsByClass("market_ticker_name").get(0).text();
            }
            return null;
        }
    }

    interface ActivityNotificationStrategy{
        void handleNotification(ActivityForItem activityForItem);
    }

    public static class EmailNotificationStrategy implements ActivityNotificationStrategy{

        @Override
        public void handleNotification(ActivityForItem event) {
            System.out.println("I am sending email becouse i go event: " + event);
        }

    }

    public static class Notifier{
        private final ActivityNotificationStrategy eventNotificationStrategy;

        public Notifier(ActivityNotificationStrategy eventNotificationStrategy){
            this.eventNotificationStrategy = eventNotificationStrategy;
        }

        public void notifyMe(ActivityForItem notificationObject){
            this.eventNotificationStrategy.handleNotification(notificationObject);
        }
    }

    @Getter
    @Setter
    @Builder
    private static class BuySellSignal{
        private String author;
        private String message;
        private boolean sellSignal;
        private boolean buySignal;
    }

    @FunctionalInterface
    interface SignalChecker{
        BuySellSignal check(double requiredSellPrice, double requiredBuyPrice, ActivityForItem activityForItem);
    }

    @Builder
    @Getter
    @Setter
    public static class SnipeCriteria{
        private Consumer<ActivityForItem> activityCallback;
        private double requiredSellPrice;
        private double requiredBuyPrice;

       public BuySellSignal checkIfActivityIsSignal(SignalChecker signalChecker, ActivityForItem activityForItem){
           return signalChecker.check(this.requiredSellPrice, requiredBuyPrice, activityForItem);
       }
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
        /**
         * correct endpointExample=https://steamcommunity.com/market/itemordershistogram?country=US&language=english&currency=1&item_nameid=1
         */
        public static final String MARKETPLACE_URL = "https://steamcommunity.com/market/itemordershistogram";
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

    @AllArgsConstructor
    @Getter
    private static class Fetcher{
        private final HttpClient client;

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
                System.out.println(responseStatus);
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
        // TODO add notification criteria

        /**
         *
         * @param requestObject
         * @param notification criteria object with optional callback
         */
        public void snipeItemActivity(RequestObject requestObject, SnipeCriteria snipeCriteria){
            URI endpoint_uri = EndpointUtils.buildURIForRequestObject(requestObject, SteamEndpoints.ITEM_ACTIVITY);
            System.out.println(endpoint_uri);
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
                    System.out.println(activityForItem);
                    SignalChecker checker = new SignalChecker() {
                        @Override
                        public BuySellSignal check(double requiredSellPrice,
                                                   double requiredBuyPrice,
                                                   ActivityForItem activityForItem) {

                            return null;
                        }
                    };

                    //snipeCriteria.checkIfActivityIsSignal(checker, activityForItem);
                }else{
                    System.out.println("server responded with status: " + responseStatus);
                }

                String jsonResponse = response.body();
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        RequestObject.Country country = RequestObject.Country.PL;

        HttpClient simpleClient = HttpClient.newBuilder()
                .build();

        Fetcher fetcher = new Fetcher(simpleClient);

        String str_to_encode="Operation Broken Fang Case";

        RequestObject requestObject = new RequestObject.RequestObjectBuilder()
                .countryCode(country.getCountryCode())
                .language(country.getLanguageForCountry())
                .currency(RequestObject.Currency.PL.getCurrencyCode())
                .itemNameId(14962905).build();

        fetcher.snipeItemActivity(requestObject, null);
    }
}
