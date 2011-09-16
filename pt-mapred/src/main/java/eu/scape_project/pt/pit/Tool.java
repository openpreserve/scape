package eu.scape_project.pt.pit;

import java.util.regex.Pattern;

/*
 *  Represents a command-line tool. Not the String that was submitted to Hadoop. 
 *  There is no notion of mapReduce at this level
 *  @author Rainer Schmidt [rschmidt13]
 */
public class Tool {

	//Parameters
	//@infiles must be retrieved prior to execution
	//@outfiles must be deposited after execution
	
	//Aux
	//input files that are not part of the executable cmd
	
	public static Tool[] cmds = {
		new Tool("filefile", "ps2pdf @infile @outfile", ""), 
		new Tool("streamfile", "ps2pdf - @outfile", ""), 
		new Tool("streamstream", "ps2pdf - -", ""),
		new Tool("dummy", "myTool -x=13 -foo @infile -bar @infile @outfile -baz @outfile", "@infile @infile")
	};

	public static String INFILE = "@infile";
	public static String OUTFILE = "@outfile";
	
	private String name = null;
	private String cmd = null;
	private String aux = null;
	
	
	public Tool (String name, String cmd, String aux) {
		this.name = name;
		this.cmd = cmd;
		this.aux = ( aux==null || aux.trim().equals("null")) ? null : aux; 
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(' ');
		buffer.append(cmd).append(' ');
		buffer.append(aux);		
		return buffer.toString();
	}
	
	public static Tool fromString(String str) {
		String[] a = str.split(Pattern.quote(" "));
    	Tool tool = new Tool(
    		a[0], a[1], a[2]);
		return tool;
	}
		
	public String replaceToken (String token, String value) {
		this.cmd = cmd.replaceAll(Pattern.quote(token), value);
		return cmd;
	}
	
	public boolean hasInfile() {
		return cmd.contains(INFILE);
	}
	
	public boolean hasOutfile() {
		return cmd.contains(OUTFILE);
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
}
