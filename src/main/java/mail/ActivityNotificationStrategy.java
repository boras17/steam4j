package mail;

import model.BuySellSignal;

public interface ActivityNotificationStrategy{
    void handleNotification(BuySellSignal activityForItem);
}