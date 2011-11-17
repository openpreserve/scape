package eu.scape_project.pt.pit;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.mapred.SimpleWrapper;
import eu.scape_project.pt.proc.Executable;

/*
 *  Represents a command-line tool to be executed by the wrapper. 
 *  Note: This is not the cmd-line string used for job submission. 
 *  There is no notion of mapReduce at this level
 *  
 *  @author Rainer Schmidt [rschmidt13]
 *  <p>
 *  
 *  @file ... file parameters are specified in the job input file 
 *	@param ...cmd-line parameters are specified when submitting a job 
 *
 */
public class ToolSpec extends Executable {
	
	//TODO implement proper serialization

	//TODO read tool_specs from file
	public static ToolSpec[] SPECS = {
		new ToolSpec("filefile", "ps2pdf @file @file"), 
		new ToolSpec("streamfile", "ps2pdf - @file"), 
		new ToolSpec("streamstream", "ps2pdf - -"),
		new ToolSpec("dummy", "myTool -x=13 -foo @file -bar @param @file")
	};
	
	private static Log LOG = LogFactory.getLog(ToolSpec.class);

	public static String EXEC_DIR = "EXEC_DIR";
	public static String RESULT_DIR = "RESULT_DIR";
	
	public static String FILE = "@file";
	public static String PARAM = "@param";	
	
	private String name = null;
	private String cmd = null;	
	
	public ToolSpec (String name, String cmd) {
		this.name = name.trim();
		this.cmd = cmd.trim();
	}
		
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(" ");
		buffer.append(cmd);
		return buffer.toString();
	}
	
	public static ToolSpec fromString(String str) {
		//String[] a = str.split(Pattern.quote(" "));
		int i = str.indexOf(' ');
		String name = str.substring(0, i);
		String cmd = str.substring(i+1);
    	ToolSpec tool = new ToolSpec(name, cmd);
		return tool;
	}
		
	public int replaceTokenInCmd(String token, String[] vals) {
		LOG.debug("replace Token called: cmd: " + cmd + " token: "+token+" vals: "+Arrays.toString(vals));
		if(vals == null) return 0;
		String[] cmds = cmd.split(Pattern.quote(token));    	
		if(cmds.length != vals.length) {
    		LOG.error("cannot replace token in command: "+cmd+" with array: "+Arrays.toString(vals));
    		return -1;
    	}
    	int i = 0;
    	StringBuffer res = new StringBuffer();
    	for(String item : cmds) {
    		res.append(item).append(vals[i++]);
    	}
		cmd = res.toString();
    	LOG.info("cmd after replacement: "+res.toString());
    	return cmds.length;
	}
	
	public int toolNameToPath(Tools tools) {
		String toolName = cmd.substring(0, cmd.indexOf(' '));
		String path = tools.find(toolName);
		if(path==null) return -1;
		cmd = cmd.replaceFirst(Pattern.quote(toolName), path);
		LOG.info("replaced tool name: "+toolName+" with: "+path);
		return 1;
	}
			
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}	
	
	public int getExecutableType() {
		return Executable.NATIVE_EXEC;
	}
}
