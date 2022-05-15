package model.trademodel.buy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuyOrderStatus {
    private boolean success;
    private boolean active;
    private int purchased;
    private int quantity;
    @JsonProperty("quantity_remaining")
    private int quantityRemaining;
    @JsonProperty("purchases")
    private List<Purchase> game;
}
