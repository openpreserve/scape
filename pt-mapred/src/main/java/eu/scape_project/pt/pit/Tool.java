package eu.scape_project.pt.pit;

import java.util.regex.Pattern;

/*
 *  Represents a command-line tool. Not the String that was submitted to Hadoop. 
 *  There is no notion of mapReduce at this level
 *  @author Rainer Schmidt [rschmidt13]
 */
public class Tool {
	
	public static Tool[] cmds = {
		new Tool("filefile", "ps2pdf @infile @outfile", false, false), 
		new Tool("streamfile", "ps2pdf - @outfile", false, true), 
		new Tool("streamstream", "ps2pdf - -", true, true)
	};

	public static String INFILE = "@infile";
	public static String OUTFILE = "@outfile";
	
	private String name = null;
	private String cmd = null;
	private boolean inFileAsStream = false;
	private boolean outFileAsStream = false;
	
	
	public Tool (String name, String cmd, boolean infileAsStream, boolean outfileAsStream) {
		this.name = name;
		this.cmd = cmd;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append(' ');
		buffer.append(cmd).append(' ');
		buffer.append(inFileAsStream).append(' ');
		buffer.append(outFileAsStream).append(' ');
		return buffer.toString();
	}
	
	public static Tool fromString(String str) {
		String[] a = str.split(Pattern.quote(" "));
    	Tool tool = new Tool(
    		a[0], a[1], new Boolean(a[2]).booleanValue(), new Boolean(a[3]).booleanValue());
		return tool;
	}
		
	public String replaceToken (String token, String value) {
		this.cmd = cmd.replaceAll(Pattern.quote(token), value);
		return cmd;
	}
	
	public boolean usesInfile() {
		return cmd.contains(INFILE);
	}
	
	public boolean usesOutfile() {
		return cmd.contains(OUTFILE);
	}

	/*
	 *  pass the input file as stream to the process
	 */
	public boolean inFileAsStream() {
		return this.inFileAsStream;
	}
	
	/*
	 *  retrieve output file as a stream from the process
	 */ 
	public boolean ouFileAsStream(){
		return this.outFileAsStream;
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
