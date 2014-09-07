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
	Word four = memory.get(9);
	// Test each memory location for contents.
	assertNotEquals(one, null);
	assertNotEquals(two, null);
	assertNotEquals(three, null);
	assertNotEquals(four, null);

	// Test for proper Word format Opcode 0-5 General Registers 6-7 Index
	// Registers 8-9 Indirection 10 Address 11-17
	// Word one should be 000100 00 00 0 0000110
	assertEquals(false, one.get(0));
	assertEquals(false, one.get(1));
	assertEquals(false, one.get(2));
	assertEquals(true, one.get(3));
	assertEquals(false, one.get(4));
	assertEquals(false, one.get(5));
	assertEquals(false, one.get(6));
	assertEquals(false, one.get(7));
	assertEquals(false, one.get(8));
	assertEquals(false, one.get(9));
	assertEquals(false, one.get(10));
	assertEquals(false, one.get(11));
	assertEquals(false, one.get(12));
	assertEquals(false, one.get(13));
	assertEquals(false, one.get(14));
	assertEquals(true, one.get(15));
	assertEquals(true, one.get(16));
	assertEquals(false, one.get(17));

	// Word two should be 000101 01 00 1 0010001
	assertEquals(false, two.get(0));
	assertEquals(false, two.get(1));
	assertEquals(false, two.get(2));
	assertEquals(true, two.get(3));
	assertEquals(false, two.get(4));
	assertEquals(true, two.get(5));
	assertEquals(false, two.get(6));
	assertEquals(true, two.get(7));
	assertEquals(false, two.get(8));
	assertEquals(false, two.get(9));
	assertEquals(true, two.get(10));
	assertEquals(false, two.get(11));
	assertEquals(false, two.get(12));
	assertEquals(true, two.get(13));
	assertEquals(false, two.get(14));
	assertEquals(false, two.get(15));
	assertEquals(false, two.get(16));
	assertEquals(true, two.get(17));

	// Word three should be 000110 10 00 0 0001000
	assertEquals(false, three.get(0));
	assertEquals(false, three.get(1));
	assertEquals(false, three.get(2));
	assertEquals(true, three.get(3));
	assertEquals(true, three.get(4));
	assertEquals(false, three.get(5));
	assertEquals(true, three.get(6));
	assertEquals(false, three.get(7));
	assertEquals(false, three.get(8));
	assertEquals(false, three.get(9));
	assertEquals(false, three.get(10));
	assertEquals(false, three.get(11));
	assertEquals(false, three.get(12));
	assertEquals(false, three.get(13));
	assertEquals(true, three.get(14));
	assertEquals(false, three.get(15));
	assertEquals(false, three.get(16));
	assertEquals(false, three.get(17));

	// Word four should be 000111 11 00 0 0010011
	assertEquals(false, four.get(0));
	assertEquals(false, four.get(1));
	assertEquals(false, four.get(2));
	assertEquals(true, four.get(3));
	assertEquals(true, four.get(4));
	assertEquals(true, four.get(5));
	assertEquals(true, four.get(6));
	assertEquals(true, four.get(7));
	assertEquals(false, four.get(8));
	assertEquals(false, four.get(9));
	assertEquals(false, four.get(10));
	assertEquals(false, four.get(11));
	assertEquals(false, four.get(12));
	assertEquals(true, four.get(13));
	assertEquals(false, four.get(14));
	assertEquals(false, four.get(15));
	assertEquals(true, four.get(16));
	assertEquals(true, four.get(17));
    }
}
