package dev.maxc.json.util;

/**
 * @author Max Carter
 * @since 11/10/2020
 */
public class JSONUtils {
    /**
     * Formats an object name and value into JSON format
     *
     * @param objectName Name of the data
     * @param value      Value of the data
     */
    public static String toJSONValue(String objectName, String value) {
        StringBuffer jsonObject = new StringBuffer();
        jsonObject.append("\"");
        jsonObject.append(objectName);
        jsonObject.append("\": \"");
        jsonObject.append(value);
        jsonObject.append("\"");
        return jsonObject.toString();
    }
}
