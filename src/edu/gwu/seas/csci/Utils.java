package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * Provides public static utility methods for use in various locations
 * throughout the simulator.
 * 
 * @author Nick Capurso, Alex Remily
 *
 */
public class Utils {

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
}
