import java.net.http.HttpClient;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class Home {
    public static void main(String[] args) {
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
        SteamLogin steamLogin = new SteamLogin("daenez1", "mikoborecki1");
        String base64Password = steamLogin.getEncryptedPasswordBase64();
        System.out.println(base64Password);
    }
}