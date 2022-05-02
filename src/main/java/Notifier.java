public class Notifier{
    private final ActivityNotificationStrategy eventNotificationStrategy;

    public Notifier(ActivityNotificationStrategy eventNotificationStrategy){
        this.eventNotificationStrategy = eventNotificationStrategy;
    }

    public void notifyMe(BuySellSignal notificationObject){
        this.eventNotificationStrategy.handleNotification(notificationObject);
    }
}
