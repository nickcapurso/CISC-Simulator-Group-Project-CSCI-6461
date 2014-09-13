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

    /**
     * Enumerates the classes of OPCODE supported by the Computer.
     * 
     * @author Alex Remily
     */
    public enum InstructionClass {
	LD_STR, LD_STR_IMD, TRANS, LOGIC, HALT, TRAP, ARITH, SHIFT, IO
    }

    private Map<String, InstructionClass> opcodeClasses = new HashMap<String, InstructionClass>();
    private Map<String, Byte> opCodeBytes = new HashMap<String, Byte>();
    private Map<Byte, String> opCodeStrings = new HashMap<Byte, String>();

    private Context() {
    	opcodeClasses.put("HLT", InstructionClass.HALT);
    opcodeClasses.put("LDR", InstructionClass.LD_STR);
    opcodeClasses.put("STR", InstructionClass.LD_STR);
    opcodeClasses.put("LDA", InstructionClass.LD_STR);
    opcodeClasses.put("LDX", InstructionClass.LD_STR);
    opcodeClasses.put("STX", InstructionClass.LD_STR);
	opcodeClasses.put("AMR", InstructionClass.LD_STR);
	opcodeClasses.put("SMR", InstructionClass.LD_STR);
	opcodeClasses.put("AIR", InstructionClass.LD_STR_IMD);
	opcodeClasses.put("SIR", InstructionClass.LD_STR_IMD);

	opCodeBytes.put("HLT", OpCodesList.HLT);
	opCodeBytes.put("LDR", OpCodesList.LDR);
	opCodeBytes.put("STR", OpCodesList.STR);
	opCodeBytes.put("LDA", OpCodesList.LDA);
	opCodeBytes.put("LDX", OpCodesList.LDX);
	opCodeBytes.put("STX", OpCodesList.STX);
	opCodeBytes.put("AMR", OpCodesList.AMR);
	opCodeBytes.put("SMR", OpCodesList.SMR);
	opCodeBytes.put("AIR", OpCodesList.AIR);
	opCodeBytes.put("SIR", OpCodesList.SIR);

	opCodeStrings.put(OpCodesList.HLT, "HLT");
	opCodeStrings.put(OpCodesList.LDR, "LDR");
	opCodeStrings.put(OpCodesList.STR, "STR");
	opCodeStrings.put(OpCodesList.LDA, "LDA");
	opCodeStrings.put(OpCodesList.LDX, "LDX");
	opCodeStrings.put(OpCodesList.STX, "STX");
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
     * @return the opCodeStrings
     */
    public Map<Byte, String> getOpCodeStrings() {
	return opCodeStrings;
    }

    /**
     * @return the opCodeBytes
     */
    public Map<String, Byte> getOpCodeBytes() {
	return opCodeBytes;
    }
}
