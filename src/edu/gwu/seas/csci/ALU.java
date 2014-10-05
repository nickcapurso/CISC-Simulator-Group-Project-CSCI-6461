package edu.gwu.seas.csci;

//import java.util.ArrayList;
import java.util.BitSet;
//import java.util.List;

/**
 * The ALU class contains the implementation for the arithmetical and logical instructions.
 */

public class ALU implements CPUConstants{
	
	private static CPU cpu = null;

	
	/**
	 * Constructor requires an instance of the CPU class.
	 * @param CPU this
	 */
	public ALU(CPU cpu1) {
		cpu = cpu1;
	}
	
	/**
	 * Sets the Condition Code register (CC) based off of the results of a an ALU instruction.  Condition codes may
	 * be set due to overflow, underflow, division by zero, or equal-or-not.  The current implementation assumes that only 1
	 * condition code can be set at a time.
	 */
	
	private static void setCC(int code) {
		
		//clear out any old CC values
		cpu.getReg(CC).clear();
		
		int numBits = cpu.getReg(CC).getNumBits();
		BitSet ccValue = new BitSet(numBits);
		
		if (code == OVERFLOW) {
			ccValue.set(0);
		} else if (code == UNDERFLOW) {
			ccValue.set(1);
		} else if (code == DIVZERO) {
			ccValue.set(2);
		} else if (code == EQUALORNOT) {
			ccValue.set(3);
		}
		
		cpu.setReg(CC, ccValue, numBits);
		
	}
	
	/**
	 * Checks if overflow has occurred by comparing the number of bits in the original value versus the result value.
	 * If the number of bits has decreased after doing addition, then it can be assumed that overflow has occurred.
	 * @param int before
	 * @param int after
	 */
	private static void checkOverflow(int before, int after) {
		BitSet orig = new BitSet();
		BitSet result = new BitSet();
		
		orig = Utils.intToBitSet(before, DEFAULT_BIT_SIZE);
		result = Utils.intToBitSet(after, DEFAULT_BIT_SIZE);

		System.out.printf("before value: %d\n", before);
		System.out.printf("before value as bitset %s\n", orig.toString());
		
		System.out.printf("after value: %d\n", after);
		System.out.printf("after value as bitset %s\n", result.toString());
		
		System.out.printf("before has a bit size of %d\n", orig.length());
		System.out.printf("after has a bit size of %d\n", result.length());
		
		if (orig.length() > result.length()) {
			setCC(OVERFLOW);
		}
	}
	
	/**
	 * Checks if underflow has occurred by comparing the number of bits in the original value versus the result value.
	 * If the number of bits has increased after doing subtraction, then it can be assumed that underflow has occurred.
	 * @param int before
	 * @param int after
	 */
	private static void checkUnderflow(int before, int after) {
		BitSet orig = new BitSet();
		BitSet result = new BitSet();
		
		orig = Utils.intToBitSet(before, DEFAULT_BIT_SIZE);
		result = Utils.intToBitSet(after, DEFAULT_BIT_SIZE);
		
		if (orig.length() < result.length()) {
			setCC(UNDERFLOW);
		}	
	}
	
	/*Arithmetic Unit */
	 
	
	private static void add(int valToBeAdded) {
		//As per pg. 9 of the Project document, if the immediate value is 0, then no arithmetic is computed.
		if (valToBeAdded == 0) {
			cpu.setReg(RESULT, cpu.getReg(OP1));
			return;
		}
		
		Register op1 = cpu.getReg(OP1);
		int regValue = Utils.convertToInt(op1, op1.getNumBits());
		int newRegValue = valToBeAdded + regValue;
		
		checkOverflow(regValue, newRegValue);
		
		BitSet newVal = new BitSet();
		newVal = Utils.intToBitSet(newRegValue, op1.getNumBits());
		
		//Set RESULT register with sum
		cpu.setReg(RESULT, newVal, 18);
	}
	
	private static void subtract(int valToBeSubtracted) {
		//As per pg. 9 of the Project document, if the immediate value is 0, then no arithmetic is computed.
		if (valToBeSubtracted == 0) {
			cpu.setReg(RESULT, cpu.getReg(OP1));
			return;
		}
		
		Register dest = cpu.getReg(OP1);
		int regValue = Utils.convertToInt(dest, dest.getNumBits());
		int newRegValue = regValue - valToBeSubtracted;
		
		checkUnderflow(regValue, newRegValue);
		
		BitSet newVal = new BitSet();
		newVal = Utils.intToBitSet(newRegValue, dest.getNumBits());
		
		//Set RESULT register with difference
		cpu.setReg(RESULT, newVal, 18);
	}
	
	/** 
	 * Add immediate to register.
	 */
	public void AIR() {
		Register i = cpu.getReg(OP2);
		int immedVal = Utils.convertToInt(i, i.getNumBits());
		
		add(immedVal);
	}
	
	/**
	 * Add value in memory to register.
	 */
	public void AMR() {
		AIR();
	}

