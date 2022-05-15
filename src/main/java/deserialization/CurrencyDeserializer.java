package deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import steamenums.Currency;

import java.io.IOException;

public class CurrencyDeserializer extends StdDeserializer<Currency> {

    public CurrencyDeserializer(Class<?> vc) {
        super(vc);
    }

    public CurrencyDeserializer(){
        this(Currency.class);
    }

    @Override
    public Currency deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        int currency = jsonParser.getValueAsInt();
        return Currency.fromInt(currency);
    }
}
