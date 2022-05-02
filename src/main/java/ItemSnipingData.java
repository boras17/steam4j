import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
class ItemSnipingData{
    private int breakTimeBetweenRequestsInMillis;
    private int itemNameId;
    private SnipeCriteria snipeCriteria;
}
