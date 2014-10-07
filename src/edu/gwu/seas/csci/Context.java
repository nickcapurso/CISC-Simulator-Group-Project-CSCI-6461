/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the application context.
 * 
 * @author Alex Remily
 */
public class Context {

	private static final Context CONTEXT = new Context();

	/**
	 * Enumerates the classes of OPCODE supported by the Computer.
	 * 
	 * @author Alex Remily
	 */
	public enum InstructionClass {
		LD_STR, TRANS, HALT, TRAP, ARITH, XY_ARITH_LOGIC, SHIFT, IO
	}

	/**
	 * Just make these generic category containers for instructions of common
	 * format for purposes of parsing.
	 * 
	 * @author Alex Remily
	 */
	public enum InstructionFormat {
		ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT
	}

	/**
	 * The dirty flag bitmask for the cache line. Indicates that the value
	 * differs from it's corresponding value in memory.
	 */
	public static final byte ONE = 1;
	public static final byte TWO = 2;
	public static final byte THREE = 4;
	public static final byte FOUR = 8;
	public static final byte FIVE = 16;
	public static final byte SIX = 32;
	public static final byte SEVEN = 64;

	private Map<String, InstructionClass> opcodeClasses = new HashMap<String, InstructionClass>();
	private Map<String, InstructionFormat> instructionFormats = new HashMap<String, InstructionFormat>();
	private Map<String, Byte> opCodeBytes = new HashMap<String, Byte>();
	private Map<Byte, String> opCodeStrings = new HashMap<Byte, String>();
	private Map<Integer, Byte> indexToFlags = new HashMap<Integer, Byte>();

