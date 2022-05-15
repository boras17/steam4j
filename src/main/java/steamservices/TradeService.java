package steamservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import constants.HttpConstants;
import constants.SteamEndpoints;
import model.trademodel.buy.BuyOrder;
import model.trademodel.buy.BuyOrderResponse;
import model.trademodel.buy.BuyOrderStatus;
import model.trademodel.cancelling.CancelBuyOrderResponse;
import model.trademodel.sell.SellOrder;
import model.trademodel.sell.SellOrderDetails;
import steamenums.ResponseStatusCompartment;
import utils.CookieUtils;
import utils.EndpointUtils;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

public class TradeService {
    private HttpClient sessionClient;

    public TradeService(HttpClient sessionClient){
        this.sessionClient = sessionClient;
    }

    public Optional<CancelBuyOrderResponse> cancelBuyOrder(int buyOrderId){
        Map<String, Object> params = Map.of("sessionid", this.getSessionId(), "buy_orderid", buyOrderId);
        URI cancelBuyOrderURI = EndpointUtils.addParamsToURL(params, SteamEndpoints.CANCEL_BUY_ORDER_ENDPOINT);
        HttpRequest cancelBuyOrderRequest = HttpRequest.newBuilder()
                .uri(cancelBuyOrderURI)
                .POST(HttpRequest.BodyPublishers.noBody())
                .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                .setHeader(HttpConstants.USER_AGENT, HttpConstants.MOZILLA_USER_AGENT)
                .build();
        try{
            HttpResponse<String> sellOrderResponse = this.sessionClient.send(cancelBuyOrderRequest, HttpResponse.BodyHandlers.ofString());
            int responseStatus = sellOrderResponse.statusCode();
            if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                ObjectMapper mapper = ObjectMapperConfig.getObjectMapper();
                CancelBuyOrderResponse cancelBuyOrderResponse = mapper.readValue(sellOrderResponse.body(), CancelBuyOrderResponse.class);
                return Optional.of(cancelBuyOrderResponse);
            }else if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("redirection occurred while trying to cancel buy order");
            }else if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("client error occurred while trying to cancel buy order");
            }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("server error occurred while trying to cancel buy order");
            }else{
                return Optional.empty();
            }
        }catch (InterruptedException | IOException e){
            throw new RuntimeException(e);
        }
    }

    public Optional<SellOrderDetails> placeSellOrder(SellOrder sellOrder) {
        String sessionId = this.getSessionId();
        Map<String, Object> sellOrderParams = sellOrder.getRequestParams(sessionId);
        URI sellOrderURI = EndpointUtils.addParamsToURL(sellOrderParams, SteamEndpoints.SELL_ITEM_ENDPOINT);
        HttpRequest sellOrderRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(sellOrderURI)
                .build();
        try{
            HttpResponse<String> sellOrderResponse = this.sessionClient.send(sellOrderRequest, HttpResponse.BodyHandlers.ofString());
            int responseStatus = sellOrderResponse.statusCode();
            if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                ObjectMapper mapper = ObjectMapperConfig.getObjectMapper();
                SellOrderDetails buyOrderStatus = mapper.readValue(sellOrderResponse.body(), SellOrderDetails.class);
                return Optional.of(buyOrderStatus);
            }else if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("redirection occurred while trying to sell item");
            }else if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("client error occurred while trying to sell item");
            }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("server error occurred while trying to sell item");
            }else{
                return Optional.empty();
            }
        }catch (InterruptedException | IOException e){
            throw new RuntimeException(e);
        }
    }

    private String getSessionId(){
        CookieHandler cookieHandler = this.sessionClient.cookieHandler().orElseThrow();
        return (String)CookieUtils.getCookieValue("sessionid", cookieHandler);
    }

    public Optional<BuyOrderStatus> getOrderStatus(int buyOrderId) {

            Map<String, Object> requestParams = Map.of("sessionid", getSessionId(), "buy_order_id", buyOrderId);
            URI endpointURI = EndpointUtils.addParamsToURL(requestParams, SteamEndpoints.ORDER_STATUS_ENDPOINT);

            HttpRequest orderStatusRequest = HttpRequest.newBuilder()
                    .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                    .GET()
                    .uri(endpointURI)
                    .build();
            try{
                HttpResponse<String> orderStatusResponse = this.sessionClient.send(orderStatusRequest, HttpResponse.BodyHandlers.ofString());
                int responseStatus = orderStatusResponse.statusCode();
                if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                    ObjectMapper mapper = ObjectMapperConfig.getObjectMapper();
                    BuyOrderStatus buyOrderStatus = mapper.readValue(orderStatusResponse.body(), BuyOrderStatus.class);
                    return Optional.of(buyOrderStatus);
                }else if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
                    throw new RuntimeException("redirection occurred while trying to get order status");
                }else if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                    throw new RuntimeException("client error occurred while trying to get order status");
                }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)){
                    throw new RuntimeException("server error occurred while trying to get order status");
                }else{
                    return Optional.empty();
                }
            }catch (InterruptedException | IOException e){
                throw new RuntimeException(e);
            }
    }

    public Optional<BuyOrderResponse> placeBuyOrder(BuyOrder order){
        Map<String, Object> buyOrderParams = order.getRequestParams(getSessionId());
        URI createFormUrlencodedURI = EndpointUtils.addParamsToURL(buyOrderParams, SteamEndpoints.CREATE_BUY_ORDER_ENDPOINT);

        HttpRequest buyOrderRequest = HttpRequest.newBuilder()
                .setHeader(HttpConstants.USER_AGENT, HttpConstants.MOZILLA_USER_AGENT)
                .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(createFormUrlencodedURI)
                .build();
        try{
            HttpResponse<String> jsonResponse = this.sessionClient.send(buyOrderRequest, HttpResponse.BodyHandlers.ofString());
            int responseStatus = jsonResponse.statusCode();
            if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){
                String json = jsonResponse.body();
                ObjectMapper mapper = ObjectMapperConfig.getObjectMapper();
                BuyOrderResponse orderResponse = mapper.readValue(json, BuyOrderResponse.class);

                boolean isSuccess = orderResponse.isSuccess();

                if(isSuccess){
                    return Optional.of(orderResponse);
                }
            }else if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("redirection occurred while trying to place new buy order");
            }else if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("client error occurred while trying to place new buy order");
            }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)){
                throw new RuntimeException("server error occurred while trying to place new buy order");
            }

        }catch (InterruptedException | IOException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }


}
