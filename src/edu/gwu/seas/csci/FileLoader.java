/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

    /**
     * 
     */
    private InstructionWriter writer = new InstructionWriter();

    /**
     * Contains the contents of ROM.
     */
    private File input = null;

    /**
     * FileLoader is constructed with a default input.
     */
    public FileLoader() {
	URL url = getClass().getResource("/input.txt");
	try {
	    input = new File(url.toURI());
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
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
	    short memory_location = 8; // Locations 0-5 are reserved.
	    while ((temp = reader.readLine()) != null) {
		Word word = new Word();
		// Read the opcode from the input line.
		String opcodeKeyString = temp.substring(0, 3);
		// Determine the class of the opcode from the Computer's
		// context.
		Context.InstructionClass instruction_class = context
			.getOpcodeClasses().get(opcodeKeyString);
		// Ensure the key returned a valid InstructionClass object.
		if (instruction_class == null)
		    continue;
		byte general_register = 0;
		String instruction_elements[] = temp.split(",");
		byte index_register = 0;
		byte address = 0;
		byte indirection = 0;
		byte opcode = 0;
		// Switch on the class of opcode.
		// TODO: Need to change the switch categories because the
		// instruction format does not correspond directly to class of
		// opcode. Create categories that uniquely identify a common
		// format of opcode instructions and use them instead.
		switch (instruction_class) {
		case ARITH:
		    break;
		case LD_STR:
		    // This is an example of why we need to switch on something
		    // other than opcode class.
		    if (opcodeKeyString.equals("LDX")
			    || opcodeKeyString.equals("STX")) {
			index_register = Byte.parseByte(temp.substring(4, 5));
			;
			address = Byte.parseByte(instruction_elements[1]);
			indirection = Byte.parseByte(instruction_elements[2]);
		    } else {
			general_register = Byte.parseByte(temp.substring(4, 5));
			index_register = Byte
				.parseByte(instruction_elements[1]);
			address = Byte.parseByte(instruction_elements[2]);
			indirection = Byte.parseByte(instruction_elements[3]);
			opcode = context.getOpCodeBytes().get(opcodeKeyString);
			writer.writeInstruction(word, opcode, general_register,
				index_register, indirection, address);
		    }

		    break;
		case LD_STR_IMD:
		    general_register = Byte.parseByte(temp.substring(4, 5));
		    address = Byte.parseByte(instruction_elements[1]);
		    opcode = context.getOpCodeBytes().get(opcodeKeyString);
		    writer.writeInstruction(word, opcode, general_register,
			    address);
		    break;
		case LOGIC:
		    break;
		case TRANS:
		    break;
		default:
		    break;
		}
		memory.put(word, memory_location++);
	    }
	    reader.close();
	} catch (IOException e) {
	    throw new ParseException(e.getMessage(), 0);
	}
    }

    @Override
    public void load() throws NullPointerException, ParseException,
	    IllegalArgumentException {
	this.load(input);
    }
}
