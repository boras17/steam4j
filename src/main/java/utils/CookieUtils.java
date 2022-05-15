package utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

public class CookieUtils {
    public static Object getCookieValue(String cookieName, CookieHandler cookieHandler){
        CookieManager cookieManager = (CookieManager) cookieHandler;
        CookieStore cookieStore = cookieManager.getCookieStore();
        List<HttpCookie> cookies = cookieStore.getCookies();

        return cookies.stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .orElseThrow();
    }
}
