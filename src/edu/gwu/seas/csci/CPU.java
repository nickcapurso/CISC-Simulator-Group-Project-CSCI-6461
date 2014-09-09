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
	
	
	private Map<String, BitSet> regMap = new HashMap<String, BitSet>();
	private Memory memory;
	
	//Constructor
	public CPU() {
			
		//4 General Purpose Registers(GPRs)		
		regMap.put(R0, new BitSet(18));
		regMap.put(R1, new BitSet(18));
		regMap.put(R2, new BitSet(18));
		regMap.put(R3, new BitSet(18));
		
		//3 Index Registers		
		regMap.put(X1, new BitSet(12));
		regMap.put(X2, new BitSet(12));
		regMap.put(X3, new BitSet(12));
		
		//Special registers
		regMap.put(PC, new BitSet(12));
		regMap.put(IR, new BitSet(18));
		regMap.put(CC, new BitSet(4));
		regMap.put(MAR, new BitSet(12));
		regMap.put(MDR, new BitSet(18));
		regMap.put(MSR, new BitSet(18));
		regMap.put(MFR, new BitSet(4));
		
		//Registers for Load and Store instructions
		regMap.put(OPCODE, new BitSet(InstructionBitFormats.OPCODE_SIZE));
		regMap.put(IX, new BitSet(InstructionBitFormats.LD_STR_IX_SIZE));
		regMap.put(R, new BitSet(InstructionBitFormats.LD_STR_R_SIZE));
		regMap.put(I, new BitSet(InstructionBitFormats.LD_STR_I_SIZE));
		regMap.put(ADDR, new BitSet(InstructionBitFormats.LD_STR_ADDR_SIZE));
		
		//Assuming EA should be as large as the ADDR register?
		regMap.put(EA, new BitSet(InstructionBitFormats.LD_STR_ADDR_SIZE));
				
		memory = Memory.getInstance();
	}
	
	//Sets a register with a BitSet value
	public void setReg(String regName, BitSet bitValue) {
		if (regMap.containsKey(regName)) {
			regMap.put(regName, bitValue);
		}
	}
	
	//Gets a specified register's value
	public BitSet getReg(String regName) {
		return regMap.get(regName);
	}
	
	/**
	 * From the OPCODE, executes the appropriate register transfer logic
	 * according to the instruction and its arguments.
	 */
	private void executeInstruction(){
		//TODO Figure out where to move this code which fetches memory at EA
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
			memory.put((Word)getReg(MDR), getReg(MAR), 18);
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
			memory.put((Word)getReg(MDR), getReg(MAR), 18);
			
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

}
