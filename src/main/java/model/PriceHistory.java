package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.DeserializeTickForPriceHistory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PriceHistory {
    private boolean success;
    @JsonProperty("price_suffix")
    private String priceSuffix;
    @JsonProperty("prices")
    @JsonDeserialize(using = DeserializeTickForPriceHistory.class)
    private List<PriceHistoryTick> ticks;
    public PriceHistory(){}

}
