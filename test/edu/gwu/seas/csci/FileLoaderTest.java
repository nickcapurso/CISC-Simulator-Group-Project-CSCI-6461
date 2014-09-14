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
	Word one = memory.get(8);
	Word two = memory.get(9);
	Word three = memory.get(10);
	Word four = memory.get(11);
	Word five = memory.get(12);
	Word six = memory.get(13);
	Word seven = memory.get(14);
	Word eight = memory.get(15);
	Word nine = memory.get(16);
	Word ten = memory.get(17);

	// Test each memory location for contents.
	assertNotEquals(one, null);
	assertNotEquals(two, null);
	assertNotEquals(three, null);
	assertNotEquals(four, null);
	assertNotEquals(five, null);
	assertNotEquals(six, null);
	assertNotEquals(seven, null);
	assertNotEquals(eight, null);
	assertNotEquals(nine, null);
	assertNotEquals(ten, null);

	// Test for proper Word format Opcode 0-5 Index Registers 6-7 General
	// Registers 8-9 Indirection 10 Address 11-17

	// Word one LDR should be 000001 00 00 0 0001000
	assertEquals(false, one.get(0));
	assertEquals(false, one.get(1));
	assertEquals(false, one.get(2));
	assertEquals(false, one.get(3));
	assertEquals(false, one.get(4));
	assertEquals(true, one.get(5));
	assertEquals(false, one.get(6));
	assertEquals(false, one.get(7));
	assertEquals(false, one.get(8));
	assertEquals(false, one.get(9));
	assertEquals(false, one.get(10));
	assertEquals(false, one.get(11));
	assertEquals(false, one.get(12));
	assertEquals(false, one.get(13));
	assertEquals(true, one.get(14));
	assertEquals(false, one.get(15));
	assertEquals(false, one.get(16));
	assertEquals(false, one.get(17));

	// Word two STR should be 000010 01 01 1 0001001
	assertEquals(false, two.get(0));
	assertEquals(false, two.get(1));
	assertEquals(false, two.get(2));
	assertEquals(false, two.get(3));
	assertEquals(true, two.get(4));
	assertEquals(false, two.get(5));
	assertEquals(false, two.get(6));
	assertEquals(true, two.get(7));
	assertEquals(false, two.get(8));
	assertEquals(true, two.get(9));
	assertEquals(true, two.get(10));
	assertEquals(false, two.get(11));
	assertEquals(false, two.get(12));
	assertEquals(false, two.get(13));
	assertEquals(true, two.get(14));
	assertEquals(false, two.get(15));
	assertEquals(false, two.get(16));
	assertEquals(true, two.get(17));

	// Word three LDA should be 000011 10 00 0 0001010
	assertEquals(false, three.get(0));
	assertEquals(false, three.get(1));
	assertEquals(false, three.get(2));
	assertEquals(false, three.get(3));
	assertEquals(true, three.get(4));
	assertEquals(true, three.get(5));
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
	assertEquals(true, three.get(16));
	assertEquals(false, three.get(17));

	// Word four AMR should be 000100 00 00 0 0000110
	assertEquals(false, four.get(0));
	assertEquals(false, four.get(1));
	assertEquals(false, four.get(2));
	assertEquals(true, four.get(3));
	assertEquals(false, four.get(4));
	assertEquals(false, four.get(5));
	assertEquals(false, four.get(6));
	assertEquals(false, four.get(7));
	assertEquals(false, four.get(8));
	assertEquals(false, four.get(9));
	assertEquals(false, four.get(10));
	assertEquals(false, four.get(11));
	assertEquals(false, four.get(12));
	assertEquals(false, four.get(13));
	assertEquals(false, four.get(14));
	assertEquals(true, four.get(15));
	assertEquals(true, four.get(16));
	assertEquals(false, four.get(17));

	// Word five SMR should be 000101 00 01 1 0010001
	assertEquals(false, five.get(0));
	assertEquals(false, five.get(1));
	assertEquals(false, five.get(2));
	assertEquals(true, five.get(3));
	assertEquals(false, five.get(4));
	assertEquals(true, five.get(5));
	assertEquals(false, five.get(6));
	assertEquals(false, five.get(7));
	assertEquals(false, five.get(8));
	assertEquals(true, five.get(9));
	assertEquals(true, five.get(10));
	assertEquals(false, five.get(11));
	assertEquals(false, five.get(12));
	assertEquals(true, five.get(13));
	assertEquals(false, five.get(14));
	assertEquals(false, five.get(15));
	assertEquals(false, five.get(16));
	assertEquals(true, five.get(17));

	// Word six LDX should be 100001 11 00 1 0001011
	assertEquals(true, six.get(0));
	assertEquals(false, six.get(1));
	assertEquals(false, six.get(2));
	assertEquals(false, six.get(3));
	assertEquals(false, six.get(4));
	assertEquals(true, six.get(5));
	assertEquals(true, six.get(6));
	assertEquals(true, six.get(7));
	assertEquals(false, six.get(8));
	assertEquals(false, six.get(9));
	assertEquals(true, six.get(10));
	assertEquals(false, six.get(11));
	assertEquals(false, six.get(12));
	assertEquals(false, six.get(13));
	assertEquals(true, six.get(14));
	assertEquals(false, six.get(15));
	assertEquals(true, six.get(16));
	assertEquals(true, six.get(17));

	// Word seven STX should be 100010 00 00 0 0001100
	assertEquals(true, seven.get(0));
	assertEquals(false, seven.get(1));
	assertEquals(false, seven.get(2));
	assertEquals(false, seven.get(3));
	assertEquals(true, seven.get(4));
	assertEquals(false, seven.get(5));
	assertEquals(false, seven.get(6));
	assertEquals(false, seven.get(7));
	assertEquals(false, seven.get(8));
	assertEquals(false, seven.get(9));
	assertEquals(false, seven.get(10));
	assertEquals(false, seven.get(11));
	assertEquals(false, seven.get(12));
	assertEquals(false, seven.get(13));
	assertEquals(true, seven.get(14));
	assertEquals(true, seven.get(15));
	assertEquals(false, seven.get(16));
	assertEquals(false, seven.get(17));

	// Word eight AIR should be 000110 00 10 0 0001000
	assertEquals(false, eight.get(0));
	assertEquals(false, eight.get(1));
	assertEquals(false, eight.get(2));
	assertEquals(true, eight.get(3));
	assertEquals(true, eight.get(4));
	assertEquals(false, eight.get(5));
	assertEquals(false, eight.get(6));
	assertEquals(false, eight.get(7));
	assertEquals(true, eight.get(8));
	assertEquals(false, eight.get(9));
	assertEquals(false, eight.get(10));
	assertEquals(false, eight.get(11));
	assertEquals(false, eight.get(12));
	assertEquals(false, eight.get(13));
	assertEquals(true, eight.get(14));
	assertEquals(false, eight.get(15));
	assertEquals(false, eight.get(16));
	assertEquals(false, eight.get(17));

	// Word nine SIR should be 000111 00 11 0 0010011
	assertEquals(false, nine.get(0));
	assertEquals(false, nine.get(1));
	assertEquals(false, nine.get(2));
	assertEquals(true, nine.get(3));
	assertEquals(true, nine.get(4));
	assertEquals(true, nine.get(5));
	assertEquals(false, nine.get(6));
	assertEquals(false, nine.get(7));
	assertEquals(true, nine.get(8));
	assertEquals(true, nine.get(9));
	assertEquals(false, nine.get(10));
	assertEquals(false, nine.get(11));
	assertEquals(false, nine.get(12));
	assertEquals(true, nine.get(13));
	assertEquals(false, nine.get(14));
	assertEquals(false, nine.get(15));
	assertEquals(true, nine.get(16));
	assertEquals(true, nine.get(17));

	// The HALT instruction should be all zeroes.
	assertEquals(false, ten.get(0));
	assertEquals(false, ten.get(1));
	assertEquals(false, ten.get(2));
	assertEquals(false, ten.get(3));
	assertEquals(false, ten.get(4));
	assertEquals(false, ten.get(5));
	assertEquals(false, ten.get(6));
	assertEquals(false, ten.get(7));
	assertEquals(false, ten.get(8));
	assertEquals(false, ten.get(9));
	assertEquals(false, ten.get(10));
	assertEquals(false, ten.get(11));
	assertEquals(false, ten.get(12));
	assertEquals(false, ten.get(13));
	assertEquals(false, ten.get(14));
	assertEquals(false, ten.get(15));
	assertEquals(false, ten.get(16));
	assertEquals(false, ten.get(17));
    }
}
