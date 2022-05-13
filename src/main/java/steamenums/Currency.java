package steamenums;

public enum Currency{
    PL(6), USD(1), EUR(3);
    private final int code;
    Currency(int code) {this.code = code;}
    public int getCurrencyCode() {return code;}
}
