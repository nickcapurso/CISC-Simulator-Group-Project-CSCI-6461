package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * Example abstract register class - maybe discuss this on Wednesday.
 */
public abstract class Register extends BitSet{
	private int numBits;
	private BitSet bitset;
	
	/**
	 * Default constructor sets size to 18.
	 */
	public Register(){
		numBits = 18;
		bitset = new BitSet(numBits);
	}
	
	/**
	 * Constructor with size given.
	 * @param numBits 
	 * 			Number of bits to be held in this register.
	 */
	public Register(int numBits){
		this.numBits = numBits;
		bitset = new BitSet(numBits);
	}
	
	/**
	 * Copies the value of the passed BitSet into this register. If the size of
	 * the passed BitSet is larger than the register's size, the value
	 * is truncated. If it is smaller, then the register gets padded with 0's.
	 * 
	 * @param set 
	 * 			BitSet to set the register equal to.
	 * @param setNumBits 
	 * 			The number of bits represented by the bitset.
	 */
	public void set(BitSet set, int setNumBits){
		if(setNumBits <= numBits){
			bitset.clear();
			for(int i = numBits - setNumBits; i < numBits; i++)
				bitset.set(i, set.get(i));
		}else{
			//Truncate
			for(int i = setNumBits - numBits, j = 0; i < setNumBits; i++, j++)
				bitset.set(j, set.get(i));
		}
	}
	
	/**
	 * Copies the value of another register into this register. Same function
	 * as set(BitSet set, int setNumBits)
	 * 
	 * @param register 
	 * 			Register to set this register equal to.
	 */
	public void set(Register register){
		int registerNumBits = register.getNumBits();
		if(registerNumBits <= numBits){
			bitset.clear();
			for(int i = numBits - registerNumBits; i < numBits; i++)
				bitset.set(i, register.get(i));
		}else{
			//Truncate
			for(int i = registerNumBits - numBits, j = 0; i < registerNumBits; i++, j++)
				bitset.set(j, register.get(i));
		}
	}
	
	/**
	 * @return The BitSet representing the value in this register.
	 */
	public BitSet get(){
		return bitset;
	}
	
	/**
	 * @return The number of bits held in this register.
	 */
	public int getNumBits(){
		return numBits;
	}
}
