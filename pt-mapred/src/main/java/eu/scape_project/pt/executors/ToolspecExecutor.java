package eu.scape_project.pt.executors;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;

//import eu.scape_project.pit.invoke.CommandNotFoundException;
//import eu.scape_project.pit.invoke.Processor;
//import eu.scape_project.pit.invoke.ToolSpecNotFoundException;

import eu.scape_project.pt.pit.invoke.CommandNotFoundException;
import eu.scape_project.pt.pit.invoke.Processor;
import eu.scape_project.pt.pit.invoke.ToolSpecNotFoundException;

import eu.scape_project.pt.proc.FileProcessor;
import eu.scape_project.pt.proc.PitProcessor;
import eu.scape_project.pt.util.ArgsParser;
import java.io.ByteArrayOutputStream;
import org.apache.hadoop.mapreduce.Mapper.Context;

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
    static PitProcessor p = null;
    /**
     * Workaround data structure to represent Toolspec Input Specifications
     */
    static HashMap<String, HashMap> mapInputs = null;
    private ArgsParser parser;
    private String tstr, astr;
    private final String strRepo;

    public ToolspecExecutor(String tstr, String astr, String strRepo ) {
        this.tstr = tstr;
        this.astr = astr;
        this.strRepo = strRepo;
    }

    @Override
    public void setup() {
        p = new PitProcessor(tstr, astr, strRepo);
        p.initialize();

        // get parameters accepted by the processor.
        // this could be needed to validate input parameters 
        // mapInputs = p.getInputs();

        // get the parameters (the vars in the toolspec action command)
        // if mapInputs can be retrieved and parsing of the record 
        // as a command line would work:
        // parser = new ArgsParser();
        // for (Entry<String, HashMap> entry : mapInputs.entrySet()) {
        //    parser.setOption(entry.getKey(), entry.getValue());
    }

    /**
     * The map gets a key and value, the latter being a single command-line 
     * with execution parameters for pre-defined Processor (@see setup())
     * 
     * 1. Parse the input command-line and read parameters and arguments.
     * 2. Find input- and output-files. Input files are copied from their 
     *    remote location (eg. HDFS) to a local temporary location. A local 
     *    temporary location for the output-files is defined.
     *    Caveat: input and output-values are found by conventional keys 
     *    "input" and "output".
     * 3. Run the tool using generic Processor.
     * 4. Copy output-files (if needed) from the temp. local location to the 
     *    remote location which may be defined in the command-line parameter.
     * 
     * @param key 
     * @param value command-line with parameters and values for the tool
     * @param context Job context
     */
    @Override
    public void map(Object key, Text value, Context context) {

        LOG.info("MyMapper.map key:" + key.toString() + " value:" + value.toString());

        // Unix-style parsing (if mapInputs would be known in advance):
        /*
            String[] args = ArgsParser.makeCLArguments(value.toString());
            parser.parse(args);
            
            for (String strKey : mapInputs.keySet()) {
            if (parser.hasOption(strKey)) {
            mapParams.put(strKey, parser.getValue(strKey));
            }
            }
         */

        // if mapInputs are not known, the paramters could be parsed that way:
        HashMap<String, String> mapParams = ArgsParser.readParameters(
                value.toString());

        // parse parameter values for input- and output-files
        // FIXME that part should be done within the FilePreprocessor
        // and it should not rely on "input" and "output" conventions
        // maybe: a separate Preconditions-Specification where
        // input- and output-files are marked up
        ArrayList<String> inFiles = new ArrayList<String>();
        ArrayList<String> outFiles = new ArrayList<String>();

        String strInputFile = mapParams.get("input");
        String strOutputFile = mapParams.get("output");

        if (strInputFile != null) {
            inFiles.add(strInputFile);
            // replace the input parameter with the tmp local location
            mapParams.put("input",
                    FileProcessor.getTempInputLocation(strInputFile));
        }
        if (strOutputFile != null) {
            outFiles.add(strOutputFile);
            // replace the output parameter with the tmpt local location
            mapParams.put("output",
                    FileProcessor.getTempOutputLocation(strOutputFile));
        }

        // bring hdfs files to the exec-dir and use a hash 
        // FIXME maybe they are not hdfs-files ...
        // of the file's full path as identifier
        // prepares input files for local processing through cmd line tool

        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(new Configuration());
        } catch (IOException ex) {
            LOG.error(ex);
        }
        FileProcessor fileProcessor = new FileProcessor(
                inFiles.toArray(new String[0]),
                outFiles.toArray(new String[0]),
                hdfs);

        try {
            fileProcessor.resolvePrecondition();
        } catch (Exception e_pre) {
            LOG.error("Exception in preprocessing phase: "
                    + e_pre.getMessage(), e_pre);
            e_pre.printStackTrace();
        }

        // run processor
        // TODO use sthg. like contextObject to manage type safety (?)

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            p.setStdout(bos);
            p.setContext(mapParams);
            p.execute();
        } catch (Exception e_exec) {
            LOG.error("Exception in execution phase: "
                    + e_exec.getMessage(), e_exec);
            e_exec.printStackTrace();
        }

        // bring output files in exec-dir back to the locations on hdfs 
        // as defined in the parameter value
        try {
            fileProcessor.resolvePostcondition();
        } catch (Exception e_post) {
            LOG.error("Exception in postprocessing phase: "
                    + e_post.getMessage(), e_post);
            e_post.printStackTrace();
        }
        try {
            // write processor output to map context
            // use the first input file as key (workaround)
            // TODO fix that workaround
            context.write(new Text(inFiles.get(0)), new Text(bos.toByteArray()));

        } catch (IOException ex) {
            LOG.error(ex);
        } catch (InterruptedException ex) {
            LOG.error(ex);
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
    public static HashMap<String, HashMap> getInputs(String strTool, String strAction) {
        HashMap<String, HashMap> mapInputz = new HashMap<String, HashMap>();
        if (strTool.equals("ghostscript") && strAction.equals("gs-to-pdfa")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put("required", false);
            mapInputParam.put("datatype", URI.class);
            mapInputParam.put("direction", "input");
            mapInputz.put("input", mapInputParam);

            HashMap<String, Object> mapOutputParam = new HashMap<String, Object>();
            mapOutputParam.put("required", false);
            mapOutputParam.put("datatype", URI.class);
            mapOutputParam.put("direction", "output");
            mapInputz.put("output", mapOutputParam);
        } else if (ArgsParser.TOOLSTRING.equals("file") && ArgsParser.ACTIONSTRING.equals("file")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put("required", true);
            mapInputParam.put("datatype", URI.class);
            mapInputz.put("input", mapInputParam);
        }
        return mapInputz;
    }
}