	/**
	 * Subtract immediate from register.
	 */
	public void SIR() {
		Register i = cpu.getReg(OP2);
		int immedVal = Utils.convertToInt(i, i.getNumBits());
		
		subtract(immedVal);
	}
	public void SMR() {
		SIR();
	}

	
	/** 
	 * Multiply register by register. OP1 and OP2 should hold the register values 
	 * to multiple.  RESULT then holds the high order bits and RESULT2 contains
	 * the low order bits.
	 */
	public void MLT() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		
		int op1Val = Utils.convertToInt(op1, op1.getNumBits());
		int op2Val = Utils.convertToInt(op2, op2.getNumBits());
		
		long result = Integer.toUnsignedLong(op1Val) * Integer.toUnsignedLong(op2Val);
		
		//maybe i can keep shifting left until i hit a number....
		while (result == (result >>> 1)) {
			result >>>= 1;
		}
		
		long highestBitVal = Long.highestOneBit(result);
		int resultSize = 0;
		
		while (highestBitVal != 0 && highestBitVal != 1) {
			highestBitVal >>>= 1;
			resultSize++;
		}
		
		
		int maxBitSize = DEFAULT_BIT_SIZE * 2;

		
		while (resultSize > maxBitSize) {
			result >>>= 1;
			resultSize = (int)Long.highestOneBit(result);
		}
		
		//determine high/low bit separation
		long highBits = 0;
		long lowBits = 0;
		
		if (resultSize < DEFAULT_BIT_SIZE) {
			lowBits = 0;
			highBits = result;
		} else {
			highBits = (result >>> DEFAULT_BIT_SIZE);
			lowBits = (result << 64 - DEFAULT_BIT_SIZE);
		}
				
		BitSet high = Utils.intToBitSet((int)highBits, DEFAULT_BIT_SIZE);
		BitSet low = Utils.intToBitSet((int)lowBits, DEFAULT_BIT_SIZE);
		
		cpu.setReg(RESULT, high, DEFAULT_BIT_SIZE);
		cpu.setReg(RESULT2, low, DEFAULT_BIT_SIZE);
		
		//BitSet totalResult = Utils.longToBitSet(result, newBitSize);
		
		//cpu.setReg(destName, sourceSet, sourceBits);
		
		
		
