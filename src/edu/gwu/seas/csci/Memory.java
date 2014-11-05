/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.Arrays;

import edu.gwu.seas.csci.CPU.L1CacheLine;

/**
 * Provides the memory for the {@link Computer} class. Leverages the Singleton
 * pattern to initialize and maintain exactly one set of system memory per
 * computer.
 * 
 * @author Alex Remily
 */
public class Memory {

	/**
	 * An array to hold words in memory.
	 */
	private static Word[] memory = new Word[2048];

	/**
	 * Convenience property specifies the number of memory locations in DRAM.
	 */
	public final static int length = memory.length;

	static {
		for (int i = 0; i < length; i++)
			memory[i] = new Word();
	}

	/**
	 * Static and final reference to memory instance ensures there is only one
	 * memory object.
	 */
	private static final Memory INSTANCE = new Memory();

	/**
	 * Private constructor forces access via the getInstance() method and starts
	 * the memory controller thread, which manages the reads and writes between
	 * the L1 cache, write buffer, and DRAM.
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
	 * @param address
	 * @return
	 */
	public Word[] getMemoryBlock(int address) {
		int tag = (address / 8) * 8;
		Word[] words = Arrays.copyOfRange(memory, tag, tag
				+ L1CacheLine.WORDS_PER_LINE);
		return words;
	}

	/**
	 * TODO: Update the JavaDocs Retrieves the {@link Word} at the specified
	 * memory address.
	 * 
	 * @param address
	 *            The memory address of the contents to retrieve.
	 * @return The contents of the specified memory address.
	 */
	public Word read(int address) throws IndexOutOfBoundsException {
		if (address > length)
			throw new IndexOutOfBoundsException();
		Word[] words = getMemoryBlock(address);
		return words[0];
	}

	/**
	 * Puts a {@link Word} in the specified address.
	 * 
	 * @param word
	 *            The contents to place in memory.
	 * @param address
	 *            The memory address to place the contents.
	 */
	public void write(Word word, int address) throws IndexOutOfBoundsException {
		if (address > length)
			throw new IndexOutOfBoundsException();
		memory[address] = word;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < memory.length; i++)
			output.append(i + ": " + memory[i].toString());
		return "Memory [" + output + "]";
	}
}
