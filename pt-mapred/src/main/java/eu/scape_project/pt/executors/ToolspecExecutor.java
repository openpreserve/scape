package eu.scape_project.pt.executors;

import eu.scape_project.pt.pit.invoke.In;
import eu.scape_project.pt.pit.invoke.Stream;
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

import eu.scape_project.pt.proc.FileProcessor;
import eu.scape_project.pt.proc.PitProcessor;
import eu.scape_project.pt.proc.ToolProcessor;
import eu.scape_project.pt.proc.Processor;
import eu.scape_project.pt.util.ArgsParser;
import eu.scape_project.pt.util.ParamSpec;
import java.io.ByteArrayOutputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
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
     * The Command-Line Processor. The same for all maps.
     */
    static Processor p = null;
    /**
     * Workaround data structure to represent Toolspec Input Specifications
     */
    static HashMap<String, ParamSpec> mapInputs = null;
    private ArgsParser parser;
    private String tstr, astr;
    private final String strRepo;

    public ToolspecExecutor(String tstr, String astr, String strRepo) {
        this.tstr = tstr;
        this.astr = astr;
        this.strRepo = strRepo;
    }

    @Override
    public void setup() {
        // FIXME workaround to switch between toolspec versions
        if (tstr.startsWith("digital-preservation")) {
            p = new ToolProcessor(tstr, astr, strRepo);
        } else {
            p = new PitProcessor(tstr, astr, strRepo);
        }

        p.initialize();

        // get parameters accepted by the processor.
        // this could be needed to validate input parameters 
        mapInputs = p.getParameters();

        // get the parameters (the vars in the toolspec action command)
        // if mapInputs can be retrieved and parsing of the record 
        // as a command line would work:
        parser = new ArgsParser();
        for (Entry<String, ParamSpec> entry : mapInputs.entrySet()) {
            parser.setOption(entry.getKey(), entry.getValue());
        }
    }

    /**
     * The map gets a key and value, the latter being a single command-line with
     * execution parameters for pre-defined Processor (
     *
     * @see setup())
     *
     * 1. Parse the input command-line and read parameters and arguments. 2.
     * Find input- and output-files. Input files are copied from their remote
     * location (eg. HDFS) to a local temporary location. A local temporary
     * location for the output-files is defined. 3. Run the tool using generic
     * Processor. 4. Copy output-files (if needed) from the temp. local location
     * to the remote location which may be defined in the command-line
     * parameter.
     *
     * @param key
     * @param value command-line with parameters and values for the tool
     * @param context Job context
     */
    @Override
    public void map(Object key, Text value, Context context) {

        LOG.info("MyMapper.map key:" + key.toString() + " value:" + value.toString());

        HashMap<String, String> inFiles = new HashMap<String, String>();
        HashMap<String, String> outFiles = new HashMap<String, String>();

        // Unix-style parsing 
        
        FileSystem fs = null;

        HashMap<String, Stream> pContext = new HashMap<String, Stream>();
        String[] args = ArgsParser.makeCLArguments(value.toString());
        parser.parse(args);

        for (Entry<String, ParamSpec> entry : mapInputs.entrySet()) {
            if (parser.hasOption(entry.getKey())) { // says true if option was set AND is contained in args
                // context map for the Processor
                String strValue = parser.getValue(entry.getKey());

                if (strValue.startsWith("hdfs://")) {
                    try {
                        fs = FileSystem.get(context.getConfiguration());
                        Path hfile = new Path(strValue);
                        
                        Stream stream = null;
                        if (entry.getValue().getDirection() == ParamSpec.Direction.IN) {
                            FSDataInputStream fsdin = fs.open(hfile);
                            stream = new Stream(fsdin);
                            inFiles.put(entry.getKey(), strValue );
                        } else if (entry.getValue().getDirection() == ParamSpec.Direction.OUT) {
                            FSDataOutputStream fsdout = fs.create(hfile);
                            stream = new Stream(fsdout);
                            outFiles.put(entry.getKey(), strValue);
                        }

                        pContext.put(entry.getKey(), stream);
                    } catch (IOException ex) {
                        LOG.error(ex);
                    }
                }

                // contexts for FileProcessor
                /*
                if (entry.getValue().getDirection() == ParamSpec.Direction.IN) {
                    inFiles.put(entry.getKey(), strValue);
                } else if (entry.getValue().getDirection() == ParamSpec.Direction.OUT) {
                    outFiles.put(entry.getKey(), strValue);
                }
                * 
                */
            }
        }

        // TODO need to tell FileProcessor if parameters are to be used as streams

        // if mapInputs are not known, the paramters could be parsed that way:
        //HashMap<String, String> pContext = ArgsParser.readParameters(
        //value.toString());

        // bring hdfs files to the exec-dir and use a hash 
        // of the file's full path as identifier
        // prepares input files for local processing through cmd line tool
        // FIXME maybe they are not hdfs-files ...

        /*
         * FileProcessor fileProcessor = new FileProcessor( inFiles, outFiles);
         *
         * try { fileProcessor.resolvePrecondition(); } catch (Exception e_pre)
         * { LOG.error("Exception in preprocessing phase: " +
         * e_pre.getMessage(), e_pre); e_pre.printStackTrace(); }
         *
         * // replace remote refs in pContext by local refs of
         * filePREprocessing pContext.putAll(fileProcessor.getLocalInRefs());
         * pContext.putAll(fileProcessor.getLocalOutRefs());
         *
        for (Entry<String, String> entry : pContext.entrySet()) {
            LOG.debug("pContext.key = " + entry.getKey() + "; value = " + entry.getValue());
        }
        * 
        */
        // run processor

        //ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // TODO maybe context should get In and Out objects and
        // the processor will decide whether to use the file or the stream
        // In and Out would then need to retrieve copies
        // maybe: by default we construct In and Out with streams
        // and they make files of them if needed
        try {
            //p.setStdout(bos);
            p.setContext(pContext);
            p.execute();
        } catch (Exception e_exec) {
            LOG.error("Exception in execution phase: "
                    + e_exec.getMessage(), e_exec);
            e_exec.printStackTrace();
        }

        // bring output files in exec-dir back to the locations on hdfs 
        // as defined in the parameter value
        /*
        try {
            fileProcessor.resolvePostcondition();
        } catch (Exception e_post) {
            LOG.error("Exception in postprocessing phase: "
                    + e_post.getMessage(), e_post);
            e_post.printStackTrace();
        }
        * 
        */
        try {
            
            for( Entry<String, String> entry : outFiles.entrySet() ) {
                String dstString = entry.getValue();
                
                Stream ostream = pContext.get(entry.getKey());
                ostream.getOutputStream().close();                
            }
            // TODO get stdout of process if it wasn't written to output files
            Text output = new Text();
            // write processor output to map context
            context.write(
                    new Text(context.getTaskAttemptID().getId() + "."
                    + System.currentTimeMillis()), output);            

        } catch (IOException ex) {
            LOG.error(ex);
        } catch (InterruptedException ex) {
            LOG.error(ex);
        }
        /**
         * STREAMING works but we'll integrate that later //Path inFile = new
         * Path("hdfs://"+value.toString()); //Path outFile = new
         * Path("hdfs://"+value.toString()+".pdf"); //Path fs_outFile = new
         * Path("/home/rainer/tmp/"+inFile.getName()+".pdf");
         *
         *
         * String[] cmds = {"ps2pdf", "-", "/home/rainer/tmp"+fn+".pdf"};
         * //Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[2]).start();
         * Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[1]).start();
         *
         * //opening file FSDataInputStream hdfs_in = hdfs.open(inFile);
         * FSDataOutputStream hdfs_out = hdfs.create(outFile);
         * //FileOutputStream fs_out = new
         * FileOutputStream(fs_outFile.toString());
         *
         * //pipe(process.getErrorStream(), System.err);
         *
         * OutputStream p_out = p.getOutputStream(); InputStream p_in =
         * p.getInputStream(); //TODO copy outstream and send to log file
         *
         * byte[] buffer = new byte[1024]; int bytesRead = -1;
         *
         * System.out.println("streaming data to process"); Thread toProc =
         * pipe(hdfs_in, new PrintStream(p_out), '>');
         *
         * System.out.println("streaming data to hdfs");() Thread toHdfs =
         * pipe(p_in, new PrintStream(hdfs_out), 'h'); *
         * //pipe(process.getErrorStream(), System.err);
         *
         * toProc.join();	*
         */
    }

  
}
