package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public
class ItemSnipingData{
    private int breakTimeBetweenRequestsInMillis;
    private int itemNameId;
    private SnipeCriteria snipeCriteria;
}
