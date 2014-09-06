package edu.gwu.seas.csci;

import java.util.*;

/*CPU class registers placed into a map. I think this implementation is hard to use...i'd prefer to use
 * the more verbose CPU class with many getters and setters.  */

public class CPU2 {
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
	
	
	
	private Map<String, BitSet> regMap = new HashMap<String, BitSet>();
	
	//Constructor
	public CPU2() {
			
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

}
