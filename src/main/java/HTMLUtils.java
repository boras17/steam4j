import java.util.Map;

class HTMLUtils{
    private static Map<String, String> escapes = Map.of("&quot;","", "&amp;","&","&lt;","<","&gt;",">");

    public static String escapeHTML(String html){
        for(Map.Entry<String, String> entry: escapes.entrySet()){
            String escapeCharacter = entry.getKey();
            boolean containsEscapeCharacter = html.contains(escapeCharacter);
            if(containsEscapeCharacter){
                html = html.replaceAll(escapeCharacter, entry.getValue());
            }
        }
        return html;
    }

    public static String cleanSlashesFromHTML(String html){
        return html.replaceAll("\\\\", "");
    }
}