	/**
	 * 
	 */
	private Context() {

		/**
		 * InstructionFormats setup
		 */

		// HALT
		instructionFormats.put("HLT", InstructionFormat.ZERO);

		// OPCODE R,X,ADDR[,I]
		instructionFormats.put("LDR", InstructionFormat.ONE);
		instructionFormats.put("STR", InstructionFormat.ONE);
		instructionFormats.put("LDA", InstructionFormat.ONE);
		instructionFormats.put("AMR", InstructionFormat.ONE);
		instructionFormats.put("SMR", InstructionFormat.ONE);
		instructionFormats.put("JZ", InstructionFormat.ONE);
		instructionFormats.put("JNE", InstructionFormat.ONE);
		instructionFormats.put("JCC", InstructionFormat.ONE);
		instructionFormats.put("SOB", InstructionFormat.ONE);
		instructionFormats.put("JGE", InstructionFormat.ONE);

		// OPCODE X,ADDR[,I]
		instructionFormats.put("LDX", InstructionFormat.TWO);
		instructionFormats.put("STX", InstructionFormat.TWO);
		instructionFormats.put("JMR", InstructionFormat.TWO);
		instructionFormats.put("JSR", InstructionFormat.TWO);

		// OPCODE R,IMMED
		instructionFormats.put("AIR", InstructionFormat.THREE);
		instructionFormats.put("SIR", InstructionFormat.THREE);

		// OPCODE IMMED
		instructionFormats.put("RFS", InstructionFormat.FOUR);

		// OPCODE RX
		instructionFormats.put("NOT", InstructionFormat.FIVE);

		// OPCODE RX,RY
		instructionFormats.put("MLT", InstructionFormat.SIX);
		instructionFormats.put("DVD", InstructionFormat.SIX);
		instructionFormats.put("TRR", InstructionFormat.SIX);
		instructionFormats.put("AND", InstructionFormat.SIX);
		instructionFormats.put("ORR", InstructionFormat.SIX);

		// OPCODE R,COUNT,L/R,A/L
		instructionFormats.put("SRC", InstructionFormat.SEVEN);
		instructionFormats.put("RRC", InstructionFormat.SEVEN);
		
		// OPCODE R,DEVID
		instructionFormats.put("IN", InstructionFormat.EIGHT);
		instructionFormats.put("OUT", InstructionFormat.EIGHT);

		/**
		 * opcodeClasses setup
		 */
		// Special instructions
		opcodeClasses.put("HLT", InstructionClass.HALT);
		opcodeClasses.put("TRAP", InstructionClass.TRAP);

		// Load/store instructions
		opcodeClasses.put("LDR", InstructionClass.LD_STR);
		opcodeClasses.put("STR", InstructionClass.LD_STR);
		opcodeClasses.put("LDA", InstructionClass.LD_STR);
		opcodeClasses.put("LDX", InstructionClass.LD_STR);
		opcodeClasses.put("STX", InstructionClass.LD_STR);

		// Transfer instructions
		opcodeClasses.put("JZ", InstructionClass.TRANS);
		opcodeClasses.put("JNE", InstructionClass.TRANS);
		opcodeClasses.put("JCC", InstructionClass.TRANS);
		opcodeClasses.put("JMP", InstructionClass.TRANS);
		opcodeClasses.put("JSR", InstructionClass.TRANS);
		opcodeClasses.put("RFS", InstructionClass.TRANS);
		opcodeClasses.put("SOB", InstructionClass.TRANS);
		opcodeClasses.put("JGE", InstructionClass.TRANS);

		// Arithmetic (register-address) instructions
		opcodeClasses.put("AMR", InstructionClass.ARITH);
		opcodeClasses.put("SMR", InstructionClass.ARITH);
		opcodeClasses.put("AIR", InstructionClass.ARITH);
		opcodeClasses.put("SIR", InstructionClass.ARITH);

		// Arithmetic/logical (register-register) instructions
		opcodeClasses.put("MLT", InstructionClass.XY_ARITH_LOGIC);
		opcodeClasses.put("DVD", InstructionClass.XY_ARITH_LOGIC);
		opcodeClasses.put("TRR", InstructionClass.XY_ARITH_LOGIC);
		opcodeClasses.put("AND", InstructionClass.XY_ARITH_LOGIC);
		opcodeClasses.put("ORR", InstructionClass.XY_ARITH_LOGIC);
		opcodeClasses.put("NOT", InstructionClass.XY_ARITH_LOGIC);

		// Shift/rotate instructions
		opcodeClasses.put("SRC", InstructionClass.SHIFT);
		opcodeClasses.put("RRC", InstructionClass.SHIFT);

		/**
		 * OpCodeBytes setup
		 */
		// Special instructions
		opCodeBytes.put("HLT", OpCodesList.HLT);
		opCodeBytes.put("TRAP", OpCodesList.TRAP);

		// Load/store instructions
		opCodeBytes.put("LDR", OpCodesList.LDR);
		opCodeBytes.put("STR", OpCodesList.STR);
		opCodeBytes.put("LDA", OpCodesList.LDA);
		opCodeBytes.put("LDX", OpCodesList.LDX);
		opCodeBytes.put("STX", OpCodesList.STX);

		// Transfer instructions
		opCodeBytes.put("JZ", OpCodesList.JZ);
		opCodeBytes.put("JNE", OpCodesList.JNE);
		opCodeBytes.put("JCC", OpCodesList.JCC);
		opCodeBytes.put("JMP", OpCodesList.JMP);
		opCodeBytes.put("JSR", OpCodesList.JSR);
		opCodeBytes.put("RFS", OpCodesList.RFS);
		opCodeBytes.put("SOB", OpCodesList.SOB);
		opCodeBytes.put("JGE", OpCodesList.JGE);

		// Arithmetic (register-address) instructions
		opCodeBytes.put("AMR", OpCodesList.AMR);
		opCodeBytes.put("SMR", OpCodesList.SMR);
		opCodeBytes.put("AIR", OpCodesList.AIR);
		opCodeBytes.put("SIR", OpCodesList.SIR);

		// Arithmetic/logical (register-register) instructions
		opCodeBytes.put("MLT", OpCodesList.MLT);
		opCodeBytes.put("DVD", OpCodesList.DVD);
		opCodeBytes.put("TRR", OpCodesList.TRR);
		opCodeBytes.put("AND", OpCodesList.AND);
		opCodeBytes.put("ORR", OpCodesList.ORR);
		opCodeBytes.put("NOT", OpCodesList.NOT);

		// Shift/rotate instructions
		opCodeBytes.put("SRC", OpCodesList.SRC);
		opCodeBytes.put("RRC", OpCodesList.RRC);
		
		// IO Instructions
		opCodeBytes.put("IN", OpCodesList.IN);
		opCodeBytes.put("OUT", OpCodesList.OUT);

		/**
		 * opCodeStrings setup
		 */
		// Special instructions
		opCodeStrings.put(OpCodesList.HLT, "HLT");
		opCodeStrings.put(OpCodesList.TRAP, "TRAP");

		// Load/store instructions
		opCodeStrings.put(OpCodesList.LDR, "LDR");
		opCodeStrings.put(OpCodesList.STR, "STR");
		opCodeStrings.put(OpCodesList.LDA, "LDA");
		opCodeStrings.put(OpCodesList.LDX, "LDX");
		opCodeStrings.put(OpCodesList.STX, "STX");

		// Transfer instructions
		opCodeStrings.put(OpCodesList.JZ, "JZ");
		opCodeStrings.put(OpCodesList.JNE, "JNE");
		opCodeStrings.put(OpCodesList.JCC, "JCC");
		opCodeStrings.put(OpCodesList.JMP, "JMP");
		opCodeStrings.put(OpCodesList.JSR, "JSR");
		opCodeStrings.put(OpCodesList.RFS, "RFS");
		opCodeStrings.put(OpCodesList.SOB, "SOB");
		opCodeStrings.put(OpCodesList.JGE, "JGE");

		// Arithmetic (register-address) instructions
		opCodeStrings.put(OpCodesList.AMR, "AMR");
		opCodeStrings.put(OpCodesList.SMR, "SMR");
		opCodeStrings.put(OpCodesList.AIR, "AIR");
		opCodeStrings.put(OpCodesList.SIR, "SIR");

		// Arithmetic/logical (register-register) instructions
		opCodeStrings.put(OpCodesList.MLT, "MLT");
		opCodeStrings.put(OpCodesList.DVD, "DVD");
		opCodeStrings.put(OpCodesList.TRR, "TRR");
		opCodeStrings.put(OpCodesList.AND, "AND");
		opCodeStrings.put(OpCodesList.ORR, "ORR");
		opCodeStrings.put(OpCodesList.NOT, "NOT");

		// Shift/rotate instructions
		opCodeStrings.put(OpCodesList.SRC, "SRC");
		opCodeStrings.put(OpCodesList.RRC, "RRC");
		
		opCodeStrings.put(OpCodesList.IN, "IN");
		opCodeStrings.put(OpCodesList.OUT, "OUT");

		indexToFlags.put(0, ONE);
		indexToFlags.put(1, TWO);
		indexToFlags.put(2, THREE);
		indexToFlags.put(3, FOUR);
		indexToFlags.put(4, FIVE);
		indexToFlags.put(5, SIX);
	}

	public static Context getInstance() {
		return CONTEXT;
	}

	/**
	 * @return the opcodeClasses
	 */
	public Map<String, InstructionClass> getOpcodeClasses() {
		return opcodeClasses;
	}

	/**
	 * @return the instructionFormats
	 */
	public Map<String, InstructionFormat> getInstructionFormats() {
		return instructionFormats;
	}

	/**
	 * @return the opCodeStrings
	 */
	public Map<Byte, String> getOpCodeStrings() {
		return opCodeStrings;
	}

	/**
	 * @return the opCodeBytes
	 */
	public Map<String, Byte> getOpCodeBytes() {
		return opCodeBytes;
	}

	/**
	 * @return the indexToFlags
	 */
	public Map<Integer, Byte> getIndexToFlags() {
		return indexToFlags;
	}
}
