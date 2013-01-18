package eu.scape_project.pt.util;

import java.util.HashMap;
import java.util.List;

import java.util.Map.Entry;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class OldArgsParser {

	private static Log LOG = LogFactory.getLog(OldArgsParser.class);

    
	private OptionParser parser = null;
	private OptionSet options = null;
	
    public OldArgsParser() {
        this.parser = new OptionParser();
    }

	public OldArgsParser(String optionString, String[] args) {
		this.parser = new OptionParser(optionString);
		this.options = parser.parse(args);
		
		LOG.info("Options: " + options.toString());
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
	
	public List<?> getValues(String opt) {
		if (options.hasArgument(opt)) return options.valuesOf(opt);
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
     * 1. Split up at every " --" 
     * 2. For each component: first word is key, rest is value
     * 2a. If value is not set, key is not put into HashMap
     * 
     * @param strParameters a string of multiples of "--{key} {value}"
     * @return a HashMap of keys and values
     */

    static public HashMap<String, String> readParameters( String strParameters ) {
        strParameters = strParameters.trim();
        if(strParameters.startsWith("--")) 
            strParameters = strParameters.substring(2);

        String[] astrParameters = strParameters.split("\\s+--");
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
     * Adapts readParameters(): It turns the output HashMap of readParameters() back to the
     * --{key} {value} template.
     * 
     * @param strParameters
     * @return OptionParser-parsable String[]
     */
    public static String[] makeCLArguments(String strParameters) {
        HashMap<String, String> args = readParameters(strParameters);
        String[] astrArgs = new String[ args.size()*2 ];
        int i = 0;
        for( Entry<String, String> entry : args.entrySet() ) {
            astrArgs[i++] = "--" + entry.getKey();
            astrArgs[i++] = entry.getValue();
        }
        return astrArgs;
    }

    /**
     * Maps a parameter and its specification to an OptionParser.accepts() call.
     * 
     * @param strName name of the option
     * @param mapSpecs specification of the option (eg. required, datatype)
     */
    public void setOption(String strName, ParamSpec param ) {
            LOG.debug( "accepts( " + strName + ")");
            // sets the name of the option
            OptionSpecBuilder builder = parser.accepts( strName );
            ArgumentAcceptingOptionSpec aaoc; 

            // sets whether the option is required or not
            if( param.isRequired() )
                aaoc = builder.withRequiredArg();
            else
                aaoc = builder.withOptionalArg();

            // sets the accepted datatype to the specified class
            //aaoc.ofType((Class)(mapSpecs.get("datatype")));
    }
	

}
