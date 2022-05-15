package model.trademodel.buy;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import steamenums.Country;

import java.util.Optional;

@Getter
@Setter
@Builder
public class Billing {
    private String firstName;
    private String lastName;
    private String billingAddress;
    private String billingAddressTwo;
    private Country billingCountry;
    private String city;
    private String billingState;
    private String billingPostalCode;
    private boolean saveMyAddress;

    public Optional<String> getBillingState() {
        return Optional.ofNullable(this.billingState);
    }
    public Optional<String> getBillingAddressTwo(){
        return Optional.ofNullable(this.billingAddressTwo);
    }
}
