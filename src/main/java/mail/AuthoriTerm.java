package mail;

import lombok.SneakyThrows;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

public class AuthoriTerm extends SearchTerm {
    private String authorEamil;

    public AuthoriTerm(String authorEamil){
        this.authorEamil = authorEamil;
    }

    @SneakyThrows
    @Override
    public boolean match(Message message) {
        return InternetAddress.toString(message.getFrom()).contains("noreply@steampowered.com");
    }
}
