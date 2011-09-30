package eu.scape_project.pt.pit.invoke;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.Tool;

/* 
 * A tool invoker for SCAPE mapReduce applications
 * @author Rainer Schmidt [rschmidt13]
 */ 
@Deprecated
public class PTInvoker extends PitInvoker {
	
	@Deprecated
	public PTInvoker(String toolspec_id) throws ToolSpecNotFoundException {
		super(toolspec_id);
	}

	@Deprecated
	public Action findTool( String command_id ) throws CommandNotFoundException {
		return findTool(command_id);
	}

}

//PTInvoker invoker = new PTInvoker(tools_spec);
//eu.scape_project.pit.tools.Tool tool = invoker.findTool(tool_name);
//System.out.println("cmd: "+ invoker.substituteTemplates(tool));
//Tool findTool( String command_id ) 	