package csci_6461_group_project;

import java.util.BitSet;

/**
 * Provides for a method to parse the IR according to the format of the current opcode.
 * <p>
 * @author Nick Capurso
 *
 */
public class IRDecoder {
	//Constants for the different types of instruction formats
	private static final byte INSTRUCTION_FORMAT_HALT = 0;
	private static final byte INSTRUCTION_FORMAT_TRAP = 1;
	private static final byte INSTRUCTION_FORMAT_LD_STR = 2;
	private static final byte INSTRUCTION_FORMAT_XY_ARITH = 3;
	private static final byte INSTRUCTION_FORMAT_SHIFT = 4;
	private static final byte INSTRUCTION_FORMAT_IO = 5;
	
	/**
	 * Keeps a reference to the CPU object in order to call the instance's public methods (getters & setters).
	 */
	private final CPU2 CPU;
	
	/**
	 * Single constructor. Takes an instance to the working CPU object.
	 * @param cpu The CPU object previously instantiated.
	 */
	public IRDecoder(CPU2 cpu){
		CPU = cpu;
	}
	
	/**
	 * Parses a given instruction into its opcode and arguments - accordingly, these go in 
	 * their special-purpose registers in the CPU object.
	 * @param IR The BitSet representing the contents of the Instruction Register.
	 */
	public void parseIR(final BitSet IR){
		final BitSet OPCODE = IR.get(InstructionBitFormats.OPCODE_START, InstructionBitFormats.OPCODE_END+1);
		final byte instructionFormat = determineInstructionFormat(OPCODE);
		
		//All instructions formats have the opcode in the first 6 bits
		CPU.setReg(CPU2.OPCODE,
			IR.get(InstructionBitFormats.OPCODE_START, InstructionBitFormats.OPCODE_END+1));
		
		/**
		 * After determining the type of instruction format, break up the instruction to 
		 * the appropriate special-purpose registers.
		 */
		//TODO Put all of the "IR.get(...)" into a setter method for the different registers in the CPU.
		switch(instructionFormat){
		case INSTRUCTION_FORMAT_HALT:
			//Halt instruction has a unique instruction format
			//IR.get(InstructionBitFormats.HALT_SUFFIX_START, InstructionBitFormats.HALT_SUFFIX_END+1);
			break;
		case INSTRUCTION_FORMAT_TRAP:
			//Trap instruction has a unique instruction format
			//IR.get(InstructionBitFormats.TRAP_CODE_START, InstructionBitFormats.TRAP_CODE_END+1);
			break;
		case INSTRUCTION_FORMAT_LD_STR:
			CPU.setReg(CPU2.IX, 
					IR.get(InstructionBitFormats.LD_STR_IX_START, InstructionBitFormats.LD_STR_IX_END+1));
			CPU.setReg(CPU2.R, 
					IR.get(InstructionBitFormats.LD_STR_R_START, InstructionBitFormats.LD_STR_R_END+1));	
			CPU.setReg(CPU2.I, 
					IR.get(InstructionBitFormats.LD_STR_I_START, InstructionBitFormats.LD_STR_I_END+1));
			CPU.setReg(CPU2.ADDR,
					IR.get(InstructionBitFormats.LD_STR_ADDR_START, InstructionBitFormats.LD_STR_ADDR_END+1));
			break;
		case INSTRUCTION_FORMAT_XY_ARITH:
			//IR.get(InstructionBitFormats.XY_ARITH_RX_START, InstructionBitFormats.XY_ARITH_RX_END+1);
			//IR.get(InstructionBitFormats.XY_ARITH_RY_START, InstructionBitFormats.XY_ARITH_RY_END+1);
			break;
		case INSTRUCTION_FORMAT_SHIFT:
			//IR.get(InstructionBitFormats.SHIFT_R_START, InstructionBitFormats.SHIFT_R_END+1);
			//IR.get(InstructionBitFormats.SHIFT_AL_START, InstructionBitFormats.SHIFT_AL_END+1);
			//IR.get(InstructionBitFormats.SHIFT_LR_START, InstructionBitFormats.SHIFT_LR_END+1);
			//IR.get(InstructionBitFormats.SHIFT_COUNT_START, InstructionBitFormats.SHIFT_COUNT_END+1);
			break;
		case INSTRUCTION_FORMAT_IO:
			//IR.get(InstructionBitFormats.IO_R_START, InstructionBitFormats.IO_R_END+1);
			//IR.get(InstructionBitFormats.IO_DEVID_START, InstructionBitFormats.IO_DEVID_END+1);
			break;
		}
	}

	
	/**
	 * Determines what format the current instruction is in (i.e. how it should be parsed) based on the passed opcode.
	 * @param OPCODE The current opcode.
	 * @return A constant corresponding to the instruction format.
	 */
	private byte determineInstructionFormat(final BitSet OPCODE){
		byte instructionFormat = 0;
		
		//Need to convert the BitSet to a value that can be compared to constants defined in OpCodesList.java
		final byte value = convertToByte(OPCODE, InstructionBitFormats.OPCODE_END - InstructionBitFormats.OPCODE_START + 1);
		
		if(value == OpCodesList.HLT){
			//Halt instruction
			instructionFormat = INSTRUCTION_FORMAT_HALT;
			
		}else if(value == OpCodesList.TRAP){
			//Trap instruction
			instructionFormat = INSTRUCTION_FORMAT_TRAP;
			
		}else if(value >= OpCodesList.LDR && value <= OpCodesList.SIR 
				|| value == OpCodesList.LDX || value == OpCodesList.STX
				|| value >= OpCodesList.JZ && value <= OpCodesList.JGE){
			//Load/store/arithmetic/jump instruction
			instructionFormat = INSTRUCTION_FORMAT_LD_STR;
			
		}else if(value >= OpCodesList.MLT && value <= OpCodesList.NOT){
			//XY arithmetic instruction
			instructionFormat = INSTRUCTION_FORMAT_XY_ARITH;
			
		}else if(value == OpCodesList.SRC || value == OpCodesList.RRC){
			//Shift or rotate instruction
			instructionFormat = INSTRUCTION_FORMAT_SHIFT;
			
		}else if(value >= OpCodesList.IN && value <= OpCodesList.CHK){
			//I/O instruction
			instructionFormat = INSTRUCTION_FORMAT_IO;
		}else{
			//Need else if for float/vector instructions?
		}
		
		return instructionFormat;
	}
	
	/**
	 * Converts a BitSet to its numeric equivalent, stored in a byte. The return value can be
	 * used for numeric based comparisons.
	 * 
	 * @param set The BitSet to be converted.
	 * @param numBits The number of bits in the BitSet (BitSet.length method is not sufficient due to its implementation).
	 * @return The numeric value represented by the BitSet.
	 */
	private static byte convertToByte(final BitSet set, final int numBits){
		byte value = 0;

		for(int i = numBits-1; i >= 0; i --)
			value += set.get(i) ? (byte)(1 << (numBits-1-i)) : 0;		
		
		//System.out.println("Value: " + value);
		return value;
	}
}
