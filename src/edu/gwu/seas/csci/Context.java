/**
 * 
 */
package edu.gwu.seas.csci;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the application context.
 * 
 * @author Alex Remily
 */
public class Context {

    private static final Context CONTEXT = new Context();

    public enum InstructionClass {
	LOADSTORE, LOADSTOREIMMED, ARITHMETIC, TRANSFER, LOGICAL
    }

    private Map<String, InstructionClass> opcodeClasses = new HashMap<String, InstructionClass>();
    private Map<String, Byte> opCodesMap = new HashMap<String, Byte>();

    private Context() {
	opcodeClasses.put("AMR", InstructionClass.LOADSTORE);
	opcodeClasses.put("SMR", InstructionClass.LOADSTORE);
	opcodeClasses.put("AIR", InstructionClass.LOADSTOREIMMED);
	opcodeClasses.put("SIR", InstructionClass.LOADSTOREIMMED);

	opCodesMap.put("AMR", OpCodesList.AMR);
	opCodesMap.put("SMR", OpCodesList.SMR);
	opCodesMap.put("AIR", OpCodesList.AIR);
	opCodesMap.put("SIR", OpCodesList.SIR);
    }

    public static Context getInstance() {
	return CONTEXT;
    }

    /**
     * @return the opcodeClasses
     */
    public Map<String, InstructionClass> getOpcodeClasses() {
	return opcodeClasses;
    }

    /**
     * @return the opCodesMap
     */
    public Map<String, Byte> getOpCodesMap() {
	return opCodesMap;
    }
}
