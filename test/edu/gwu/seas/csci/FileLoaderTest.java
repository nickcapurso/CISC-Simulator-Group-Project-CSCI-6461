/**
 * 
 */
package edu.gwu.seas.csci;

import static org.junit.Assert.*;

import java.io.File;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Remily
 */
public class FileLoaderTest {

    private String base_directory = null;
    private File sample_spn_input_file = null;
    private Loader loader = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	base_directory = getClass().getResource("").toURI().getPath();
	sample_spn_input_file = new File(base_directory + File.separator
		+ "resources", "input.txt");
	loader = new FileLoader();
    }

    /**
     * Test method for
     * {@link edu.gwu.seas.csci.FileLoader#load(java.lang.Object)}.
     */
    @Test
    public void testLoad() {
	try {
	    loader.load(sample_spn_input_file);
	} catch (IllegalArgumentException | ParseException e) {
	    fail(e.getMessage());
	    e.printStackTrace();
	}
	assertTrue(true);
    }

}
