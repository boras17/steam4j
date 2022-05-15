package steamservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import constants.SteamEndpoints;
import model.*;
import notification.Notifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import steamenums.Country;
import steamenums.Game;
import steamenums.ResponseStatusCompartment;
import utils.EndpointUtils;
import utils.URLBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamMarketplace {

        private ConcurrentHashMap<Integer, Thread> itemThread = new ConcurrentHashMap<>();
        private CopyOnWriteArrayList<ItemSnipingData> snipedItems = new CopyOnWriteArrayList<>();
        private Country country;

        private final HttpClient client;

        public SteamMarketplace(HttpClient client) {
            this.client = client;
        }

        public SteamMarketplace(){
            this(HttpClient.newBuilder().build());
        }

        public void setFetchingCountry(Country country){
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
            String item_endpoint = SteamEndpoints.ITEM_NAME_ID_ENDPOINT.replace("{gameId}",String.valueOf(game.getGameId()));

            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.baseUrl(item_endpoint);
            urlBuilder.addPathVariable(item_name_hash);

            String str_resource_address = urlBuilder.buildStringUrl();
            try{

                Document itemPage = Jsoup.connect(str_resource_address)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36")
                        .get();
                List<Element> scripts = itemPage.body().getElementsByTag("script");
                Element lastScript = scripts.get(scripts.size()-1);
                String script_text = lastScript.toString();
                Pattern pattern = Pattern.compile("Market_LoadOrderSpread\\( ([0-9]*) \\);");
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

        public void startSnipingItem(ExecutorService executorService, Notifier notifier){
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
