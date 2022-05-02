import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class DeserializeListOfPriceTicks extends StdDeserializer<Set<BuyOrderTick>> {

    public DeserializeListOfPriceTicks(Class<?> vc) {
        super(vc);
    }

    public DeserializeListOfPriceTicks(){
        this(null);
    }

    @Override
    public Set<BuyOrderTick> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final JsonNode node = (JsonNode) mapper.readTree(jsonParser);

        final String nodeString = node.toString();
        final String clearedNodeString = nodeString.replaceFirst("\\[", "");
        final String finalCleared = clearedNodeString.substring(1, clearedNodeString.length()-2);

        final String[] ticks = finalCleared.split("],\\[");

        final Set<BuyOrderTick> buyOrderTicks = new HashSet<>();

        for(final String tick: ticks){
            final String[] tickPartsArray = tick.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            final int ticks_size = tickPartsArray.length;

            if(ticks_size == 3){
                final double price = Double.parseDouble(tickPartsArray[0]);
                final int sold_amount = Integer.parseInt(tickPartsArray[1]);
                final String tick_additional_info = tickPartsArray[2];
                final BuyOrderTick buyOrderTick = new BuyOrderTick.BuyOrderTickBuilder()
                        .price(price)
                        .sold_amount(sold_amount)
                        .sold_info(tick_additional_info)
                        .build();
                buyOrderTicks.add(buyOrderTick);
            }else{
                throw new RuntimeException("Invalid number of tick parts during deserialization. Expected: [price, amount, information_string]");
            }
        }
        return buyOrderTicks;
    }
}
