package csci_6461_group_project;

import java.util.*;

/*CPU class registers placed into a map. I think this implementation is hard to use...i'd prefer to use
 * the more verbose CPU class with many getters and setters.  */

public class CPU2 {
		
	private Map<String, BitSet> regMap = new HashMap<String, BitSet>();
	
	//Constructor
	public CPU2() {
			
		//4 General Purpose Registers(GPRs)		
		regMap.put("R0", new BitSet(18));
		regMap.put("R1", new BitSet(18));
		regMap.put("R2", new BitSet(18));
		regMap.put("R3", new BitSet(18));
		
		//3 Index Registers		
		regMap.put("X1", new BitSet(12));
		regMap.put("X2", new BitSet(12));
		regMap.put("X3", new BitSet(12));
		
		//Special registers
		regMap.put("PC", new BitSet(12));
		regMap.put("IR", new BitSet(18));
		regMap.put("CC", new BitSet(4));
		regMap.put("MAR", new BitSet(12));
		regMap.put("MBR", new BitSet(18));
		regMap.put("MSR", new BitSet(18));
		regMap.put("MFR", new BitSet(4));
	}
	
	//Sets a register with a BitSet value
	public void setReg(String regName, BitSet bitValue) {
		if (regMap.containsKey(regName)) {
			regMap.put(regName, bitValue);
		}
	}
	
	//Gets a specified register's value
	public BitSet getReg(String regName) {
		return regMap.get(regName);
	}

}
