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
 * TODO: Need to change the switch categories in the load method because the
 * instruction format does not correspond directly to class of opcode. Create
 * categories that uniquely identify a common format of opcode instructions and
 * use them instead.
 * 
 * TODO: Fix the architecture to reflect a resource stream vs a file object
 * load. This is just a quick fix to get the project to run for Part 1.
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
	
	public void Load_File(String file) {
		InputStream in = getClass().getResourceAsStream("/" + file);
		reader = new BufferedReader(new InputStreamReader(in));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gwu.seas.csci.Loader#load(java.lang.Object)
	 */
	@Override
	public void load(Object input) throws ParseException {
		try {
			String temp = null;
			short memory_location = 8; // Locations 0-5 are reserved.
			while ((temp = reader.readLine()) != null) {
				Word word = new Word();
				// Read the opcode from the reader line.
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

				switch (instruction_class) {
				case ARITH:
					break;
				case LD_STR:
					// This is an example of why we need to switch on something
					// other than opcode class.
					if (opcodeKeyString.equals("LDX")
							|| opcodeKeyString.equals("STX")) {
						index_register = Byte.parseByte(temp.substring(4, 5));
						address = Byte.parseByte(instruction_elements[1]);
						indirection = Byte.parseByte(instruction_elements[2]);
						opcode = context.getOpCodeBytes().get(opcodeKeyString);
					} else {
						general_register = Byte.parseByte(temp.substring(4, 5));
						index_register = Byte
								.parseByte(instruction_elements[1]);
						address = Byte.parseByte(instruction_elements[2]);
						indirection = Byte.parseByte(instruction_elements[3]);
						opcode = context.getOpCodeBytes().get(opcodeKeyString);
					}
					writer.writeInstruction(word, opcode, general_register,
							index_register, indirection, address);
					break;
				case LD_STR_IMD:
					general_register = Byte.parseByte(temp.substring(4, 5));
					address = Byte.parseByte(instruction_elements[1]);
					opcode = context.getOpCodeBytes().get(opcodeKeyString);
					writer.writeInstruction(word, opcode, general_register,
							index_register, indirection, address);
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
			System.out.println(memory_location);
			reader.close();
		} catch (IOException e) {
			//Illegal Operation Fault
			throw new ParseException(e.getMessage(), 0);
		}
	}

	@Override
	public void load() throws NullPointerException, ParseException,
	IllegalArgumentException {
		this.load(reader);
	}
}
