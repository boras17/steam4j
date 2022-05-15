package model.trademodel.buy;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class BuyOrder {
    private Billing billing;
    private BuyOrderDetails buyOrderDetails;
    public Map<String, Object> getRequestParams(String sessionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionid", sessionId);
        params.put("currency", this.buyOrderDetails.getCurrency().getCurrencyCode());
        params.put("appid", this.buyOrderDetails.getGame().getGameId());
        params.put("market_hash_name", this.buyOrderDetails.getMarketMashName());
        params.put("price_total", this.buyOrderDetails.getTotal());
        params.put("quantity", this.buyOrderDetails.getQuantity());
        params.put("first_name", this.billing.getFirstName());
        params.put("last_name", this.billing.getLastName());
        params.put("billing_address", this.billing.getBillingAddress());
        params.put("billing_address_two", this.billing.getBillingAddressTwo().orElse(""));
        params.put("billing_country", this.billing.getBillingCountry().getCountryCode());
        params.put("billing_city", this.billing.getCity());
        params.put("billing_state", this.billing.getBillingState().orElse(""));
        params.put("billing_postal_code", this.billing.getBillingPostalCode());
        params.put("save_my_address", this.billing.isSaveMyAddress() ? 1 : 0);
        return params;
    }
}
