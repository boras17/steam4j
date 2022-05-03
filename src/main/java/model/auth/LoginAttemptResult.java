package model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginAttemptResult{
    private boolean success;
    @JsonProperty("requires_twofactor")
    private boolean requiresTwoFactor;
    private String message;
    @JsonProperty("emailauth_needed")
    private boolean emailAuthNeeded;
    @JsonProperty("emaildomain")
    private String emailDomain;
    @JsonProperty("emailsteamid")
    private String emailSteamId;
    @JsonProperty("captcha_needed")
    private boolean captchaNeeded;
    @JsonProperty("captcha_gid")
    private String captchaGid;
    @JsonProperty("transfer_parameters")
    private TransferParameters transferParameters;
    @JsonProperty("transfer_urls")
    private List<String> transferUrls;
}
