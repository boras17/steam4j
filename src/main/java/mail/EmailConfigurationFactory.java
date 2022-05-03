package mail;

import java.util.Properties;

public class EmailConfigurationFactory {
    private static Properties imapProperties;

    private static void initCommonProperties() {
        imapProperties = new Properties();
        imapProperties.put("mail.store.protocol", "imaps");
        imapProperties.setProperty("mail.imaps.port", "993");
        imapProperties.setProperty("mail.imaps.auth", "true");
        imapProperties.setProperty("mail.debug", "false");
    }

    private static void addConfigurationProperty(String property_name, String property_value) {
        imapProperties.put(property_name, property_value);
    }

    public static EmailConfiguration defaultWpImapConfiguration(EmailCredentials emailCredentials) {
        initCommonProperties();
        addConfigurationProperty("mail.imaps.host", "imap.wp.pl");
        return new EmailConfiguration(emailCredentials.getUsername(), emailCredentials.getPassword(), imapProperties);
    }

    public static EmailConfiguration defaultGmailImapConfiguration(EmailCredentials emailCredentials){
        initCommonProperties();
        addConfigurationProperty("mail.imaps.host", "imap.gmail.com");
        return new EmailConfiguration(emailCredentials.getUsername(), emailCredentials.getPassword(), imapProperties);
    }
}
