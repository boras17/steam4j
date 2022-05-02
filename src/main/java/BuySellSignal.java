import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
class BuySellSignal{
    private String author;
    private double price;
    private String msg;
    private Signal signal;
    private long timeStamp;
}