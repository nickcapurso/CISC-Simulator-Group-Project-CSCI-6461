package edu.gwu.seas.csci;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import edu.gwu.seas.csci.Utils;

/**
 * The CPU class is modeled after the Von Neumann architecture, where the CPU
 * contains various types of registers and controls the logic between them. The
 * registers include four general purpose registers, three index registers,
 * memory-access registers, and various special-purpose registers (for example,
 * a register to hold the opcode of an instruction). In addition, the CPU class
 * executes a program's instructions from the micro-operation level and
 * simulates a clock for the micro-operations to adhere to. Each of the
 * registers is represented by a Register object (an extension of the BitSet
 * object) of the appropriate size. Each register is placed into a HashMap,
 * where the key is the register's name, for simplified access. Finally, the CPU
 * keeps references to the Memory, IRDecoder, and Loader classes to respectively
 * access memory, parse instructions, and load the boot loader program.
 * 
 * This CPU implements a unified, buffered write-through memory cache. All reads
 * and writes are done from and to the cache, respectively, and the memory
 * controller maintains the synchronization between the values in the cache and
 * the values in main memory by the use of an intermediary write buffer.
 */
public class CPU implements CPUConstants {

	/**
	 * This is a 16 {@link L1CacheLine} unified, buffered write-through, L1
	 * cache. All interaction between the CPU and memory occurs via the cache.
	 */
	static class L1Cache {

		/**
		 * The number of {@link L1CacheLine}s in this cache.
		 */
		private static final int CACHE_LENGTH = 16;

		/**
		 * The array of {@link L1CacheLine}s that form the L1 cache.
		 */
		private final L1CacheLine[] cache = new L1CacheLine[CACHE_LENGTH];

		/**
		 * A random number generator used for the cache eviction algorithm.
		 */
		private final Random generator = new Random();

		/**
		 * Used to simplify cache initialization.
		 */
		private byte cache_adds_counter = 0;

		/**
		 * Adds a cache line to the cache. If the cache is full, it evicts a
		 * cache line at random to make room for the new cache line. Will not
		 * evict a "dirty" cache line.
		 * 
		 * @param line
		 *            The line to add to the cache.
		 */
		private void add(L1CacheLine line) {
			if (cache_adds_counter < CACHE_LENGTH) {
				System.out.println(Thread.currentThread().getName().toString()
						+ ": Adding cache line with tag " + line.getTag()
						+ " to cache at position " + cache_adds_counter + ".");
				cache[cache_adds_counter++] = line;
			} else {
				int cache_position = 0;
				do {
					cache_position = generator.nextInt(cache.length);
				} while (cache[cache_position].isDirty());
				System.out.println(Thread.currentThread().getName().toString()
						+ ": Adding cache line with tag " + line.getTag()
						+ " to cache at position " + cache_position + ".");
				cache[cache_position] = line;
			}
		}

		/**
		 * Locates the index of the cache line containing a given address.
		 * 
		 * @param address
		 *            The address for which to find the corresponding cache
		 *            line.
		 * @return the index of the cache line, or null if the address is not in
		 *         the cache.
		 */
		private L1CacheLine getCacheLine(int address) {
			L1CacheLine line = null;
			for (int i = 0; i < CACHE_LENGTH; i++) {
				line = cache[i];
				if (line != null && address >= line.getTag()
						&& address < line.getTag() + L1CacheLine.WORDS_PER_LINE)
					return line;
			}
			return null;
		}

		/**
		 * Checks the cache for the contents of a given memory address. Iterates
		 * through the tags of each line in the cache, and checks whether the
		 * value of the search address is between the value of the tag line
		 * address and the tag line address plus the number of words in the
		 * cache line, inclusive.
		 * 
		 * @param address
		 *            The memory address to search for in the cache, i.e., the
		 *            search address.
		 * @return The contents of the specified address or null if the
		 *         specified memory address is not in the cache.
		 */
		private Word read(int address) {
			for (L1CacheLine line : cache) {
				if (line != null) {
					if (address >= line.getTag()
							&& address < line.getTag()
									+ L1CacheLine.WORDS_PER_LINE) {
						System.out.println(Thread.currentThread().getName()
								.toString()
								+ ": Cache read hit.  Found address "
								+ address
								+ " in cache line with tag "
								+ line.getTag()
								+ ".");
						return line.getWord(address - line.getTag());
					}
				}
			}
			System.out.println("Cache read miss.");
			return null;
		}

		/**
		 * Updates the flags bitmask on the cache line that contains the
		 * specified address. If the value of the add parameter is true, this
		 * method adds the value of the flag parameter to the cache line flags
		 * bitmask, otherwise it subtracts the value of the flag parameter from
		 * the cache line flags bitmask.
		 * 
		 * @param address
		 *            The address whose cache line flags bitmask to update.
		 * @param flag
		 *            The value to add or subtract from the cache line flags
		 *            bitmask.
		 * @param add
		 *            Whether to add or subtract the value of flag from the
		 *            cache line flags bitmask.
		 */
		private void upadteFlags(int address, byte flag, boolean add) {
			L1CacheLine line = l1_cache.getCacheLine(address);
			byte flags = line.getFlags();
			if (add)
				flags = (byte) (flags + flag);
			else
				flags = (byte) (flags - flag);
			line.setFlags(flags);
		}

