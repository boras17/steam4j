package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PriceHistoryTick {
    private LocalDate date;
    private double price;
    private int soldAmount;
}
