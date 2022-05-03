import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.EmailConfiguration;
import mail.EmailConfigurationFactory;
import mail.EmailCredentials;

import javax.mail.MessagingException;
import java.io.IOException;

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
        EmailCredentials emailCredentials = new EmailCredentials("mikolaj91048@gmail.com", "mikoborecki1");
        EmailConfiguration emailConfiguration = EmailConfigurationFactory.defaultGmailImapConfiguration(emailCredentials);

        SteamLogin steamLogin = new SteamLogin("daenez1", "mikoborecki1", emailConfiguration);

        steamLogin.login();
    }
}