		/**
		 * Writes the value of the specified word to the cache at the specified
		 * address location. This method handles two cases:
		 * <ol>
		 * <li>Cache Hit.</li>
		 * <li>Cache Miss.</li>
		 * </ol>
		 * For a cache hit, this method updates the value in the cache with the
		 * value of the word parameter, sets the appropriate dirty flag on the
		 * cache line, and adds the value of the word parameter to the write
		 * buffer. For a cache miss, this method returns false.
		 * 
		 * @param word
		 *            The content to write to the cache.
		 * @param address
		 *            The main memory address of the content to write to the
		 *            cache.
		 * @return true if a cache hit; otherwise false.
		 */
		private boolean write(Word word, int address) {
			for (L1CacheLine line : cache) {
				if (line != null) {
					if (address >= line.getTag()
							&& address < line.getTag()
									+ L1CacheLine.WORDS_PER_LINE) {
						System.out.println(Thread.currentThread().getName()
								.toString()
								+ ": Cache write hit.  Found address "
								+ address
								+ " in cache line with tag "
								+ line.getTag() + ".");
						int index = address - line.getTag();
						byte flag = Utils.l1IndexToFlag(index);
						line.updateFlags(flag, true);
						line.setWord(word, index);
						return write_buffer.addToBuffer(word, address, flag);
					}
				}
			}
			System.out.println("Cache write miss.");
			return false;
		}

		/**
		 * @return the cache
		 */
		L1CacheLine[] getCache() {
			return cache;
		}
	}

	/**
	 * Represents the structure of each line in the L1 cache. We have not been
	 * directed to use a specific cache line structure, so for our case each
	 * line contains 1 address tag, 6 Words, and 1 flags bitmask. This puts our
	 * total L1 cache contents at 96 Words (16 lines x 6 words per line).
	 */
	static class L1CacheLine {

		/**
		 * The number of words in each cache line.
		 */
		public static final int WORDS_PER_LINE = 6;

		/**
		 * The main memory address of this cache line.
		 */
		private int tag;

		/**
		 * The contents of the main memory location identified by the tag
		 * address.
		 */
		private Word[] words;

		/**
		 * A bitmask for cache operations.
		 */
		private byte flags;

		/**
		 * Creates a new cache line from the given parameters.
		 * 
		 * @param address
		 * @param block
		 * @param flags
		 */
		private L1CacheLine(int address, Word[] block, byte flags) {
			this.tag = address;
			this.words = block;
			this.flags = flags;
		}

		/**
		 * @return the flags
		 */
		byte getFlags() {
			return flags;
		}

		/**
		 * @return the tag
		 */
		int getTag() {
			return tag;
		}

		/**
		 * @return the words
		 */
		private Word getWord(int index) {
			return words[index];
		}

		/**
		 * Checks to see if a value in the cache line differs from the value
		 * that was originally fetched from memory. This would occur if the CPU
		 * has written to the cache and the memory controller has not yet
		 * updated the value in main memory,
		 * 
		 * @return true if a value in the cache line differs from that in its
		 *         corresponding address location in main memory.
		 */
		private boolean isDirty() {
			return flags > 0;
		}

		/**
		 * @param flags
		 *            the flags to set
		 */
		private void setFlags(byte flags) {
			this.flags = flags;
		}

		/**
		 * @param word
		 * @param index
		 */
		private void setWord(Word word, int index) {
			words[index] = word;
		}

		/**
		 * @param words
		 *            the words to set
		 */
		private void setWords(Word[] words) {
			this.words = words;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "L1CacheLine [tag=" + tag + ", words="
					+ Arrays.toString(words) + ", flags=" + flags + "]";
		}

		/**
		 * Updates the flags bitmask. If the value of the add parameter is true,
		 * this method adds the value of the flag parameter to the cache line
		 * flags bitmask, otherwise it subtracts the value of the flag parameter
		 * from the cache line flags bitmask.
		 * 
		 * @param flag
		 *            The value to add or subtract from the cache line flags
		 *            bitmask.
		 * @param add
		 *            Whether to add or subtract the value of flag from the
		 *            cache line flags bitmask.
		 */
		private void updateFlags(byte flag, boolean add) {
			if (add)
				this.flags = (byte) (flags + flag);
			else
				this.flags = (byte) (flags - flag);
		}
	}

	/**
	 * Maintains synchronization of the memory contents between the L1 cache and
	 * main memory by use of an intermediary write buffer. Runs as a separate
	 * Runnable to simulate the hardware separation between the CPU and the
	 * memory controller. Waits for the CPU to write to the write buffer and
	 * then processes the contents of the buffer until the buffer is empty
	 * again, at which time it returns to the waiting state until the CPU makes
	 * another write to the write buffer.
	 */
	static class MemoryController implements Runnable {

		private boolean terminate = false;

		@Override
		public void run() {
			while (!terminate) {
				synchronized (write_buffer) {
					if (write_buffer.isEmpty()) {
						try {
							System.out
									.println(Thread.currentThread().getName()
											.toString()
											+ ":  Memory controller is waiting for an element to be added to the write write_buffer.");
							write_buffer.wait();

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out
								.println(Thread.currentThread().getName()
										.toString()
										+ ":  Memory controller has been notified that an element has been added to the write write_buffer.");
						write_buffer.writeToMainMemory();
					} else {
						write_buffer.writeToMainMemory();
					}
				}
				synchronized (this) {
					this.notify();
				}
			}
		}

		/**
		 * Tells this runnable to terminate the run method.
		 */
		public void terminate() {
			synchronized (write_buffer) {
				terminate = true;
				write_buffer.notify();
			}
		}
	}

	/**
	 * Holds data being written back from the cache to main memory. This is a
	 * variation of write-through caching called buffered write-through.
	 * Implements a simple FIFO queue. Notifies the memory controller when the
	 * write buffer has elements to process.
	 */
	static class WriteBuffer {

