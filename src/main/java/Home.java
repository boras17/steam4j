import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import mail.*;
import model.BuySellSignal;
import steam.SteamLogin;

import javax.mail.MessagingException;
import java.io.IOException;

public class Home {
    private static class ConsoleNotifier implements ActivityNotificationStrategy{
        @Override
        public void handleNotification(BuySellSignal activityForItem) {
            System.out.println(activityForItem);
        }
    }
    public static void main(String[] args) throws CaptchaRequiredException, MessagingException, CouldNotFindSteamGuardException, IOException {

        EmailCredentials emailCredentials = new EmailCredentials("mikolaj91048@gmail.com", "pass");
        EmailConfiguration emailConfiguration = EmailConfigurationFactory.defaultGmailImapConfiguration(emailCredentials);

        SteamLogin steamLogin = new SteamLogin("username", "password", emailConfiguration);

        steamLogin.login();

    }
}