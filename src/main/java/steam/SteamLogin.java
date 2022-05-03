package steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import constants.HttpConstants;
import constants.SteamEndpoints;
import exceptions.CaptchaRequiredException;
import exceptions.CouldNotFindSteamGuardException;
import lombok.*;
import mail.*;
import model.auth.LoginAttemptResult;
import model.auth.LoginRequest;
import model.auth.RSAResponse;
import model.auth.TransferParameters;
import steamenums.ResponseStatusCompartment;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.math.BigInteger;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class SteamLogin {
    private String username;
    private String password;
    private HttpClient client;
    private List<HttpCookie> loginExtractedCookies;
    private EmailService emailService;

    public SteamLogin(String username, String password, HttpClient client){
        this.username = username;
        this.password = password;
        this.client = client;
        this.loginExtractedCookies = new ArrayList<>();
    }

    public SteamLogin(String username, String password){
        this.username = username;
        this.password = password;
        CookieHandler.setDefault(new CookieManager());
        this.client = HttpClient.newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public SteamLogin(String username, String password, EmailConfiguration emailConfiguration){
        this(username, password);
        this.emailService = new SteamGuardEmailService(emailConfiguration);
    }

    private String tryToExtractSessionCookie(){
        return null;
    }

    String encrypt(String password, BigInteger exponent, BigInteger modulus) {
        BigInteger data = pkcs1pad2(password.getBytes(StandardCharsets.ISO_8859_1), (modulus.bitLength() + 7) >> 3);
        BigInteger d2 = data.modPow(exponent, modulus);
        String dataHex = d2.toString(16);
        if ((dataHex.length() & 1) == 1) {
            dataHex = "0" + dataHex;
        }
        byte[] encrypted = hexStringToByteArray(dataHex);
        return this.encodeBytesToBase64Str(encrypted);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private BigInteger pkcs1pad2(byte[] data, int n) {
        byte[] bytes = new byte[n];
        int i = data.length - 1;
        while ((i >= 0) && (n > 11)) {
            bytes[--n] = data[i--];
        }
        bytes[--n] = 0;

        while (n > 2) {
            bytes[--n] = 0x01;
        }

        bytes[--n] = 0x2;
        bytes[--n] = 0;

        return new BigInteger(bytes);
    }

    private String encryptPassword(RSAResponse rsaResponse) {
        BigInteger rsaMod = new BigInteger(rsaResponse.getPublicKeyMod(), 16);
        BigInteger rsaExp = new BigInteger(rsaResponse.getPublicKeyExp(), 16);

        return this.encrypt(this.getPassword(),rsaExp, rsaMod);
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
    private Map<String, Object> getEncryptedPasswordBase64(){
        RSAResponse rsaResponse = this.getRSAKey(this.username);
        if(rsaResponse != null){
            String encodedPassword = this.encryptPassword(rsaResponse);
            return Map.of("password",encodedPassword,
                          "rsaResponse", rsaResponse);

        }else{
            System.err.println("An RSAResponse can not be null...");
        }
        return null;
    }

    private String encodeBytesToBase64Str(byte[] bytes){
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }

    private LoginAttemptResult sendDoLoginRequest(LoginRequest loginRequest) {
        URI loginEndpoint = URI.create(SteamEndpoints.DO_LOGIN_ENDPOINT);
        String urlEncodedStr = LoginRequest.convertToUrlEncoded(loginRequest);

        HttpRequest.BodyPublisher formUrlEncodedPublisher = HttpRequest.BodyPublishers.ofString(urlEncodedStr);

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                .uri(loginEndpoint)
                .POST(formUrlEncodedPublisher)
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        try{
            HttpResponse<String> loginAttemptResponse = this.client.send(request, responseBodyHandler);

            int responseStatus = loginAttemptResponse.statusCode();

            if(ResponseStatusCompartment.SUCCESS.checkIfStatusEquals(responseStatus)){
                String jsonBody = loginAttemptResponse.body();
                System.out.println(jsonBody);
                ObjectMapper objectMapperConfig = ObjectMapperConfig.getObjectMapper();
                return objectMapperConfig.readValue(jsonBody, LoginAttemptResult.class);
            }
        }catch (InterruptedException | IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public void login() {
        try{
            LoginAttemptResult completeSuccessfulLoginAttemptResult = this.initTransferParameters();
            this.performSteamAuthRedirects(completeSuccessfulLoginAttemptResult.getTransferUrls(), completeSuccessfulLoginAttemptResult.getTransferParameters());
        }catch (CaptchaRequiredException e){
            e.printStackTrace();
        }
    }

    private LoginAttemptResult initTransferParameters() throws CaptchaRequiredException {
        Map<String, Object> encryptionData = this.getEncryptedPasswordBase64();
        if (encryptionData != null) {
            boolean encryptionDataContainsPassword = encryptionData.containsKey("password");
            boolean encryptionDataContainsRsaResponse = encryptionData.containsKey("rsaResponse");

            if (encryptionDataContainsPassword && encryptionDataContainsRsaResponse) {
                String encryptedPassword = (String) encryptionData.get("password");
                RSAResponse response = (RSAResponse) encryptionData.get("rsaResponse");
                LoginRequest loginRequest = LoginRequest.buildDefaultLoginRequest(this.getUsername(), encryptedPassword, response.getTimestamp());

                LoginAttemptResult loginAttemptResult = this.sendDoLoginRequest(loginRequest);
                if(loginAttemptResult != null){
                    return this.tryToExtract(loginAttemptResult, loginRequest);
                }else{
                    throw new RuntimeException("loginAttemptResult can not be null");
                }
            } else {
            throw new RuntimeException("Error occurend when encrypting password");
        }

        }
        return null;
    }

    private LoginAttemptResult tryToExtract(LoginAttemptResult loginAttemptResult, LoginRequest loginRequest) throws CaptchaRequiredException {
        boolean success = loginAttemptResult.isSuccess();
        boolean captchaNeeded = loginAttemptResult.isCaptchaNeeded();
        boolean emailAuthNeeded = loginAttemptResult.isEmailAuthNeeded();

        if (success) {
            return loginAttemptResult;
        } else if (captchaNeeded) {
            String captchaGid = loginAttemptResult.getCaptchaGid();
            throw new CaptchaRequiredException("https://steamcommunity.com/public/captcha.php?gid=".concat(captchaGid));
        } else if (emailAuthNeeded) {
            LoginAttemptResult transferParametersViaSteamGuard = this.emailAuthNeeded(loginRequest);
            if(transferParametersViaSteamGuard != null){
                boolean isLoginSuccess = transferParametersViaSteamGuard.isSuccess();
                if(isLoginSuccess){
                    return transferParametersViaSteamGuard;
                }else{
                    throw new RuntimeException("Could not log in with steam guard code");
                }
            }
        }

        return null;
    }
    private LoginAttemptResult emailAuthNeeded(LoginRequest loginRequest) {
        try {
            Thread.sleep(6000); // wait for steam email
            String code = this.getTwoFactorCode();
            System.out.println("login with code: " + code);
            loginRequest.setEmailAuth(code);
            return this.sendDoLoginRequest(loginRequest);
        } catch (CouldNotFindSteamGuardException | MessagingException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTwoFactorCode() throws MessagingException,
                                            CouldNotFindSteamGuardException,
                                            IOException {
        EmailService emailService = this.emailService;
        Message[] messages = emailService.findEmail(new FlagTerm(new Flags(Flags.Flag.SEEN), true));
        Message steamGuardMessage = Arrays.stream(messages).filter(message -> {
            try {
                return InternetAddress.toString(message.getFrom()).contains("noreply@steampowered.com");
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }
        }).findFirst().orElseThrow(() -> new CouldNotFindSteamGuardException("Could not find steam guard code"));
        String msg = this.emailService.readContentFromEmailAsString(steamGuardMessage);

        Pattern pattern = Pattern.compile("<td.*>\\s*(.{0,5})\\s*</td>");
        Matcher matcher = pattern.matcher(msg);

        List<MatchResult> matches = matcher.results().toList();

        if(!matches.isEmpty()){
            MatchResult codeResult = matches.get(1);
            return codeResult.group(1).trim();
        }
        else{
            throw new RuntimeException("could not extract steam guard code");
        }
    }
    private void performSteamAuthRedirects(List<String> redirectEndpoints, TransferParameters transferParameters){

        for(String endpoint: redirectEndpoints){
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(TransferParameters.convertToUrlEncoded(transferParameters)))
                    .uri(URI.create(endpoint.replaceAll("\\\\","/")))
                    .setHeader(HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE)
                    .build();
            try{
                this.client.send(request, HttpResponse.BodyHandlers.ofString());
            }catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        this.client.cookieHandler().ifPresent(cookieHandler -> {
            CookieManager cookieManager = (CookieManager) cookieHandler;
            cookieManager.getCookieStore()
                    .getCookies()
                    .forEach(cookie -> {
                        System.out.println("-----------------");
                        System.out.println(cookie.getName());
                        System.out.println(cookie.getValue());
                        System.out.println("-----------------");
                    });
        });
    }
}