		/**
		 * Represents the structure of each element in the write buffer queue.
		 * Each element contains the main memory address, the contents of that
		 * address, and a flag value that indicates the index of the address on
		 * its cache line in the L1 cache.
		 */
		private class WriteBufferContents {
			private int address;
			private Word word;
			private byte flag;

			/**
			 * @param address
			 * @param word
			 */
			private WriteBufferContents(int address, Word word, byte flag) {
				this.address = address;
				this.word = word;
				this.flag = flag;
			}

			/**
			 * @return the address
			 */
			public int getAddress() {
				return address;
			}

			/**
			 * @return the flag
			 */
			public byte getFlag() {
				return flag;
			}

			/**
			 * @return the word
			 */
			public Word getWord() {
				return word;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				return "WriteBufferContents [address=" + address + ", word="
						+ word + ", flag=" + flag + "]";
			}
		}

		/**
		 * FIFO queue of size 4 serves as a write write_buffer. Holds the dirty
		 * L1CacheLines as elements in the queue. L1CacheLines flag bits are
		 * ignored, and the L1CacheLine word is written to the main memory
		 * address specified by the L1CacheLine tag value.
		 */
		private final Queue<WriteBufferContents> buffer = new ArrayBlockingQueue<WriteBufferContents>(
				4);

