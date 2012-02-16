/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.IOException;

import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.ToolSpec;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Identify extends Processor {

	/**
	 * @param ts
	 * @param action
	 * @throws ToolSpecNotFoundException
	 */
	public Identify(ToolSpec ts, Action action)
			throws ToolSpecNotFoundException {
		super(ts, action);
	}

	/**
	 * @param input
	 * @throws CommandNotFoundException
	 * @throws IOException
	 */
	public void identify( File input ) throws CommandNotFoundException, IOException {
		this.execute(new In(input), null);
	}
	
}
