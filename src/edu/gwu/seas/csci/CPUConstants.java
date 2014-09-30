package edu.gwu.seas.csci;

/**
 * This file contains constants used with the CPU class and any related classes that manipulate CPU data.
 *
 */

public interface CPUConstants {
	
	/**
	 * General CPU constants
	 */
	
	public static final int DEFAULT_BIT_SIZE = 18;
	
	/**
	 * Public constants for register names
	 */
	
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
	public static final String OP1 = "OP1";
	public static final String OP2 = "OP2";
	public static final String OP3 = "OP3";
	public static final String OP4 = "OP4";
	public static final String RESULT = "RESULT";
	public static final String RESULT2 = "RESULT2";
	public static final String RX = "RX";
	public static final String RY = "RY";
	public static final String AL = "AL";
	public static final String LR = "LR";
	public static final String COUNT = "COUNT";
	
	/**
	 * ALU constants
	 */
	
	public static final String AND_OP = "AND";
	public static final String OR_OP = "OR";
	public static final String RIGHT_SHIFT = "right_shift";
	public static final String LEFT_SHIFT = "left_shift";
	
	//On bits in the CC register that indicate conditions.
	public static final int OVERFLOW = 0;
	public static final int UNDERFLOW = 1;
	public static final int DIVZERO = 2;
	public static final int EQUALORNOT = 3;
	

	
	
	
	
}
