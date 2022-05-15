import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.*;
import model.BuySellSignal;
import model.PriceHistory;
import model.PriceHistoryTick;
import steamservices.AuthenticatedMarketplaceData;
import steamenums.Game;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.function.Consumer;

public class Home {
    private static class ConsoleNotifier implements ActivityNotificationStrategy{
        @Override
        public void handleNotification(BuySellSignal activityForItem) {
            System.out.println(activityForItem);
        }
    }
    public static void main(String[] args) throws CaptchaRequiredException, MessagingException, CouldNotFindSteamGuardException, IOException {

        AuthenticatedMarketplaceData steamMarketplace = new AuthenticatedMarketplaceData(HttpClient.newBuilder().build());

        steamMarketplace.getItemPriceHistory(Game.COUNTER_STRIKE, "Nova | Sand Dune (Field-Tested)")
                .ifPresent(new Consumer<PriceHistory>() {
                    @Override
                    public void accept(PriceHistory priceHistory) {
                        priceHistory.getTicks()
                                .forEach(new Consumer<PriceHistoryTick>() {
                                    @Override
                                    public void accept(PriceHistoryTick priceHistoryTick) {
                                        System.out.println("----------------------------");
                                        System.out.println("price: " + priceHistoryTick.getPrice());
                                        System.out.println("date: " + priceHistoryTick.getDate());
                                        System.out.println("----------------------------");
                                        System.out.println();
                                    }
                                });
                    }
                });

    }
}