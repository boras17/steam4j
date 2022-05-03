package deserialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesDeserializer extends StdDeserializer<Element> {

    public ActivitiesDeserializer(){
        this(null);
    }

    public ActivitiesDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Element deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = objectMapper.readTree(jsonParser);
        if(node.isArray()){
            List<Element> activities = new ArrayList<>();
            for(final JsonNode array_filed: node){
                String arr_field_str = array_filed.toString();
                Element activity_html_element = Jsoup.parse(arr_field_str);
                activities.add(activity_html_element);
                return activities.get(0);
            }
        }else{
            throw new RuntimeException("deserialization.ActivitiesDeserializer have to be changed because steam is not sending array of activities");
        }

        return null;
    }
}