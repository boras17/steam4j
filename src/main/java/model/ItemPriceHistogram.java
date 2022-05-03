package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.DeserializeListOfPriceTicks;
import deserialization.DeserializeSuccessFromIntToBoolean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@ToString
@Getter
@Setter
public class ItemPriceHistogram{
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