import constants.SteamEndpoints;
import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.*;
import model.*;
import steamenums.Country;
import steamenums.Currency;
import steamservices.AuthenticatedMarketplaceData;
import steamenums.Game;
import steamservices.SteamMarketplace;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.function.Consumer;

public class Home {
    private static class ConsoleNotifier implements ActivityNotificationStrategy{
        @Override
        public void handleNotification(BuySellSignal activityForItem) {
            System.out.println(activityForItem);
        }
    }
    public static void main(String[] args) throws CaptchaRequiredException, MessagingException, CouldNotFindSteamGuardException, IOException {

        SteamMarketplace steamMarketplace = new SteamMarketplace(HttpClient.newBuilder().build());
        //25611250832
        Optional<ItemPriceHistogram> itemPriceHistogramOptional = steamMarketplace.getItemPriceHistogram(RequestObject.builder()
                .countryCode(Country.PL.getCountryCode())
                .currency(Currency.PL.getCurrencyCode())
                .language(Country.PL.getLanguageForCountry())
                .itemNameId(176288467)
                .build());
        itemPriceHistogramOptional.ifPresent(data -> {
            double highest = data.getHighestPrice();
            double lowest = data.getLowestPrice();
            data.getHistoricalPriceData()
                    .forEach(historical_price -> {
                        double price = historical_price.getPrice();
                        int sold_amount = historical_price.getSoldAmount();
                        String soldInfo = historical_price.getSoldInfo();
                    });
        });

    }
}