/**
 * 
 */
package edu.gwu.seas.csci;

import java.text.ParseException;

/**
 * Provides a mechanism for loading program instructions into memory.
 * Implementing classes handle the IO related to a specific input source. The
 * {@link Computer} can then load instructions into memory via a call to the
 * load method of the implementing class.
 * 
 * @author Alex Remily
 */
public interface Loader {

    /**
     * 
     * @param input
     *            The resource to load. Implementing classes should specify the
     *            resource type that they load, e.g., a file or an input stream.
     * @throws ParseException
     *             If the input cannot be parsed.
     * @throws IllegalArgumentException
     *             If the input is not of the expected type.
     */
    public void load(Object input) throws ParseException,
	    IllegalArgumentException;
}
