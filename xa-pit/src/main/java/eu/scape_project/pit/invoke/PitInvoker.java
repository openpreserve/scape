/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import eu.scape_project.pit.tools.Parameter;
import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.ToolSpec;
import eu.scape_project.pit.tools.Template;

/**
 * @author Andrew.Jackson@bl.uk [AnJackson]
 *
 */
public class PitInvoker {
	
	protected ToolSpec ts;

	public PitInvoker( String toolspec_id ) throws ToolSpecNotFoundException {
		try {
			ts = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/"+toolspec_id+".ptspec.xml"));
		} catch (FileNotFoundException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not found!", e);
		} catch (JAXBException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not parseable!", e);
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
	
	private void runCommand( String[] cmd_template ) throws IOException {
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
	
	public HashMap<String,String> getStandardVars( Action cmd, File input ) throws IOException {
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

	public void identify( String command_id, File input ) throws CommandNotFoundException, IOException {
		Action cmd = findTool(command_id);
		String[] cmd_template = substituteTemplates(cmd);
		HashMap<String, String> vars = getStandardVars(cmd, input);
		
		for( String key : vars.keySet() ) {
			System.out.println("Key: "+key+" = "+vars.get(key));
		}
		
		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
	}
	
	/**
	 * Generic invocation method:
	 * @param command_id
	 * @param parameters
	 * @throws IOException 
	 * @throws CommandNotFoundException 
	 */
	public void execute( String command_id, HashMap<String,String> parameters) throws IOException, CommandNotFoundException {
		Action cmd = findTool(command_id);
		String[] cmd_template = substituteTemplates(cmd);
		// FIXME This is the part that needs close consideration - See README
		HashMap<String, String> vars = getStandardVars(cmd, new File(parameters.get("input")));
		
		for( String key : vars.keySet() ) {
			System.out.println("Key: "+key+" = "+vars.get(key));
		}
		
		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
	}
	
	public void convert( String command_id, File input, File output) throws CommandNotFoundException, IOException {
		Action cmd = findTool(command_id);
		String[] cmd_template = substituteTemplates(cmd);
		HashMap<String, String> vars = getStandardVars(cmd, input);
		
		// TODO Check output file does not exist!
		vars.put("output", output.getAbsolutePath());

		// Now substitute the parameters:
		replaceAll(cmd_template,vars);

		// Now run the command:
		runCommand(cmd_template);
		
	}
	
	private void replaceAll(String[] cmd_template, HashMap<String,String> vars) {
		for( int i = 0; i < cmd_template.length; i++ ) {
			for( String key : vars.keySet() ) {
				String matchTo = Pattern.quote("${"+key+"}");
				cmd_template[i] = cmd_template[i].replaceAll(matchTo, vars.get(key).replace("\\", "\\\\") );
			}
		}
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws ToolSpecNotFoundException 
	 * @throws CommandNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ToolSpecNotFoundException, CommandNotFoundException {
		/* Parse arguments */
		/* First argument specifies the toolspec to load. */
		String toolspec = args[0];
		PitInvoker ib = new PitInvoker(toolspec);
		/* Second argument specifies the action to invoke. */
		String action = args[1];
		/* Third argument specifies the input file. */
		String inputFile = args[2];

		HashMap<String,String> par = new HashMap<String,String>();
		par.put("input", inputFile);
		
		/* For identification actions, we invoke like this. */
		ib.execute(action, par);
		
		//ib.identify(action, new File( inputFile ) );
				//, 
				//new File("test.jp2") );
//				File.createTempFile("DISC_1",".iso") );
		
		/* Other actions, like migrations, would have different parameters. */
	}
	
}
