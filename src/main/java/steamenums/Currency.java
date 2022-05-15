package steamenums;

public enum Currency{
    PL(6), USD(1), EUR(3);
    private final int code;
    Currency(int code) {this.code = code;}
    public int getCurrencyCode() {return code;}
    public static Currency fromInt(int currencyCode) {
        return switch (currencyCode){
            case 6 -> PL;
            case 1 -> USD;
            case 3 -> EUR;
            default -> throw new IllegalStateException("Unexpected value: " + currencyCode);
        };
    }
}
