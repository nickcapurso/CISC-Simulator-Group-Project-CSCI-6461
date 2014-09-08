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
	    short memory_location = 6; // Locations 0-5 are reserved.
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
		byte general_register = Byte.parseByte(temp.substring(4, 5));
		String instruction_elements[] = temp.split(",");
		byte index_register = 0;
		byte address = 0;
		byte indirection = 0;
		byte opcode = 0;
		// Switch on the class of opcode.
		switch (instruction_class) {
		case ARITH:
		    break;
		case LD_STR:
		    index_register = Byte.parseByte(instruction_elements[1]);
		    address = Byte.parseByte(instruction_elements[2]);
		    indirection = Byte.parseByte(instruction_elements[3]);
		    opcode = context.getOpCodeBytes().get(opcodeKeyString);
		    setLoadStoreInstruction(word, opcode, general_register,
			    index_register, indirection, address);
		    break;
		case LD_STR_IMD:
		    address = Byte.parseByte(instruction_elements[1]);
		    opcode = context.getOpCodeBytes().get(opcodeKeyString);
		    setLoadStoreImmedInstruction(word, opcode,
			    general_register, address);
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

    /**
     * Inserts the internal components of a {@link Word} in an
     * {@link Context.InstructionClass} LD_STR_IMD internal format.
     * 
     * @param word
     *            The word into which the elements will be inserted.
     * @param opcode
     *            The opcode value to insert into the word.
     * @param general_register
     *            The general register value to insert into the word.
     * @param address
     *            The address value to insert into the word.
     */
    public void setLoadStoreImmedInstruction(Word word, byte opcode,
	    byte general_register, byte address) {
	setOpcode(word, opcode);
	setGeneralRegister(word, general_register);
	setAddress(word, address);

    }

    /**
     * @param opcode
     * @param general_register
     * @param index_register
     * @param indirection
     * @param address
     */
    public void setLoadStoreInstruction(Word word, byte opcode,
	    byte general_register, byte index_register, byte indirection,
	    byte address) {
	setOpcode(word, opcode);
	setGeneralRegister(word, general_register);
	setIndexRegister(word, index_register);
	setIndirection(word, indirection);
	setAddress(word, address);
    }

    /**
     * Sets the instruction opcode to bit positions 0-5.
     * 
     * @param word
     * @param opcode
     */
    public void setOpcode(Word word, byte opcode) {
	for (byte i = 0; i < 6; i++) {
	    if (Utils.isBitSet(opcode, i))
		word.set(5 - i);
	}
    }

    /**
     * @param word
     * @param general_register
     */
    public void setGeneralRegister(Word word, byte general_register) {
	for (byte i = 0; i < 2; i++) {
	    if (Utils.isBitSet(general_register, i))
		word.set(7 - i);
	}
    }

    /**
     * @param word
     * @param index_register
     */
    public void setIndexRegister(Word word, byte index_register) {
	for (byte i = 0; i < 2; i++) {
	    if (Utils.isBitSet(index_register, i))
		word.set(9 - i);
	}
    }

    /**
     * @param word
     * @param indirection
     */
    public void setIndirection(Word word, byte indirection) {
	for (byte i = 0; i < 1; i++) {
	    if (Utils.isBitSet(indirection, i))
		word.set(10);
	}
    }

    /**
     * @param word
     * @param address
     */
    public void setAddress(Word word, byte address) {
	for (byte i = 0; i < 8; i++) {
	    if (Utils.isBitSet(address, i))
		word.set(17 - i);
	}
    }
}
