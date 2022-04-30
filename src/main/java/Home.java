import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

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

    @ToString
    @Getter
    @Setter
    @Builder
    public static class BuyOrderTick{
        private double price;
        private int sold_amount;
        private String sold_info;
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
        private String baseURL;

        public URLBuilder addParam(String paramKey, Object paramValue){
            params.putIfAbsent(paramKey, paramValue);
            return this;
        }
        public URLBuilder baseUrl(String baseURL){
            this.baseURL = baseURL;
            return this;
        }
        public URI buildUri(){
            StringBuilder urlBuilder = new StringBuilder(this.baseURL);
            urlBuilder.append('?');
            Iterator<Map.Entry<String, Object>> paramsIterator = this.params.entrySet().iterator();
            while(paramsIterator.hasNext()){
                Map.Entry<String, Object> paramEntry = paramsIterator.next();
                urlBuilder.append(paramEntry.getKey()).append('=').append(paramEntry.getValue()).append(paramsIterator.hasNext()?'&':"");
            }
            return URI.create(urlBuilder.toString());
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

    private static class Fetcher{
        private URI buildURIForRequestObject(RequestObject requestObject){
            return new URLBuilder()
                    .baseUrl(ItemOrderHistogramConstants.MARKETPLACE_URL)
                    .addParam(ItemOrderHistogramConstants.COUNTRY, requestObject.getCountryCode())
                    .addParam(ItemOrderHistogramConstants.LANGUAGE, requestObject.getLanguage())
                    .addParam(ItemOrderHistogramConstants.CURRENCY, requestObject.getCurrency())
                    .addParam(ItemOrderHistogramConstants.ITEM_ID, requestObject.getItemNameId())
                    .buildUri();
        }
        public Optional<ItemPriceHistogram> getItemPriceHistogram(RequestObject requestObject){
            HttpClient client = HttpClient.newBuilder()
                    .build();
            URI histogramEndpoint = this.buildURIForRequestObject(requestObject);
            System.out.println("histogram endpoint: " + histogramEndpoint);
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
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
                    objectMapper.readValue(jsonBody, ItemPriceHistogram.class);
                    ItemPriceHistogram priceHistogram = objectMapper.readValue(jsonBody, ItemPriceHistogram.class);
                    return Optional.ofNullable(priceHistogram);
                }
                return Optional.empty();
            }catch (InterruptedException | IOException e){
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }
    public static void main(String[] args) {
        RequestObject.Country country = RequestObject.Country.PL;
        Fetcher fetcher = new Fetcher();

        Optional<ItemPriceHistogram> result = fetcher.getItemPriceHistogram(
                new RequestObject.RequestObjectBuilder()
                        .countryCode(country.getCountryCode())
                        .language(country.getLanguageForCountry())
                        .currency(RequestObject.Currency.PL.getCurrencyCode())
                        .itemNameId(1)
                        .build()
        );
        result.ifPresent(System.out::println);
    }
}
