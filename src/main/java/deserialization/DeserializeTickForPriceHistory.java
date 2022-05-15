package deserialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import model.BuyOrderTick;
import model.PriceHistoryTick;

import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeserializeTickForPriceHistory extends StdDeserializer<List<PriceHistoryTick>> {
    public DeserializeTickForPriceHistory(Class<?> vc) {
        super(vc);
    }
    public DeserializeTickForPriceHistory(){
        this(null);
    }

    @Override
    public List<PriceHistoryTick> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper)jsonParser.getCodec();
        JsonNode node = mapper.readTree(jsonParser);
        boolean isArray = node.isArray();
        DateTimeFormatter simpleDateFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM dd yyyy HH")
                .toFormatter(Locale.ENGLISH);

        if(isArray){
            int arraySize = node.size();
            List<PriceHistoryTick> ticks = new ArrayList<>();
            for (int i = 0; i < arraySize; i++) {
                JsonNode embeddedArray = node.get(i);
                boolean isEmbeddedArray = embeddedArray.isArray();

                if(isEmbeddedArray){
                    String dateStr = embeddedArray.get(0).textValue();
                    int dateStrLen = dateStr.length();
                    String extracted = dateStr.substring(0, dateStrLen-4);
                    LocalDate date = LocalDate.parse(extracted, simpleDateFormat);

                    double price = embeddedArray.get(1).doubleValue();

                    int amount = Integer.parseInt(embeddedArray.get(2).textValue().replace("\"", ""));

                    ticks.add(PriceHistoryTick.builder()
                                    .price(price)
                                    .soldAmount(amount)
                                    .date(date)
                            .build());
                }
            }
            return ticks;
        }
        return null;
    }
}
