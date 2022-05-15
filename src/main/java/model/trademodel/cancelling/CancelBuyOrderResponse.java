package model.trademodel.cancelling;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.FromIntToGameEnumDeserializer;
import lombok.Getter;

@Getter
public class CancelBuyOrderResponse {
    @JsonDeserialize(using = FromIntToGameEnumDeserializer.class)
    private boolean success;
}
