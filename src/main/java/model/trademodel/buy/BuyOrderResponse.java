package model.trademodel.buy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.DeserializeSuccessFromIntToBoolean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyOrderResponse {
    @JsonDeserialize(using = DeserializeSuccessFromIntToBoolean.class)
    @JsonProperty("success")
    private boolean isSuccess;
    @JsonProperty("buy_orderid")
    private int orderId;
}
