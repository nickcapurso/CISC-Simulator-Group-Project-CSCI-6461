package edu.gwu.seas.csci;

import java.text.ParseException;
import java.util.*;

import edu.gwu.seas.csci.Utils;

/**
 * The CPU class is modelled after the Von Neumann architecture, where the 
 * CPU contains various types of registers and controls the logic between them. 
 * The registers include four general purpose registers, three index registers, 
 * memory-access registers, and various special-purpose registers (for example, a 
 * register to hold the opcode of an instruction). In addition, the CPU class executes 
 * a program's instructions from the micro-operation level and simulates a clock for the 
 * micro-operations to adhere to.  Each of the registers is represented by a Register 
 * object (an extension of the BitSet object) of the appropriate size. Each register is
 * placed into a HashMap, where the key is the register's name, for simplified access.  
 * Finally, the CPU keeps references to the Memory, IRDecoder, and Loader classes to 
 * respectively access memory, parse instructions, and load the boot loader program.
 * 
 */
public class CPU {
	// Public constants for register names
	public static final String R0 = "R0";
	public static final String R1 = "R1";
	public static final String R2 = "R2";
	public static final String R3 = "R3";
	public static final String X1 = "X1";
	public static final String X2 = "X2";
	public static final String X3 = "X3";
	public static final String PC = "PC";
	public static final String IR = "IR";
	public static final String CC = "CC";
	public static final String MAR = "MAR";
	public static final String MDR = "MDR";
	public static final String MSR = "MSR";
	public static final String MFR = "MFR";
	public static final String OPCODE = "OPCODE";
	public static final String I = "I";
	public static final String R = "R";
	public static final String IX = "IX";
	public static final String ADDR = "ADDR";
	public static final String EA = "EA";

	public static final byte BOOTLOADER_START = 010;

	public static Boolean cont_execution = true;
	public static int prog_step = 0;
	public static int cycle_count = 0;

	private Map<String, Register> regMap = new HashMap<String, Register>();
	private Memory memory;
	private IRDecoder irdecoder;
	private Loader romLoader;

	// Constructor
	public CPU() {

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
		// set PC to start of Program Counter
		regMap.put(IR, new Register());
		regMap.put(CC, new Register(4));
		regMap.put(MAR, new Register());
		regMap.put(MDR, new Register());
		regMap.put(MSR, new Register());
		regMap.put(MFR, new Register(4));

		// Registers for Load and Store instructions
		regMap.put(OPCODE, new Register(InstructionBitFormats.OPCODE_SIZE));
		regMap.put(IX, new Register(InstructionBitFormats.LD_STR_IX_SIZE));
		regMap.put(R, new Register(InstructionBitFormats.LD_STR_R_SIZE));
		regMap.put(I, new Register(InstructionBitFormats.LD_STR_I_SIZE));
		regMap.put(ADDR, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));

		// Assuming EA should be as large as the MAR register?
		regMap.put(EA, new Register());

		memory = Memory.getInstance();
		irdecoder = new IRDecoder(this);
		romLoader = new FileLoader();

		
		// Example of manually setting up memory and running LDR instruction

		//$54 = 100 (1100100)
		Word location54 = new Word();
		location54.set(15, true);
		location54.set(12, true);
		location54.set(11, true);	
		
		memory.put(location54, 54);
		
		//$100 = 5 (101)
		Word location100 = new Word();
		location100.set(15, true);
		location100.set(17, true);
		
		memory.put(location100, 100);
		
		//$105 = 16 (10000)
		Word location105 = new Word();
		location105.set(13, true);
		
		memory.put(location105, 105);
		
		//$200 = 54 (110110)
		Word location200 = new Word();
		location200.set(12, true);
		location200.set(13, true);
		location200.set(15, true);
		location200.set(16, true);
		
		memory.put(location200, 200);
		
		//$20 = 54 (110110)
		Word location20 = new Word();
		location20.set(12, true);
		location20.set(13, true);
		location20.set(15, true);
		location20.set(16, true);
		
