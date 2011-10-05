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
public class Tool extends Executable {
	
	//TODO implement proper serialization
	
	public static Tool[] cmds = {
		new Tool("filefile", "ps2pdf @file @file"), 
		new Tool("streamfile", "ps2pdf - @file"), 
		new Tool("streamstream", "ps2pdf - -"),
		new Tool("dummy", "myTool -x=13 -foo @file -bar @param @file")
	};

	public static String FILE = "@file";
	public static String PARAM = "@param";
	
	private static Log LOG = LogFactory.getLog(Tool.class);
	
	private String name = null;
	private String cmd = null;	
	
	public Tool (String name, String cmd) {
		this.name = name;
		this.cmd = cmd;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(" ");
		buffer.append(cmd);
		return buffer.toString();
	}
	
	public static Tool fromString(String str) {
		//String[] a = str.split(Pattern.quote(" "));
		int i = str.indexOf(' ');
		String name = str.substring(0, i);
		String cmd = str.substring(i+1);
    	Tool tool = new Tool(name, cmd);
		return tool;
	}
		
	public int replaceToken(String token, String[] vals) {
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
