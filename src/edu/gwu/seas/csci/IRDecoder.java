package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * Provides for a method to parse the IR according to the format of the current
 * opcode.
 * <p>
 * TODO: Test this class.
 * 
 * @author Nick Capurso
 *
 */
public class IRDecoder {
	// Constants for the different types of instruction formats
	private static final byte INSTRUCTION_FORMAT_HALT = 0;
	private static final byte INSTRUCTION_FORMAT_TRAP = 1;
	private static final byte INSTRUCTION_FORMAT_LD_STR = 2;
	private static final byte INSTRUCTION_FORMAT_XY_ARITH = 3;
	private static final byte INSTRUCTION_FORMAT_SHIFT = 4;
	private static final byte INSTRUCTION_FORMAT_IO = 5;

	/**
	 * Keeps a reference to the CPU object in order to call the instance's
	 * public methods (getters & setters).
	 */
	private final CPU cpu;

	private Context context = null;

	/**
	 * Single constructor. Takes an instance to the working CPU object.
	 * 
	 * @param cpu
	 *            The CPU object previously instantiated.
	 */
	public IRDecoder(CPU cpu) {
		this.cpu = cpu;
		context = Context.getInstance();
	}

	/**
	 * Parses a given instruction into its opcode and arguments - accordingly,
	 * these go in their special-purpose registers in the CPU object.
	 * 
	 * @param IR
	 *            The BitSet representing the contents of the Instruction
	 *            Register.
	 */
	public void parseIR(final Register IR) {
		String instruction_string;
		Context.InstructionClass instruction_class;
		BitSet opcode;
		
		// All instructions formats have the opcode in the first 6 bits
		opcode = IR.get(InstructionBitFormats.OPCODE_START, InstructionBitFormats.OPCODE_END + 1);
		cpu.setReg(CPU.OPCODE, opcode, InstructionBitFormats.OPCODE_SIZE);
		
		// Print IR and OPCODE to console
		Utils.BitSetToString(CPU.IR, IR, 18);
		Utils.BitSetToString(CPU.OPCODE, cpu.getReg(CPU.OPCODE), InstructionBitFormats.OPCODE_SIZE);
		
		// Get the instruction class for the current opcode
		instruction_string = context.getOpCodeStrings().get(
				Utils.convertToByte(opcode, InstructionBitFormats.OPCODE_SIZE));
		instruction_class = context.getOpcodeClasses().get(instruction_string);
		

		/**
		 * After determining the type of instruction format, break up the
		 * instruction to the appropriate special-purpose registers.
		 */
		// TODO Put all of the "IR.get(...)" into a setter method for the
		// different registers in the CPU.
		switch (instruction_class) {
		case HALT:
			// Halt instruction has a unique instruction format
			// IR.get(InstructionBitFormats.HALT_SUFFIX_START,
			// InstructionBitFormats.HALT_SUFFIX_END+1);
			break;
		case TRAP:
			// Trap instruction has a unique instruction format
			// IR.get(InstructionBitFormats.TRAP_CODE_START,
			// InstructionBitFormats.TRAP_CODE_END+1);
			break;
		case LD_STR:
			cpu.setReg(CPU.IX, IR.get(
					InstructionBitFormats.LD_STR_IX_START,
					InstructionBitFormats.LD_STR_IX_END + 1),
					InstructionBitFormats.LD_STR_IX_SIZE);
			
			Utils.BitSetToString(CPU.IX, cpu.getReg(CPU.IX), InstructionBitFormats.LD_STR_IX_SIZE);
			
			cpu.setReg(CPU.R, IR.get(
					InstructionBitFormats.LD_STR_R_START,
					InstructionBitFormats.LD_STR_R_END + 1),
					InstructionBitFormats.LD_STR_R_SIZE);
			
			Utils.BitSetToString(CPU.R, cpu.getReg(CPU.R), InstructionBitFormats.LD_STR_R_SIZE);
			
			cpu.setReg(CPU.I, IR.get(
					InstructionBitFormats.LD_STR_I_START,
					InstructionBitFormats.LD_STR_I_END + 1),
					InstructionBitFormats.LD_STR_I_SIZE);
			
			Utils.BitSetToString(CPU.I, cpu.getReg(CPU.I), InstructionBitFormats.LD_STR_I_SIZE);
			
			cpu.setReg(CPU.ADDR, IR.get(
					InstructionBitFormats.LD_STR_ADDR_START,
					InstructionBitFormats.LD_STR_ADDR_END + 1),
					InstructionBitFormats.LD_STR_ADDR_SIZE);
			
			Utils.BitSetToString(CPU.ADDR, cpu.getReg(CPU.ADDR), InstructionBitFormats.LD_STR_ADDR_SIZE);
			
			break;
		case LD_STR_IMD:
			break;
		case ARITH:
			// IR.get(InstructionBitFormats.XY_ARITH_RX_START,
			// InstructionBitFormats.XY_ARITH_RX_END+1);
			// IR.get(InstructionBitFormats.XY_ARITH_RY_START,
			// InstructionBitFormats.XY_ARITH_RY_END+1);
			break;
		case SHIFT:
			// IR.get(InstructionBitFormats.SHIFT_R_START,
			// InstructionBitFormats.SHIFT_R_END+1);
			// IR.get(InstructionBitFormats.SHIFT_AL_START,
			// InstructionBitFormats.SHIFT_AL_END+1);
			// IR.get(InstructionBitFormats.SHIFT_LR_START,
			// InstructionBitFormats.SHIFT_LR_END+1);
			// IR.get(InstructionBitFormats.SHIFT_COUNT_START,
			// InstructionBitFormats.SHIFT_COUNT_END+1);
			break;
		case IO:
			// IR.get(InstructionBitFormats.IO_R_START,
			// InstructionBitFormats.IO_R_END+1);
			// IR.get(InstructionBitFormats.IO_DEVID_START,
			// InstructionBitFormats.IO_DEVID_END+1);
			break;
		case LOGIC:
			break;
		case TRANS:
			break;
		default:
			break;
		}
	}

