package dev.maxc.json;

import dev.maxc.json.util.JSONUtils;
import dev.maxc.json.util.XMLUtils;

import java.util.InvalidPropertiesFormatException;
import java.util.Objects;

/**
 * @author Max Carter
 * @since 11/10/2020
 */
public class Serializer {
    /**
     * XML file to translate the JSON from
     */
    private final String file;

    /**
     * The next character to be read in the file
     */
    private int pointer = 0;

    /**
     * The level of indentation required
     */
    private int indentLevel = 0;

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
    public String getJSONString() throws InvalidPropertiesFormatException {
        //loading the contents of the xml file as a string
        final LocalFileReader fileReader = new LocalFileReader(file);
        final String content = fileReader.getFileContents();

        if (content.trim().isEmpty()) {
            return "{}";
        }

        //creating the json string to be returned
        StringBuffer jsonString = new StringBuffer();
        indentLevel++;
        indent(jsonString);

        //acquires the first tag - the root node
        //from here we can acquire the rest of the child nodes
        String rootNode = getNextXMLTag(content);
        getNodeValue(jsonString, content, rootNode);
        indentLevel--;

        /*
            If the json string is not longer than 1 then it is
            empty ({}) and does not require new lines in the
            parenthesis
         */
        return (jsonString.length() > 1 ? "{\n" + jsonString.toString() + "\n}" : "{}");
    }

    /**
     * Adds the value of a node to a StringBuffer object
     *
     * @param jsonString The StringBuffer object to add the value to
     * @param context    The source string to read from
     * @param parent     The name of the parent node who's data we are finding
     */
    private void getNodeValue(StringBuffer jsonString, String context, String parent) throws InvalidPropertiesFormatException {
        int relativePointer = pointer;
        String nextNode = getNextXMLTag(context);

        /*
            When the next node (tag) is the same as the ("/" + parent node),
            it means the tree has reached the leaf (child node)

                EG. <attr>val</attr>
            The current parent node is "attr" and the next node is "/attr"
            so they must have a value between them.
         */
        if (Objects.equals(nextNode, "/" + parent)) {
            jsonString.append(JSONUtils.toJSONValue(parent, extractXMLValue(context, relativePointer)));

        } else if (!Objects.equals(nextNode, parent)) {
            /*
                In this case the next node does not equal the parent node so
                it must have children nodes, and it recursively finds the
                child nodes and repeats the process.

                The pointer is rolled back to the index it was at, at the
                start of the method so it can find the next tag
             */
            pointer = relativePointer;

            /*
                This checks if the parent XML tag has an internal attribute inside
                the tag, if it does, it has to extract the tag name to display
                in the JSON file.
             */
            jsonString.append("\"");
            jsonString.append(hasXMLTagAttributes(parent) ? extractXMLTagName(parent) : parent);
            jsonString.append("\": {");
            jsonString.append("\n");
            indentLevel++;
            indent(jsonString);

            /*
                If the parent node did have an attribute in the tag,
                we extract the value and append it to the string buffer
             */
            if (hasXMLTagAttributes(parent)) {
                jsonString.append(extractXMLTagValue(parent));
            }

            /*
                NextNode is reset (why we reset the pointer) and recursively
                uses a depth search down every branch of the parent node.
                Once the parent node's ending tag has been found (EG </attr>)
                we have searched all the child nodes and can break out of the loop
             */
            nextNode = null;
            while (!Objects.equals(nextNode, "/" + extractXMLTagName(parent))) {
                nextNode = getNextXMLTag(context);
                if (Objects.equals(nextNode, "/" + extractXMLTagName(parent)) || nextNode == null) {
                    /*
                        This replaces the ,\n\t* with a \n to ensure
                        the indentation remains consistent when we
                        break from the loop.
                        It essentially means theres no extra commas at
                        the end of the "list"
                            EG 1,2,3,4,5,
                            Should be 1,2,3,4,5
                        We just clean up the end of the string
                     */
                    int index = jsonString.lastIndexOf(",\n");
                    jsonString.delete(index, index + 3);
                    jsonString.insert(index, "\n");
                    break;
                }

                /*
                    Recursive call to get either more child nodes
                    or leaf nodes with JSON attributes
                 */
                getNodeValue(jsonString, context, nextNode);
                jsonString.append(",\n");
                indent(jsonString);
            }

            indentLevel--;
            jsonString.append("}");
        }
    }

    /**
     * Adds indentation to the start of a line
     */
    private void indent(StringBuffer jsonString) {
        for (int i = 0; i < indentLevel; i++) {
            jsonString.append("\t");
        }
    }

