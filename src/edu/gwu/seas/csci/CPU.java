package edu.gwu.seas.csci;

import java.text.ParseException;
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
 * This CPU implements a simple unified (instructions and data)
 * write-through memory cache.
 */
/**
 * @author Alex Remily
 */
public class CPU implements CPUConstants {

	/**
	 * @author Alex Remily
	 */
	private class MemoryController implements Runnable {

		Memory memory = Memory.getInstance();

		@Override
		public void run() {
			synchronized (memory) {
				try {
					// Wait for notification from Memory that an event needs to
					// be handled.
					memory.wait();
					System.out.println("Notify has been called.");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * We have an address space of 2KB. We have 16 cache lines. Each cache line
	 * contains 4 Words (18 bits each).
	 * 
	 * @author Alex Remily
	 */

	private static class L1Cache {

		/**
		 * Contains the contents of the L1 cache. We have 2<sup>4</sup> cache
		 * lines.
		 */
		private static final L1CacheLine[] cache = new L1CacheLine[16];

		/**
		 * 
		 */
		private static final Random generator = new Random();

		/**
		 * 
		 */
		private static byte cache_adds_counter = 0;

		/**
		 * TODO: Test Me.
		 * 
		 * @param address
		 * @return
		 */
		public static Word read(int address) {
			for (L1CacheLine line : cache) {
				if (line.getTag() >= address && address < line.getTag() + 6)
					return line.getWord(address - line.getTag());
			}
			return null;
		}

		/**
		 * @param word
		 * @param address
		 */
		public static void write(Word word, byte address) {

		}

		/**
		 * Adds a cache line to the cache. If the cache is full, it evicts a
		 * random cache line to make room for the new cache line.
		 * 
		 * @param line
		 *            The line to add to the cache.
		 */
		public static void add(L1CacheLine line) {
			if (cache_adds_counter < 16)
				cache[cache_adds_counter++] = line;
			else
				cache[generator.nextInt(cache.length)] = line;
		}
	}

	/**
	 * Need to research the structure of cache lines, and develop a mapping
	 * system to map main memory addresses to locations in the cache. We have
	 * not been directed to use a specific cache line structure, so for our case
	 * each line is 8 bytes: 1 address tag (18 bits), 6 Words (108 bits), 2 flag
	 * bits. This puts our total cache contents at 16 x 6 = 96 Words.
	 * 
	 * @author Alex Remily
	 */
	private static class L1CacheLine {

		public static final byte DIRTY = 0x1;

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
		public L1CacheLine(int address, Word[] block, byte flags) {
			this.tag = address;
			this.words = block;
			this.flags = flags;
		}

		/**
		 * @return the flags
		 */
		public byte getFlags() {
			return flags;
		}

		/**
		 * @return the tag
		 */
		public int getTag() {
			return tag;
		}

		/**
		 * @return the words
		 */
		public Word getWord(int index) {
			return words[index];
		}

		/**
		 * @param flags
		 *            the flags to set
		 */
		public void setFlags(byte flags) {
			this.flags = flags;
		}

		/**
		 * @param tag
		 *            the tag to set
		 */
		public void setTag(int tag) {
			this.tag = tag;
		}

		/**
		 * @param words
		 *            the words to set
		 */
		public void setWords(Word[] words) {
			this.words = words;
		}
	}

	/**
	 * Used to hold data being written back from the cache to main memory. This
	 * is a variation of write-through caching called buffered write-through.
	 * 
	 * @author Alex Remily
	 */
	private static class WriteBuffer {

		static class WriteBufferContents {
			private int address;
			private Word word;

			/**
			 * @return the address
			 */
			public int getAddress() {
				return address;
			}

			/**
			 * @param address
			 *            the address to set
			 */
			public void setAddress(int address) {
				this.address = address;
			}

			/**
			 * @return the word
			 */
			public Word getWord() {
				return word;
			}

			/**
			 * @param word
			 *            the word to set
			 */
			public void setWord(Word word) {
				this.word = word;
			}
		}

		/**
		 * FIFO queue of size 4 serves as a write buffer. Holds the dirty
		 * L1CacheLines as elements in the queue. L1CacheLines flag bits are
		 * ignored, and the L1CacheLine word is written to the main memory
		 * address specified by the L1CacheLine tag value.
		 */
		private static final Queue<WriteBufferContents> buffer = new ArrayBlockingQueue<WriteBufferContents>(
				4);

		/**
		 * Adds a dirty cache value to the write buffer for synchronization with
		 * main memory. If the buffer is full, the add is blocked and the CPU
		 * sill stall until there is room in the buffer.
		 * 
		 * @param line
		 *            An L1CacheLine element with the dirty bit set, indicating
		 *            that it has been written to in the cache and must be
		 *            synchronized with main memory.
		 */
		public boolean addToBuffer(WriteBufferContents contents) {
			boolean success = false;
			try {
				success = buffer.add(contents);
			} catch (IllegalStateException e) {
				// TODO Handle the Exception. Halt the CPU thread until the
				// memory_controller_thread makes room for more elements.
			}
			return success;
		}

		/**
		 * Returns true if this WriteBuffer contains no elements.
		 * 
		 * @return true if this WriteBuffer contains no elements.
		 */
		public boolean isEmpty() {
			return buffer.isEmpty();
		}

		/**
		 * Retrieves and writes the head of this FIFO queue to main memory.
		 * 
		 * @param memory
		 *            The main memory to write the head of the FIFO queue.
		 * @return true if successful, otherwise false;
		 */
		public boolean writeToMemory(Memory memory) {
			boolean success = true;
			try {
				WriteBufferContents line = buffer.remove();
				Word word = line.getWord();
				int address = line.getAddress();
				memory.write(word, address);
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

	// TODO: Refactor this class variable out and replace with method args.
	private Memory memory;

	private IRDecoder irdecoder;

	private Loader romLoader;

	/**
	 * The memory write buffer with a fast FIFO algorithm.
	 */
	private final WriteBuffer buffer = new WriteBuffer();

	/**
	 * Maintains memory state by managing the reads and writes amongst the
	 * components of memory.
	 */
	private final MemoryController memory_controller = new MemoryController();

	/**
	 * Runs the memory_controller asynchronously in a separate dedicated thread.
	 */
	private final Thread memory_controller_thread = new Thread(
			memory_controller, "memory_controller");

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
		regMap.put(LR, new Register(InstructionBitFormats.SHIFT_LR_START));
		regMap.put(COUNT, new Register(InstructionBitFormats.SHIFT_COUNT_SIZE));

		irdecoder = new IRDecoder(this);
		romLoader = new FileLoader();
	}

	/**
	 * @param memory
	 *            The main memory to target.
	 * @param address
	 *            The address in main memory to target.
	 * @param word
	 *            The contents to write.
	 * @return true if successful, false otherwise.
	 */
	public boolean writeToMemory(Memory memory, byte address, Word word) {
		return true;
	}

	/**
	 * @param memory
	 *            The main memory to target.
	 * @param address
	 *            The address in main memory to target.
	 * @return the contents of the specified address in the specified memory.
	 */
	public Word readFromMemory(Memory memory, int address) {
		Word word = L1Cache.read(address);
		if (word != null)
			return word;
		else {
			Word[] block = memory.read(address);
			word = block[0];
			L1CacheLine line = new L1CacheLine(address, block, (byte) 0);
			L1Cache.add(line);
		}
		return word;
	}

	/**
	 * Called from continue/start gui button
	 *
	 * @param cont
	 *            Branch logic for continous processing or macro/micro step
	 */
	public void executeInstruction(String step_type) {
		switch (step_type) {
		case "continue":
			System.out.println("Continue");
			while (cont_execution) {
				singleInstruction();

				if (prog_step == 0) {
					System.out.println("--------- Instruction Done ---------");
					printAllRegisters();
					advancePC();
				}
			}
			cont_execution = true;
			break;

		case "micro step":
			System.out.println("Micro Step");
			singleInstruction();

			if (prog_step == 0) {
				System.out.println("--------- Instruction Done ---------");
				printAllRegisters();
				advancePC();
			}
			break;

		case "macro step":
			System.out.println("Macro Step");
			do {
				singleInstruction();
			} while (prog_step != 0);

			System.out.println("--------- Instruction Done ---------");
			printAllRegisters();
			advancePC();
			break;

		default:
			System.out.println("Direct Exectuion");
			// setReg(IR, BitSet(from step_type));
		}
	}

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

	public void loadROM() {
		try {
			romLoader.load();
			startBootloader();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private void advancePC() {
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
		Word[] words = null;

		if (Utils.convertToByte(i, i.getNumBits()) == 0) { // No indirect
															// addressing
			if (LDXSTXInstruction
					|| Utils.convertToByte(ix, ix.getNumBits()) == 0) { // No
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
					|| Utils.convertToByte(ix, ix.getNumBits()) == 0) { // No
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
			words = memory.read(getReg(MAR), getReg(MAR).getNumBits());
			setReg(MDR, words[0]);

			// MDR -> EA
			setReg(EA, getReg(MDR));
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
		switch (Utils.convertToByte(IX, InstructionBitFormats.LD_STR_IX_SIZE)) {
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

		Word[] words = null;

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
				words = memory.read(mar_addr);
				setReg(MDR, words[0]);
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
				memory.write(Utils.registerToWord(getReg(MDR), getReg(MDR)
						.getNumBits()), getReg(MAR), getReg(MAR).getNumBits());
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
				words = memory.read(mar_addr);
				setReg(MDR, words[0]);
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
				memory.write(Utils.registerToWord(getReg(MDR), getReg(MDR)
						.getNumBits()), getReg(MAR), regMap.get(MAR)
						.getNumBits());
				cycle_count++;
				prog_step = 0;
				break;

			}
			break;

		case OpCodesList.JZ:
			switch (prog_step) {
			case 4:
				calculateEA(true);
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
				// Perform equal to zero comparison in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1)
					setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JNE:
			switch (prog_step) {
			case 4:
				calculateEA(true);
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
				// Perform not equal to zero comparison in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1)
					setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JCC:
			// Multiple scenarios possible based on ALU implementation
			// Could move CC to OP1 and the mask to OP2, then perform AND
			// Could just supply a mask and use a test CC method in ALU
			// etc..
			// If RESULT == 1
			// EA -> PC
			cycle_count++;
			prog_step = 0;
			break;

		case OpCodesList.JMP:
			switch (prog_step) {
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// EA -> PC
				setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JSR:
			switch (prog_step) {
			case 4:
				calculateEA(true);
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
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SOB:
			switch (prog_step) {
			case 4:
				calculateEA(true);
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
				// Perform subtract one (or need another move -1 to OP2) in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// Perform greater than 0 comparison in ALU
				cycle_count++;
				prog_step++;
				break;
			case 8:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1)
					setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.JGE:
			switch (prog_step) {
			case 4:
				calculateEA(true);
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
				// Perform greater than/equal comparison in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// If RESULT == 1
				// EA -> PC
				if (Utils.convertToInt(getReg(RESULT), getReg(RESULT)
						.getNumBits()) == 1)
					setReg(PC, getReg(EA));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.AMR:
			switch (prog_step) {
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// EA -> OP2 (if indirection, EA will also already be holding
				// the data)
				setReg(OP2, getReg(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform add in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
				// RESULT -> registerFile(R)
				setReg(registerFile(getReg(R)), getReg(RESULT));
				cycle_count++;
				prog_step = 0;
				break;
			}
			break;

		case OpCodesList.SMR:
			switch (prog_step) {
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// registerFile(R) -> OP1
				setReg(OP1, getReg(registerFile(getReg(R))));

				// EA -> OP2 (if indirection, EA will also already be holding
				// the data)
				setReg(OP2, getReg(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				// Perform subtract in ALU
				cycle_count++;
				prog_step++;
				break;
			case 7:
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
				setReg(OP2, getReg(registerFile(getReg(COUNT))));

				// AL -> OP3
				setReg(OP3, getReg(registerFile(getReg(AL))));

				// LR -> OP4
				setReg(OP4, getReg(registerFile(getReg(LR))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform shift in ALU
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
				setReg(OP2, getReg(registerFile(getReg(COUNT))));

				// AL -> OP3
				setReg(OP3, getReg(registerFile(getReg(AL))));

				// LR -> OP4
				setReg(OP4, getReg(registerFile(getReg(LR))));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				// Perform rotate in ALU
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

		case OpCodesList.HLT:
			System.out.println("End of the program");
			cont_execution = false;
			prog_step = 0;
			Computer_GUI.disable_btns();
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
		Utils.bitsetToString(CC, getReg(CC), getReg(CC).getNumBits());
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
		switch (Utils.convertToByte(R, InstructionBitFormats.LD_STR_R_SIZE)) {
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
		Word[] words = null;
		switch (prog_step) {
		case 0:
			setReg(MAR, regMap.get(PC));
			cycle_count++;
			prog_step++;
			break;

		case 1:
			int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR)
					.getNumBits());
			words = memory.read(mar_addr);
			setReg(MDR, words[0]);
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
			opcodeInstruction(Utils.convertToByte(getReg(OPCODE),
					InstructionBitFormats.OPCODE_SIZE));
		}
	}
}
