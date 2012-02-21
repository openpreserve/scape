package eu.scape_project.pt.executors;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.Processor;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pt.proc.FileProcessor;
import eu.scape_project.pt.util.ArgsParser;

/**
 * The Toolspec executor.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 *
 */
public class ToolspecExecutor implements Executor {
	private static Log LOG = LogFactory.getLog(ToolspecExecutor.class);
	
	/**
     * The Command-Line Processor. 
     * The same for all maps.
     */
    static Processor p = null;

    /**
     * Workaround data structure to represent Toolspec Input Specifications
     */
    static HashMap<String, HashMap> mapInputs = null;
    
    /**
     * Parser for the parameters in the command-lines (records).
     */
    private ArgsParser parser;
    
    private String tstr, astr;
    
    public ToolspecExecutor(String tstr, String astr) {
    	this.tstr = tstr;
    	this.astr = astr;
    }
    
    /**
     * Sets up stuff which needs to be created only once and can be used in all maps this Mapper performs.
     * 
     * For per Job there can only be one Tool and one Action selected, this stuff is the processor and the input parameters parser.
     * @param context
     */
	@Override
	public void setup() {
        p = null;
        try {
            p = Processor.createProcessor( tstr, astr );
        } catch( ToolSpecNotFoundException e ) {
        	LOG.error("Toolspec not found: " + tstr);
        	LOG.error(e.getMessage());
            e.printStackTrace();
        } catch (CommandNotFoundException e) {
        	LOG.error("Command not found: " + astr);
        	LOG.error(e.getMessage());
            e.printStackTrace();
        }
    	
        // FIXME get parameter keys (inputs) used in the toolspec action
        // preferably: HashMap<String, String> inputs = p.getInputs();
        // workaround: 
        mapInputs = getInputs(tstr, astr);

        // get the parameters (the vars in the toolspec action command)
        // if mapInputs can be retrieved and parsing of the record as a command line would work:
        parser = new ArgsParser();
        for( Entry<String, HashMap> entry: mapInputs.entrySet()) 
            parser.setOption( entry.getKey(), entry.getValue() );
	}

