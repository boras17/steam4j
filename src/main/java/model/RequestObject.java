package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import steamenums.Country;
import steamenums.Currency;


@Getter
@Setter
@Builder
public
class RequestObject{
    private String countryCode;
    private String language;
    private int currency;
    private int itemNameId;

    public static RequestObject buildDefaultRequestObjectForCountry(Country country, int itemNameId){
        // TODO default build for EUR
        return switch (country){
            case PL -> new RequestObject.RequestObjectBuilder()
                    .countryCode(Country.PL.getCountryCode())
                    .currency(Currency.PL.getCurrencyCode())
                    .language(Country.PL.getLanguageForCountry())
                    .itemNameId(itemNameId)
                    .build();
            case US -> new RequestObject.RequestObjectBuilder()
                    .countryCode(Country.US.getCountryCode())
                    .currency(Currency.USD.getCurrencyCode())
                    .language(Country.US.getLanguageForCountry())
                    .itemNameId(itemNameId)
                    .build();
        };
    }

}