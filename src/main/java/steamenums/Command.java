package steamenums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command{
    SOMEBODY_BUYS(Pattern.compile("(tworzy zlecenia kupna \\([0-9]{0,}\\) za [0-9]{0,99},[0-9]{0,99}zł)"), Signal.SELL),
    UNRECOGNIZED_COMMAND(null, null),
    SOMEBODY_SELLS(Pattern.compile("(wystawia ten przedmiot na sprzedaż za [0-9]{0,99},[0-9]{0,99}[a-zA-Z]{0,99})"), Signal.BUY);

    Pattern pattern;
    Signal signal;

    Command(Pattern pattern, Signal signal) {this.pattern = pattern; this.signal = signal;}

    public Pattern getPattern() {
        return this.pattern;
    }
    public Signal getSignal() {
        return this.signal;
    }

    public static Command calculate(String text){
        Pattern buy_pattern = SOMEBODY_BUYS.getPattern();
        Pattern sell_pattern = SOMEBODY_SELLS.getPattern();

        Matcher matcher = buy_pattern.matcher(text);
        if(matcher.find()){
            return SOMEBODY_BUYS;
        }else{
            matcher = sell_pattern.matcher(text);
            if(matcher.find()){
                return SOMEBODY_SELLS;
            }
        }
        return UNRECOGNIZED_COMMAND;
    }

}
