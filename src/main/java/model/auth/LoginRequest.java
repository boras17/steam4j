package model.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import utils.EndpointUtils;

@Builder
@Getter
@Setter
public class LoginRequest{
    private String doNotCache;
    private String encryptedPassword;
    private String username;
    private String twoFactorCode;
    private String emailAuth;
    private String loginFriendlyName;
    private long captchaGid;
    private String captchaText;
    private String emailSteamId;
    private long rsaTimestamp;
    private boolean rememberLogin;
    public static String convertToUrlEncoded(LoginRequest loginRequest){
        final String FORM_URL_ENCODED_NEW_LINE = "&";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("donotcache=").append(loginRequest.getDoNotCache())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("password=").append(loginRequest.getEncryptedPassword())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("username=").append(loginRequest.getUsername())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("twofactorcode=").append(loginRequest.getTwoFactorCode())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("emailauth=").append(loginRequest.getEmailAuth())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("loginfriendlyname=").append(loginRequest.getLoginFriendlyName())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("captchagid=").append(loginRequest.getCaptchaGid())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("captcha_text=").append(loginRequest.getCaptchaText())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("emailsteamid=").append(loginRequest.getEmailSteamId())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("rsatimestamp=").append(loginRequest.getRsaTimestamp())
                .append(FORM_URL_ENCODED_NEW_LINE)
                .append("remember_login=").append(loginRequest.isRememberLogin());
        return stringBuilder.toString();
    }
    public static LoginRequest buildDefaultLoginRequest(String username, String encryptedPassword, long rsaTimestamp){
        return LoginRequest.builder()
                .encryptedPassword(EndpointUtils.encodeStringToUrlStandard(encryptedPassword))
                .username(username)
                .emailSteamId("")
                .loginFriendlyName("")
                .emailAuth("")
                .rememberLogin(false)
                .captchaText("")
                .twoFactorCode("")
                .emailSteamId("")
                .rsaTimestamp(rsaTimestamp)
                .doNotCache(Long.toString(System.currentTimeMillis()))
                .captchaGid(-1)
                .build();
    }
}
