import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.EmailConfiguration;
import mail.EmailConfigurationFactory;
import mail.EmailCredentials;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class Home {
    public static void main(String[] args) throws CaptchaRequiredException, MessagingException, CouldNotFindSteamGuardException, IOException {
        /*

        HttpClient simpleClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        SteamMarketplace fetcher = new SteamMarketplace(simpleClient);

        fetcher.setFetchingCountry(RequestObject.Country.PL);

        fetcher.addSnipedItem(new ItemSnipingData.ItemSnipingDataBuilder()
                .itemNameId(14962905)
                .breakTimeBetweenRequestsInMillis(100)
                .snipeCriteria(new SnipeCriteria(new Predicate<BuySellSignal>() {
                            @Override
                            public boolean test(BuySellSignal buySellSignal) {

                                return true;
                            }
                        })).build());

        Notifier notifier = new Notifier(new EmailNotificationStrategy());

        fetcher.startSniping(Executors.newSingleThreadExecutor(), notifier);
         */
        EmailConfiguration emailConfiguration = EmailConfigurationFactory.defaultWpImapConfiguration(new EmailCredentials("mikolaj48910@wp.pl", "mikolajborek2013"));
        SteamLogin steamLogin = new SteamLogin("gjob7", "Mikoborecki1@", emailConfiguration);
        steamLogin.extractCookie();
    }
}