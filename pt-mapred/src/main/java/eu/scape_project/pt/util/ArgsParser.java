package eu.scape_project.pt.util;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ArgsParser {

	static public final String INFILE = "INFILE";
	static public final String OUTDIR = "OUTDIR";
	static public final String TOOL = "TOOL";
	
	private OptionParser parser = null;
	private OptionSet options = null;
	
	public ArgsParser(String optionString, String[] args) {
		this.parser = new OptionParser(optionString);
		this.options = parser.parse(args);
	}
	
	public String getValue(String opt) {
		
		//System.out.println("found option "+opt+": "+options.has(opt));
		//System.out.println(opt+" has am argument: "+options.hasArgument(option));
		//System.out.println(opt+" has argument: "+options.valueOf(opt));
		if (options.hasArgument(opt)) return options.valueOf(opt).toString();
		return null;
	}

}
