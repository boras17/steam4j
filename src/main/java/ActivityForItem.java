import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
@Setter
@NoArgsConstructor
public  class ActivityForItem{
    @JsonProperty("success")
    @JsonDeserialize(using = DeserializeSuccessFromIntToBoolean.class)
    private boolean success;
    @JsonProperty("activity")
    @JsonDeserialize(using = ActivitiesDeserializer.class)
    private Element activities;
    @JsonProperty("timestamp")
    private long activityTimestamp;

    public static BuySellSignal extractBuySellSignalFromHTML(Element element, long activity_time){
        Optional<Element> elementChecker = Optional.ofNullable(element);

        if(elementChecker.isEmpty()){
            return null;
        }
        Element body = elementChecker.get().getElementsByTag("body").get(0);

        String escape = HTMLUtils.escapeHTML(body.toString());
        String cleaned = HTMLUtils.cleanSlashesFromHTML(escape);

        Element div = Jsoup.parse(cleaned).body();

        String author = div.getElementsByClass("market_ticker_name").get(0).text();
        String message = div.getElementsByClass("market_activity_line_item").get(0).getElementsByTag("span").get(0).text();

        Pattern pattern = Pattern.compile("([0-9]{1,},[0-9]{0,})");
        Matcher matcher = pattern.matcher(message);
        double price = 0;
        if(matcher.find()){
            String[] pricePart = matcher.group(1).split(",");
            if(pricePart.length == 2){
                price = Double.parseDouble(pricePart[0]) + (Double.parseDouble(pricePart[1])/100);
            }else{
                price = Double.parseDouble(pricePart[0]);
            }
        }
        Command command = Command.calculate(message);

        if(!Command.UNRECOGNIZED_COMMAND.equals(command)){
            return new BuySellSignal.BuySellSignalBuilder()
                    .author(author)
                    .msg(message)
                    .price(price)
                    .timeStamp(activity_time)
                    .signal(command.getSignal())
                    .build();
        }else{
            return null;
        }
    }
}