	/**
	 * Determines what format the current instruction is in (i.e. how it should
	 * be parsed) based on the passed opcode.
	 * 
	 * @param OPCODE
	 *            The current opcode.
	 * @return A constant corresponding to the instruction format.
	 */
	private byte determineInstructionFormat(final BitSet OPCODE) {
		byte instructionFormat = 0;

		// Need to convert the BitSet to a value that can be compared to
		// constants defined in OpCodesList.java
		final byte value = Utils.convertToByte(OPCODE,
				InstructionBitFormats.OPCODE_END
				- InstructionBitFormats.OPCODE_START + 1);

		if (value == OpCodesList.HLT) {
			// Halt instruction
			instructionFormat = INSTRUCTION_FORMAT_HALT;

		} else if (value == OpCodesList.TRAP) {
			// Trap instruction
			instructionFormat = INSTRUCTION_FORMAT_TRAP;

		} else if (value >= OpCodesList.LDR && value <= OpCodesList.SIR
				|| value == OpCodesList.LDX || value == OpCodesList.STX
				|| value >= OpCodesList.JZ && value <= OpCodesList.JGE) {
			// Load/store/arithmetic/jump instruction
			instructionFormat = INSTRUCTION_FORMAT_LD_STR;

		} else if (value >= OpCodesList.MLT && value <= OpCodesList.NOT) {
			// XY arithmetic instruction
			instructionFormat = INSTRUCTION_FORMAT_XY_ARITH;

		} else if (value == OpCodesList.SRC || value == OpCodesList.RRC) {
			// Shift or rotate instruction
			instructionFormat = INSTRUCTION_FORMAT_SHIFT;

		} else if (value >= OpCodesList.IN && value <= OpCodesList.CHK) {
			// I/O instruction
			instructionFormat = INSTRUCTION_FORMAT_IO;
		} else {
			// Need else if for float/vector instructions?
		}

		return instructionFormat;
	}
}
