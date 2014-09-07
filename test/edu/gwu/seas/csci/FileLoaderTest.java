/**
 * 
 */
package edu.gwu.seas.csci;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Remily
 */
public class FileLoaderTest {

    private File input_file = null;
    private Loader loader = null;
    private Memory memory = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	memory = Memory.getInstance();
	URL url = getClass().getResource("/input.txt");
	input_file = new File(url.toURI());
	loader = new FileLoader();
    }

    /**
     * Test method for
     * {@link edu.gwu.seas.csci.FileLoader#load(java.lang.Object)}.
     */
    @Test
    public void testLoad() {
	try {
	    loader.load(input_file);
	} catch (IllegalArgumentException | ParseException e) {
	    fail(e.getMessage());
	}
	Word one = memory.get(6);
	Word two = memory.get(7);
	Word three = memory.get(8);
	Word four = memory.get(6);
	assertTrue(one != null);
	assertTrue(two != null);
	assertTrue(three != null);
	assertTrue(four != null);
    }
}
