package model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
public class TransferParameters{
    @JsonProperty("steamid")
    private String steamId;
    @JsonProperty("token_secure")
    private String tokenSecure;
    private String auth;
    private String webcookie;
    public static String convertToUrlEncoded(TransferParameters transferParameters) {
        final String FORM_URL_ENCODED_NEW_LINE = "&";
        StringBuilder formUrlEncoded = new StringBuilder();
        formUrlEncoded.append("steamid=").append(transferParameters.getSteamId()).append(FORM_URL_ENCODED_NEW_LINE)
                .append("token_secure=").append(transferParameters.getTokenSecure()).append(FORM_URL_ENCODED_NEW_LINE)
                .append("auth=").append(transferParameters.getAuth()).append(FORM_URL_ENCODED_NEW_LINE)
                .append("webcookie=").append(transferParameters.getWebcookie()).append(FORM_URL_ENCODED_NEW_LINE);
        return formUrlEncoded.toString();
    }
}
