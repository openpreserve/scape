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
public class Convert extends Processor {

	public Convert(ToolSpec ts, Action action) {
		super(ts, action);
	}

	public void convert( File input, File output) throws CommandNotFoundException, IOException {
		String[] cmd_template = substituteTemplates(action);
		HashMap<String, String> vars = getStandardVars(action, input);
		
		// TODO Check output file does not exist!
		vars.put("output", output.getAbsolutePath());

		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
		
	}
	
}