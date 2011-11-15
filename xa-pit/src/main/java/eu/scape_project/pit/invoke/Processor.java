/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.Parameter;
import eu.scape_project.pit.tools.Template;
import eu.scape_project.pit.tools.ToolSpec;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Processor {

	protected ToolSpec ts;
	
	protected Action action;

	public Processor( String toolspec_id, String action_id ) throws ToolSpecNotFoundException {
		try {
			ts = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/"+toolspec_id+".ptspec.xml"));
			this.action = this.findTool(action_id);
		} catch (FileNotFoundException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not found!", e);
		} catch (JAXBException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not parseable!", e);
		} catch (CommandNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected Action findTool( String command_id ) throws CommandNotFoundException {
		Action cmd = null;
		for( Action c : ts.getActions() ) {
			if( c.getId() != null && c.getId().equals(command_id) ) cmd = c;
		}
		if( cmd == null ) throw new CommandNotFoundException("No command "+command_id+" could be found.");
		return cmd;
	}
	
	protected void runCommand( String[] cmd_template ) throws IOException {
		// Build the command:
		ProcessBuilder pb = new ProcessBuilder(cmd_template);
		System.out.println("Executing: "+pb.command());
		/*
		for( String command : pb.command() ) {
			System.out.println("Command : "+command);			
		}
		*/
		
		pb.redirectErrorStream(true);
		Process start = pb.start();
		try {
			// Needs time-out. 
			InputStream procStdout = start.getInputStream();
			IOUtils.copy( procStdout , System.out);
			// Moved here, as will not finish until start.getInputStream is called (?)
			int waitFor = start.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] substituteTemplates( Action cmd ) {
		String[] cmd_template = cmd.getCommand().split(" ");
		if( ts.getTemplate() == null ) return cmd_template;
		// Substitute the templates into the command string:
		HashMap<String,String> tpls = new HashMap<String,String>();
		for( Template v : ts.getTemplate() ) {
			tpls.put(v.getName(), v.getValue());
		}
		replaceAll(cmd_template,tpls);
		return cmd_template;
	}
	
	protected HashMap<String,String> getStandardVars( Action cmd, File input ) throws IOException {
		// Now substiture the parameters into the templates.
		HashMap<String,String> vars = new HashMap<String,String>();
		if( ts.getParam() != null ) {
			for( Parameter v : ts.getParam() ) {
				vars.put(v.getVar(), v.getDefault());
			}
		}
		// TODO Check input file exists!
		// Create standard parameters.
		vars.put("input", input.getAbsolutePath());
		vars.put("logFile", File.createTempFile(ts.getTool().getName()+"-"+cmd.getId(), ".log").getAbsolutePath());
		return vars;
	}

	protected void replaceAll(String[] cmd_template, HashMap<String,String> vars) {
		for( int i = 0; i < cmd_template.length; i++ ) {
			for( String key : vars.keySet() ) {
				String matchTo = Pattern.quote("${"+key+"}");
				cmd_template[i] = cmd_template[i].replaceAll(matchTo, vars.get(key).replace("\\", "\\\\") );
			}
		}
	}

	public HashMap<String,String> getParameterMap() {
		HashMap<String, String> vars = new HashMap<String, String>();
		return vars;
	}
	
	public void getParameters() {
	}
	
	
	/**
	 * Generic invocation method:
	 * @param command_id
	 * @param parameters
	 * @throws IOException 
	 * @throws CommandNotFoundException 
	 */
	public void execute( HashMap<String,String> parameters) throws IOException, CommandNotFoundException {
		String[] cmd_template = substituteTemplates(action);
		// FIXME This is the part that needs close consideration - See README
		HashMap<String, String> vars = getStandardVars(action, new File(parameters.get("input")));
		
		for( String key : vars.keySet() ) {
			System.out.println("Key: "+key+" = "+vars.get(key));
		}
		
		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
	}
	
	public void execute( InputStream in, HashMap<String,String> inputs ) {
		
	}
	

}
