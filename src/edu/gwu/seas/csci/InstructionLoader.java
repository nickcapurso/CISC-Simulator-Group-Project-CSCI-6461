/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads input and loads it into {@link Memory}.
 * 
 * TODO: Document the InstructionFormat switching.
 * 
 * @author Alex Remily
 */
public class InstructionLoader implements Loader {

	static final Logger logger = LogManager.getLogger(InstructionLoader.class
			.getName());

	public static final byte JUMP_INDIRECTION_ADDR = 8;
	public static final byte BOOT_PROGRAM_LOADING_ADDR = 21;
	public static final byte GENERAL_PROGRAM_LOADING_ADDR = 96;

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
	 * Keeps track of jump labels.
	 */
	private static ArrayList<LabelEntry> labelTable;

	private short memory_location = 0;

	/**
	 * InstructionLoader is constructed with the boot program as the default
	 * target of the reader.
	 */
	public InstructionLoader() {
		InputStream in = getClass().getResourceAsStream("/boot.txt");
		reader = new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * InstructionLoader is constructed with the file parameter as the target of
	 * the reader.
	 * 
	 * @param file
	 *            A file with program instructions to load into memory. The file
	 *            is expected to contain instructions that constitute a program,
	 *            with one instruction per line, and elements of the instruction
	 *            separated by a comma. The expected line format varies with the
	 *            type of instruction.
	 */
	public InstructionLoader(String file) {
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in));
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gwu.seas.csci.Loader#load(java.lang.Object)
	 */
	@Override
	public void load(BufferedReader reader) throws ParseException {
		labelTable = new ArrayList<LabelEntry>();
		try {
			String temp = null;
			memory_location = isAddressEmpty(95, memory) ? BOOT_PROGRAM_LOADING_ADDR
					: GENERAL_PROGRAM_LOADING_ADDR;
			while ((temp = reader.readLine()) != null) {
				if (temp.equals("") || temp.charAt(0) == '/') {
					logger.debug("Ignoring line: blank or a comment");
					continue;
				}
				if (temp.indexOf('/') != -1) {
					logger.debug("End-of-line comment found, ignoring");
					temp = temp.substring(0, temp.indexOf('/')).trim();
				}
				logger.debug("Checking for jump labels by looking for the colon.");
				if (temp.indexOf(':') != -1) {
					String label = temp.substring(0, temp.indexOf(':')).trim();
					int labelIndex = 0;
					logger.debug("Checking if the label is already in the label table.");
					if ((labelIndex = searchLabelTable(label)) == -1) {
						labelTable.add(new LabelEntry(label, memory_location));
						logger.debug("Found new label: " + label
								+ " at address " + memory_location);
					} else {
						// If the label is already in the table, but has an
						// associated
						// address of 0, then there are forward references that
						// need to be resolved now
						if (labelTable.get(labelIndex).address == 0) {
							labelTable.get(labelIndex).address = memory_location;

							LabelEntry entry = labelTable.get(labelIndex);
							Stack<Short> references = entry.forwardReferences;

							// Loop until all forward references have been
							// resolved
							while (!references.isEmpty()) {
								short address = references.pop();
								Word word = memory.read(address);

								// If the address if above 127, then we need to
								// do indirection through address 8
								if (entry.address >= 128) {
									// Set jump indirection address
									Utils.byteToBitSetDeepCopy(
											(byte) JUMP_INDIRECTION_ADDR,
											word,
											(byte) InstructionBitFormats.LD_STR_ADDR_SIZE,
											(byte) InstructionBitFormats.LD_STR_ADDR_END);
									// Set indirection flag
									Utils.byteToBitSetDeepCopy(
											(byte) 1,
											word,
											(byte) InstructionBitFormats.LD_STR_I_SIZE,
											(byte) InstructionBitFormats.LD_STR_I_END);
									logger.debug("Resolving forward reference at address: "
											+ address
											+ ", jump address = "
											+ entry.address
											+ ", using indirection");
								} else {
									// Else, just update the address field in
									// the instruction with a forward reference
									Utils.byteToBitSetDeepCopy(
											(byte) entry.address,
											word,
											(byte) InstructionBitFormats.LD_STR_ADDR_SIZE,
											(byte) InstructionBitFormats.LD_STR_ADDR_END);
									logger.debug("Resolving forward reference at address: "
											+ address
											+ ", jump address = "
											+ entry.address);
								}

								// Write back the instruction to memory
								memory.write(word, address);
							}
						} else {
							// Can't have duplicate labels
							throw new ParseException("Error: Duplicate Label: "
									+ label, 0);
						}
					}
					continue;
				}

				Word word = stringToWord(temp);

				if (word != null)
					memory.write(word, memory_location++);
			}
			logger.debug("Final instruction loaded at memory location "
					+ memory_location + ".");
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

	public Word stringToWord(String input) {
		String temp = input;
		String labelCheck;
		byte opcode, general_register, index_register, address, indirection, register_x, register_y, count, lr, al, devid;
		Word word = new Word();
		int labelIndex = 0;

		try {
			// Read the opcode from the reader line.
			String opcodeKeyString = temp.substring(0, 3).trim();

			// Determine the instruction's format from the Computer's
			// context.
			Context.InstructionFormat instruction_format = context
					.getInstructionFormats().get(opcodeKeyString);
			// Ensure the key returned a valid InstructionClass object.
			if (instruction_format == null)
				return null;

			String instruction_elements[] = temp.split(",");
			for (int i = 0; i < instruction_elements.length; i++)
				instruction_elements[i] = instruction_elements[i].trim();

			opcode = general_register = index_register = address = indirection = register_x = register_y = count = lr = al = devid = 0;
			opcode = context.getOpCodeBytes().get(opcodeKeyString);

			switch (instruction_format) {
			case ONE:
				if (opcodeKeyString.equals("JZ"))
					general_register = Byte.parseByte(temp.substring(3, 4));
				else
					general_register = Byte.parseByte(temp.substring(4, 5));
				index_register = Byte.parseByte(instruction_elements[1]);

				labelCheck = instruction_elements[2];

				// Check to see if the address field contains a label
				if (Character.isAlphabetic(labelCheck.charAt(0))) {

					// Check if the label is currently in the label table
					if ((labelIndex = searchLabelTable(labelCheck)) != -1) {
						// If the label's address if above 127, need to do
						// indirection
						if (labelTable.get(labelIndex).address >= 128) {
							address = JUMP_INDIRECTION_ADDR;
						} else {
							// Else, just fetch the address from the table
							address = (byte) labelTable.get(labelIndex).address;
						}

						// Add this instruction to the label's list of total
						// references
						labelTable.get(labelIndex).references
								.add(memory_location);

						// If this is a forward reference, push it onto the
						// label's forward reference stack
						if (address == 0) {
							labelTable.get(labelIndex).forwardReferences
									.push(memory_location);
							logger.debug("Found another forward reference for label: "
									+ labelCheck
									+ " at address: "
									+ memory_location);
						} else {
							logger.debug("Label: " + labelCheck
									+ " translated to address " + address);
						}
					} else {
						// Create a new label entry if this is the first time
						// seeing the label
						labelTable.add(new LabelEntry(labelCheck, (byte) 0,
								memory_location));
						logger.debug("Creating new label: " + labelCheck
								+ " for forward reference at address: "
								+ memory_location);
					}
				} else {
					address = Byte.parseByte(labelCheck);
				}

				// Optional indirection check
				if (instruction_elements.length < 4)
					indirection = 0;
				else
					indirection = Byte.parseByte(instruction_elements[3]);

				// Set indirection bit if needed to do the label jump
				if (address == JUMP_INDIRECTION_ADDR)
					indirection = 1;
				break;
			case TWO:
				index_register = Byte.parseByte(temp.substring(4, 5));

				labelCheck = instruction_elements[1];

				// Check to see if the address field contains a label
				if (Character.isAlphabetic(labelCheck.charAt(0))) {

					// Check if the label is currently in the label table
					if ((labelIndex = searchLabelTable(labelCheck)) != -1) {

						// If the label's address if above 127, need to do
						// indirection
						if (labelTable.get(labelIndex).address >= 128) {
							address = JUMP_INDIRECTION_ADDR;
						} else {
							// Else, just fetch the address from the table
							address = (byte) labelTable.get(labelIndex).address;
						}

						// Add this instruction to the label's list of total
						// references
						labelTable.get(labelIndex).references
								.add(memory_location);

						// If this is a forward reference, push it onto the
						// label's forward reference stack
						if (address == 0) {
							labelTable.get(labelIndex).forwardReferences
									.push(memory_location);
							logger.debug("Found another forward reference for label: "
									+ labelCheck
									+ " at address: "
									+ memory_location);
						} else {
							logger.debug("Label: " + labelCheck
									+ " translated to address " + address);
						}
					} else {
						// Create a new label entry if this is the first time
						// seeing the label
						labelTable.add(new LabelEntry(labelCheck, (byte) 0,
								memory_location));
						logger.debug("Creating new label: " + labelCheck
								+ " for forward reference at address: "
								+ memory_location);

					}
				} else {
					address = Byte.parseByte(labelCheck);
				}

				// Optional indirection check
				if (instruction_elements.length < 3)
					indirection = 0;
				else
					indirection = Byte.parseByte(instruction_elements[2]);

				// Set indirection bit if needed to do the label jump
				if (address == JUMP_INDIRECTION_ADDR)
					indirection = 1;
				break;
			case THREE:
				general_register = Byte.parseByte(temp.substring(4, 5));
				address = Byte.parseByte(instruction_elements[1]);
				break;
			case FOUR:
				// Immed portion is optional
				try {
					address = Byte.parseByte(temp.substring(4, 5));
				} catch (NumberFormatException e) {
					address = 0;
				} catch (StringIndexOutOfBoundsException e2) {
					address = 0;
				}
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
			case EIGHT:
				if (opcodeKeyString.equals("IN"))
					general_register = Byte.parseByte(temp.substring(3, 4));
				else
					general_register = Byte.parseByte(temp.substring(4, 5));
				devid = Byte.parseByte(instruction_elements[1]);
				break;
			default:
				break;
			}

			switch (instruction_format) {
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
				logger.debug("Writing: opcode = " + opcode + ", R = "
						+ general_register + ", X = " + index_register
						+ ", I = " + indirection + ", ADDR = " + address);
				writer.writeLoadStoreFormatInstruction(word, opcode,
						general_register, index_register, indirection, address);
				break;
			case FIVE:
			case SIX:
				logger.debug("Writing: opcode = " + opcode + ", RX = "
						+ register_x + ", RY = " + register_y);
				writer.writeXYArithInstruction(word, opcode, register_x,
						register_y);
				break;
			case SEVEN:
				logger.debug("Writing: opcode = " + opcode + ", R = "
						+ general_register + ", COUNT = " + count + ", LR = "
						+ lr + ", AL = " + al);
				writer.writeShiftInstruction(word, opcode, general_register,
						count, lr, al);
				break;
			case EIGHT:
				logger.debug("Writing: opcode= " + opcode + ", R= "
						+ general_register + ", DEVID = " + devid);
				writer.writeIOInstruction(word, opcode, general_register, devid);
				break;
			default:
				break;
			}
			return word;
		} catch (Exception e) {
			// This will be a Illegal Operation code
			logger.error(e);
			return null;
		}
	}

	/**
	 * Searches the label table for the given string.
	 * 
	 * @param searchString
	 * @return Index of the label within the table, or -1 if not found
	 */
	public int searchLabelTable(String searchString) {
		for (int i = 0; i < labelTable.size(); i++)
			if (labelTable.get(i).label.equals(searchString))
				return i;
		return -1;
	}

	/**
	 * Returns the jump address based on an instruction's location in memory.
	 * Essentially searches the references for each label, and returns the
	 * address of the label which the passed address references.
	 * 
	 * Ex. An instruction at memory location 50 references "LABEL". The address
	 * of "LABEL" will be returned.
	 * 
	 * @param instructionAddress
	 *            The address of the instruction.
	 * @return The address of the label (i.e. the jump address)
	 */
	public static short getJumpAddrFromReference(short instructionAddress) {
		short jumpAddr = 0;

		OuterLoop: for (int i = 0; i < labelTable.size(); i++) {
			ArrayList<Short> references = labelTable.get(i).references;
			for (int j = 0; j < references.size(); j++) {
				if (references.get(j) == instructionAddress) {
					jumpAddr = labelTable.get(i).address;
					break OuterLoop;
				}
			}
		}
		return jumpAddr;
	}

	/**
	 * Tests a memory address for contents.
	 * 
	 * @param address
	 *            The memory location to test for contents.
	 * @param memory
	 *            The physical memory that contains the address.
	 * @return true if the address is empty; false otherwise.
	 */
	public boolean isAddressEmpty(int address, Memory memory) {
		Word word = memory.read(address);
		return word.isEmpty();
	}

	/**
	 * Entry for the label table. Holds the label and its address. A stack is
	 * used to keep track of forward references, so they can be resolved when
	 * the label is finally found. An ArrayList is used to keep track of all
	 * references to the label - forward or backward.
	 */
	class LabelEntry {
		public String label;
		public short address;
		public Stack<Short> forwardReferences;
		public ArrayList<Short> references;

		public LabelEntry(String label, short address) {
			this.label = label;
			this.address = address;
			forwardReferences = new Stack<Short>();
			references = new ArrayList<Short>();
		}

		public LabelEntry(String label, short address, short forwardRefAddress) {
			this.label = label;
			this.address = address;
			forwardReferences = new Stack<Short>();
			forwardReferences.push(forwardRefAddress);
			references = new ArrayList<Short>();
			references.add(forwardRefAddress);
		}
	}
}
