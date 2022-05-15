package model.trademodel.buy;

import lombok.Builder;
import lombok.Getter;
import steamenums.Currency;
import steamenums.Game;

@Getter
@Builder
public class BuyOrderDetails {
    private Currency currency;
    private Game game;
    private String marketMashName;
    private int quantity;
    private double itemPrice;

    public double getTotal(){
        return this.getItemPrice()*this.getQuantity();
    }
}
