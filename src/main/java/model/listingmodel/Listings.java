package model.listingmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserialization.HoverDeserializer;
import lombok.Getter;

import java.util.List;

@Getter
public class Listings {
    private boolean success;
    @JsonProperty("pagesize")
    private int pageSize;
    @JsonProperty("total_count")
    private int totalCount;
    @JsonProperty("hovers")
    @JsonDeserialize(using = HoverDeserializer.class)
    private Items items;
}
