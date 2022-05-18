package steamservices;

import model.ErrorMessages;
import steamenums.ResponseStatusCompartment;

public class HttpStatusHandlerService {
    public static void handleStatus(int responseStatus, ErrorMessages messages) {
        if(ResponseStatusCompartment.REDIRECT_ERROR.checkIfStatusEquals(responseStatus)){
            throw new RuntimeException(messages.getOnRedirect());
        }else if(ResponseStatusCompartment.SERVER_ERROR.checkIfStatusEquals(responseStatus)) {
            throw new RuntimeException(messages.getOnServer());
        }else if(ResponseStatusCompartment.CLIENT_ERROR.checkIfStatusEquals(responseStatus)){
            throw new RuntimeException(messages.getOnClient());
        }
    }
}
