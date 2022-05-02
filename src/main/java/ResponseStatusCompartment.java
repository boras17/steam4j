public enum ResponseStatusCompartment{
    SUCCESS(200, 300), SERVER_ERROR(500,600),
    CLIENT_ERROR(400, 500), REDIRECT_ERROR(300, 400);
    private final int fromInclusive,toExclusive;
    ResponseStatusCompartment(int fromInclusive, int toExclusive) {this.toExclusive = toExclusive; this.fromInclusive = fromInclusive;}
    public int getFromInclusive(){return this.fromInclusive;}
    public int getToExclusive(){return this.toExclusive;}
    public boolean checkIfStatusEquals(int status ){
        int min = this.getFromInclusive();
        int max = this.getToExclusive();
        return status >= min && status < max;
    }
}