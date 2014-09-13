/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * Provides the memory for the {@link Computer} class. Leverages the Singleton
 * pattern to initialize and maintain exactly one set of system memory per
 * computer.
 * 
 * @author Alex Remily
 */
public class Memory {

    /**
     * Static and final reference to memory instance ensures there is only one
     * memory object.
     */
    private static final Memory INSTANCE = new Memory();

    /**
     * An array to hold words in memory.
     */
    private Word[] memory = new Word[2048];

    /**
     * Private constructor forces access via the getInstance() method.
     */
    private Memory() {
    	for(int i = 0; i < memory.length; i++)
    		memory[i] = new Word();
    }

    /**
     * @return The {@link Computer} memory.
     */
    public static Memory getInstance() {
	return INSTANCE;
    }

    /**
     * Puts a {@link Word} in the specified address.
     * 
     * @param word
     *            The contents to place in memory.
     * @param address
     *            The memory address to place the contents.
     */
    public void put(Word word, int address) throws IndexOutOfBoundsException {
	// TODO: Need to work out Fault and Trap Logic
	if (address > memory.length)
	    throw new IndexOutOfBoundsException();
	memory[address] = word;
    }
    
    /**
     * Puts a {@link Word} in the address specified by the passed register.
     * 
     * @param word 
     * 				The contents to place in memory.
     * @param register 
     * 				The BitSet containing the address.
     * @param numBits 
     * 				The size of the BitSet in bits.
     * @throws IndexOutOfBoundsException
     */
    public void put(Word word, BitSet register, int numBits) throws IndexOutOfBoundsException {
    	int address = Utils.convertToInt(register, numBits);
    	
    	if(address > memory.length)
    		throw new IndexOutOfBoundsException();
    	memory[address] = word;
    }

    /**
     * Retrieves the {@link Word} at the specified memory address.
     * 
     * @param address
     *            The memory address of the contents to retrieve.
     * @return The contents of the specified memory address.
     */
    public Word get(int address) throws IndexOutOfBoundsException {
	// TODO: Need to work out Fault and Trap Logic
	if (address > memory.length)
	    throw new IndexOutOfBoundsException();
	return memory[address];
    }

    /**
     * Retrieves the {@link Word} at the address specified in the passed register.
     * 
     * @param register The BitSet containing the value to convert.
     * @param numBits The size of the BitSet in bits.
     * @return The numeric (base 10) representation of what is stored in the BitSet.
     * @throws IndexOutOfBoundsException
     */
    public Word get(BitSet register, int numBits) throws IndexOutOfBoundsException {
    	int address = Utils.convertToInt(register, numBits);
    	
    	if(address > memory.length)
    		throw new IndexOutOfBoundsException();
    	return memory[address];
    }
}
