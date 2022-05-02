package exceptions;

public class CaptchaRequiredException extends Exception{
    public CaptchaRequiredException(String msg){
        super(msg);
    }
}
