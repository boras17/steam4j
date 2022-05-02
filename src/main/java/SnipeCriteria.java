import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

@Builder
@Getter
@Setter
public  class SnipeCriteria{
    private Predicate<BuySellSignal> activityCallback;
    private double requiredSellPrice;
    private double requiredBuyPrice;
}