package model.trademodel.buy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.CurrencyDeserializer;
import deserialization.FromIntToGameEnumDeserializer;
import lombok.Getter;
import lombok.Setter;
import steamenums.Currency;
import steamenums.Game;

@Getter
@Setter
public class Purchase {
    @JsonProperty("listingid")
    private String listingId;
    @JsonProperty("price_subtotal")
    private int priceSubtotal;
    @JsonProperty("price_fee")
    private int priceFee;
    @JsonProperty("price_total")
    private int priceTotal;
    @JsonDeserialize(using = CurrencyDeserializer.class)
    private Currency currency;
    @JsonProperty("appid")
    @JsonDeserialize(using = FromIntToGameEnumDeserializer.class)
    private Game game;
    @JsonProperty("assetid")
    private String assetId;
    @JsonProperty("contextid")
    private String contextId;
    @JsonProperty("account_seller")
    private int accountSeller;
    @JsonProperty("purchase_amount_text")
    private String summaryText;
}
