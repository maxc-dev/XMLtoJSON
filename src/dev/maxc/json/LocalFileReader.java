package dev.maxc.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Max Carter
 * @since 11/10/2020
 */
public class LocalFileReader {
    /**
     * File path to the resources
     */
    private static final String RESOURCE_PATH = "src/dev/maxc/json/";

    /**
     * Name of the file
     */
    private final String file;

    /**
     * To include \n at the start of every new line
     */
    private boolean addNewLines = false;

    /**
     * Reads the contents of a local file and returns it as a String
     *
     * @param file The name of the local file
     */
    public LocalFileReader(String file, boolean addNewLines) {
        this.file = file;
        this.addNewLines = addNewLines;
    }

    /**
     * Reads the contents of a local file and returns it as a String
     *
     * @param file The name of the local file
     */
    public LocalFileReader(String file) {
        this.file = file;
    }

    /**
     * Returns a string of the file contents
     */
    public String getFileContents() {
        StringBuffer fileContentBuilder = new StringBuffer();
        String line;
        try {
            BufferedReader re = new BufferedReader(new FileReader(RESOURCE_PATH + file));
            while ((line = re.readLine()) != null) {
                fileContentBuilder.append(line);
                if (addNewLines) {
                    fileContentBuilder.append("\n");
                }
            }
        } catch (IOException | IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        if (addNewLines) {
            fileContentBuilder.deleteCharAt(fileContentBuilder.length()-1);
        }
        return fileContentBuilder.toString();
    }
}