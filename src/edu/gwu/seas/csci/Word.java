/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * A word is simply an 18-bit BitSet.
 * 
 * @author Alex Remily
 */
public class Word extends BitSet {

    /**
     * For serialization. Gets rid of compiler warning.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an 18 bit word initialized to zero.
     */
    public Word() {
	super(18);
    }

    /**
     * @param opcode
     * @param generalRegister
     * @param indexRegister
     * @param indirection
     * @param address
     */
    public void setLoadStoreInstruction(byte opcode, byte generalRegister,
	    byte indexRegister, byte indirection, byte address) {

    }

    /**
     * Sets the instruction opcode to bit positions 0-5.
     * 
     * @param opcode
     */
    public void setOpcode(byte opcode) {

    }
}
