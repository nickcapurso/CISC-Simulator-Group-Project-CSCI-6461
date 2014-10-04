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
			byte opcode, general_register, index_register, address, indirection, register_x, register_y, count, lr, al;

			while ((temp = reader.readLine()) != null) {
				Word word = new Word();
				// Read the opcode from the reader line.
				String opcodeKeyString = temp.substring(0, 3);
				// Determine the instruction's format from the Computer's
				// context.
				Context.InstructionFormat instruction_format = context
						.getInstructionFormats().get(opcodeKeyString);
				// Ensure the key returned a valid InstructionClass object.
				if (instruction_format == null)
					continue;

				String instruction_elements[] = temp.split(",");
				opcode = general_register = index_register = address = indirection = register_x = register_y = count = lr = al = 0;
				opcode = context.getOpCodeBytes().get(opcodeKeyString);
				
				switch (instruction_format) {
				case ONE:
					general_register = Byte.parseByte(temp.substring(4, 5));
					index_register = Byte.parseByte(instruction_elements[1]);
					address = Byte.parseByte(instruction_elements[2]);
					// Optional indirection check
					if (instruction_elements.length < 4)
						indirection = 0;
					else
						indirection = Byte.parseByte(instruction_elements[3]);
					break;
				case TWO:
					index_register = Byte.parseByte(temp.substring(4, 5));
					address = Byte.parseByte(instruction_elements[1]);

					// Optional indirection check
					if (instruction_elements.length < 3)
						indirection = 0;
					else
						indirection = Byte.parseByte(instruction_elements[2]);
					break;
				case THREE:
					general_register = Byte.parseByte(temp.substring(4, 5));
					address = Byte.parseByte(instruction_elements[1]);
					break;
				case FOUR:
					address = Byte.parseByte(temp.substring(4, temp.length()));
					break;
				case FIVE:
					register_x = Byte.parseByte(temp.substring(4, 5));
					break;
				case SIX:
					register_x = Byte.parseByte(temp.substring(4, 5));
					register_y = Byte.parseByte(instruction_elements[1]);
					break;
				case SEVEN:
					general_register = Byte.parseByte(temp.substring(4, 5));
					count = Byte.parseByte(instruction_elements[1]);
					lr = Byte.parseByte(instruction_elements[2]);
					al = Byte.parseByte(instruction_elements[3]);
					break;
				default:
					break;
				}

				switch (instruction_format) {
				case ONE:
				case TWO:
				case THREE:
				case FOUR:
					System.out.println("Writing: opcode = " + opcode + ", R = "
							+ general_register + ", X = " + index_register
							+ ", I = " + indirection + ", ADDR = " + address);
					writer.writeLoadStoreFormatInstruction(word, opcode,
							general_register, index_register, indirection,
							address);
					break;
				case FIVE:
				case SIX:
					System.out.println("Writing: opcode = " + opcode
							+ ", RX = " + register_x + ", RY = " + register_y);
					writer.writeXYArithInstruction(word, opcode, register_x,
							register_y);
					break;
				case SEVEN:
					System.out.println("Writing: opcode = " + opcode + ", R = "
							+ general_register + ", COUNT = " + count
							+ ", LR = " + lr + ", AL = " + al);
					writer.writeShiftInstruction(word, opcode,
							general_register, count, lr, al);
					break;
				default:
					break;
				}

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
