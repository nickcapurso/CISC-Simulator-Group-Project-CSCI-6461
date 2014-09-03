package csci_6461_group_project;

/**
 * Lists the basic descriptions and formats for the various instructions. Provides
 * constants for each opcode for the purpose of parsing and code readability.
 * <p>
 * <b>Instruction format types</b> (each has their own diagram in the project description): <br>
 * <ul>
 * 	<li>Halt instruction (page 6)</li>
 * 	<li>Trap instruction (page 6)</li>
 * 	<li>Standard/arithmetic/jump instructions (page 6)</li>
 * 	<li>Special arithmetic/logical instructions (page 10)</li>
 * 	<li>Shift/rotate instructions (page 11)</li>
 * 	<li>I/O instructions (page 12)</li>
 * 	<li>Floating point/vector instructions (page 13)</li>
 * </ul>
 * 
 * @author Nick Capurso
 *
 */
public class OpCodesList {
	/**
	 * Halt - stops the machine. 
	 * <p>
	 * Format: HLT
	 */
	public static final byte HLT = 0;

	/**
	 * Load register from memory.
	 * <p>
	 * Format: LDR r, x, address[, i]
	 */
	public static final byte LDR = 1;
	
	/**
	 * Store register to memory.
	 * <p>
	 * Format: STR r, x, address[, i]
	 */
	public static final byte STR = 2;
	
	/**
	 * Load register with address.
	 * <p>
	 * Format: LDA r, x, address[, i]
	 */
	public static final byte LDA = 3;
	
	/**
	 * Add memory to register.
	 * <p>
	 * Format: AMR r, x, address[, i]
	 */
	public static final byte AMR = 4;
	
	/**
	 * Subtract memory from register.
	 * <p>
	 * Format: SMR r, x, address[, i]
	 */
	public static final byte SMR = 5;
	
	/**
	 * Add immediate to register. IX and I are ignored.
	 * <p>
	 * Format: AIR r, immed
	 */
	public static final byte AIR = 6;
	
	/**
	 * Subtract immediate from register. IX and I are ignored.
	 * <p>
	 * Format: SIR r, immed
	 */
	public static final byte SIR = 7;
	
	/**
	 * Jump if zero.
	 * <p>
	 * Format: JZ r, x, address[, i]
	 */
	public static final byte JZ = 10;
	
	/**
	 * Jump if not equal.
	 * <p>
	 * Format: JNE r, x, address[, i]
	 */
	public static final byte JNE = 11;
	
	/**
	 * Jump if condition code - specified in first argument (replaces r) and
	 * specifies which bit of the CC to check against.
	 * <p>
	 * Format: JCC cc, x, address[, i]
	 */
	public static final byte JCC = 12;
	
	/**
	 * Unconditional jump. R is ignored.
	 * <p>
	 * Format: JMP x, address[, i]
	 */
	public static final byte JMP = 13;
	
	/**
	 * Jump subroutine (or jump and save return address). R is ignored.
	 * <p>
	 * Format: JSR x, address[, i]
	 */
	public static final byte JSR = 14;
	
	/**
	 * Return from subroutine - return code specified in ADDR field (optional). 
	 * IX and I are ignored.
	 * <p>
	 * Format: RFS Immed
	 */
	public static final byte RFS = 15;
	
	/**
	 * Subtract one and branch.
	 * <p>
	 * Format: SOB r, x, address[, i]
	 */
	public static final byte SOB = 16;
	
	/**
	 * Jump if greater than or equal to.
	 * <p>
	 * Format: JGE r, x, address[, i]
	 */
	public static final byte JGE = 17;
	
	/**
	 * Multiply register by register.
	 * <p>
	 * Format: MLT rx, ry
	 */
	public static final byte MLT = 20;
	
	/**
	 * Divide register by register.
	 * <p>
	 * Format: DVD rx, ry
	 */
	public static final byte DVD = 21;
	
	/**
	 * Test if the contents of two registers are equal.
	 * <p>
	 * Format: TRR rx, ry
	 */
	public static final byte TRR = 22;
	
	/**
	 * Logical AND of two registers.
	 * <p>
	 * Format: AND rx, ry
	 */
	public static final byte AND = 23;
	
	/**
	 * Logical OR of two registers.
	 * <p>
	 * Format: ORR rx, ry
	 */
	public static final byte ORR = 24;
	
	/**
	 * Logical NOT of two registers.
	 * <p>
	 * Format: NOT rx, ry
	 */
	public static final byte NOT = 25;

	/**
	 * Traps to memory address 0 - a trap table containing addresses of a maximum of
	 * 16 user-specified routines. The index into the table is specified in the ADDR field.
	 * <p>
	 * Format: TRAP code
	 */
	public static final byte TRAP  = 30;
	
	/**
	 * Shift register by count. 
	 * <p>
	 * Format: SRC r, count, L/R, A/L
	 */
	public static final byte SRC = 31;
	
	/**
	 * Rotate register by count.
	 * <p>
	 * Format: RRC r, count, L/R/ A/L
	 */
	public static final byte RRC = 32;
	
	/**
	 * Floating add memory to register.
	 * <p>
	 * Format: FADD fr, x, address[, i]
	 */
	public static final byte FADD = 33;
	
	/**
	 * Floating subtract from register.
	 * <p>
	 * Format: FSUB fr, x, address[, i]
	 */
	public static final byte FSUB = 34;
	
	/**
	 * Vector add.
	 * <p>
	 * Format: VADD fr, x, address[, i]
	 */
	public static final byte VADD = 35;
	
	/**
	 * Vector subtract.
	 * <p>
	 * Format: VSUB fr, x, address[, i]
	 */
	public static final byte VSUB = 36;
	
	/**
	 * Convert to fixed/floating point
	 * <p>
	 * Format: CNVRT r, x, address[, i]
	 */
	public static final byte CNVRT = 37;
	
	/**
	 * Load index register from memory.
	 * <p>
	 * Format: LDX x, address[, i]
	 */
	public static final byte LDX = 41;
	
	/**
	 * Store index register to memory.
	 * <p>
	 * Format: STX x, address[, i]
	 */
	public static final byte STX = 42;
	
	/**
	 * Load floating register from memory.
	 * <p>
	 * Format: LDRF fr, x, address[, i]
	 */
	public static final byte LDFR = 50;
	
	/**
	 * Store floating register to memory.
	 * <p>
	 * Format: STFR fr, x, address[, i]
	 */
	public static final byte STFR = 51;
	
	/**
	 * Input character to register from device.
	 * <p>
	 * Format: IN r, devid
	 */
	public static final byte IN	= 61;
	
	/**
	 * Output character to device from register.
	 * <p>
	 * Format: OUT r, devid
	 */
	public static final byte OUT = 62;
	
	/**
	 * Check device status to register.
	 * <p>
	 * Format: CHK r, devid
	 */
	public static final byte CHK = 63;
}
