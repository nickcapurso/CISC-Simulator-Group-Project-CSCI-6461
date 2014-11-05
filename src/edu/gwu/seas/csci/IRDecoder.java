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
				Utils.convertToUnsignedByte(opcode, InstructionBitFormats.OPCODE_SIZE));
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
		case TRANS:
		case ARITH:
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

		case XY_ARITH_LOGIC:
			cpu.setReg(CPU.RX, IR.get(
					InstructionBitFormats.XY_ARITH_RX_START,
					InstructionBitFormats.XY_ARITH_RX_END+1),
					InstructionBitFormats.XY_ARITH_RX_SIZE);
			
			cpu.setReg(CPU.RY, IR.get(
					InstructionBitFormats.XY_ARITH_RY_START, 
					InstructionBitFormats.XY_ARITH_RY_END+1),
					InstructionBitFormats.XY_ARITH_RY_SIZE);
			break;
			
		case SHIFT:
			cpu.setReg(CPU.R, IR.get(
					InstructionBitFormats.SHIFT_R_START, 
					InstructionBitFormats.SHIFT_R_END+1),
					InstructionBitFormats.SHIFT_R_SIZE);
			
			cpu.setReg(CPU.AL, IR.get(
					InstructionBitFormats.SHIFT_AL_START, 
					InstructionBitFormats.SHIFT_AL_END+1),
					InstructionBitFormats.SHIFT_AL_SIZE);
			
			cpu.setReg(CPU.LR, IR.get(
					InstructionBitFormats.SHIFT_LR_START, 
					InstructionBitFormats.SHIFT_LR_END+1),
					InstructionBitFormats.SHIFT_LR_SIZE);
			
			cpu.setReg(CPU.COUNT, IR.get(
					InstructionBitFormats.SHIFT_COUNT_START, 
					InstructionBitFormats.SHIFT_COUNT_END+1),
					InstructionBitFormats.SHIFT_COUNT_SIZE);
			break;
		case IO:
			cpu.setReg(CPU.R, IR.get(
					InstructionBitFormats.IO_R_START,
					InstructionBitFormats.IO_R_END+1),
					InstructionBitFormats.IO_R_SIZE);
			cpu.setReg(CPU.DEVID, IR.get(
					InstructionBitFormats.IO_DEVID_START,
					InstructionBitFormats.IO_DEVID_END+1),
					InstructionBitFormats.IO_DEVID_SIZE);
			break;
		default:
			
			/*
			 * Illegal opcode has occurred
			 * 
			 * Registers PC and MSR are saved to memory. Next, the fault error routine.
			 */
			
			Word pc = Utils.registerToWord(cpu.getReg(CPU.PC), 12);
			cpu.writeToMemory(pc, 4);
			Word msr = Utils.registerToWord(cpu.getReg(CPU.PC), 18);
			cpu.writeToMemory(msr, 5);
			
			//Change PC to fault error routine
			Word faultRoutine = cpu.readFromMemory(1);
			cpu.setReg(CPU.PC, faultRoutine); //Is this ok...just truncate least important?
			
			//Execute fault error routine
			cpu.executeInstruction("continue");
			
			break;
		}
	}
}
