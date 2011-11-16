/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.ToolSpec;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Identify extends Processor {

	public Identify(ToolSpec ts, Action action)
			throws ToolSpecNotFoundException {
		super(ts, action);
	}

	public void identify( File input ) throws CommandNotFoundException, IOException {
		String[] cmd_template = substituteTemplates(action);
		HashMap<String, String> vars = getStandardVars(action, input);
		
		for( String key : vars.keySet() ) {
			System.out.println("Key: "+key+" = "+vars.get(key));
		}
		
		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
	}
	
}
