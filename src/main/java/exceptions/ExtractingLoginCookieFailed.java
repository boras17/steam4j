package exceptions;

public class ExtractingLoginCookieFailed extends RuntimeException{
    public ExtractingLoginCookieFailed(String msg) {
        super(msg);
    }
}
