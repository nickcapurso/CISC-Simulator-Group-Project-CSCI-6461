/**
 * 
 */
package edu.gwu.seas.csci;

/**
 * Writes program instructions into {@link Word} objects in {@link Memory}. Note
 * that there are versions of the instructions that identify different bit
 * locations for the index and general registers in the load and store
 * instructions. This implementation places the index register value in bits 6-7
 * and the general register value in bits 8-9. Bit counts are all zero-based.
 * 
 * @author Alex Remily
 */
public class InstructionWriter {

	/**
	 * Inserts the internal components of a {@link Word} in an
	 * {@link Context.InstructionClass} LD_STR internal format.
	 * 
	 * @param word
	 *            The word into which the elements will be inserted.
	 * @param opcode
	 *            The opcode value to insert into the word.
	 * @param general_register
	 *            The general register value to insert into the word.
	 * @param index_register
	 *            The index register value to insert into the word.
	 * @param indirection
	 *            The indirection value (0, 1) to insert into the word.
	 * @param address
	 *            The address value to insert into the word.
	 */
	public void writeLoadStoreFormatInstruction(Word word, byte opcode, byte general_register,
			byte index_register, byte indirection, byte address) {
		setOpcode(word, opcode);
		setGeneralRegister(word, general_register);
		setIndexRegister(word, index_register);
		setIndirection(word, indirection);
		setAddress(word, address);
	}
	
	/**
	 *  Inserts the internal components of a {@link Word} in an
	 *  {@link Context.InstructionClass} XY_ARITH internal format.
	 *  
	 * @param word
	 *            The word into which the elements will be inserted.
	 * @param opcode
	 *            The opcode value to insert into the word.
	 * @param register_x
	 * 			  The first register operand.
	 * @param register_y
	 * 			  The second register operand.
	 */
	public void writeXYArithInstruction(Word word, byte opcode, byte register_x, byte register_y){
		setOpcode(word, opcode);
		setXRegister(word, register_x);
		setYRegister(word, register_y);
	}
	
	/**
	 * Inserts the internal components of a {@link Word} in an
	 *  {@link Context.InstructionClass} SHIFT internal format.
	 *  
	 * @param word
	 *            The word into which the elements will be inserted.
	 * @param opcode
	 *            The opcode value to insert into the word.
	 * @param general_register
	 * 			  The general register value to insert into the word.
	 * @param count
	 * 			  The amount to shift by.
	 * @param lr
	 * 			  Indicates left or right shift.
	 * @param al
	 * 			  Indicates arithmetic or logical shift.
	 */
	public void writeShiftInstruction(Word word, byte opcode, byte general_register, byte count, byte lr, byte al){
		setOpcode(word, opcode);
		setGeneralRegister(word, general_register);
		setCount(word, count);
		setLR(word, lr);
		setAL(word, al);
	}

	/**
	 * Puts the instruction opcode in bit positions 0-5.
	 * 
	 * @param word
	 *            The unit of memory that will contain the general register
	 *            value.
	 * @param opcode
	 *            The binary value of the opcode to insert into the word at
	 *            positions 0-5.
	 */
	private void setOpcode(Word word, byte opcode) {
		Utils.byteToBitSetDeepCopy(opcode, word, (byte) 6, (byte) 5);
	}

	/**
	 * Puts the general register value in bit positions 8-9.
	 * 
	 * @param word
	 *            The unit of memory that will contain the general register
	 *            value.
	 * @param general_register
	 *            The value of the general register to insert into the word at
	 *            positions 8-9.
	 */
	private void setGeneralRegister(Word word, byte general_register) {
		Utils.byteToBitSetDeepCopy(general_register, word, (byte) 2, (byte) 9);
	}

	/**
	 * Puts the index register value in bit positions 6-7.
	 * 
	 * @param word
	 *            The unit of memory that will contain the general register
	 *            value.
	 * @param index_register
	 *            The value of the index register to insert into the word at
	 *            positions 6-7.
	 */
	private void setIndexRegister(Word word, byte index_register) {
		Utils.byteToBitSetDeepCopy(index_register, word, (byte) 2, (byte) 7);
	}

	/**
	 * Puts the indirection bit flag in bit position 10.
	 * 
	 * @param word
	 *            The unit of memory that will contain the general register
	 *            value.
	 * @param indirection
	 *            The value of the indirection bit to set at position 10.
	 */
	private void setIndirection(Word word, byte indirection) {
		Utils.byteToBitSetDeepCopy(indirection, word, (byte) 1, (byte) 10);
	}

	/**
	 * Puts the binary address value in bit positions 11-17.
	 * 
	 * @param word
	 *            The unit of memory that will contain the general register
	 *            value.
	 * @param address
	 *            The value of the address to set at position 11-17.
	 */
	private void setAddress(Word word, byte address) {
		Utils.byteToBitSetDeepCopy(address, word, (byte) 8, (byte) 17);
	}
	
	/**
	 * Puts the first register operand in bit positions 6-7
	 * 
	 * @param word
	 * 			  The unit of memory.
	 * @param register_x
	 * 			  The first register operand.
	 */
	private void setXRegister(Word word, byte register_x){
		Utils.byteToBitSetDeepCopy(register_x, word, InstructionBitFormats.XY_ARITH_RX_SIZE, InstructionBitFormats.XY_ARITH_RX_END);
	}
	
	/**
	 * Puts the first register operand in bit positions 8-9
	 * 
	 * @param word
	 * 			  The unit of memory.
	 * @param register_x
	 * 			  The second register operand.
	 */
	private void setYRegister(Word word, byte register_y){
		Utils.byteToBitSetDeepCopy(register_y, word, InstructionBitFormats.XY_ARITH_RY_SIZE, InstructionBitFormats.XY_ARITH_RY_END);
	}
	
	/**
	 * Puts the LR operand in bit position 11
	 * @param word
	 * 			  The unit of memory.
	 * @param lr
	 * 			  Indicates left or right shift.
	 */
	private void setLR(Word word, byte lr){
		Utils.byteToBitSetDeepCopy(lr, word, InstructionBitFormats.SHIFT_LR_SIZE, InstructionBitFormats.SHIFT_LR_END);
	}
	
	/**
	 * Puts the AL operand in bit position 10.
	 * @param word
	 * 			  The unit of memory.
	 * @param al
	 * 			  Indicates arithmetic or logical shift.
	 */
	private void setAL(Word word, byte al){
		Utils.byteToBitSetDeepCopy(al, word, InstructionBitFormats.SHIFT_AL_SIZE, InstructionBitFormats.SHIFT_AL_END);
	}
	
	/**
	 * Puts the COUNT operand in bit positions 13-17
	 * @param word
	 * @param count
	 */
	private void setCount(Word word, byte count){
		Utils.byteToBitSetDeepCopy(count, word, InstructionBitFormats.SHIFT_COUNT_SIZE, InstructionBitFormats.SHIFT_COUNT_END);
	}
}
