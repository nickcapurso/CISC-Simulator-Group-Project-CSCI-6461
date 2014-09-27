package edu.gwu.seas.csci;

/**
 * Lists the basic descriptions and formats for the various instructions. Provides
 * constants for each opcode for the purpose of parsing and code readability.
 * <p>
 * <b>Instruction format types</b> (each has their own diagram in the project description): <br>
 * <ul>
 * 	<li>Halt instruction (page 6)</li>
 * 	<li>Trap instruction (page 6)</li>
 * 	<li>Load/store instructions (page 6) (also used for some arithmetic & jumps)</li>
 * 	<li>Register to register (X&Y) arithmetic/logic instructions (page 10)</li>
 * 	<li>Shift/rotate instructions (page 11)</li>
 * 	<li>I/O instructions (page 12)</li>
 * 	<li>Floating point/vector instructions (page 13)</li>
 * </ul>
 * See InstructionBitFormats.java for bit breakdowns.
 * <p>
 * 
 * @author Nick Capurso
 */
public class OpCodesList {
	/**
	 * Halt - stops the machine. 
	 * <p>
	 * Usage: HLT <br>
	 * Instruction format type: Special halt instruction
	 */
	public static final byte HLT = 00;

	/**
	 * Load register from memory.
	 * <p>
	 * Usage: LDR r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte LDR = 01;

	/**
	 * Store register to memory.
	 * <p>
	 * Usage: STR r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte STR = 02;

	/**
	 * Load register with address.
	 * <p>
	 * Usage: LDA r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte LDA = 03;

	/**
	 * Add memory to register.
	 * <p>
	 * Usage: AMR r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte AMR = 04;

	/**
	 * Subtract memory from register.
	 * <p>
	 * Usage: SMR r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte SMR = 05;

	/**
	 * Add immediate to register (immediate is specified in ADDR). IX and I are ignored.
	 * <p>
	 * Usage: AIR r, immed <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte AIR = 06;

	/**
	 * Subtract immediate from register (immediate is specified in ADDR). IX and I are ignored.
	 * <p>
	 * Usage: SIR r, immed <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte SIR = 07;

	/**
	 * Jump if zero.
	 * <p>
	 * Usage: JZ r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JZ = 010;

	/**
	 * Jump if not equal.
	 * <p>
	 * Usage: JNE r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JNE = 011;

	/**
	 * Jump if condition code - specified in first argument (replaces r) and
	 * specifies which bit of the CC to check against.
	 * <p>
	 * Usage: JCC cc, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JCC = 012;

	/**
	 * Unconditional jump. R is ignored.
	 * <p>
	 * Usage: JMP x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JMP = 013;

	/**
	 * Jump subroutine (or jump and save return address). R is ignored.
	 * <p>
	 * Usage: JSR x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JSR = 014;

	/**
	 * Return from subroutine - return code specified in ADDR field (optional). 
	 * IX and I are ignored.
	 * <p>
	 * Usage: RFS Immed <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte RFS = 015;

	/**
	 * Subtract one and branch.
	 * <p>
	 * Usage: SOB r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte SOB = 016;

	/**
	 * Jump if greater than or equal to.
	 * <p>
	 * Usage: JGE r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte JGE = 017;

	/**
	 * Multiply register by register. Result stored in RX (high order bits) and RX+1 (low order bits)
	 * <p>
	 * Usage: MLT rx, ry <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte MLT = 020;

	/**
	 * Divide register by register. RX contains the quotient, RX+1 contains the remainder.
	 * <p>
	 * Usage: DVD rx, ry  <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte DVD = 021;

	/**
	 * Test if the contents of two registers are equal.
	 * <p>
	 * Usage: TRR rx, ry  <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte TRR = 022;

	/**
	 * Logical AND of two registers.
	 * <p>
	 * Usage: AND rx, ry  <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte AND = 023;

	/**
	 * Logical OR of two registers.
	 * <p>
	 * Usage: ORR rx, ry  <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte ORR = 024;

	/**
	 * Logical NOT of two registers.
	 * <p>
	 * Usage: NOT rx, ry  <br>
	 * Instruction format type: X & Y Arithmetic/Logical
	 */
	public static final byte NOT = 025;

	/**
	 * Traps to memory address 0 - a trap table containing addresses of a maximum of
	 * 16 user-specified routines. The index into the table is specified in the ADDR field.
	 * <p>
	 * Usage: TRAP code <br>
	 * Instruction format type: Special trap instruction
	 */
	public static final byte TRAP  = 030;

	/**
	 * Shift register by count. 
	 * <p>
	 * Usage: SRC r, count, L/R, A/L <br>
	 * Instruction format type: Shift/Rotate
	 */
	public static final byte SRC = 031;

	/**
	 * Rotate register by count.
	 * <p>
	 * Usage: RRC r, count, L/R/ A/L <br>
	 * Instruction format type: Shift/Rotate
	 */
	public static final byte RRC = 032;

	/**
	 * Floating add memory to register.
	 * <p>
	 * Usage: FADD fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte FADD = 033;

	/**
	 * Floating subtract from register.
	 * <p>
	 * Usage: FSUB fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte FSUB = 034;

	/**
	 * Vector add.
	 * <p>
	 * Usage: VADD fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte VADD = 035;

	/**
	 * Vector subtract.
	 * <p>
	 * Usage: VSUB fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte VSUB = 036;

	/**
	 * Convert to fixed/floating point
	 * <p>
	 * Usage: CNVRT r, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte CNVRT = 037;

	/**
	 * Load index register from memory.
	 * <p>
	 * Usage: LDX x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte LDX = 041;

	/**
	 * Store index register to memory.
	 * <p>
	 * Usage: STX x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte STX = 042;

	/**
	 * Load floating register from memory.
	 * <p>
	 * Usage: LDRF fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte LDFR = 050;

	/**
	 * Store floating register to memory.
	 * <p>
	 * Usage: STFR fr, x, address[, i] <br>
	 * Instruction format type: Load/Store
	 */
	public static final byte STFR = 051;

	/**
	 * Input character to register from device.
	 * <p>
	 * Usage: IN r, devid <br>
	 * Instruction format type: I/O
	 */
	public static final byte IN	= 061;

	/**
	 * Output character to device from register.
	 * <p>
	 * Usage: OUT r, devid <br>
	 * Instruction format type: I/O
	 */
	public static final byte OUT = 062;

	/**
	 * Check device status to register.
	 * <p>
	 * Usage: CHK r, devid <br>
	 * Instruction format type: I/O
	 */
	public static final byte CHK = 063;
}
