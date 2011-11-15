/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Identify extends Processor {

	public Identify(String toolspec_id, String action_id)
			throws ToolSpecNotFoundException {
		super(toolspec_id, action_id);
		// TODO Auto-generated constructor stub
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
