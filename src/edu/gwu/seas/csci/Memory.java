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
     * @param address
     * @return
     * @throws IndexOutOfBoundsException
     */
    public Word get(BitSet address) throws IndexOutOfBoundsException {
	// TODO: Implement me.
	return null;
    }
}