    /**
     * Extracts XML attributes from inside the XML tags and returns them as attributes
     * to be appended to a string buffer
     *
     * @param context The XML Tag context
     *                EG: 'riskMeasures version="v1.0"'
     *                EG: 'riskMeasures version="v1.0" attr2="test"'
     * @return Will return the JSON format of the version & attr2 attribute
     */
    private String extractXMLTagValue(String context) {
        String[] attributes = context.split("[ |\\n]");
        /*
            If the attributes is 0 or 1 then there are ni attributes
            to extract so nothing is returned
         */
        if (attributes.length <= 1) {
            return "";
        }
        StringBuffer constructXMLTags = new StringBuffer();
        for (String attr : attributes) {
            //split into attribute name and value
            String[] nameValueSplit = attr.replace("\"", "").split("=");

            /*
                Validation to make sure that there actually exists
                a name and value
             */
            if (nameValueSplit.length == 2) {
                /*
                    Since this is an XML tag value, there is a prefix
                    applied which can be modified in the XMLUtils class
                 */
                constructXMLTags.append(JSONUtils.toJSONValue(XMLUtils.XML_TAG_ATTRIBUTE_PREFIX + nameValueSplit[0], nameValueSplit[1]));
                constructXMLTags.append(",\n");
                indent(constructXMLTags);
            }
        }
        return constructXMLTags.toString();
    }

    /**
     * If an XML tag has additional attributes in it, the name can
     * be extracted separate from the extracted values in the latter
     * method - extractXMLTagValue()
     *
     * @param context The XML Tag context
     *                EG: 'riskMeasures version="v1.0"'
     *                EG: 'riskMeasures version="v1.0" attr2="test"'
     * @return Will return just 'riskMeasures'
     * If there are no values in the XML tag then the original
     * argument is returned.
     */
    private String extractXMLTagName(String context) {
        String[] attributes = context.split("[ |\n]");
        if (attributes.length >= 1) {
            return attributes[0].trim();
        }
        return context;
    }

    /**
     * Returns true if the context contains a " " or a new line,
     * indicating a space in the XML tag for an attribute
     */
    private boolean hasXMLTagAttributes(String context) {
        return context.contains(" ") || context.contains("\n");
    }

    /**
     * Returns the next XML tag in a string from a certain pointer
     *
     * @param context String of text to find the next tag in
     * @throws InvalidPropertiesFormatException When the XML file is incorrectly formatted
     */
    private String getNextXMLTag(String context) throws InvalidPropertiesFormatException {
        //Validation on the pointer to ensure that it is within the context size
        if (pointer >= context.length()) {
            throw new IndexOutOfBoundsException("Pointer " + pointer + " exceeds the length of the string " + context.length() + ".");
        }

        /*
            Null string buffer created, if the string buffer is ever
            initialised it means that an open XML tag (<) has been found.
            It won't initialise the string buffer until it is sure it
            has found the next potential tag
            For loop over context's characters, adding one character at a
            time until the closing XML tag (>) is found
         */
        StringBuffer tagCreator = null;
        for (int i = pointer; i < context.length(); i++) {
            char nextCharacter = context.charAt(i);

            if (nextCharacter == XMLUtils.XML_OPEN_TAG) {
                tagCreator = new StringBuffer();

            } else if (nextCharacter == XMLUtils.XML_CLOSE_TAG) {
                if (tagCreator != null) {
                    if (tagCreator.length() < 1) {
                        throw new InvalidPropertiesFormatException("XML tag does not have a name at pointer: " + pointer);
                    }

                    //increments the pointer to the next character for the next tag outside the >
                    pointer = i + 1;
                    return tagCreator.toString();
                } else {
                    throw new InvalidPropertiesFormatException("Opening XML tag cannot be located.");
                }
            } else if (tagCreator != null) {
                tagCreator.append(nextCharacter);
            }
        }

        //null return means no tag was found
        return null;
    }

    /**
     * Extracts the XML value from an index in the context
     *
     * @param context The text to extract the value from
     * @param index   The index of the start of the value
     */
    private String extractXMLValue(String context, int index) {
        StringBuffer valueCreator = new StringBuffer();
        for (int i = index; i < context.length(); i++) {
            char nextCharacter = context.charAt(i);
            //returns value up until the start of the closing tag
            if (nextCharacter == XMLUtils.XML_OPEN_TAG) {
                return valueCreator.toString().trim();
            }
            valueCreator.append(nextCharacter);
        }

        //no value is found - returns empty
        return "";
    }
}