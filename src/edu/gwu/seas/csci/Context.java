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
	LOADSTORE, ARITHMETIC, TRANSFER, LOGICAL
    }

    private Map<String, InstructionClass> opcodeClasses = new HashMap<String, InstructionClass>();
    private Map<String, Byte> opCodesMap = new HashMap<String, Byte>();

    private Context() {
	opcodeClasses.put("AMR", InstructionClass.LOADSTORE);
	opcodeClasses.put("SMR", InstructionClass.LOADSTORE);
	opcodeClasses.put("AIR", InstructionClass.LOADSTORE);
	opcodeClasses.put("SIR", InstructionClass.LOADSTORE);

	opCodesMap.put("AMR", OpCodesList.AMR);
	opCodesMap.put("SMR", OpCodesList.AMR);
	opCodesMap.put("AIR", OpCodesList.AMR);
	opCodesMap.put("SIR", OpCodesList.AMR);
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
     * @param opcodeClasses
     *            the opcodeClasses to set
     */
    public void setOpcodeClasses(Map<String, InstructionClass> opcodeClasses) {
	this.opcodeClasses = opcodeClasses;
    }

    /**
     * @return the opCodesMap
     */
    public Map<String, Byte> getOpCodesMap() {
	return opCodesMap;
    }

    /**
     * @param opCodesMap
     *            the opCodesMap to set
     */
    public void setOpCodesMap(Map<String, Byte> opCodesMap) {
	this.opCodesMap = opCodesMap;
    }
}
