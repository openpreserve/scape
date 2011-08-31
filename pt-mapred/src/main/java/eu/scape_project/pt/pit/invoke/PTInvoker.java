package eu.scape_project.pt.pit.invoke;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pit.tools.Tool;

/* 
 * A tool invoker for SCAPE mapReduce applications
 * @author Rainer Schmidt [rschmidt13]
 */ 
public class PTInvoker extends PitInvoker {
	
	public PTInvoker(String toolspec_id) throws ToolSpecNotFoundException {
		super(toolspec_id);
	}

	public Tool findTool( String command_id ) throws CommandNotFoundException {
		return findTool(command_id);
	}

}