	/**
     * The map gets a key and value, the latter being a single command-line with execution parameters for pre-defined Toolspec and Action-id.
     * 
     * 1. Parse the input command-line and read parameters and arguments.
     * 2. Find input- and output-files. Input files are copied from their remote location (eg. HDFS) to a local temporary location. A local temporary location for the output-files is defined.
     * 3. Run the tool using xa-pits Processor.
     * 4. Copy output-files (if needed) from the temp. local location to the remote location which may be defined in the command-line parameter.
     * 
     * @param key 
     * @param value command-line with parameters and values for the tool
     * @param context Job context
     * @throws IOException
     * @throws InterruptedException
     */
	@Override
	public void map(Object key, Text value) throws IOException {
		LOG.info("MyMapper.map key:"+key.toString()+" value:"+value.toString());

	    String[] args = ArgsParser.makeCLArguments( value.toString() );
	    parser.parse( args );
	
	    HashMap<String, String> mapParams = new HashMap<String, String>();
	    for( String strKey : mapInputs.keySet() )
	        if( parser.hasOption( strKey ) ) mapParams.put( strKey, parser.getValue(strKey) );
	
	    // if mapInputs cannot be retrieved, the paramters could be parsed with that function:
	    //HashMap<String, String> mapParams = ArgsParser.readParameters( value.toString() );
	
	    // parse parameter values for input- and output-files
	    // FIXME need distinct datatypes in Toolspec Inputs for input- and output-files to distinguish between input- and output-file-parameters
	    // workaround: for now "direction" does that
	    ArrayList<String> inFiles = new ArrayList<String>();
	    ArrayList<String> outFiles = new ArrayList<String>();
	    for( Entry<String, HashMap> entry : mapInputs.entrySet() ) {
	        HashMap<String, Object> mapValues = entry.getValue();
	        if( mapValues.get("datatype").equals( URI.class ) && mapValues.containsKey("direction")) {
	            String strFile = mapParams.get( entry.getKey() );
	            if( mapValues.get("direction").equals("input"))
	            {
	                inFiles.add( strFile );
	                // replace the input parameter with the temporary local location
	                mapParams.put( entry.getKey(), FileProcessor.getTempInputLocation(strFile));
	            }
	            else if( mapValues.get("direction").equals("output") )
	            {
	                outFiles.add( strFile );
	                // replace the output parameter with the temporary local location
	                mapParams.put( entry.getKey(), FileProcessor.getTempOutputLocation(strFile));
	            }
	        }
	    }
	
	    // bring hdfs files to the exec-dir and use a hash of the file's full path as identifier
	    // prepares input files for local processing through command line tool
		FileSystem hdfs = FileSystem.get(new Configuration());
		FileProcessor fileProcessor = new FileProcessor(inFiles.toArray(new String[0]), outFiles.toArray(new String[0]), hdfs);
		
		try {
			fileProcessor.resolvePrecondition();
		} catch(Exception e_pre) {
			LOG.error("Exception in preprocessing phase: " + e_pre.getMessage(), e_pre);
			e_pre.printStackTrace();
		}	    	
		
	    // run processor
		// TODO use sthg. like contextObject to manage type safety (?)
		
		try {
	        p.execute( mapParams );
		} catch(Exception e_exec) {
			LOG.error("Exception in execution phase: " + e_exec.getMessage(), e_exec);
			e_exec.printStackTrace();
		}	    		    		
		
	    // TODO bring output files in exec-dir back to the locations on hdfs as defined in the parameter value
		try {
			fileProcessor.resolvePostcondition();
		} catch(Exception e_post) {
			LOG.error("Exception in postprocessing phase: " + e_post.getMessage(), e_post);
			e_post.printStackTrace();
		}
		
		
		/** STREAMING works but we'll integrate that later
		//Path inFile = new Path("hdfs://"+value.toString());
		//Path outFile = new Path("hdfs://"+value.toString()+".pdf");
		//Path fs_outFile = new Path("/home/rainer/tmp/"+inFile.getName()+".pdf");
	
		 
		String[] cmds = {"ps2pdf", "-", "/home/rainer/tmp"+fn+".pdf"};
		//Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[2]).start();
		Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[1]).start();
		
		//opening file
		FSDataInputStream hdfs_in = hdfs.open(inFile);
		FSDataOutputStream hdfs_out = hdfs.create(outFile);
		//FileOutputStream fs_out = new FileOutputStream(fs_outFile.toString());
			    	
		//pipe(process.getErrorStream(), System.err);
		
		OutputStream p_out = p.getOutputStream();
		InputStream p_in = p.getInputStream();
		//TODO copy outstream and send to log file
		
		byte[] buffer = new byte[1024];
		int bytesRead = -1;
		
		System.out.println("streaming data to process");
		Thread toProc = pipe(hdfs_in, new PrintStream(p_out), '>');
		
		System.out.println("streaming data to hdfs");()
		Thread toHdfs = pipe(p_in, new PrintStream(hdfs_out), 'h'); 
		
		//pipe(process.getErrorStream(), System.err);
		
		toProc.join();	    	
		
		*/
	}
	
	/**
     * Just a workaround to get some form of representation of Toolspec Inputs.
     * Should be replaced by an appropriate method in pit.invoke.Processor.
     * 
     * @param strTool 
     * @param strAction 
     * @return 
     */
    public static HashMap<String, HashMap> getInputs( String strTool, String strAction ) {
        HashMap<String, HashMap> mapInputz = new HashMap<String,HashMap>();
        if( strTool.equals("ghostscript") && strAction.equals("gs-to-pdfa")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put( "required", false );
            mapInputParam.put( "datatype", URI.class );
            mapInputParam.put( "direction", "input");
            mapInputz.put("input", mapInputParam);

            HashMap<String, Object> mapOutputParam = new HashMap<String, Object>();
            mapOutputParam.put( "required", false );
            mapOutputParam.put( "datatype", URI.class );
            mapOutputParam.put( "direction", "output");
            mapInputz.put("output", mapOutputParam);
        }
        else if( ArgsParser.TOOLSTRING.equals("file") && ArgsParser.ACTIONSTRING.equals("file")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put( "required", true );
            mapInputParam.put( "datatype", URI.class );
            mapInputz.put("input", mapInputParam);
        }
        return mapInputz;
    }

}