		/**
		 * Adds a dirty cache value to the write buffer for synchronization with
		 * main memory. If the write_buffer is full, the add is blocked and the
		 * CPU will stall until there is room in the write_buffer.
		 * 
		 * @param word
		 * @param address
		 * @param flag
		 * @return
		 */
		private boolean addToBuffer(Word word, int address, byte flag) {
			WriteBufferContents contents = new WriteBufferContents(address,
					word, flag);
			boolean success = false;
			while (buffer.size() == 4) {
				synchronized (memory_controller) {
					try {
						System.out
								.println(Thread.currentThread().getName()
										.toString()
										+ ": Buffer is full.  Waiting on memory controller to process buffer.");
						memory_controller.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (this) {
				success = buffer.add(contents);
				System.out
						.println(Thread.currentThread().getName().toString()
								+ ": Element added to write write_buffer.  Notifying memory controller.");
				notify();
			}

			System.out.println(Thread.currentThread().getName().toString()
					+ ": Write write_buffer is returning with value of: "
					+ success + ".");
			return success;
		}

		/**
		 * @return the write_buffer
		 */
		private Queue<WriteBufferContents> getBuffer() {
			return buffer;
		}

		/**
		 * Returns true if this WriteBuffer contains no elements.
		 * 
		 * @return true if this WriteBuffer contains no elements.
		 */
		private boolean isEmpty() {
			return buffer.isEmpty();
		}

		/**
		 * Retrieves and writes the head of this FIFO queue to main memory.
		 * Updates the flag on the cache line to reflect the synchronization
		 * between the cache and main memory of the address that was written to.
		 * 
		 * @return true if successful, otherwise false;
		 */
		private boolean writeToMainMemory() {
			boolean success = true;
			try {
				WriteBufferContents line = buffer.remove();
				System.out.println(Thread.currentThread().getName().toString()
						+ ": Removing line " + line + " From write_buffer.");
				Word word = line.getWord();
				int address = line.getAddress();
				byte flag = line.getFlag();
				Memory.getInstance().write(word, address);
				l1_cache.upadteFlags(address, flag, false);
			} catch (NoSuchElementException | IndexOutOfBoundsException e) {
				success = false;
			}
			return success;
		}
	}

	public static final byte BOOTLOADER_START = 010;
	public static Boolean cont_execution = true;
	public static int prog_step = 0;
	public static int cycle_count = 0;
	private Map<String, Register> regMap = new HashMap<String, Register>();
	private IRDecoder irdecoder;
	private ALU alu;

	public String input_buffer = "";
	public int memory_stack = 2047;

	/**
	 * The memory write write_buffer with a fast FIFO algorithm.
	 */
	private static WriteBuffer write_buffer = new WriteBuffer();
	/**
	 * Maintains memory state by managing the reads and writes amongst the
	 * components of memory.
	 */
	private static MemoryController memory_controller = new MemoryController();

	/**
	 * Runs the memory_controller asynchronously in a separate dedicated thread.
	 */
	private Thread memory_controller_thread = new Thread(memory_controller,
			"memory_controller");

	/**
	 * The CPU's L1 cache.
	 */
	private static final L1Cache l1_cache = new L1Cache();

	private boolean waitForInterrupt;
	private boolean jumpTaken;
	private String currentExecution = "";

	// Constructor
	public CPU(Memory memory) {

		memory_controller_thread.start();

		// 4 General Purpose Registers(GPRs)
		regMap.put(R0, new Register());
		regMap.put(R1, new Register());
		regMap.put(R2, new Register());
		regMap.put(R3, new Register());

		// 3 Index Registers
		regMap.put(X1, new Register());
		regMap.put(X2, new Register());
		regMap.put(X3, new Register());

		// Special registers
		regMap.put(PC, new Register(12));
		regMap.put(IR, new Register());
		regMap.put(CC, new Register(4));
		regMap.put(MAR, new Register());
		regMap.put(MDR, new Register());
		regMap.put(MSR, new Register());
		regMap.put(MFR, new Register(4));

		// Assuming EA should be as large as the MAR register
		regMap.put(EA, new Register());

		// ALU Registers
		regMap.put(OP1, new Register());
		regMap.put(OP2, new Register());
		regMap.put(OP3, new Register());
		regMap.put(OP4, new Register());
		regMap.put(RESULT, new Register());
		regMap.put(RESULT2, new Register());

		// Registers for Load and Store instructions
		regMap.put(OPCODE, new Register(InstructionBitFormats.OPCODE_SIZE));
		regMap.put(IX, new Register(InstructionBitFormats.LD_STR_IX_SIZE));
		regMap.put(R, new Register(InstructionBitFormats.LD_STR_R_SIZE));
		regMap.put(I, new Register(InstructionBitFormats.LD_STR_I_SIZE));
		regMap.put(ADDR, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));

		// Registers for register-register instructions
		regMap.put(RX, new Register(InstructionBitFormats.XY_ARITH_RX_SIZE));
		regMap.put(RY, new Register(InstructionBitFormats.XY_ARITH_RY_SIZE));

		// Registers for shift instructions
		regMap.put(AL, new Register(InstructionBitFormats.SHIFT_AL_SIZE));
		regMap.put(LR, new Register(InstructionBitFormats.SHIFT_LR_SIZE));
		regMap.put(COUNT, new Register(InstructionBitFormats.SHIFT_COUNT_SIZE));

		// Registers for IO instructions
		regMap.put(DEVID, new Register(InstructionBitFormats.IO_DEVID_SIZE));

		irdecoder = new IRDecoder(this);
		alu = new ALU(this);
	}

	/**
	 * Called from continue/start gui button
	 *
	 * @param cont
	 *            Branch logic for continous processing or macro/micro step
	 */

	/**
	 * Gets a register from the map.
	 * 
	 * @param regName
	 *            Key into the register map.
	 * @return The register associated with the key.
	 */
	public Register getReg(String regName) {
		return regMap.get(regName);
	}

	/**
	 * Reads the contents of a specified address from memory. The override
	 * parameter forces a skip of the cache check and reads directly from main
	 * memory. Used when a cache check would be redundant; otherwise, perform
	 * the default behavior of checking the cache for the desired address.
	 * Updates the cache with the block fetched from memory regardless of the
	 * override option.
	 * 
	 * @param address
	 *            The address in main memory to target.
	 * @param override
	 *            Skip the cache check if true.
	 * 
	 * @return the contents of the specified address.
	 */
	private Word readFromMemory(int address, boolean override) {
		Word word = null;
		if (override)
			word = readFromMainMemory(address);
		else {
			word = l1_cache.read(address);
			if (word != null)
				return word;
			else {
				word = readFromMainMemory(address);
			}
		}
		return word;
	}

	/**
	 * Reads the contents of a specified address from memory. Checks the cache
	 * for the desired address before searching main memory. Updates the cache
	 * with the block fetched from memory regardless of the override option.
	 * 
	 * @param address
	 *            The address in main memory to target.
	 * @return the contents of the specified address.
	 */
	public Word readFromMemory(int address) {
		return this.readFromMemory(address, false);
	}

	/**
	 * Sets a register with a BitSet value.
	 * 
	 * @param destName
	 *            Key into the register map.
	 * @param sourceSet
	 *            BitSet to set the register equal to.
	 * @param sourceBits
	 *            Number of bits in the BitSet.
	 */
	public void setReg(String destName, BitSet sourceSet, int sourceBits) {
		if (regMap.containsKey(destName)) {
			// regMap.put(regName, bitValue);

			Register destination = regMap.get(destName);
			Utils.bitsetDeepCopy(sourceSet, sourceBits, destination,
					destination.getNumBits());

			// update the GUI
			Computer_GUI.update_register(destName, getReg(destName));

		}
	}

	/**
	 * Set a register with the contents of another register (given it's key in
	 * the register map).
	 * 
	 * @param destName
	 *            Key into the register map (the destination register).
	 * @param source
	 */
	public void setReg(String destName, Register source) {
		if (regMap.containsKey(destName)) {
			Register destination = regMap.get(destName);
			Utils.bitsetDeepCopy(source, source.getNumBits(), destination,
					destination.getNumBits());

			// update the GUI
			Computer_GUI.update_register(destName, getReg(destName));
		} else
			System.out.println("Register map does not contain key " + destName);
	}

	/**
	 * Set a register with the contents of a word gotten from memory.
	 * 
	 * @param destName
	 * @param sourceMemory
	 */
	public void setReg(String destName, Word sourceMemory) {
		if (regMap.containsKey(destName)) {
			Register destination = regMap.get(destName);
			// System.out.println("Source: " + destName + " size: "
			// + destination.getNumBits());
			Utils.bitsetDeepCopy(sourceMemory, DEFAULT_BIT_SIZE, destination,
					destination.getNumBits());

			// update the GUI
			Computer_GUI.update_register(destName, getReg(destName));
		}
	}

	/**
	 * Points the PC to Octal 10 where the bootloader program is loaded and
	 * starts execution (by default, runs until HLT)
	 */
	public void startBootloader() {
		setReg(PC,
				Utils.intToBitSet(BOOTLOADER_START, getReg(PC).getNumBits()),
				getReg(PC).getNumBits());
		prog_step = 0;
		// Utils.bitsetToString(PC, getReg(PC), getReg(PC).getNumBits());
		// executeInstruction("continue");
	}

	/**
	 * Writes the value of the specified word to the cache at the specified
	 * address location. The memory controller handles the synchronization
	 * between the cache and main memory. This method handles two cases:
	 * <ol>
	 * <li>Cache Hit.</li>
	 * <li>Cache Miss.</li>
	 * </ol>
	 * For a cache hit, this method updates the value in the cache with the
	 * value of the word parameter, sets the appropriate dirty flag on the cache
	 * line, and adds the value of the word parameter to the write buffer. For a
	 * cache miss, this method first fetches the appropriate block from memory,
	 * loads it into the cache, evicting an existing cache line if necessary,
	 * and then executes the logic for a cache hit.
	 * 
	 * @param word
	 *            The contents to write.
	 * @param address
	 *            The address in main memory to target.
	 * 
	 * @return true if successful, false otherwise.
	 */
	public boolean writeToMemory(Word word, int address) {
		if (l1_cache.write(word, address)) {
			// Cache Hit.
			return true;
		} else {
			// Cache Miss.
			this.readFromMemory(address, false);
			return this.writeToMemory(word, address);
		}
	}

	/**
	 * Forces the memory controller thread out of its run loop so it can
	 * terminate gracefully.
	 */
	public void shutdown() {
		memory_controller.terminate();
		System.out.println("CPU Shutdown.");
	}

	private void advancePC() {
		if(jumpTaken){
			jumpTaken = false;
			return;
		}
		int pcContents = Utils
				.convertToInt(getReg(PC), getReg(PC).getNumBits());
		setReg(PC, Utils.intToBitSet(++pcContents, getReg(PC).getNumBits()),
				getReg(PC).getNumBits());
	}

	/**
	 * Calculates the EA (effective address). Boolean parameter is used for LDX
	 * or STX instructions where IX specifies the index register to load/store
	 * and NOT to be used when calculating the EA.
	 * 
	 * @param loadStoreIndex
	 *            Set to true if doing a LDX or STX instruction.
	 */
	private void calculateEA(boolean LDXSTXInstruction) {
		Register i = regMap.get(I);
		Register ix = regMap.get(IX);
		Register ea = regMap.get(EA);
		Register addr = regMap.get(ADDR);

		if (Utils.convertToUnsignedByte(i, i.getNumBits()) == 0) { // No
																	// indirect
			// addressing
			if (LDXSTXInstruction
					|| Utils.convertToUnsignedByte(ix, ix.getNumBits()) == 0) { // No
				// indexing
				setReg(EA, regMap.get(ADDR));
			} else { // Indexing, no indirect
				// ADDR + indexregisterfile(IX)
				int temp = Utils.convertToInt(getReg(indexRegisterFile(ix)),
						getReg(indexRegisterFile(ix)).getNumBits())
						+ Utils.convertToInt(addr, addr.getNumBits());

				// EA = ADDR + Xx
				setReg(EA, Utils.intToBitSet(temp, ea.getNumBits()),
						ea.getNumBits());
			}

		} else { // Indirect addressing
			if (LDXSTXInstruction
					|| Utils.convertToUnsignedByte(ix, ix.getNumBits()) == 0) { // No
				// indexing
				setReg(EA, regMap.get(ADDR));
			} else { // Indexing, no indirect
				// ADDR + indexregisterfile(IX)
				int temp = Utils.convertToInt(getReg(indexRegisterFile(ix)),
						getReg(indexRegisterFile(ix)).getNumBits())
						+ Utils.convertToInt(addr, addr.getNumBits());

				// EA = ADDR + Xx
				setReg(EA, Utils.intToBitSet(temp, ea.getNumBits()),
						ea.getNumBits());
			}

			// TODO implement the clock
			// Taking care of the indirect part
			// EA -> MAR
			setReg(MAR, getReg(EA));

			// Memory(MAR) -> MDR
			Register register = this.getReg(MAR);
			int address = Utils.convertToInt(register, register.getNumBits());
			Word word = this.readFromMemory(address);
			setReg(MDR, word);
			// MDR -> EA
			setReg(EA, getReg(MDR));
		}
	}

	/**
	 * Handles the interrupt sent to the CPU. For I/O interrupts, this means
	 * restarting the current instruction.
	 * 
	 * @param interruptCode
	 *            The type of interrupt generated.
	 */
	public void handleInterrupt(byte interruptCode) {
		switch (interruptCode) {
		case INTERRUPT_IO:
			if (!input_buffer.equals("")) {
				System.out.println("Restarting instruction");
				waitForInterrupt = false;
				executeInstruction(currentExecution);
			}
			break;
		}
	}

	public void executeInstruction(String step_type) {
		currentExecution = step_type;
		switch (step_type) {
		case "continue":
			Computer_GUI.toggle_button("load", false);
			System.out.println("Continue");
			while (cont_execution) {
				singleInstruction();
				if (waitForInterrupt)
					return;
				if (prog_step == 0) {
					System.out.println("--------- Instruction Done ---------");
					printAllRegisters();
					advancePC();
				}
			}
			cont_execution = true;
			break;

		case "micro step":
			Computer_GUI.toggle_button("load", false);
			// Computer_GUI.toggle_button("runinput", false);
			System.out.println("Micro Step");
			singleInstruction();
			if (waitForInterrupt)
				return;

			if (prog_step == 0) {
				System.out.println("--------- Instruction Done ---------");
				printAllRegisters();
				advancePC();
				Computer_GUI.toggle_button("runinput", true);
			}
			break;

		case "macro step":
			Computer_GUI.toggle_button("load", false);
			System.out.println("Macro Step");
			do {
				singleInstruction();
				if (waitForInterrupt)
					return;

			} while (prog_step != 0);

			System.out.println("--------- Instruction Done ---------");
			printAllRegisters();
			advancePC();
			Computer_GUI.toggle_button("runinput", true);
			break;

		// Direct Execution - Does not advance PC
		default:
			System.out.println("Running user input");
			try {
				System.out.println(step_type);
				Word word_command = Utils.StringToWord(step_type);
				Utils.bitsetToString("input", word_command, 18);
				setReg(MDR, word_command);
				cycle_count++;
				prog_step = prog_step + 2;
				do {
					singleInstruction();
					if (waitForInterrupt) {
						cycle_count -= 2;
						prog_step = 0;
						return;
					}
				} while (prog_step != 0);

				System.out.println("--------- Instruction Done ---------");
				printAllRegisters();
				// Does not advance PC
				// setReg(IR, BitSet(from step_type));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a String key into the register map according to the contents of
	 * IX (the index register index)
	 * 
	 * @param R
	 *            The R register.
	 * @return A String key into the register map.
	 */
	private String indexRegisterFile(BitSet IX) {
		switch (Utils.convertToUnsignedByte(IX,
				InstructionBitFormats.LD_STR_IX_SIZE)) {
		case 1:
			return X1;
		case 2:
			return X2;
		case 3:
			return X3;
		}

		return null;
	}

	/**
	 * Branch Logic for individual opcodes - at end of any opcode logic it
	 * should reset prog_step counter - This will make singleInstruction
	 * restart, thus reaching the next PC - Currently Opcodes not properly setup
	 * 
	 * @param op_byte
	 *            Opcode to do case branching
	 */
	private void opcodeInstruction(byte op_byte) {
		// memory = Memory.getInstance();
		switch (op_byte) {

		case OpCodesList.LDR:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;

			case 5:
				// EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;

			case 6:
				// Mem(MAR) -> MDR
				int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR)
						.getNumBits());
				setReg(MDR, this.readFromMemory(mar_addr));
				cycle_count++;
				prog_step++;
				break;

			case 7:
				// MDR -> registerFile(R)
				setReg(registerFile(getReg(R)), regMap.get(MDR));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.STR:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;

			case 5:
				// EA -> MAR
				setReg(MAR, regMap.get(EA));

				// registerFile(R) -> MDR
				setReg(MDR, getReg(registerFile(getReg(R))));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// MDR -> Mem(MAR)
				Word word = Utils.registerToWord(getReg(MDR), getReg(MDR)
						.getNumBits());
				Register register = this.getReg(MAR);
				int address = Utils.convertToInt(register,
						register.getNumBits());
				this.writeToMemory(word, address);
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.LDA:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;

			case 5:
				// EA -> regFile(R)
				setReg(registerFile(getReg(R)), getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.LDX:
			switch (prog_step) {
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;

			case 5:
				// EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Mem(MAR) -> MDR
				int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR)
						.getNumBits());
				setReg(MDR, this.readFromMemory(mar_addr));
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// MDR -> indexRegFile(R)
				setReg(indexRegisterFile(getReg(IX)), getReg(MDR));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.STX:
			switch (prog_step) {
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;

			case 5:
				// EA -> MAR
				setReg(MAR, regMap.get(EA));

				// indexRegFile(R) -> MDR
				setReg(MDR, getReg(indexRegisterFile(getReg(IX))));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// MDR -> Mem(MAR)
				Word word = Utils.registerToWord(getReg(MDR), getReg(MDR)
						.getNumBits());
				Register register = this.getReg(MAR);
				int address = Utils.convertToInt(register,
						register.getNumBits());
				this.writeToMemory(word, address);
				cycle_count++;
				prog_step = 0;
				break;

			}
			break;

		case OpCodesList.JZ:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));
				getReg(OP2).clear();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform equal to zero comparison in ALU
				alu.TRR();
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 1
				// EA -> PC
				if (!getReg(CC).get(EQUALORNOT)){
					setReg(PC, getReg(EA));
					jumpTaken = true;
				}
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JNE:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));
				getReg(OP2).clear();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform not equal to zero comparison in ALU
				alu.TRR();
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 0
				// EA -> PC
				if (!getReg(CC).get(EQUALORNOT)){
					setReg(PC, getReg(EA));
					jumpTaken = true;
				}
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JCC:
			// If RESULT == 1
			// EA -> PC
			if (getReg(CC).get(
					Utils.convertToUnsignedByte(getReg(R), getReg(R)
							.getNumBits()))){
				setReg(EA, getReg(PC), getReg(PC).getNumBits());
				jumpTaken = true;
			}

