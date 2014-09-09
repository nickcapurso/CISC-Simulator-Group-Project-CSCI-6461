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
	 * 			The BitSet to be converted.
	 * @param numBits 
	 * 			The number of bits in the BitSet (BitSet.length method is not 
	 * 			sufficient due to its implementation). You can get 
	 * 			special-register lengths in InstructionBitFormats.java.
	 * @return The numeric value represented by the BitSet.
	 */
	public static int convertToInt(final BitSet set, final int numBits){
		int value = 0;

		for(int i = numBits-1; i >= 0; i --)
			value += set.get(i) ? (1 << (numBits-1-i)) : 0;		
		
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
     * Prints the binary representation of a BitSet.
     * 
     * @param name
     * 			  The name of the BitSet (i.e. "OPCODE", "ADDR", etc.)
     * @param set 
     * 			  The BitSet to print.			
     * @param numBits
     * 			  The number of bits in the BitSet.
     */
    public static void BitSetToString(final String name, final BitSet set, final int numBits){
    	System.out.println(name + " contains: ");
    	for(int i = 0; i < numBits; i++)
    		System.out.print(set.get(i) == false? "0" : "1");
    	System.out.println();
    }
}
