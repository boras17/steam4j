package utils;

import constants.HttpConstants;

public class HeaderUtils {
    public static String[] getStandardFormURLEncodedForSteam() {
        return new String[]{
                HttpConstants.CONTENT_TYPE, HttpConstants.FORM_URL_ENCODED_HEADER_VALUE,
                HttpConstants.USER_AGENT, HttpConstants.MOZILLA_USER_AGENT
        };
    }
}
