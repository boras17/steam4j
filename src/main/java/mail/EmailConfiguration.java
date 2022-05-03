package mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.mail.*;
import java.util.Properties;

@Getter
@Builder
@Setter
public class EmailConfiguration {
    private String email;
    private String password;
    private Properties connectionConfiguration;

    private Session createSession() {
        return Session.getInstance(this.connectionConfiguration, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getEmail(), getPassword());
            }
        });
    }

    public Store getEmailStore(String storeProtocol) {
        Session session = this.createSession();
        Store store = null;
        try{
            store = session.getStore(storeProtocol);
            store.connect(getEmail(), getPassword());
            return store;
        } catch (MessagingException e){
            e.printStackTrace();
        }
        return null;
    }

    public Folder getFolder(String folderName, String storeProtocol, int folderOpenMode) throws MessagingException {
        Folder folder = null;
        Store store = null;
        try{
            store = getEmailStore(storeProtocol);
            folder = store.getFolder(folderName);
            folder.open(folderOpenMode);
            return folder;
        }catch (MessagingException e){
            if(folder != null){
                folder.close();
            }
            store.close();
            e.printStackTrace();
        }
        return null;
    }
}
