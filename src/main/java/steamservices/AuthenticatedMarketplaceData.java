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
                .setHeader("cookie", "timezoneOffset=7200,0; cookieSettings=%7B%22version%22%3A1%2C%22preference_state%22%3A1%2C%22content_customization%22%3Anull%2C%22valve_analytics%22%3Anull%2C%22third_party_analytics%22%3Anull%2C%22third_party_content%22%3Anull%2C%22utm_enabled%22%3Atrue%7D; _ga=GA1.2.1586892830.1651100067; steamMachineAuth76561198826938787=EE4DDC276380B545C2FD6239FBE7190D824C5BC7; browserid=2572128446518951942; steamMachineAuth76561198364892289=AC59C04B774EBD29B42EFC55EE89B7A7C4BE6785; sessionid=a6367fe88414a30201d61a5f; steamCountry=PL%7C60571fd72817ec68887d3173e53a26cb; _gid=GA1.2.1576399216.1652546090; steamLoginSecure=76561198826938787%7C%7C5E2B801A860B7392E11B50806BC8B7CEB72243E4; webTradeEligibility=%7B%22allowed%22%3A0%2C%22reason%22%3A16680%2C%22allowed_at_time%22%3A1653693034%2C%22steamguard_required_days%22%3A15%2C%22new_device_cooldown_days%22%3A7%2C%22time_checked%22%3A1652546117%7D")
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