		/*
		//Attempt 2: binary multiplication
		
		List<BitSet> addList = new ArrayList<BitSet>();
		BitSet finalResult = new BitSet();
		
		int op2Length = op2.length();
		int op1Length = op1.length();
		
		for (int i = 0; i < op2Length; i++) {
			//Create and fill a new bitset for each index in the op2 binary array that is multiplied against op1
			BitSet b = new BitSet();
			
			//Multiply op2[i] * op1.  Store product in b.
			//int carry = 0;
			
			for (int j = 0; j < op1Length; j++) {
				if (op1.get(j) && op2.get(i)) { //1 * 1
					b.set(i + j, true);
				} else {
					b.set(i + j, false);
				}
			}
			
			addList.add(b);
		}
		
		//After list of all product bitsets is generated...they are added together.
		
		//One addition operand is removed from the list		
		if (!addList.isEmpty()) {
			finalResult = addList.get(0);
			addList.remove(0);
		}
		
		
		while (!addList.isEmpty()) {
			for (BitSet b : addList) {
				//Add 2 numbers together and remove b from list
			}
		}
		
		*/
	
	}
	
	/* Division of register by register.  OP1 should contain the dividend and OP2 the divisor.
	 * After the operation, RESULT will contain the quotient and RESULT2 the remainder.  */
	public void DVD() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		
		int op1Val = Utils.convertToInt(op1, op1.getNumBits());
		int op2Val = Utils.convertToInt(op2, op2.getNumBits());
		
		if (op2Val == 0) {
			setCC(DIVZERO);
			return;
		}
		
		int quotient = op1Val / op2Val;
		int remainders = op1Val % op2Val;
		
		BitSet quotSet = Utils.intToBitSet(quotient, DEFAULT_BIT_SIZE);
		BitSet remainSet = Utils.intToBitSet(remainders, DEFAULT_BIT_SIZE);
		
		cpu.setReg(RESULT, quotSet, DEFAULT_BIT_SIZE);
		cpu.setReg(RESULT2, remainSet, DEFAULT_BIT_SIZE);
		
	}
	
	

	
	/* Logical Unit */
	
	/**
	 * Equality test of registers 
	 */
	public void TRR() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		
		int op1Val = Utils.convertToInt(op1, op1.getNumBits());
		int op2Val = Utils.convertToInt(op2, op2.getNumBits());
		
		if (op1Val == op2Val) {
			setCC(EQUALORNOT);
		}
		
	}


	/**
	 * Executes the specified bitwise operation on OP1 and OP2.
	 * @param op
	 */
	private static void bitwiseOp(String op) {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		
		if (op.matches(AND_OP)) {
			//Performs logical AND and stores result in OP1
			op1.and(op2);
		} else if (op.matches(OR_OP)) {
			//Performs logical OR and stores result in OP1
			op1.or(op2);
		}
		
		cpu.setReg(RESULT, op1);
	}
	
	/**
	 * Logical AND of Register and Register
	 */
	public void AND() {
		bitwiseOp(AND_OP);
	}

	/**
	 * Logical OR of Register and Register
	 */
	public void ORR() {
		bitwiseOp(OR_OP);
	}

	/**
	 * Logical NOT of register OP1
	 */
	public void NOT() {
		Register op1 = cpu.getReg(OP1);
		int regSize = op1.getNumBits();
		
		op1.flip(0, regSize - 1);
		
		cpu.setReg(RESULT, op1);
	}
	
	/* Shifter */
	
	/**
	 * Shifts the contents of a register.  The value to be shifted is stored in OP1.  The number of shifts is in OP2.
	 * If left shifting, then OP3 should hold a value of 1.  If right shifting is specified, then OP3 should be empty.
	 * If logically shifting, then OP4 should have a value of 1.  If arithmetic shifting, OP4 should be empty.
	 */
	public void SRC() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		Register op3 = cpu.getReg(OP3);
		Register op4 = cpu.getReg(OP4);
		
		Boolean left_shift = true;
		Boolean logical_shift = true;
		int regSize = op1.getNumBits();
		int origVal = Utils.convertToInt(op1, regSize);
		int count = Utils.convertToInt(op2, op2.getNumBits());
		
		if (op3.isEmpty()) {
			left_shift = false;
		}
		if (op4.isEmpty()) {
			logical_shift = false;
		}
		
		
		int finalVal = 0;
		
		
		if (left_shift) { //Left shift is same for both arith and logical
			finalVal = origVal << count;
		} else if (logical_shift) { //Right shift filling in zeros from leftmost side.
			finalVal = origVal >>> count;
		} else if (!logical_shift) { //Right shift fills in same as sign bit from leftmost side.
			finalVal = origVal >> count;
		}
		
		BitSet resultVal = Utils.intToBitSet(finalVal, regSize);
		
		cpu.setReg(RESULT, resultVal, regSize);
		
		
		
		// part of attempt when considering implementation with for loops...
		/*
		if (left_shift) {
			for (int i = 0; i < regSize; i++) {
				int newIndex = i + count;
				
				if (newIndex >= (regSize - 1) ) {
					continue; //These bits have been shifted out of scope for the register.
				} else {
					if (op1.get(i)) {
						resultVal.set(bitIndex);
					}
				}
			}
		}
		*/

		
	}
	/**
	 * Rotates the contents of a register.  The value to be shifted is stored in OP1.  The number of shifts is in OP2.
	 * If left shifting, then OP3 should hold a value of 1.  If right shifting is specified, then OP3 should be empty.
	 * If logically shifting, then OP4 should have a value of 1.  If arithmetic shifting, OP4 should be empty.
	 */
	public void RRC() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		Register op3 = cpu.getReg(OP3);
		Register op4 = cpu.getReg(OP4);
		
		Boolean left_shift = true;
		Boolean logical_shift = true;
		int regSize = op1.getNumBits();
		int origVal = Utils.convertToInt(op1, regSize);
		int count = Utils.convertToInt(op2, op2.getNumBits());
		int result = 0;
		
		if (op3.isEmpty()) {
			left_shift = false;
		}
		if (op4.isEmpty()) {
			logical_shift = false;
		}
		
		//Rotate bits
		if (left_shift) {
			result = (origVal << count) | (origVal >> (DEFAULT_BIT_SIZE - count));
		} else {
			result = (origVal >> count) | (origVal << (DEFAULT_BIT_SIZE - count));
		}
		
		BitSet resultVal = Utils.intToBitSet(result, regSize);
		cpu.setReg(RESULT, resultVal, regSize);
		
		
		/*
		//manual rotation
		BitSet resultVal = new BitSet(regSize);
		
		if (left_shift) {
			//copy orig right half
			for (int i = 0; i < regSize - 1 - count; i++) {
				resultVal.set(i + count, op1.get(i));
			}
			//copy orig left half
			for (int i = 0; i < count; i++) {
				resultVal.set(i, op1.get(regSize - count));
			}
		}
		*/
		
	}
	
	/**
	 * Greater than or equal comparison.  Given the contents of OP1 and OP2, a greater than or equal to check
	 * is performed (OP1 >= OP2).  If the check is true, then RESULT will contain a positive value.  If false, then the
	 * result will contain the value 0.
	 */
	
	public void GTE() {
		Register op1 = cpu.getReg(OP1);
		Register op2 = cpu.getReg(OP2);
		
		int op1Val = Utils.convertToInt(op1, op1.getNumBits());
		int op2Val = Utils.convertToInt(op2, op2.getNumBits());
		
		BitSet result = new BitSet(DEFAULT_BIT_SIZE);
		
		if (op1Val >= op2Val) {
			result.set(0);
		}
		
		cpu.setReg(RESULT, result, DEFAULT_BIT_SIZE);
	}
	
	
	

}

