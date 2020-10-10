package dev.maxc.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Max Carter
 * @since 09/10/2020
 */
public class Redo {
    //name of the xml file
    public static final String XML_SCRIPT = "xml_script";

    public static void main(String[] args) {
        final Serializer serializer = new Serializer(XML_SCRIPT);
        final String jsonString = serializer.getJSONString();
        System.out.println(jsonString);
    }

    public static class Serializer {
        /**
         * XML file to translate the JSON from.
         */
        private final String file;
        private int pointer = 0;

        /**
         * Serializer to convert an XML file to JSON
         *
         * @param file XML file to translate
         */
        public Serializer(String file) {
            this.file = file;
        }

        /**
         * Returns the JSON string of an XML file in the Serializer object
         */
        public String getJSONString() {
            //loading the contents of the xml file as a string
            final LocalFileReader fileReader = new LocalFileReader(file);
            final String content = fileReader.getFileContents();

            //creating the json string to be returned
            StringBuffer jsonString = new StringBuffer("string:");

            String parentNode = getNextXMLTag(content);
            getNodeValue(jsonString, content, parentNode);

            return jsonString.toString();
        }


        private void getNodeValue(StringBuffer jsonString, String context, String parent) {
            int relativePointer = pointer;
            String nextNode = getNextXMLTag(context);
            //base case
            if (Objects.equals(nextNode, "/" + parent)) {
                jsonString.append(JSONUtils.toJSONValue(parent, extractXMLValue(context, relativePointer)));
            } else if (!Objects.equals(nextNode, parent)) {
                pointer = relativePointer;
                jsonString.append("\"");
                jsonString.append(parent);
                jsonString.append("\": {");
                getAllChildNodes(jsonString, context, parent);
                jsonString.append("}");
            }
        }

        private void getAllChildNodes(StringBuffer jsonString, String content, String parent) {
            String nextNode = null;
            while (!Objects.equals(nextNode, "/" + parent)) {
                nextNode = getNextXMLTag(content);
                //System.out.println("Parent: " + parent + ", NextNode: " + nextNode);
                if (Objects.equals(nextNode, "/" + parent) || nextNode == null) {
                    jsonString.setLength(jsonString.length() - 2);
                    break;
                }
                getNodeValue(jsonString, content, nextNode);
                jsonString.append(", ");
            }
        }

        /**
         * Returns the next XML tag in a string from a certain pointer
         *
         * @param context
         */
        private String getNextXMLTag(String context) {
            if (pointer >= context.length()) {
                System.out.println("pointer error: pointer exceeds the size of the context");
                return null;
            }
            StringBuffer tagCreator = null;

            for (int i = pointer; i < context.length(); i++) {
                char nextCharacter = context.charAt(i);

                if (nextCharacter == XMLUtils.XML_OPEN_TAG) {
                    tagCreator = new StringBuffer();

                } else if (nextCharacter == XMLUtils.XML_CLOSE_TAG) {
                    if (tagCreator != null) {
                        pointer = i + 1; //increments the pointer to the next character for the next tag outside the >
                        return tagCreator.toString();
                    } else {
                        System.out.println("error: tag closed with no content");
                        return null;
                    }
                } else if (tagCreator != null) {
                    tagCreator.append(nextCharacter);
                }
            }

            return null;
        }

        private String extractXMLValue(String context, int index) {
            StringBuffer valueCreator = new StringBuffer();
            for (int i = index; i < context.length(); i++) {
                char nextCharacter = context.charAt(i);
                if (nextCharacter == XMLUtils.XML_OPEN_TAG) {
                    return valueCreator.toString().trim();
                }
                valueCreator.append(nextCharacter);
            }

            return "N/A";
        }

        protected static class JSONUtils {
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

        protected static class XMLUtils {
            private static final char XML_OPEN_TAG = '<';
            private static final char XML_CLOSE_TAG = '>';
        }

        private static class LocalFileReader {
            private static final String RESOURCE_PATH = "src/dev/maxc/json/";
            private static final String FILE_EXTENSION = ".xml";

            /**
             * Name of the file
             */
            private String file;

            /**
             * Reads the contents of a local file and returns it as a String
             *
             * @param file The name of the local file
             */
            public LocalFileReader(String file) {
                this.file = file;
            }

            public String getFileContents() {
                StringBuffer fileContentBuilder = new StringBuffer();
                String line;
                try {
                    BufferedReader re = new BufferedReader(new FileReader(RESOURCE_PATH + file + FILE_EXTENSION));
                    while ((line = re.readLine()) != null) {
                        fileContentBuilder.append(line);
                    }
                } catch (IOException | IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
                return fileContentBuilder.toString();
            }
        }

    }
}