			cycle_count++;
			prog_step = 0;
			break;

		case OpCodesList.JMP:
			System.out.println("JUMP");
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// EA -> PC
				setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				jumpTaken = true;
				break;
			}
			break;

		case OpCodesList.JSR:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				int temp = Utils.convertToInt(getReg(PC), getReg(PC)
						.getNumBits()) + 1;
				// PC+1 -> R3
				setReg(R3, Utils.intToBitSet(temp, getReg(R3).getNumBits()),
						getReg(R3).getNumBits());
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// EA -> PC
				setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				jumpTaken = true;
				break;
			}
			break;

		case OpCodesList.RFS:
			switch (prog_step) {
			case 4:
				// ADDR -> R0
				setReg(R0, getReg(ADDR));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// R3 -> PC
				setReg(PC, getReg(R3));
				jumpTaken = true;
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SOB:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform subtract one in ALU
				// alu.SOB();
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// Clearing OP2 in preparation for GTE comparison
				getReg(OP2).clear();
				// Perform greater than 0 comparison in ALU
				alu.GTE();
				cycle_count++;
				prog_step++;
				break;
			case 8:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1){
					setReg(PC, getReg(EA));
					jumpTaken = true;
				}
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JGE:
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));
				getReg(OP2).clear();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform greater than/equal comparison in ALU
				alu.GTE();
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1){
					setReg(PC, getReg(EA));
					jumpTaken = true;
				}
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.AMR:
			System.out.println("Prog step:" + prog_step);
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Need to fetch the data from memory, EA -> MAR
				setReg(MAR, getReg(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Memory(MAR) -> MDR
				Register register = this.getReg(MAR);
				int address = Utils.convertToInt(register,
						register.getNumBits());
				Word word = this.readFromMemory(address);
				setReg(MDR, word);
				cycle_count++;
				prog_step++;
				break;

			case 7:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// MDR -> OP2
				setReg(OP2, getReg(MDR));
				cycle_count++;
				prog_step++;
				break;
			case 8:
				// Perform add in ALU
				System.out.println("Performing add");
				alu.AMR();
				cycle_count++;
				prog_step++;
				break;
			case 9:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SMR:
			System.out.println("SMR");
			switch (prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Need to fetch the data from memory, EA -> MAR
				setReg(MAR, getReg(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Memory(MAR) -> MDR
				Register register = this.getReg(MAR);
				int address = Utils.convertToInt(register,
						register.getNumBits());
				Word word = this.readFromMemory(address);
				setReg(MDR, word);
				cycle_count++;
				prog_step++;
				break;

			case 7:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// MDR -> OP2
				setReg(OP2, getReg(MDR));
				cycle_count++;
				prog_step++;
				break;
			case 8:
				// Perform subtract in ALU
				alu.SMR();
				cycle_count++;
				prog_step++;
				break;
			case 9:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.AIR:
			switch (prog_step) {
			case 4:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// ADDR -> OP2 (ADDR contains immediate data)
				setReg(OP2, getReg(ADDR));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform add in ALU
				alu.AIR();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SIR:
			switch (prog_step) {
			case 4:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// ADDR -> OP2 (ADDR contains immediate data)
				setReg(OP2, getReg(ADDR));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform subtract in ALU
				alu.SIR();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.MLT:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));

				// registerFile(RY) -> OP2
				setReg(OP2, getReg(registerFile(getReg(RY))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform multiply in ALU
				alu.MLT();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RX will contain the high order word
				setReg(registerFile(getReg(RX)), getReg(RESULT));

				// RX+1 will contain the low order word
				// RX can only be 0 or 2
				if (registerFile(getReg(RX)).equals(R0))
					setReg(R1, getReg(RESULT2));
				else
					setReg(R3, getReg(RESULT2));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.DVD:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));

				// registerFile(RY) -> OP2
				setReg(OP2, getReg(registerFile(getReg(RY))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform divide in ALU
				alu.DVD();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RX will contain the quotient
				setReg(registerFile(getReg(RX)), getReg(RESULT));

				// RX+1 will contain the remainder
				// RX can only be 0 or 2
				if (registerFile(getReg(RX)).equals(R0))
					setReg(R1, getReg(RESULT2));
				else
					setReg(R3, getReg(RESULT2));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.TRR:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));

				// registerFile(RY) -> OP2
				setReg(OP2, getReg(registerFile(getReg(RY))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform equality test in ALU (also sets the condition code)
				alu.TRR();
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.AND:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));

				// registerFile(RY) -> OP2
				setReg(OP2, getReg(registerFile(getReg(RY))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform AND in ALU
				alu.AND();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(RX)
				setReg(registerFile(getReg(RX)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.ORR:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));

				// registerFile(RY) -> OP2
				setReg(OP2, getReg(registerFile(getReg(RY))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform OR in ALU
				alu.ORR();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(RX)
				setReg(registerFile(getReg(RX)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.NOT:
			switch (prog_step) {
			case 4:
				// registerFile(RX) -> OP1
				setReg(OP1, getReg(registerFile(getReg(RX))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform NOT in ALU
				alu.NOT();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(RX)
				setReg(registerFile(getReg(RX)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SRC:
			switch (prog_step) {
			case 4:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// COUNT -> OP2
				setReg(OP2, getReg(COUNT));

				// LR -> OP3
				setReg(OP3, getReg(LR));

				// AL -> OP4
				setReg(OP4, getReg(AL));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform shift in ALU
				alu.SRC();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.RRC:
			switch (prog_step) {
			case 4:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// COUNT -> OP2
				setReg(OP2, getReg(COUNT));

				// LR -> OP3
				setReg(OP3, getReg(LR));

				// AL -> OP4
				setReg(OP4, getReg(AL));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform rotate in ALU
				alu.RRC();
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		// Needs logic for different devices??
		case OpCodesList.IN:
			System.out.println("RUNNING IN");
			if (input_buffer.equals("")) {
				System.out.println("Waiting for interrupt...");
				waitForInterrupt = true;
				return;
			}

			try {
				int input = Integer.parseInt(input_buffer);
				BitSet input_bitset = Utils.intToBitSet(input, 18);
				setReg(registerFile(getReg(R)), input_bitset, 18);
				Computer_GUI.append_to_terminal(input_buffer);
			} catch (NumberFormatException e) {
				// Does not handle string input!!
				// Word input_word = (Word) Utils.StringToWord(input_buffer);
				// setReg(registerFile(getReg(R)), input_word);
			}
			input_buffer = "";
			cycle_count++;
			prog_step = 0;
			break;

		case OpCodesList.OUT:
			if (Utils.convertToInt(getReg(DEVID), getReg(DEVID).getNumBits()) == 1) {
				int output = Utils.convertToInt(
						getReg(registerFile(getReg(R))), 18);
				
				if(Utils.convertToUnsignedByte(this.readFromMemory(120), 18) == (byte)1)
					Computer_GUI.append_to_terminal("" + Integer.toString(output));
				else
					Computer_GUI.append_to_terminal("" + (char) (output));
				System.out.println("Memory addr 120 = " + Utils.convertToUnsignedByte(this.readFromMemory(120),18));
				System.out.println("OUT: " + output);
			}
			cycle_count++;
			prog_step = 0;
			break;

		case OpCodesList.HLT:
			System.out.println("End of the program");
			cont_execution = false;
			prog_step = 0;
			Computer_GUI.disable_btns();
			Computer_GUI.toggle_button("load", true);
			break;
		}
	}

	/**
	 * Prints the contents of all the registers to the console. (Eventually will
	 * become obsolete when GUI is done)
	 */
	private void printAllRegisters() {
		Utils.bitsetToString(R0, getReg(R0), getReg(R0).getNumBits());
		Utils.bitsetToString(R1, getReg(R1), getReg(R1).getNumBits());
		Utils.bitsetToString(R2, getReg(R2), getReg(R2).getNumBits());
		Utils.bitsetToString(R3, getReg(R3), getReg(R3).getNumBits());
		Utils.bitsetToString(X1, getReg(X1), getReg(X1).getNumBits());
		Utils.bitsetToString(X2, getReg(X2), getReg(X2).getNumBits());
		Utils.bitsetToString(X3, getReg(X3), getReg(X3).getNumBits());
		Utils.bitsetToString(PC, getReg(PC), getReg(PC).getNumBits());
		Utils.bitsetToString(IR, getReg(IR), getReg(IR).getNumBits());
		Utils.bitsetToString(MAR, getReg(MAR), getReg(MAR).getNumBits());
		Utils.bitsetToString(MDR, getReg(MDR), getReg(MDR).getNumBits());
		Utils.bitsetToString(MSR, getReg(MSR), getReg(MSR).getNumBits());
		Utils.bitsetToString(MFR, getReg(MFR), getReg(MFR).getNumBits());
		Utils.bitsetToString(OPCODE, getReg(OPCODE), getReg(OPCODE)
				.getNumBits());
		Utils.bitsetToString(IX, getReg(IX), getReg(IX).getNumBits());
		Utils.bitsetToString(R, getReg(R), getReg(R).getNumBits());
		Utils.bitsetToString(I, getReg(I), getReg(I).getNumBits());
		Utils.bitsetToString(ADDR, getReg(ADDR), getReg(ADDR).getNumBits());
		Utils.bitsetToString(EA, getReg(EA), getReg(EA).getNumBits());
		Utils.bitsetToString(RX, getReg(RX), getReg(RX).getNumBits());
		Utils.bitsetToString(RY, getReg(RY), getReg(RY).getNumBits());
		Utils.bitsetToString(COUNT, getReg(COUNT), getReg(COUNT).getNumBits());
		Utils.bitsetToString(LR, getReg(LR), getReg(LR).getNumBits());
		Utils.bitsetToString(AL, getReg(AL), getReg(AL).getNumBits());
		Utils.bitsetToString(OP1, getReg(OP1), getReg(OP1).getNumBits());
		Utils.bitsetToString(OP2, getReg(OP2), getReg(OP2).getNumBits());
		Utils.bitsetToString(OP3, getReg(OP3), getReg(OP3).getNumBits());
		Utils.bitsetToString(OP4, getReg(OP4), getReg(OP4).getNumBits());
		Utils.bitsetToString(RESULT, getReg(RESULT), getReg(RESULT)
				.getNumBits());
		Utils.bitsetToString(DEVID, getReg(DEVID), getReg(DEVID).getNumBits());
		Utils.bitsetToString(CC, getReg(CC), getReg(CC).getNumBits());
	}

	/**
	 * Omits a check to the cache and fetches contents directly from memory.
	 * Used when a cache check would be redundant. Updates the cache with the
	 * block fetched from memory via a call to
	 * {@link L1Cache#add(L1CacheLine line)} .
	 * 
	 * @param address
	 *            The address of the contents to fetch from mmain memory.
	 * @return The contents of the specified address in main memory.
	 */
	private Word readFromMainMemory(int address) {
		Word[] block = Memory.getInstance().getMemoryBlock(address);
		Word word = block[0];
		L1CacheLine line = new L1CacheLine(address, block, (byte) 0);
		l1_cache.add(line);
		return word;
	}

	/**
	 * Returns a String key into the register map according to the contents of R
	 * (the register index register)
	 * 
	 * @param R
	 *            The R register.
	 * @return A String key into the register map.
	 */
	private String registerFile(BitSet R) {
		switch (Utils.convertToUnsignedByte(R,
				InstructionBitFormats.LD_STR_R_SIZE)) {
		case 0:
			return R0;
		case 1:
			return R1;
		case 2:
			return R2;
		case 3:
			return R3;
		}
		return null;
	}

	/**
	 * Run a single instruction - enables micro steps - reliant upon the
	 * prog_step counter tracking step progress
	 */
	private void singleInstruction() {
		switch (prog_step) {
		case 0:
			setReg(MAR, regMap.get(PC));
			cycle_count++;
			prog_step++;
			break;

		case 1:
			int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR)
					.getNumBits());
			setReg(MDR, this.readFromMemory(mar_addr));
			cycle_count++;
			prog_step++;
			break;

		case 2:
			setReg(IR, regMap.get(MDR));
			cycle_count++;
			prog_step++;
			break;

		case 3:
			irdecoder.parseIR(regMap.get(IR));
			cycle_count++;
			prog_step++;
			break;

		default:
			opcodeInstruction(Utils.convertToUnsignedByte(getReg(OPCODE),
					InstructionBitFormats.OPCODE_SIZE));
		}
	}

	public void loadROM() {
		try {
			/* romLoader.load(); */
			startBootloader();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*
		 * catch (ParseException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	/**
	 * @return the l1Cache
	 */
	static L1Cache getL1Cache() {
		return l1_cache;
	}
}
