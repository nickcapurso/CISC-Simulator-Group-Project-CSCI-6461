package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * The Register class extends the BitSet class. 
 * It provides greater code readability and also maintains the number of
 * bits a given Register was instantiated to be (for example, MAR is 18 bits).
 * 
 * @author Nick Capurso
 */
public class Register extends BitSet{
	private int numBits;

	/**
	 * Default constructor sets size to 18.
	 */
	public Register(){
		super(18);
		numBits = 18;
	}

	/**
	 * Constructor with size given.
	 * @param numBits 
	 * 			Number of bits to be held in this register.
	 */
	public Register(int numBits){
		super(numBits);
		this.numBits = numBits;
	}

	/**
	 * @return The BitSet representing the value in this register.
	 */
	public BitSet getValue(){
		return this;
	}

	/**
	 * @return The number of bits held in this register.
	 */
	public int getNumBits(){
		return numBits;
	}
}
