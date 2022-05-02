import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class URLBuilder{
    private Map<String, Object> params = new LinkedHashMap<>();
    private StringBuilder baseURL;

    public URLBuilder addParam(String paramKey, Object paramValue){
        params.putIfAbsent(paramKey, paramValue);
        return this;
    }
    public URLBuilder baseUrl(String baseURL){
        this.baseURL = new StringBuilder(baseURL);
        return this;
    }
    public URLBuilder addPathVariable(String pathVariable){
        this.baseURL.append("/".concat(pathVariable));
        return this;
    }
    private String build(){
        StringBuilder urlBuilder = new StringBuilder(this.baseURL);
        urlBuilder.append('?');
        Iterator<Map.Entry<String, Object>> paramsIterator = this.params.entrySet().iterator();
        while(paramsIterator.hasNext()){
            Map.Entry<String, Object> paramEntry = paramsIterator.next();
            urlBuilder.append(paramEntry.getKey()).append('=').append(paramEntry.getValue()).append(paramsIterator.hasNext()?'&':"");
        }
        return urlBuilder.toString();
    }
    public URI buildUri(){

        return URI.create(this.build());
    }
    public String buildStringUrl(){
        return this.build();
    }
}