package csci_6461_group_project;

import java.util.BitSet;

public class CPU extends Computer {
	
	//4 General Purpose Registers(GPRs)
	private BitSet R0 = new BitSet(18);
	private BitSet R1 = new BitSet(18);
	private BitSet R2 = new BitSet(18);
	private BitSet R3 = new BitSet(18);
	
	//3 Index Registers
	private BitSet X1 = new BitSet(12);
	private BitSet X2 = new BitSet(12);
	private BitSet X3 = new BitSet(12);
	
	//Special registers
	private BitSet PC = new BitSet(12);
	private BitSet CC = new BitSet(4);
	private BitSet IR = new BitSet(18);
	private BitSet MAR = new BitSet(12);
	private BitSet MBR = new BitSet(18);
	private BitSet MSR = new BitSet(18);
	private BitSet MFR = new BitSet(4);
	
	//Other registers we deemed necessary to add
	
}
