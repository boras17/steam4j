package model.trademodel.sell;

import lombok.Builder;
import lombok.Getter;
import steamenums.Game;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class SellOrder {
    private Game game;
    private int contextId;
    private int assetId;
    private int amount;
    private double price;

    public Map<String, Object> getRequestParams(String sessionId){
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("appid", game.getGameId());
        params.put("contextid", game.getContextId());
        params.put("assetid", assetId);
        params.put("amount", amount);
        params.put("price", price);
        return params;
    }
}
