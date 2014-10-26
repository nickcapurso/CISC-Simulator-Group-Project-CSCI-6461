/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class InstructionLoader implements Loader {

	static final Logger logger = LogManager.getLogger(InstructionLoader.class
			.getName());

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
	
	private ArrayList<LabelEntry> labelTable = new ArrayList<LabelEntry>();
	private short memory_location = 0;

	/**
	 * FileLoader is constructed with a default reader.
	 */
	public InstructionLoader() {
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
	public void load(BufferedReader reader) throws ParseException {
		try {
			String temp = null;
			memory_location = 8; // Locations 0-5 are reserved.

			while ((temp = reader.readLine()) != null) {
				if (temp.equals("") || temp.charAt(0) == '/') {
					logger.debug("Ignoring line: blank or a comment");
					continue;
				}
				
				//Checking for jump labels
				if(temp.indexOf(':') != -1){
					String label = temp.substring(0, temp.indexOf(':')).trim();
					int labelIndex = 0;
					if((labelIndex = searchLabelTable(label)) == -1){
						if(memory_location > 128)
							throw new ParseException("Error: new jump label address won't fit within 7 bits",0);
						
						labelTable.add(new LabelEntry(label, (byte) memory_location));
						logger.debug("Found new label: " + label);
					}else{
						//Resolve forward references
						if(labelTable.get(labelIndex).address == 0){
							labelTable.get(labelIndex).address = (byte) memory_location;
							
							LabelEntry entry = labelTable.get(labelIndex);
							Stack<Short> references = entry.forwardReferences;
							while(!references.isEmpty()){
								short address = references.pop();
								Word word = memory.read(address);
								Utils.byteToBitSetDeepCopy(entry.address, word, 
										(byte) InstructionBitFormats.LD_STR_ADDR_SIZE, 
										(byte) InstructionBitFormats.LD_STR_ADDR_END);
								logger.debug("Resolving forward reference at address: " + address + ", jump address = " + entry.address);
								memory.write(word, address);
							}
						}else{
							throw new ParseException("Error: Duplicate Label: " + label,0);
						}
					}
					continue;
				}

				Word word = StringToWord(temp);
				
				if (word != null)
					memory.write(word, memory_location++);
			}
			logger.debug(memory_location);
			reader.close();
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}
	
	public Word StringToWord(String input) {
		String temp = input;
		String labelCheck;
		short labelAddress = 0;
		byte opcode, general_register, index_register, address, indirection, register_x, register_y, count, lr, al, devid;
		Word word = new Word();
		int numElements = 0;
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
			for(int i = 0; i < instruction_elements.length; i++)
				instruction_elements[i] = instruction_elements[i].trim();
			
			numElements = instruction_elements.length;
			
			//Checking for end-of-line comments
			if(instruction_elements[numElements-1].indexOf('/') != -1){
				logger.debug("End-of-line comment found, ignoring");
				String temp2 = instruction_elements[numElements-1];
				temp2 = temp2.substring(0, temp2.indexOf('/'));
				instruction_elements[numElements-1] = temp2;
			}
			
			opcode = general_register = index_register = address = indirection = register_x = register_y = count = lr = al = devid = 0;
			opcode = context.getOpCodeBytes().get(opcodeKeyString);

			switch (instruction_format) {
			case ONE:
				if(opcodeKeyString.equals("JZ"))
					general_register = Byte.parseByte(temp.substring(3, 4));
				else
					general_register = Byte.parseByte(temp.substring(4, 5));
				index_register = Byte.parseByte(instruction_elements[1]);
				
				labelCheck = instruction_elements[2];
				
				if(Character.isAlphabetic(labelCheck.charAt(0))){
					if((labelIndex = searchLabelTable(labelCheck)) != -1){
						//Get address
						address = labelTable.get(labelIndex).address;
						
						//If this is a forward reference
						if(address == 0){
							labelTable.get(labelIndex).forwardReferences.push(memory_location);
							logger.debug("Found another forward reference for label: " + labelCheck + " at address: " + memory_location);
						}else{
							logger.debug("Label: " + labelCheck + " translated to address " + address);
						}
					}else{
						if(memory_location > 128)
							throw new ParseException("Error: new jump label address won't fit within 7 bits",0);
						//Add forward reference
						labelTable.add(new LabelEntry(labelCheck, (byte) 0, memory_location));
						logger.debug("Creating new label: " + labelCheck + " for forward reference at address: " + memory_location);
					}
				}else{
					address = Byte.parseByte(labelCheck);
				}
				
				// Optional indirection check
				if (instruction_elements.length < 4)
					indirection = 0;
				else
					indirection = Byte.parseByte(instruction_elements[3]
							);
				break;
			case TWO:
				index_register = Byte
						.parseByte(temp.substring(4, 5));
				
				labelCheck = instruction_elements[1];
				
				if(Character.isAlphabetic(labelCheck.charAt(0))){
					if((labelIndex = searchLabelTable(labelCheck)) != -1){
						//Get address
						address = labelTable.get(labelIndex).address;
						
						//If this is a forward reference
						if(address == 0){
							labelTable.get(labelIndex).forwardReferences.push(memory_location);
							logger.debug("Found another forward reference for label: " + labelCheck + " at address: " + memory_location);
						}else{
							logger.debug("Label: " + labelCheck + " translated to address " + address);
						}
					}else{
						if(memory_location > 128)
							throw new ParseException("Error: new jump label address won't fit within 7 bits",0);
						//Add forward reference
						labelTable.add(new LabelEntry(labelCheck, (byte) 0, memory_location));
						logger.debug("Creating new label: " + labelCheck + " for forward reference at address: " + memory_location);
						
					}
				}else{
					address = Byte.parseByte(labelCheck);
				}

				// Optional indirection check
				if (instruction_elements.length < 3)
					indirection = 0;
				else
					indirection = Byte.parseByte(instruction_elements[2]
							);
				break;
			case THREE:
				general_register = Byte.parseByte(temp.substring(4, 5)
						);
				address = Byte.parseByte(instruction_elements[1]);
				break;
			case FOUR:
				address = Byte.parseByte(temp.substring(4, temp.length())
						);
				break;
			case FIVE:
				register_x = Byte.parseByte(temp.substring(4, 5));
				break;
			case SIX:
				register_x = Byte.parseByte(temp.substring(4, 5));
				register_y = Byte.parseByte(instruction_elements[1]);
				break;
			case SEVEN:
				general_register = Byte.parseByte(temp.substring(4, 5)
						);
				count = Byte.parseByte(instruction_elements[1]);
				lr = Byte.parseByte(instruction_elements[2]);
				al = Byte.parseByte(instruction_elements[3]);
				break;
			case EIGHT:
				if (opcodeKeyString.equals("IN"))
					general_register = Byte.parseByte(temp.substring(3, 4)
							);
				else
					general_register = Byte.parseByte(temp.substring(4, 5)
							);
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
						general_register, index_register, indirection,
						address);
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
						+ general_register + ", COUNT = " + count
						+ ", LR = " + lr + ", AL = " + al);
				writer.writeShiftInstruction(word, opcode,
						general_register, count, lr, al);
				break;
			case EIGHT:
				logger.debug("Writing: opcode= " + opcode + ", R= "
						+ general_register + ", DEVID = " + devid);
				writer.writeIOInstruction(word, opcode, general_register,
						devid);
				break;
			default:
				break;
			}
			return word;
		} catch (Exception e) {
			// This will be a Illegal Operation code
			System.out.println(e);
			return null;
		}
	}

	@Override
	public void load() throws NullPointerException, ParseException,
			IllegalArgumentException {
		this.load(reader);
	}
	
	public int searchLabelTable(String searchString){
		for(int i = 0; i < labelTable.size(); i++)
			if(labelTable.get(i).label.equals(searchString))
				return i;
		return -1;
	}
	
	class LabelEntry{
		public String label;
		public byte address;
		public Stack<Short> forwardReferences; 
		public LabelEntry(String label, byte address){
			this.label = label;
			this.address = address;
			forwardReferences = new Stack<Short>();
		}
		
		public LabelEntry(String label, byte address, short forwardRefAddress){
			this.label = label;
			this.address = address;
			forwardReferences = new Stack<Short>();
			forwardReferences.push(forwardRefAddress);
		}
	}
}
