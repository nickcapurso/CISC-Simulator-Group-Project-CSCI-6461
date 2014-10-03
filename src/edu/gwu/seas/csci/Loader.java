/**
 * 
 */
package edu.gwu.seas.csci;

import java.io.BufferedReader;
import java.text.ParseException;

/**
 * Provides a mechanism for loading program instructions into memory.
 * Implementing classes handle the IO related to a specific input source. The
 * {@link CPU} can then load instructions into memory via a call to the load
 * method of the implementing class.
 * 
 * @author Alex Remily
 */
public interface Loader {

	/**
	 * Requires a source of input from which to load instructions into memory.
	 * 
	 * @param input
	 *            The resource to load. Implementing classes should specify the
	 *            resource type that they load, e.g., what type of Reader is
	 *            being buffered.
	 * @throws ParseException
	 *             If the input cannot be parsed.
	 */
	public void load(BufferedReader reader) throws ParseException;

	/**
	 * Expects a source of input to be provided by the implementing class and
	 * throws a NPE if one is not.
	 * 
	 * @throws NullPointerException
	 *             If there is no set default input to load.
	 * @throws ParseException
	 *             If the input cannot be parsed.
	 * @throws IllegalArgumentException
	 *             If the input is not of the expected type.
	 */
	public void load() throws NullPointerException, ParseException,
			IllegalArgumentException;
}
