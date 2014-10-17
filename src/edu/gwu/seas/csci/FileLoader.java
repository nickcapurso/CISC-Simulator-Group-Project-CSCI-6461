/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads the contents of a file from disk and loads it into {@link Memory}. The
 * file is expected to contain instructions that constitute a program, with one
 * instruction per line, and elements of the instruction separated by a comma.
 * The expected line format varies with the type of instruction.
 * 
 * TODO: Document the InstructionFormat switching.
 * 
 * @author Alex Remily
 */
public class FileLoader implements Loader {

	static final Logger logger = LogManager.getLogger(FileLoader.class
			.getName());

	/**
	 * Provide a reference to the Computer's memory to hold the contents of the
	 * reader file.
	 */
	private Memory memory = Memory.getInstance();

	/**
	 * Provide a reference to the Computer's context.
	 */
	private Context context = Context.getInstance();

	/**
	 * Used to write instructions into Word objects in memory.
	 */
	private InstructionWriter writer = new InstructionWriter();

	/**
	 * Contains the contents of ROM.
	 */
	private BufferedReader reader = null;
	
	private static ArrayList<LabelEntry> LabelTable = new ArrayList<LabelEntry>();

	/**
	 * FileLoader is constructed with a default reader.
	 */
	public FileLoader() {
		InputStream in = getClass().getResourceAsStream("/input.txt");
		reader = new BufferedReader(new InputStreamReader(in));
	}

	public void Load_File(String file) {
		InputStream in = getClass().getResourceAsStream("/" + file);
		reader = new BufferedReader(new InputStreamReader(in));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.gwu.seas.csci.Loader#load(java.lang.Object)
	 */
	@Override
	public void load(BufferedReader reader) throws ParseException {
		try {
			String temp = null;
			short memory_location = 8; // Locations 0-5 are reserved.

			while ((temp = reader.readLine()) != null) {
				if (temp.equals("") || temp.charAt(0) == '/') {
					logger.debug("Ignoring line: blank or a comment");
					continue;
				}
				
				//Checking for jump labels
				if(temp.indexOf(':') != -1){
					String label = temp.substring(0, temp.indexOf(':')).trim();
					if(searchLabelTable(label) == -1){
						LabelTable.add(new LabelEntry(label, memory_location));
						logger.debug("Found new label: " + label);
					}else{
						throw new ParseException("Error: Duplicate Label",0);
					}
					continue;
				}

				Word word = Utils.StringToWord(temp);
				
				if (word != null)
					memory.write(word, memory_location++);
			}
			logger.debug(memory_location);
			reader.close();
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}

	@Override
	public void load() throws NullPointerException, ParseException,
			IllegalArgumentException {
		this.load(reader);
	}
	
	public int searchLabelTable(String searchString){
		for(int i = 0; i < LabelTable.size(); i++)
			if(LabelTable.get(i).label.equals(searchString))
				return i;
		return -1;
	}
	
	class LabelEntry{
		public String label;
		public short address;
		public Stack<Short> forwardReferences; 
		public LabelEntry(String label, short address){
			this.label = label;
			this.address = address;
			forwardReferences = new Stack<Short>();
		}
	}
}
