/**
 * 
 */
package edu.gwu.seas.csci;

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
 * </br><u>Element, (Valid Values), Positions</u> </br>OPCODE (Valid Values
 * Many) 0-2 </br>GeneralRegister (Valid Values 0-3) 3 </br>Index Register
 * (Valid Values 0-3) 4 </br>Indirection Flag (Valid Values 0-1) 5 </br>Address
 * (Valid Values 0-127) 6-12
 * 
 * @author Alex Remily
 */
public class FileLoader implements Loader {

    /**
     * Provide a reference to the Computer's memory to hold the contents of the
     * input file.
     */
    private Memory memory = Memory.getInstance();

    /**
     * Provide a reference to the Computer's context.
     */
    private Context context = Context.getInstance();

    public FileLoader() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.gwu.seas.csci.Loader#load(java.lang.Object)
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
	    Word word = new Word();
	    while ((temp = reader.readLine()) != null) {
		// Read the opcode from the input line.
		String opcodeKeyString = temp.substring(0, 2);
		// Determine the class of the opcode from the Computer's
		// context.
		Context.InstructionClass instructionClass = context
			.getOpcodeClasses().get(opcodeKeyString);
		// Switch on the class of opcode.
		switch (instructionClass) {
		case ARITHMETIC:
		    break;
		case LOADSTORE:
		    // Read remaining characters in line
		    byte generalRegister = Byte.parseByte(temp.substring(3, 4));
		    byte indexRegister = Byte.parseByte(temp.substring(4, 5));
		    byte indirection = Byte.parseByte(temp.substring(5, 6));
		    byte address = Byte.parseByte(temp.substring(6, 13));
		    byte opcode = context.getOpCodesMap().get(opcodeKeyString);
		    word.setLoadStoreInstruction(opcode, generalRegister,
			    indexRegister, indirection, address);
		    break;
		case LOGICAL:
		    break;
		case TRANSFER:
		    break;
		default:
		    break;
		}
		memory.put(word, 0);
	    }
	    reader.close();
	} catch (IOException e) {
	    throw new ParseException(e.getMessage(), 0);
	}
    }
}
