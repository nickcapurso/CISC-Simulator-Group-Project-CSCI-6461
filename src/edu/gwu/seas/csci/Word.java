/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * A word is simply an 18-bit BitSet that is used to populate locations in
 * Memory.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Word" + super.toString() + " ";
	}

}
