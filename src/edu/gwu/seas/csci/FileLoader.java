/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

/**
 * Reads the contents of a file from disk and loads it into {@link Memory}. The
 * file is expected to contain instructions that constitute a program, with one
 * instruction per line, and elements of the instruction separated by a comma.
 * The expected line format varies with the type of instruction.
 * 
 * TODO: Document the InstructionFormat switching.
 * 
 * @author Alex Remily
 */
public class FileLoader implements Loader {

	/**
	 * Provide a reference to the Computer's memory to hold the contents of the
	 * reader file.
	 */
	private Memory memory = Memory.getInstance();

	/**
	 * Provide a reference to the Computer's context.
	 */
	private Context context = Context.getInstance();

	/**
	 * Used to write instructions into Word objects in memory.
	 */
	private InstructionWriter writer = new InstructionWriter();

	/**
	 * Contains the contents of ROM.
	 */
	private BufferedReader reader = null;

	/**
	 * FileLoader is constructed with a default reader.
	 */
	public FileLoader() {
		InputStream in = getClass().getResourceAsStream("/input.txt");
		reader = new BufferedReader(new InputStreamReader(in));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gwu.seas.csci.Loader#load(java.lang.Object)
	 */
	@Override
	public void load(BufferedReader reader) throws ParseException {
		try {
			String temp = null;
			short memory_location = 8; // Locations 0-5 are reserved.
			while ((temp = reader.readLine()) != null) {
				Word word = new Word();
				// Read the opcode from the reader line.
				String opcodeKeyString = temp.substring(0, 3);
				// Determine the class of the opcode from the Computer's
				// context.
				Context.InstructionFormat instruction_format = context
						.getInstructionFormats().get(opcodeKeyString);
				// Ensure the key returned a valid InstructionClass object.
				if (instruction_format == null)
					continue;
				byte general_register = 0;
				String instruction_elements[] = temp.split(",");
				byte index_register = 0;
				byte address = 0;
				byte indirection = 0;
				byte opcode = 0;
				switch (instruction_format) {
				case ZERO:
					break;
				case ONE:
					general_register = Byte.parseByte(temp.substring(4, 5));
					index_register = Byte.parseByte(instruction_elements[1]);
					address = Byte.parseByte(instruction_elements[2]);
					indirection = Byte.parseByte(instruction_elements[3]);
					opcode = context.getOpCodeBytes().get(opcodeKeyString);
					break;
				case TWO:
					index_register = Byte.parseByte(temp.substring(4, 5));
					address = Byte.parseByte(instruction_elements[1]);
					indirection = Byte.parseByte(instruction_elements[2]);
					opcode = context.getOpCodeBytes().get(opcodeKeyString);
					break;
				case THREE:
					general_register = Byte.parseByte(temp.substring(4, 5));
					address = Byte.parseByte(instruction_elements[1]);
					opcode = context.getOpCodeBytes().get(opcodeKeyString);
					writer.writeInstruction(word, opcode, general_register,
							index_register, indirection, address);
					break;
				case FOUR:
					break;
				default:
					break;
				}
				writer.writeInstruction(word, opcode, general_register,
						index_register, indirection, address);
				memory.write(word, memory_location++);
			}
			reader.close();
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}

	@Override
	public void load() throws NullPointerException, ParseException,
			IllegalArgumentException {
		this.load(reader);
	}
}
