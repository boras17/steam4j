package utils;

import constants.ItemOrderHistogramConstants;
import model.RequestObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class EndpointUtils{

    public static URI addParamsToURL(Map<String, Object> params, String url) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        int entriesSize = params.size();
        int counter = 1;
        for(Map.Entry<String, Object> entry: params.entrySet()){
            urlBuilder.append(entry.getKey()).append("=").append(encodeStringToUrlStandard(String.valueOf(entry.getValue()))).append(counter==entriesSize ? "" : "&");
            counter+=1;
        }
        return URI.create(urlBuilder.toString());
    }
    public static URI buildURIForRequestObject(RequestObject requestObject, String base_url){
        return new URLBuilder()
                .baseUrl(base_url)
                .addParam(ItemOrderHistogramConstants.COUNTRY, requestObject.getCountryCode())
                .addParam(ItemOrderHistogramConstants.LANGUAGE, requestObject.getLanguage())
                .addParam(ItemOrderHistogramConstants.CURRENCY, requestObject.getCurrency())
                .addParam(ItemOrderHistogramConstants.ITEM_ID, requestObject.getItemNameId())
                .buildUri();
    }
    public static String encodeStringToUrlStandard(String str){

        Map<String, String> character_code = new HashMap<>();
        character_code.put(" ", "%20");
        character_code.put("!", "%21");
        character_code.put("#", "%23");
        character_code.put("$","%24");
        character_code.put("%", "%25");
        character_code.put("&", "%26");
       // character_code.put("(", "%28");
        //character_code.put(")","%29");
        character_code.put("*", "%2A");
        character_code.put("+", "%2B");
        character_code.put(",", "%2C");
        character_code.put("/", "%2F");
        character_code.put(":", "%3A");
        character_code.put(";", "%3B");
        character_code.put("=", "%3D");
        character_code.put("?", "%3F");
        character_code.put("@", "%40");
        character_code.put("[", "%5B");
        character_code.put("]", "%5D");
        character_code.put("'", "%27");
        character_code.put("|","%7C");

        StringBuilder encodedBuilder = new StringBuilder();

        int str_len = str.length();

        for(int i = 0; i < str_len; ++i){
            char current_character = str.charAt(i);

            String current_character_str = String.valueOf(current_character);
            boolean should_be_encoded =  character_code.containsKey(current_character_str);
            if(should_be_encoded){
                String encoded_character = character_code.get(current_character_str);
                encodedBuilder.append(encoded_character);
            }else{
                encodedBuilder.append(current_character);
            }
        }
        return encodedBuilder.toString();
    }
}