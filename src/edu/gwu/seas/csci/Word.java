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

    public void setOpcode(byte opcode) {

    }

    public void setGeneralRegister(byte register) {

    }

    public void setIndexRegister(byte register) {

    }

    public void setAddress(byte address) {

    }
}
