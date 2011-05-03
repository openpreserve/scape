/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import eu.scape_project.pit.tools.Tool;
import eu.scape_project.pit.tools.ToolSpec;
import eu.scape_project.pit.tools.Var;

/**
 * @author Andrew.Jackson@bl.uk [AnJackson]
 *
 */
public class PitInvoker {
	
	private ToolSpec ts;

	

	public PitInvoker( String toolspec_id ) throws ToolSpecNotFoundException {
		try {
			ts = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/"+toolspec_id+".ptspec"));
		} catch (FileNotFoundException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not found!", e);
		} catch (JAXBException e) {
			throw new ToolSpecNotFoundException("Toolspec "+toolspec_id+" not parseable!", e);
		}
		
	}

	public void migrate( String command_id, File input, File output) throws CommandNotFoundException, IOException {
		Tool cmd = null;
		for( Tool c : ts.getTools() ) {
			if( c.getId() != null && c.getId().equals(command_id) ) cmd = c;
		}
		if( cmd == null ) throw new CommandNotFoundException("No command "+command_id+" could be found.");
		
		HashMap<String,String> vars = new HashMap<String,String>();
		for( Var v : ts.getVar() ) {
			vars.put(v.getName(), v.getValue());
		}
		
		// Check input file exists, and output file does not!
		
		vars.put("inFile", input.getAbsolutePath());
		vars.put("outFile", output.getAbsolutePath());
		vars.put("logFile", File.createTempFile(ts.getName()+"-"+command_id, ".log").getAbsolutePath());
		
		// First substitute local vars into the command, then split and substitute some more?
		// Vars in vars?

		String[] cmd_template = cmd.getCommand().split(" ");
		replaceAll(cmd_template,vars);
		replaceAll(cmd_template,vars);

		ProcessBuilder pb = new ProcessBuilder(cmd_template);
		System.out.println("Command : "+pb.toString());
		System.out.println("Command : "+pb.command());
		for( String command : pb.command() ) {
			System.out.println("Command : "+command);			
		}
		
		pb.redirectErrorStream(true);
		Process start = pb.start();
		try {
			// Needs time-out.
			int waitFor = start.waitFor();
			IOUtils.copy( start.getInputStream() , System.out);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void replaceAll(String[] cmd_template, HashMap<String,String> vars) {
		for( int i = 0; i < cmd_template.length; i++ ) {
			for( String key : vars.keySet() ) {
				String matchTo = Pattern.quote("%{"+key+"}");
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
		PitInvoker ib = new PitInvoker("isobuster");
		ib.migrate("image-to-userdata-image", 
				new File("Y:\\DropBox\\Akiko\\test-nimbie\\DISK_1.mds"), 
				new File("test1") );
//				File.createTempFile("DISC_1",".iso") );
	}
	
}
