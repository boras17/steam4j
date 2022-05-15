package deserialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import steamenums.Game;

import java.io.IOException;

public class FromIntToGameEnumDeserializer extends StdDeserializer<Game> {
    public FromIntToGameEnumDeserializer(Class<?> vc) {
        super(vc);
    }
    public FromIntToGameEnumDeserializer(){
        this(Game.class);
    }
    @Override
    public Game deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        int gameNumber = jsonParser.getValueAsInt();
        return Game.fromInt(gameNumber);
    }
}
