package edu.gwu.seas.csci;

import java.util.BitSet;

/**
 * The IRDecoder class is responsible for parsing the contents of the 
 * IR into the special-purpose registers based on the opcode and the instruction 
 * bit format. It makes use of the Context and InstructionBitFormats classes to 
 * determine the correct bit format against which to parse the IR.
 * 
 * @author Nick Capurso
 */
public class IRDecoder {

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
			
			cpu.setReg(CPU.R, IR.get(
					InstructionBitFormats.LD_STR_R_START,
					InstructionBitFormats.LD_STR_R_END + 1),
					InstructionBitFormats.LD_STR_R_SIZE);
			
			cpu.setReg(CPU.I, IR.get(
					InstructionBitFormats.LD_STR_I_START,
					InstructionBitFormats.LD_STR_I_END + 1),
					InstructionBitFormats.LD_STR_I_SIZE);
			
			cpu.setReg(CPU.ADDR, IR.get(
					InstructionBitFormats.LD_STR_ADDR_START,
					InstructionBitFormats.LD_STR_ADDR_END + 1),
					InstructionBitFormats.LD_STR_ADDR_SIZE);
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
}
