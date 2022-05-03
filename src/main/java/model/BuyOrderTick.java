package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
public class BuyOrderTick{
    private double price;
    private int sold_amount;
    private String sold_info;
}