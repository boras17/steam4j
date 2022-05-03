package model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class RSAResponse{
    private boolean success;
    @JsonProperty("publickey_mod")
    private String publicKeyMod;
    @JsonProperty("publickey_exp")
    private String publicKeyExp;
    private long timestamp;
    @JsonProperty("token_gid")
    private String tokenGid;
}