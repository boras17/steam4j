package model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorMessages {
    private String onRedirect;
    private String onClient;
    private String onServer;

    public static ErrorMessages construct(String baseMessage) {
        return ErrorMessages.builder()
                .onRedirect("An redirection occurred while trying to ".concat(baseMessage))
                .onClient("An client error occurred while trying to ".concat(baseMessage))
                .onServer("An server error occurred while trying to ".concat(baseMessage))
                .build();
    }
}
