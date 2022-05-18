package deserialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.listingmodel.Items;

import java.io.IOException;

public class HoverDeserializer extends StdDeserializer<Items> {
    public HoverDeserializer(Class<?> vc) {
        super(vc);
    }

    public HoverDeserializer(){
        this(Items.class);
    }

    @Override
    public Items deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }
}
