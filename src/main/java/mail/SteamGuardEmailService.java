package mail;

import javax.mail.*;
import javax.mail.search.SearchTerm;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.mail.Folder.READ_ONLY;
import static javax.mail.Folder.READ_WRITE;

public class SteamGuardEmailService implements EmailService{

    private EmailConfiguration emailConfiguration;

    public SteamGuardEmailService(EmailConfiguration emailConfiguration){
        this.emailConfiguration = emailConfiguration;
    }

    @Override
    public Message[] findEmail(SearchTerm term) {
        try{
            Folder folder = this.emailConfiguration.getFolder("INBOX", "imaps", READ_WRITE);
            return folder.search(term);
        }catch (MessagingException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Matcher findContentByPatternInMessage(Message message, Pattern pattern) throws MessagingException, IOException {
        String emailContent = this.readContentFromEmailAsString(message);
        return pattern.matcher(emailContent);
    }

    @Override
    public String readContentFromEmailAsString(Message message) throws MessagingException, IOException {

        String contentType = message.getContentType();
        StringBuilder result = new StringBuilder();

        if(contentType.contains("multipart")){

            Multipart multipart = (Multipart) message.getContent();
            int partsSize = multipart.getCount();

            for (int i = 0; i < partsSize; i++) {
                BodyPart part = multipart.getBodyPart(i);
                String partType = part.getContentType();
                System.out.println(partType);
                if(partType.toLowerCase(Locale.ROOT).contains("text/html")){
                    String data = (String) part.getContent();
                    result.append(data);
                }
            }
        }else{
            return (String) message.getContent();
        }

        return result.toString();
    }
}
