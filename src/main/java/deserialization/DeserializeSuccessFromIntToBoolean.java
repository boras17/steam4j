package deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DeserializeSuccessFromIntToBoolean extends StdDeserializer<Boolean> {
    public DeserializeSuccessFromIntToBoolean(){
        this(null);
    }
    public DeserializeSuccessFromIntToBoolean(Class<?> vc) {
        super(vc);
    }

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        int successValue = jsonParser.getIntValue();
        return switch (successValue) {
            case 1 -> true;
            case 0 -> false;
            default -> throw new IllegalStateException("Unexpected value: " + successValue);
        };
    }
}
