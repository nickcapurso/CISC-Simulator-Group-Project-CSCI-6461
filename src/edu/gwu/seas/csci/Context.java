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
	LD_STR, LD_STR_IMD, TRANS, LOGIC, HALT, TRAP, ARITH, SHIFT, IO
    }

    private Map<String, InstructionClass> opcodeClasses = new HashMap<String, InstructionClass>();
    private Map<String, Byte> opCodesMap = new HashMap<String, Byte>();
    private Map<Byte, String> opCodeStrings = new HashMap<Byte, String>();

    private Context() {
	opcodeClasses.put("AMR", InstructionClass.LD_STR);
	opcodeClasses.put("SMR", InstructionClass.LD_STR);
	opcodeClasses.put("AIR", InstructionClass.LD_STR_IMD);
	opcodeClasses.put("SIR", InstructionClass.LD_STR_IMD);

	opCodesMap.put("AMR", OpCodesList.AMR);
	opCodesMap.put("SMR", OpCodesList.SMR);
	opCodesMap.put("AIR", OpCodesList.AIR);
	opCodesMap.put("SIR", OpCodesList.SIR);

	opCodeStrings.put(OpCodesList.AMR, "AMR");
	opCodeStrings.put(OpCodesList.SMR, "SMR");
	opCodeStrings.put(OpCodesList.AIR, "AIR");
	opCodeStrings.put(OpCodesList.SIR, "SIR");
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

    /**
     * @return the opCodeStrings
     */
    public Map<Byte, String> getOpCodeStrings() {
	return opCodeStrings;
    }
}
