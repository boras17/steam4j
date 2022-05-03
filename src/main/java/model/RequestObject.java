package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public
class RequestObject{
    private String countryCode;
    private String language;
    private int currency;
    private int itemNameId;

    enum Currency{
        PL(6), USD(1), EUR(3);
        private final int code;
        Currency(int code) {this.code = code;}
        public int getCurrencyCode() {return code;}
    }

    enum Country{
        PL("PL","polish"), US("US", "english");
        private final String countryCode;
        private final String countryLanguage;
        Country(String countryCode, String countryLanguage){this.countryCode = countryCode; this.countryLanguage = countryLanguage;}
        String getCountryCode() {return this.countryCode;} String getLanguageForCountry(){return this.countryLanguage;}
    }

    public static RequestObject buildDefaultRequestObjectForCountry(RequestObject.Country country, int itemNameId){
        // TODO default build for EUR
        return switch (country){
            case PL -> new RequestObject.RequestObjectBuilder()
                    .countryCode(RequestObject.Country.PL.getCountryCode())
                    .currency(RequestObject.Currency.PL.getCurrencyCode())
                    .language(RequestObject.Country.PL.getLanguageForCountry())
                    .itemNameId(itemNameId)
                    .build();
            case US -> new RequestObject.RequestObjectBuilder()
                    .countryCode(RequestObject.Country.US.getCountryCode())
                    .currency(RequestObject.Currency.USD.getCurrencyCode())
                    .language(RequestObject.Country.US.getLanguageForCountry())
                    .itemNameId(itemNameId)
                    .build();
        };
    }

}