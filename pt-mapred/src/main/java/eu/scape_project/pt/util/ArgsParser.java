package eu.scape_project.pt.util;

import java.util.HashMap;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ArgsParser {

	private static Log LOG = LogFactory.getLog(ArgsParser.class);

	static public final String INFILE = "INFILE";
	static public final String OUTDIR = "OUTDIR";
	static public final String TOOLSTRING = "TOOLSTRING";
	static public final String ACTIONSTRING = "ACTIONSTRING";
	static public final String PARAMETERLIST = "PARAMETERLIST";

	static public final String PROCSTRING = "PROC_STRING";
	static public final String PROC_TOOLSPEC = "toolspec";
	static public final String PROC_TAVERNA = "taverna";

	private OptionParser parser = null;
	private OptionSet options = null;
	
    public ArgsParser() {
        this.parser = new OptionParser();
    }

	public ArgsParser(String optionString, String[] args) {
		this.parser = new OptionParser(optionString);
		this.options = parser.parse(args);
	}

    public void accepts( String strOption ) {
        this.parser.accepts(strOption);
    }
	
	public String getValue(String opt) {
		
		//System.out.println("found option "+opt+": "+options.has(opt));
		//System.out.println(opt+" has am argument: "+options.hasArgument(opt));
		//System.out.println(opt+" has argument: "+options.valueOf(opt));
		if (options.hasArgument(opt)) return options.valueOf(opt).toString();
		return null;
	}
	
	public boolean hasOption(String opt) {
		return options.has(opt);
	}

    /**
     * Wrapper for OptionParser.parse()
     * 
     * @param astrParameters an array of strings of OptionParser ready parameters
     */
    public void parse( String[] astrParameters ) {
        options = parser.parse( astrParameters);
    }

    /**
     * Reads keys and values out of a string of parameters.
     * 
     * 0. Trim input string and remove first dash.
     * 1. Split up at every " -" 
     * 2. For each component: first word is key, rest is value
     * 2a. If value is not set, key is not put into HashMap
     * 
     * @param strParameters a string of multiples of "-{key} {value}"
     * @return a HashMap of keys and values
     */

    static public HashMap<String, String> readParameters( String strParameters ) {
        strParameters = strParameters.trim();
        if(strParameters.startsWith("-")) 
            strParameters = strParameters.substring(1);

        String[] astrParameters = strParameters.split("\\s+-");
        HashMap<String, String> mapParameters = new HashMap<String, String>();

        for( String strParameter : astrParameters ) {
            String[] astrKV = strParameter.split("\\s+", 2 );
            if(astrKV.length > 1 )
                mapParameters.put( astrKV[0], astrKV[1] );
        }
        return mapParameters;
    }

    /**
     * Takes a string of command-line input arguments and turns it into a args[] like String array.
     * 
     * Only rudimentary implementation. Maybe there is some tool out there that can do that.
     * 
     * @param strParameters
     * @return 
     */
    public static String[] makeCLArguments(String strParameters) {
        return strParameters.split(" ");

    }

    /**
     * Maps a parameter and its specification to an OptionParser.accepts() call.
     * TODO refactor to be a Specification object not a HashMap
     * 
     * @param strName name of the option
     * @param mapSpecs specification of the option (eg. required, datatype)
     */
    public void setOption(String strName, HashMap<String, Object> mapSpecs ) {
            LOG.debug( "accepts( " + strName + ")");
            // sets the name of the option
            OptionSpecBuilder builder = parser.accepts( strName );
            ArgumentAcceptingOptionSpec aaoc; 

            // sets whether the option is required or not
            if( (Boolean)(mapSpecs.get("required")).equals(true) )
                aaoc = builder.withRequiredArg();
            else
                aaoc = builder.withOptionalArg();

            // sets the accepted datatype to the specified class
            aaoc.ofType((Class)(mapSpecs.get("datatype")));
    }
	

}
