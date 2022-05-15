package model.trademodel.sell;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.DeserializeSuccessFromIntToBoolean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellOrderDetails {
    private boolean success;
    @JsonDeserialize(using = DeserializeSuccessFromIntToBoolean.class)
    @JsonProperty("requires_confirmation")
    private boolean requiresConfirmation;
}
