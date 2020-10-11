package dev.maxc.json;

import org.junit.Test;

import java.util.InvalidPropertiesFormatException;

import static org.junit.Assert.assertEquals;

public class SerializerTest {
    public static final String XML_SCRIPT = "xml_script.xml";
    public static final String XML_FORMAT_ERROR = "xml_format_error.xml";
    public static final String XML_EMPTY = "xml_empty.xml";
    public static final String JSON_EXPECTED = "json_expected.json";

    final LocalFileReader reader = new LocalFileReader(JSON_EXPECTED, true);

    /**
     * Tests the serializer with the provided XML file
     */
    @Test
    public void testSerializer() {
        //gets the actual result
        Serializer serializer = new Serializer(XML_SCRIPT);
        String jsonString = null;
        try {
            jsonString = serializer.getJSONString();
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }

        assertEquals(reader.getFileContents(), jsonString);
    }

    /**
     * Tests serializer for an empty XML script
     */
    @Test
    public void testSerializerEmpty() {
        //gets the actual result
        Serializer serializer = new Serializer(XML_EMPTY);
        String jsonString = null;
        try {
            jsonString = serializer.getJSONString();
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }

        assertEquals("{}", jsonString);
    }

    /**
     * Tests serializer for an error
     */
    @Test(expected = InvalidPropertiesFormatException.class)
    public void testSerializerError() throws InvalidPropertiesFormatException {
        //gets the actual result
        Serializer serializer = new Serializer(XML_FORMAT_ERROR);
        serializer.getJSONString();
    }

}