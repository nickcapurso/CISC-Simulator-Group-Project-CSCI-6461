package csci_6461_group_project;

/**
 * Lists the bit breakdowns (start and ending bits) for each of the different
 * instruction formats given in our project. The starting and ending constants can
 * be used with the BitSet's get method to break instructions into pieces.
 * <p>
 * @author Nick Capurso
 */
public class InstructionBitFormats {
	//Opcodes are always bits 0-5
	public static final byte OPCODE_START = 0;
	public static final byte OPCODE_END = 5;
	
	//Constants for the halt instruction's unique format.
	public static final byte HALT_SUFFIX_START = 12;
	public static final byte HALT_SUFFIX_END = 17;
	
	//Constants for the trap instruction's unique format.
	public static final byte TRAP_CODE_START = 12;
	public static final byte TRAP_CODE_END = 17;
	
	//Constants for the "Load/Store" format - also used for some arithmetic and jump instructions.
	public static final byte LD_STR_IX_START = 6;
	public static final byte LD_STR_IX_END = 7;
	public static final byte LD_STR_R_START = 8;
	public static final byte LD_STR_R_END = 9;
	public static final byte LD_STR_I_START = 10;
	public static final byte LD_STR_I_END = 10;
	public static final byte LD_STR_ADDR_START = 11;
	public static final byte LD_STR_ADDR_END = 17;
	
	//Constants for the arithmetic instructions that do register to register operations
	public static final byte XY_ARITH_RX_START = 6;
	public static final byte XY_ARITH_RX_END = 7;
	public static final byte XY_ARITH_RY_START = 8;
	public static final byte XY_ARITH_RY_END = 9;
	
	//Constants for the shift and rotate instructions
	public static final byte SHIFT_R_START = 8;
	public static final byte SHIFT_R_END = 9;
	public static final byte SHIFT_AL_START = 10;
	public static final byte SHIFT_AL_END = 10;
	public static final byte SHIFT_LR_START = 11;
	public static final byte SHIFT_LR_END = 11;
	public static final byte SHIFT_COUNT_START = 13;
	public static final byte SHIFT_COUNT_END = 17;
	
	//Constants for the IO instructions
	public static final byte IO_R_START = 8;
	public static final byte IO_R_END = 9;
	public static final byte IO_DEVID_START = 14;
	public static final byte IO_DEVID_END = 17;
}
