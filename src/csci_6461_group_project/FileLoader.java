/**
 * 
 */
package csci_6461_group_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

/**
 * Reads the contents of a file from disk and loads it into {@link Memory}. The
 * file is expected to contain instructions that constitute a program, with one
 * instruction per line. The expected line format is as follows:
 * 
 * TODO: Document instruction format.
 * 
 * @author Alex Remily
 */
public class FileLoader implements Loader {

    /**
     * Provide a reference to the Computer's memory to hold the contents of the
     * input file.
     */
    private Memory memory = Memory.getInstance();

    /*
     * (non-Javadoc)
     * 
     * @see csci_6461_group_project.Loader#load(java.lang.Object)
     */
    @Override
    public void load(Object input) throws ParseException,
	    IllegalArgumentException {
	// Test method argument.
	if (!(input instanceof File)) {
	    throw new IllegalArgumentException("Object " + input.getClass()
		    + " not an instance of java.io.File.");
	}
	// Create objects for file read.
	File inputFile = (File) input;
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(inputFile));
	} catch (FileNotFoundException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
	try {
	    String temp = null;
	    while ((temp = reader.readLine()) != null) {
		// TODO: Load contents into memory.
	    }
	    reader.close();
	} catch (IOException e) {
	    throw new ParseException(e.getMessage(), 0);
	}
    }
}
