package steamservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import constants.HttpConstants;
import constants.SteamEndpoints;
import model.PriceHistory;
import steamenums.Game;
import steamenums.ResponseStatusCompartment;
import utils.EndpointUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AuthenticatedMarketplaceData {

    private HttpClient sessionClient;

    public AuthenticatedMarketplaceData(HttpClient sessionClient){
        this.sessionClient = sessionClient;
    }

    public Optional<PriceHistory> getItemPriceHistory(Game game, String itemName) {
        String base_url = SteamEndpoints.STEAM_PRICE_HISTORY;
        Map<String, Object> params = new LinkedHashMap<>(Map.of("appid", game.getGameId(), "market_hash_name",itemName));
        URI uri = EndpointUtils.addParamsToURL(params, base_url);
        System.out.println(uri);
        HttpRequest priceHistoryRequest = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .setHeader(HttpConstants.USER_AGENT, HttpConstants.MOZILLA_USER_AGENT)
                .build();
        try{
            HttpResponse<String> priceHistoryResponse = this.sessionClient.send(priceHistoryRequest, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = ObjectMapperConfig.getObjectMapper();
            int responseStatus = priceHistoryResponse.statusCode();
            if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){
                PriceHistory priceHistory = mapper.readValue(priceHistoryResponse.body(), PriceHistory.class);
                boolean isSuccess = priceHistory.isSuccess();
                return isSuccess ? Optional.of(priceHistory) : Optional.empty();
            }else if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("an redirection occurred while trying to request ");
            }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("Server error occured while trying to get price history data");
            }
            return Optional.empty();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
