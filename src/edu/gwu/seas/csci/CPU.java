package edu.gwu.seas.csci;

import java.util.*;

/*CPU class registers placed into a map. I think this implementation is hard to use...i'd prefer to use
 * the more verbose CPU class with many getters and setters.  */

public class CPU {
	//Public constants for register names
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
	
	
	private Map<String, Register> regMap = new HashMap<String, Register>();
	private Memory memory;
	private IRDecoder irdecoder;
	
	//Constructor
	public CPU() {
			
		//4 General Purpose Registers(GPRs)		
		regMap.put(R0, new Register());
		regMap.put(R1, new Register());
		regMap.put(R2, new Register());
		regMap.put(R3, new Register());
		
		//3 Index Registers		
		regMap.put(X1, new Register(12));
		regMap.put(X2, new Register(12));
		regMap.put(X3, new Register(12));
		
		//Special registers
		regMap.put(PC, new Register(12));
		regMap.put(IR, new Register());
		regMap.put(CC, new Register(4));
		regMap.put(MAR, new Register(12));
		regMap.put(MDR, new Register());
		regMap.put(MSR, new Register());
		regMap.put(MFR, new Register(4));
		
		//Registers for Load and Store instructions
		regMap.put(OPCODE, new Register(InstructionBitFormats.OPCODE_SIZE));
		regMap.put(IX, new Register(InstructionBitFormats.LD_STR_IX_SIZE));
		regMap.put(R, new Register(InstructionBitFormats.LD_STR_R_SIZE));
		regMap.put(I, new Register(InstructionBitFormats.LD_STR_I_SIZE));
		regMap.put(ADDR, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));
		
		//Assuming EA should be as large as the ADDR register?
		regMap.put(EA, new Register(InstructionBitFormats.LD_STR_ADDR_SIZE));
				
		memory = Memory.getInstance();
		irdecoder = new IRDecoder(this);

	}
	
	/**
	 * Sets a register with a BitSet value.
	 * 
	 * @param destName
	 * 				Key into the register map.
	 * @param sourceSet
	 * 				BitSet to set the register equal to.
	 * @param sourceBits
	 * 				Number of bits in the BitSet.
	 */
	public void setReg(String destName, BitSet sourceSet, int sourceBits) {
		if (regMap.containsKey(destName)) {
			//regMap.put(regName, bitValue);
			
			Register destination = regMap.get(destName);
			Utils.bitsetDeepCopy(sourceSet, sourceBits, 
					destination, destination.getNumBits());
		}
	}
	
	/**
	 * Set a register with the contents of another register (given it's key in the
	 * register map).
	 * 
	 * @param destName
	 * 				Key into the register map (the destination register).
	 * @param source
	 */
	public void setReg(String destName, Register source){
		if (regMap.containsKey(destName)){
			Register destination = regMap.get(destName);
			Utils.bitsetDeepCopy(source, source.getNumBits(), 
					destination, destination.getNumBits());
		}
	}
	
	/**
	 * Set a register with the contents of a word gotten from memory.
	 * @param destName
	 * @param sourceMemory
	 */
	public void setReg(String destName, Word sourceMemory){
		if (regMap.containsKey(destName)){
			Register destination = regMap.get(destName);
			System.out.println("Source: " + destName + " size: " + destination.getNumBits());
			Utils.bitsetDeepCopy(sourceMemory, 18, 
					destination, destination.getNumBits());
		}
	}
	
	/**
	 * Gets a register from the map.
	 * @param regName
	 * 			Key into the register map.
	 * @return The register associated with the key.
	 */
	public Register getReg(String regName) {
		return regMap.get(regName);
	}
	
	/**
	 * From the OPCODE, executes the appropriate register transfer logic
	 * according to the instruction and its arguments.
	 */
	private void executeInstruction(){
		//TODO Figure out where to move code which fetches memory at EA
		//Not all instructions need to get memory at an effective address
		
		
		switch(Utils.convertToByte(getReg(OPCODE), InstructionBitFormats.OPCODE_SIZE)){
		
		case OpCodesList.LDR:
			//EA -> MAR
			setReg(MAR, regMap.get(EA));
			
			//Mem(EA) -> MDR
			setReg(MDR, memory.get(getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE));
			
			//MDR -> regFile(R)
			setReg(registerFile(getReg(R)), getReg(MDR));
			break;
			
		case OpCodesList.STR:
			//EA -> MAR
			setReg(MAR, regMap.get(EA));
			
			//regFile(R) -> MDR
			setReg(MDR, getReg(registerFile(getReg(R))));
			
			//TODO fix MDR to be a word, add a constant "word size" to the Word class
			//MDR -> Mem(MAR)
			memory.put((Word)getReg(MDR).getValue(), getReg(MAR), 18);
			break;
			
		case OpCodesList.LDA:
			//If no indirection, then load regFile(R) with EA
			//else, need to go get the address to load from memory
			if(Utils.convertToByte(getReg(I), InstructionBitFormats.LD_STR_I_SIZE) == 0){
				//EA -> regFile(R)
				setReg(registerFile(getReg(R)), getReg(EA));
			}else{
				//EA -> MAR
				setReg(MAR, regMap.get(EA));
				
				//Mem(EA) -> MDR
				setReg(MDR, memory.get(getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE));
				
				//MDR -> regFile(R)
				setReg(registerFile(getReg(R)), getReg(MDR));
			}
			break;
			
		case OpCodesList.LDX:
			//EA -> MAR
			setReg(MAR, regMap.get(EA));
			
			//Mem(EA) -> MDR
			setReg(MDR, memory.get(getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE));
			
			//MDR -> indexRegFile(R)
			setReg(indexRegisterFile(getReg(IX)), getReg(MDR));			
			break;
			
		case OpCodesList.STX:
			//EA -> MAR
			setReg(MAR, regMap.get(EA));
			
			//indexRegFile(R) -> MDR
			setReg(MDR, getReg(indexRegisterFile(getReg(IX))));
			
			//TODO fix MDR to be a word, add a constant "word size" to the Word class
			//MDR -> Mem(MAR)
			memory.put((Word)getReg(MDR).getValue(), getReg(MAR), 18);
			
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
	 * Prints the contents of all the registers to the console.
	 * (Eventually will become obsolete when GUI is done)
	 */
	private void printAllRegisters(){
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
		Utils.BitSetToString(OPCODE, getReg(OPCODE), InstructionBitFormats.OPCODE_SIZE);
		Utils.BitSetToString(IX, getReg(IX), InstructionBitFormats.LD_STR_IX_SIZE);
		Utils.BitSetToString(R, getReg(R), InstructionBitFormats.LD_STR_R_SIZE);
		Utils.BitSetToString(I, getReg(I), InstructionBitFormats.LD_STR_I_SIZE);
		Utils.BitSetToString(ADDR, getReg(ADDR), InstructionBitFormats.LD_STR_ADDR_SIZE);
		Utils.BitSetToString(EA, getReg(EA), InstructionBitFormats.LD_STR_ADDR_SIZE);
	}
}
