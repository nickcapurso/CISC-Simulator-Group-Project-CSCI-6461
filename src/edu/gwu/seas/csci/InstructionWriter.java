/**
 * 
 */
package edu.gwu.seas.csci;

/**
 * Writes program instructions into {@link Word} objects in {@link Memory}.
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
    public void writeInstruction(Word word, byte opcode, byte general_register,
	    byte index_register, byte indirection, byte address) {
	setOpcode(word, opcode);
	setGeneralRegister(word, general_register);
	setIndexRegister(word, index_register);
	setIndirection(word, indirection);
	setAddress(word, address);
    }

    /**
     * Inserts the internal components of a {@link Word} in an
     * {@link Context.InstructionClass} LD_STR_IMD internal format.
     * 
     * @param word
     *            The word into which the elements will be inserted.
     * @param opcode
     *            The opcode value to insert into the word.
     * @param general_register
     *            The general register value to insert into the word.
     * @param address
     *            The address value to insert into the word.
     */
    public void writeInstruction(Word word, byte opcode, byte general_register,
	    byte address) {
	setOpcode(word, opcode);
	setGeneralRegister(word, general_register);
	setAddress(word, address);
    }

    /**
     * Sets the instruction opcode to bit positions 0-5.
     * 
     * @param word
     *            The unit of memory that will contain the general register
     *            value.
     * @param opcode
     */
    private void setOpcode(Word word, byte opcode) {
	Utils.byteToBitSetDeepCopy(opcode, word, (byte) 6, (byte) 5);
    }

    /**
     * @param word
     *            The unit of memory that will contain the general register
     *            value.
     * @param general_register
     *            The value of the general register to insert into the word.
     */
    private void setGeneralRegister(Word word, byte general_register) {
	Utils.byteToBitSetDeepCopy(general_register, word, (byte) 2, (byte) 7);
    }

    /**
     * @param word
     *            The unit of memory that will contain the general register
     *            value.
     * @param index_register
     */
    private void setIndexRegister(Word word, byte index_register) {
	Utils.byteToBitSetDeepCopy(index_register, word, (byte) 2, (byte) 9);
    }

    /**
     * @param word
     *            The unit of memory that will contain the general register
     *            value.
     * @param indirection
     */
    private void setIndirection(Word word, byte indirection) {
	Utils.byteToBitSetDeepCopy(indirection, word, (byte) 1, (byte) 10);
    }

    /**
     * @param word
     *            The unit of memory that will contain the general register
     *            value.
     * @param address
     */
    private void setAddress(Word word, byte address) {
	Utils.byteToBitSetDeepCopy(address, word, (byte) 8, (byte) 17);
    }
}
