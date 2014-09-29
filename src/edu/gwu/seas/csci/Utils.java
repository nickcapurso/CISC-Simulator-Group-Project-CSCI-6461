package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * The Utils class contains a variety of utility methods that various other classes 
 * make use of. It contains various conversion methods - for instance, to convert 
 * back and forth a BitSet and its numeric equivalent. It also includes deep copy 
 * methods to copy the contents of one Register or BitSet to another.
 * 
 * @author Nick Capurso, Alex Remily
 */
public class Utils {
	
	private static Context context = Context.getInstance();
	private static InstructionWriter writer = new InstructionWriter();

	/**
	 * Converts a BitSet to its numeric equivalent, stored in a byte. The return
	 * value can be used for numeric based comparisons.
	 * 
	 * @param set
	 *            The BitSet to be converted.
	 * @param numBits
	 *            The number of bits in the BitSet (BitSet.length method is not
	 *            sufficient due to its implementation). You can get
	 *            special-register lengths in InstructionBitFormats.java.
	 * @return The numeric value represented by the BitSet.
	 */
	public static byte convertToByte(final BitSet set, final int numBits) {
		byte value = 0;

		for (int i = numBits - 1; i >= 0; i--)
			value += set.get(i) ? (byte) (1 << (numBits - 1 - i)) : 0;
			return value;
	}

	/**
	 * Converts a BitSet to its numeric equivalent, stored in a int. Can be used
	 * for values expected to be greater than a byte (i.e. addresses).
	 * 
	 * @param set
	 *            The BitSet to be converted.
	 * @param numBits
	 *            The number of bits in the BitSet (BitSet.length method is not
	 *            sufficient due to its implementation). You can get
	 *            special-register lengths in InstructionBitFormats.java.
	 * @return The numeric value represented by the BitSet.
	 */
	public static int convertToInt(final BitSet set, final int numBits) {
		int value = 0;

		for (int i = numBits - 1; i >= 0; i--)
			value += set.get(i) ? (1 << (numBits - 1 - i)) : 0;

			return value;
	}

	/**
	 * Determines whether the n<sup>th</sup> bit in a byte is set, with zero
	 * being the LSB and 7 being the MSB.
	 * 
	 * @param b
	 *            The byte under test.
	 * @param bit
	 *            The bit (0-7) to test.
	 * @return True if the bit is set, false otherwise.
	 */
	public static boolean isBitSet(byte b, byte bit) {
		return (b & (1 << bit)) != 0;
	}

	/**
	 * Copies the contents of one BitSet to another.
	 * 
	 * @param source
	 *            The source BitSet.
	 * @param sourceBits
	 *            The number of bits represented by the source.
	 * @param destination
	 *            The destination BitSet.
	 * @param destinationBits
	 *            The number of bits represented by the destination.
	 */
	public static void bitsetDeepCopy(BitSet source, int sourceBits,
			BitSet destination, int destinationBits) {
		if (sourceBits <= destinationBits) {
			destination.clear();
			for (int i = destinationBits - sourceBits, j = 0; i < destinationBits; i++, j++)
				destination.set(i, source.get(j));

		} else {
			// Truncate
			for (int i = sourceBits - destinationBits, j = 0; i < sourceBits; i++, j++)
				destination.set(j, source.get(i));
		}
	}

	/**
	 * Prints the binary representation of a BitSet.
	 * 
	 * @param name
	 *            The name of the BitSet (i.e. "OPCODE", "ADDR", etc.)
	 * @param set
	 *            The BitSet to print.
	 * @param numBits
	 *            The number of bits in the BitSet.
	 */
	public static void bitsetToString(final String name, final BitSet set,
			final int numBits) {
		System.out.println(name + " contains: ");
		for (int i = 0; i < numBits; i++)
			System.out.print(set.get(i) == false ? "0" : "1");
		System.out.println();
	}

	/**
	 * TODO: Comment Me.
	 * 
	 * @param source
	 * @param target
	 * @param num_bits_to_copy
	 * @param bit_index
	 */
	public static void byteToBitSetDeepCopy(byte source, Word target,
			byte num_bits_to_copy, byte bit_index) {
		for (byte i = 0; i < num_bits_to_copy; i++) {
			if (Utils.isBitSet(source, i))
				target.set(bit_index - i);
		}
	}

	/**
	 * Converts a int value into a BitSet. If the value cannot
	 * fit into a BitSet of size setSize, it should be truncated.
	 *  
	 * @param value The value to convert into a BitSet representation.
	 * @param setSize The number of bits the BitSet will hold.
	 * @return
	 */
	public static BitSet intToBitSet(int value, int setSize){
		BitSet set = new BitSet(setSize);
		for(int i = setSize-1; i > 0; i--){
			set.set(i, (value & 1) == 1? true:false);
			value >>>= 1;
		}
		return set;
	}

	/**
	 * Converts a Register (casted to it's superclass, BitSet)
	 * to a Word (since subclasses can't be casted to subclasses)
	 * 
	 * @param set The Register
	 * @param numBits The number of bits represented by the Register (a Word is 18-bits)
	 * @return
	 */
	public static Word registerToWord(BitSet set, int numBits){
		Word word = new Word();
		if (numBits <= 18) {
			for (int i = 18 - numBits, j = 0; i < 18; i++, j++)
				word.set(i, set.get(j));

		} else {
			// Truncate
			for (int i = numBits - 18, j = 0; i < numBits; i++, j++)
				word.set(j, set.get(i));
		}
		return word;
	}
	
	public static Word StringToWord(String input) {
		System.out.println("Test input is: " + input);
		Word word = new Word();
		// Read the opcode from the reader line.
		String opcodeKeyString = input.substring(0, 3);
		System.out.println(opcodeKeyString);
		// Determine the class of the opcode from the Computer's
		// context.
		Context.InstructionClass instruction_class = context
				.getOpcodeClasses().get(opcodeKeyString);
		// Ensure the key returned a valid InstructionClass object.
		if (instruction_class == null)
			return null;
		byte general_register = 0;
		String instruction_elements[] = input.split(",");
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
				index_register = Byte.parseByte(input.substring(4, 5));
				address = Byte.parseByte(instruction_elements[1]);
				indirection = Byte.parseByte(instruction_elements[2]);
				opcode = context.getOpCodeBytes().get(opcodeKeyString);
			} else {
				general_register = Byte.parseByte(input.substring(4, 5));
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
			general_register = Byte.parseByte(input.substring(4, 5));
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
		return word;
	}
}
