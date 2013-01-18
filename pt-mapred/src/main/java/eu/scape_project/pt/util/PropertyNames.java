package eu.scape_project.pt.util;

/**
 * Keeps property names used in Hadoop Configuration.
 * 
 * @author Matthias Rella, DME-AIT
 */
public class PropertyNames {

	static public final String INFILE = "INFILE";
	static public final String OUTDIR = "OUTDIR";
	static public final String TOOLSTRING = "TOOLSTRING";
	static public final String ACTIONSTRING = "ACTIONSTRING";
    static public final String REPO_LOCATION = "REPO_LOCATION";
    
    //nInputFormat
    static public final String NUM_LINES_PER_SPLIT = "NUM_LINES_PER_SPLIT";
    

	/* seems to be unused
	static public final String PROCSTRING = "PROC_STRING";
	static public final String PROC_TOOLSPEC = "toolspec";
	static public final String PROC_TAVERNA = "taverna";
	*/
	
	// Taverna specific settigns
	static public final String TAVERNA_HOME = "TAVERNA_HOME";
	static public final String TAVERNA_WORKFLOW = "WORKFLOW_LOCATION";
    
}