		memory.put(location20, 20);

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
			Computer_GUI.update_register(destName, sourceSet);

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
			Computer_GUI.update_register(destName, source);
		}else
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
			//System.out.println("Source: " + destName + " size: "
				//	+ destination.getNumBits());
			Utils.bitsetDeepCopy(sourceMemory, 18, destination,
					destination.getNumBits());

			// update the GUI
			Computer_GUI.update_register(destName, (BitSet) sourceMemory);
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
	
	/**
	 * Points the PC to Octal 10 where the bootloader program is loaded
	 * and starts execution (by default, runs until HLT)
	 */
	public void startBootloader(){
		setReg(PC, 
				Utils.intToBitSet(BOOTLOADER_START, getReg(PC).getNumBits()), 
				getReg(PC).getNumBits());
		prog_step = 0;
		//Utils.bitsetToString(PC, getReg(PC), getReg(PC).getNumBits());
		//executeInstruction("continue");
	}

	/**
	 * Called from continue/start gui button
	 *
	 * @param cont
	 *            Branch logic for continous processing or macro/micro step
	 */
	public void executeInstruction(String step_type){
		switch (step_type){
			case "continue":
				System.out.println("Continue");
				while (cont_execution) {
					singleInstruction();
					
					if(prog_step == 0){
						System.out.println("--------- Instruction Done ---------");
						printAllRegisters();
						advancePC();
					}
				}
				break;
				
			case "micro step":
				System.out.println("Micro Step");
				singleInstruction();
				
				if(prog_step == 0){
					System.out.println("--------- Instruction Done ---------");
					printAllRegisters();
					advancePC();
				}
				break;
				
			case "macro step":
				System.out.println("Macro Step");
				do {
					singleInstruction();
				} while(prog_step != 0);
				
				System.out.println("--------- Instruction Done ---------");
				printAllRegisters();
				advancePC();
				break;
				
			default:
				System.out.println("Direct Exectuion");
				//setReg(IR, BitSet(from step_type));
		}
	}

	/**
	 * Run a single instruction
	 *  - enables micro steps
	 *  - reliant upon the prog_step counter tracking step progress
	 */
	private void singleInstruction() {
		switch (prog_step) {
		case 0:
			setReg(MAR, regMap.get(PC));
			cycle_count++;
			prog_step++;
			break;

		case 1:
			int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR).getNumBits());
			setReg(MDR, memory.get(mar_addr));
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
			opcodeInstruction(Utils.convertToByte(getReg(OPCODE), InstructionBitFormats.OPCODE_SIZE));
		}
	}


	/**
	 * Branch Logic for individual opcodes
	 *  - at end of any opcode logic it should reset prog_step counter
	 *    - This will make singleInstruction restart, thus reaching the next PC
	 *  - Currently Opcodes not properly setup
	 * 
	 * @param op_byte
	 * 		Opcode to do case branching
	 */
	private void opcodeInstruction(byte op_byte) {

		switch(op_byte){

		case OpCodesList.LDR:
			switch(prog_step) {
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
				
			case 5:
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;

			case 6:
				//Mem(MAR) -> MDR
				int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR).getNumBits());
				setReg(MDR, memory.get(mar_addr));
				cycle_count++;
				prog_step++;
				break;

			case 7:
				//MDR -> registerFile(R)
				setReg(registerFile(getReg(R)), regMap.get(MDR));
				cycle_count++;
				prog_step=0;
				break;
			}
			break;

		case OpCodesList.STR:
			switch(prog_step){
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
				
			case 5:
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				setReg(MDR, getReg(registerFile(getReg(R))));
				cycle_count++;
				prog_step++;
				break;
			case 7:
				//MDR -> Mem(MAR)
				memory.put(Utils.registerToWord(getReg(MDR), getReg(MDR).getNumBits()), getReg(MAR), getReg(MAR).getNumBits());
				cycle_count++;
				prog_step=0;
				break;
			}
			break;

		case OpCodesList.LDA:
			switch(prog_step){
			case 4:
				calculateEA(false);
				cycle_count++;
				prog_step++;
				break;
				
			case 5:
				//EA -> regFile(R)
				setReg(registerFile(getReg(R)), getReg(EA));
				cycle_count++;
				prog_step=0;
				break;
			}
			break;

		case OpCodesList.LDX:
			switch(prog_step){
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;
				
			case 5:
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				//Mem(MAR) -> MDR
				int mar_addr = Utils.convertToInt(regMap.get(MAR), getReg(MAR).getNumBits());
				setReg(MDR, memory.get(mar_addr));
				cycle_count++;
				prog_step++;
				break;
			case 7:
				//MDR -> indexRegFile(R)
				setReg(indexRegisterFile(getReg(IX)), getReg(MDR));	
				cycle_count++;
				prog_step=0;
				break;
			}	
			break;

		case OpCodesList.STX:
			switch(prog_step){
			case 4:
				calculateEA(true);
				cycle_count++;
				prog_step++;
				break;
				
			case 5:
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				//indexRegFile(R) -> MDR
				setReg(MDR, getReg(indexRegisterFile(getReg(IX))));
				cycle_count++;
				prog_step++;
				break;
			case 7:
				//MDR -> Mem(MAR)
				memory.put(Utils.registerToWord(getReg(MDR), getReg(MDR).getNumBits()), getReg(MAR), regMap.get(MAR).getNumBits());
				cycle_count++;
				prog_step=0;
				break;

			}
			break;
			
		case OpCodesList.HLT:
			System.out.println("End of the program");
			cont_execution = false;
			prog_step=0;
			break;
		}
	}
	

	/**
	 * Calculates the EA (effective address). Boolean parameter is used
	 * for LDX or STX instructions where IX specifies the index register
	 * to load/store and NOT to be used when calculating the EA.
	 * 
	 * @param loadStoreIndex Set to true if doing a LDX or STX instruction.
	 */
	private void calculateEA(boolean LDXSTXInstruction) { 
		Register i = regMap.get(I);
		Register ix = regMap.get(IX);
		Register ea = regMap.get(EA);
		Register addr = regMap.get(ADDR);

		if (Utils.convertToByte(i, i.getNumBits()) == 0) { //No indirect addressing
			if (LDXSTXInstruction || 
					Utils.convertToByte(ix, ix.getNumBits()) == 0) { //No indexing			
				setReg(EA, regMap.get(ADDR));
			} else { //Indexing, no indirect
				//ADDR + indexregisterfile(IX)
				int temp = Utils.convertToInt(getReg(indexRegisterFile(ix)), getReg(indexRegisterFile(ix)).getNumBits()) +
						Utils.convertToInt(addr, addr.getNumBits());
			
				//EA = ADDR + Xx
				setReg(EA, Utils.intToBitSet(temp, ea.getNumBits()), ea.getNumBits());
			}
			
		} else { //Indirect addressing	
			if (LDXSTXInstruction || 
					Utils.convertToByte(ix, ix.getNumBits()) == 0) { //No indexing		
				setReg(EA, regMap.get(ADDR));
			} else { //Indexing, no indirect
				//ADDR + indexregisterfile(IX)
				int temp = Utils.convertToInt(getReg(indexRegisterFile(ix)), getReg(indexRegisterFile(ix)).getNumBits()) +
						Utils.convertToInt(addr, addr.getNumBits());
				
				//EA = ADDR + Xx
				setReg(EA, Utils.intToBitSet(temp, ea.getNumBits()), ea.getNumBits());
			}
			
			//TODO implement the clock
			//Taking care of the indirect part
			//EA -> MAR
			setReg(MAR, getReg(EA));
			
			//Memory(MAR) -> MDR
			setReg(MDR, memory.get(getReg(MAR), getReg(MAR).getNumBits()));
			
			//MDR -> EA
			setReg(EA, getReg(MDR));
		}
	}
	
	private void advancePC(){
		int pcContents = Utils.convertToInt(getReg(PC), getReg(PC).getNumBits());
		setReg(PC,
				Utils.intToBitSet(++pcContents, getReg(PC).getNumBits()),
						getReg(PC).getNumBits());
	}

	/**
	 * Returns a String key into the register map according to the
	 * contents of R (the register index register)
	 * @param R The R register.
	 * @return A String key into the register map.
	 */
	private String registerFile(BitSet R){
		switch(Utils.convertToByte(R, InstructionBitFormats.LD_STR_R_SIZE)){
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
	 * Returns a String key into the register map according to the
	 * contents of IX (the index register index)
	 * @param R The R register.
	 * @return A String key into the register map.
	 */
	private String indexRegisterFile(BitSet IX){
		switch(Utils.convertToByte(IX, InstructionBitFormats.LD_STR_IX_SIZE)){
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
		Utils.bitsetToString(OPCODE, getReg(OPCODE), getReg(OPCODE).getNumBits());
		Utils.bitsetToString(IX, getReg(IX), getReg(IX).getNumBits());
		Utils.bitsetToString(R, getReg(R), getReg(R).getNumBits());
		Utils.bitsetToString(I, getReg(I), getReg(I).getNumBits());
		Utils.bitsetToString(ADDR, getReg(ADDR), getReg(ADDR).getNumBits());
		Utils.bitsetToString(EA, getReg(EA), getReg(EA).getNumBits());
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
}
