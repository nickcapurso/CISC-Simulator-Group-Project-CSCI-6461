package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * Provides public static utility methods for use in various locations 
 * throughout the simulator.
 *
 */
public class Utils {
	
	/**
	 * Converts a BitSet to its numeric equivalent, stored in a byte. The return value can be
	 * used for numeric based comparisons.
	 * 
	 * @param set The BitSet to be converted.
	 * @param numBits The number of bits in the BitSet (BitSet.length method is not sufficient due to its implementation). You can get special-register lengths in InstructionBitFormats.java.
	 * @return The numeric value represented by the BitSet.
	 */
	public static byte convertToByte(final BitSet set, final int numBits){
		byte value = 0;

		for(int i = numBits-1; i >= 0; i --)
			value += set.get(i) ? (byte)(1 << (numBits-1-i)) : 0;		
		
		//System.out.println("Value: " + value);
		return value;
	}
}
