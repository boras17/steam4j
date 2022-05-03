import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.EmailConfiguration;
import mail.EmailConfigurationFactory;
import mail.EmailCredentials;
import steam.SteamLogin;

import javax.mail.MessagingException;
import java.io.IOException;

public class Home {
    public static void main(String[] args) throws CaptchaRequiredException, MessagingException, CouldNotFindSteamGuardException, IOException {
        /*

        HttpClient simpleClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        steam.SteamMarketplace fetcher = new steam.SteamMarketplace(simpleClient);

        fetcher.setFetchingCountry(model.RequestObject.Country.PL);

        fetcher.addSnipedItem(new model.ItemSnipingData.ItemSnipingDataBuilder()
                .itemNameId(14962905)
                .breakTimeBetweenRequestsInMillis(100)
                .snipeCriteria(new model.SnipeCriteria(new Predicate<model.BuySellSignal>() {
                            @Override
                            public boolean test(model.BuySellSignal buySellSignal) {

                                return true;
                            }
                        })).build());

        notification.Notifier notifier = new notification.Notifier(new mail.EmailNotificationStrategy());

        fetcher.startSniping(Executors.newSingleThreadExecutor(), notifier);
         */
        EmailCredentials emailCredentials = new EmailCredentials("mikolaj91048@gmail.com", "mikoborecki1");
        EmailConfiguration emailConfiguration = EmailConfigurationFactory.defaultGmailImapConfiguration(emailCredentials);

        SteamLogin steamLogin = new SteamLogin("daenez1", "mikoborecki1", emailConfiguration);

        steamLogin.login();
    }
}