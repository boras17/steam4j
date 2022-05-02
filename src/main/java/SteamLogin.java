import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Getter
@Setter
public class SteamLogin {
    private String username;
    private String password;
    private HttpClient client;

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    private static class RSAResponse{
        private boolean success;
        @JsonProperty("publickey_mod")
        private String publicKeyMod;
        @JsonProperty("publickey_exp")
        private String publicKeyExp;
        private long timestamp;
        @JsonProperty("token_gid")
        private String tokenGid;
    }

    public SteamLogin(String username, String password, HttpClient client){
        this.username = username;
        this.password = password;
        this.client = client;
    }

    public SteamLogin(String username, String password){
        this(username, password, HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build());
    }

    private String tryToExtractSessionCookie(){
        return null;
    }

    public PublicKey getKeyFromModulus(RSAResponse rsaResponse) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger rsaMod = new BigInteger(rsaResponse.getPublicKeyMod(), 16);
        BigInteger rsaExp = new BigInteger(rsaResponse.getPublicKeyExp(), 16);

        RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(rsaMod, rsaExp);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pub = factory.generatePublic(publicSpec);

        return pub;
    }

    private RSAResponse getRSAKey(String username){
        try{

            String formURLContent = "username=".concat(username);

            URI steamRSAKeyEndpointURI = URI.create(SteamEndpoints.RSA_KEY_ENDPOINT);

            HttpRequest getRSAKeyRequest = HttpRequest.newBuilder()
                    .setHeader(HttpConstants.USER_AGENT, HttpConstants.MOZILLA_USER_AGENT)
                    .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                    .uri(steamRSAKeyEndpointURI)
                    .POST(HttpRequest.BodyPublishers.ofString(formURLContent))
                    .build();
            try{
                HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
                HttpResponse<String> responseRsa =
                        this.client.send(getRSAKeyRequest,bodyHandler);

                int responseStatus = responseRsa.statusCode();
                System.out.println(responseStatus);
                if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){
                    String jsonRSAResponse = responseRsa.body();
                    ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
                    return objectMapper.readValue(jsonRSAResponse, RSAResponse.class);
                }else{
                    System.err.println("Server respond with "+responseStatus+" after hitting login rsa endpont");
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
    public String getEncryptedPasswordBase64(){
        RSAResponse rsaResponse = this.getRSAKey(this.username);
        if(rsaResponse != null){
            try{
                try{
                    PublicKey publicKey = this.getKeyFromModulus(rsaResponse);
                    Cipher encryptCipher = this.initCipherForRSA(publicKey);
                    String password = this.getPassword();
                    byte[] password_bytes = password.getBytes(StandardCharsets.UTF_8);
                    if(encryptCipher != null){
                        try{
                            byte[] encodedPassword = encryptCipher.doFinal(password_bytes);
                            return this.encodeBytesToBase64Str(encodedPassword);
                        }catch (javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException e){
                            e.printStackTrace();
                        }
                    }
                }catch (InvalidKeySpecException e){
                    e.printStackTrace();
                }
            }catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        }else{
            System.err.println("An RSAResponse can not be null...");
        }
        return null;
    }

    private String encodeBytesToBase64Str(byte[] bytes){
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }

    private Cipher initCipherForRSA(PublicKey publicKey){
        try{
            Cipher encrypt_cipher = Cipher.getInstance("RSA");
            encrypt_cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return encrypt_cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

}
