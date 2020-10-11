package dev.maxc.json;

import java.util.InvalidPropertiesFormatException;

/**
 * @author Max Carter
 * @since 09/10/2020
 */
public class JSONParser {
    /**
     * Name of the XML script to read from
     */
    public static final String XML_SCRIPT = "xml_script.xml";

    public static void main(String[] args) {
        Serializer serializer = new Serializer(XML_SCRIPT);
        String jsonString = null;
        try {
            jsonString = serializer.getJSONString();
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }
        System.out.println(jsonString);
    }

}