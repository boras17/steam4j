package mail;

import model.BuySellSignal;

public class EmailNotificationStrategy implements ActivityNotificationStrategy {

    @Override
    public void handleNotification(BuySellSignal event) {
        System.out.println(event);
    }

}