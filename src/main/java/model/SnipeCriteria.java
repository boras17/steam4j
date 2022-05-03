package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

@AllArgsConstructor
@Getter
@Setter
public  class SnipeCriteria{
    private Predicate<BuySellSignal> activityCallback;
}