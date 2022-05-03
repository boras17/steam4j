package mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface EmailService {
    Message[] findEmail(SearchTerm terms);
    Matcher findContentByPatternInMessage(Message message, Pattern pattern) throws MessagingException, IOException;
    String readContentFromEmailAsString(Message message) throws MessagingException, IOException;
}
