package edu.gwu.seas.csci;

import java.text.ParseException;
import java.util.*;

/*CPU class registers placed into a map. I think this implementation is hard to use...i'd prefer to use
 * the more verbose CPU class with many getters and setters.  */

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
		regMap.put(X1, new Register(12));
		regMap.put(X2, new Register(12));
		regMap.put(X3, new Register(12));

		// Special registers
		regMap.put(PC, new Register(12));
		// set PC to start of Program Counter
		regMap.put(IR, new Register());
		regMap.put(CC, new Register(4));
		regMap.put(MAR, new Register(12));
		regMap.put(MDR, new Register());
		regMap.put(MSR, new Register());
		regMap.put(MFR, new Register(4));

		// Registers for Load and Store instructions
		regMap.put(OPCODE, new Register(InstructionBitFormats.OPCODE_SIZE));
		regMap.put(IX, new Register(InstructionBitFormats.LD_STR_IX_SIZE));
		regMap.put(R, new Register(InstructionBitFormats.LD_STR_R_SIZE));
		regMap.put(I, new Register(InstructionBitFormats.LD_STR_I_SIZE));
		regMap.put(ADDR, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));

		// Assuming EA should be as large as the ADDR register?
		regMap.put(EA, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));

		memory = Memory.getInstance();
		irdecoder = new IRDecoder(this);
		romLoader = new FileLoader();

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
		}
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
			System.out.println("Source: " + destName + " size: "
					+ destination.getNumBits());
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
	 * Called from continue/start gui button
	 *
	 * @param cont
	 *            Branch logic for continous processing or macro/micro step
	 */
	public void executeInstruction(String step_type){
		//TODO Figure out where to move code which fetches memory at EA
		//Not all instructions need to get memory at an effective address
		switch (step_type){
			case "continue":
				System.out.println("Continue");
				while (cont_execution) {
					singleInstruction();
				}
				break;
				
			case "micro step":
				System.out.println("Micro Step");
				singleInstruction();
				break;
				
			case "macro step":
				System.out.println("Macro Step");
				do {
					singleInstruction();
				} while(prog_step != 0);
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
			int mar_addr = Utils.convertToInt(regMap.get(MAR), 18);
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
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;

			case 5:
				//Mem(MAR) -> MDR
				int mar_addr = Utils.convertToInt(regMap.get(MAR), 18);
				setReg(MDR, memory.get(mar_addr));
				cycle_count++;
				prog_step++;
				break;

			case 6:
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
				//MDR -> Mem(MAR)
				memory.put((Word)getReg(MDR).getValue(), getReg(MAR), 18);
				cycle_count++;
				prog_step=0;
				break;
			}
			break;

		case OpCodesList.LDA:
			//If no indirection, then load regFile(R) with EA
			//else, need to go get the address to load from memory
			if(Utils.convertToByte(getReg(I), InstructionBitFormats.LD_STR_I_SIZE) == 0){
				switch(prog_step){
				case 4:
					//EA -> regFile(R)
					setReg(registerFile(getReg(R)), getReg(EA));
					cycle_count++;
					prog_step=0;
					break;
				}
			}else{
				switch(prog_step){
				case 4:
					//EA -> MAR
					setReg(MAR, regMap.get(EA));
					cycle_count++;
					prog_step++;
					break;
				case 5:
					//Mem(EA) -> MDR
					setReg(MDR, memory.get(getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE));
					cycle_count++;
					prog_step++;
					break;
				case 6:
					//MDR -> regFile(R)
					setReg(registerFile(getReg(R)), getReg(MDR));
					cycle_count++;
					prog_step=0;
					break;
				}	
			}
			break;

		case OpCodesList.LDX:
			switch(prog_step){
			case 4:
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				//Mem(EA) -> MDR
				setReg(MDR, memory.get(getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE));
				cycle_count++;
				prog_step++;
				break;
			case 6:
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
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				cycle_count++;
				prog_step++;
				break;
			case 5:
				//indexRegFile(R) -> MDR
				setReg(MDR, getReg(indexRegisterFile(getReg(IX))));
				cycle_count++;
				prog_step++;
				break;
			case 6:
				//MDR -> Mem(MAR)
				memory.put((Word)getReg(MDR).getValue(), getReg(MAR), 18);
				cycle_count++;
				prog_step=0;
				break;

			}
			break;
			
		case OpCodesList.HLT:
			System.out.println("End of the program");
			cont_execution = false;
			break;
		}
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
		Utils.BitSetToString(R0, getReg(R0), 18);
		Utils.BitSetToString(R1, getReg(R1), 18);
		Utils.BitSetToString(R2, getReg(R2), 18);
		Utils.BitSetToString(R3, getReg(R3), 18);
		Utils.BitSetToString(X1, getReg(X1), 12);
		Utils.BitSetToString(X2, getReg(X2), 12);
		Utils.BitSetToString(X3, getReg(X3), 12);
		Utils.BitSetToString(PC, getReg(PC), 12);
		Utils.BitSetToString(IR, getReg(IR), 18);
		Utils.BitSetToString(CC, getReg(CC), 4);
		Utils.BitSetToString(MAR, getReg(MAR), 12);
		Utils.BitSetToString(MDR, getReg(MDR), 18);
		Utils.BitSetToString(MSR, getReg(MSR), 18);
		Utils.BitSetToString(MFR, getReg(MFR), 4);
		Utils.BitSetToString(OPCODE, getReg(OPCODE),
				InstructionBitFormats.OPCODE_SIZE);
		Utils.BitSetToString(IX, getReg(IX),
				InstructionBitFormats.LD_STR_IX_SIZE);
		Utils.BitSetToString(R, getReg(R), InstructionBitFormats.LD_STR_R_SIZE);
		Utils.BitSetToString(I, getReg(I), InstructionBitFormats.LD_STR_I_SIZE);
		Utils.BitSetToString(ADDR, getReg(ADDR),
				InstructionBitFormats.LD_STR_ADDR_SIZE);
		Utils.BitSetToString(EA, getReg(EA),
				InstructionBitFormats.LD_STR_ADDR_SIZE);
	}

	public void loadROM() {
		try {
			romLoader.load();
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
