package steamenums;

public enum Country{
    PL("PL","polish"), US("US", "english");
    private final String countryCode;
    private final String countryLanguage;
    Country(String countryCode, String countryLanguage){this.countryCode = countryCode; this.countryLanguage = countryLanguage;}
    public String getCountryCode() {return this.countryCode;} public String getLanguageForCountry(){return this.countryLanguage;}
}
