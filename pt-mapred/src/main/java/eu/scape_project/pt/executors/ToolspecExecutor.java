package eu.scape_project.pt.executors;

import eu.scape_project.pt.util.fs.Filer;
import eu.scape_project.pt.invoke.Stream;
import eu.scape_project.pt.invoke.ToolSpecNotFoundException;
import eu.scape_project.pt.proc.ToolProcessor;
import eu.scape_project.pt.repo.ToolRepository;
import eu.scape_project.pt.tool.Operation;
import eu.scape_project.pt.tool.Tool;
import eu.scape_project.pt.util.OldArgsParser;
import eu.scape_project.pt.util.ParamSpec;
import eu.scape_project.pt.util.ArgsParser;
import eu.scape_project.pt.util.PropertyNames;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
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
    static ToolProcessor processor = null;
    /**
     * Workaround data structure to represent Toolspec Input Specifications
     */
    static Map<String, String> mapInputFileParameters = null;
    static Map<String, String> mapOutputFileParameters = null;
    static Map<String, String> mapOtherParameters = null;
    private ArgsParser parser;
    private ToolRepository repo;
    private Tool tool;
    private Operation operation;

    /**
     */
    @Override
    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        String strTool = conf.get(PropertyNames.TOOLSTRING);
        String strOperation = conf.get(PropertyNames.ACTIONSTRING);
        String strRepo = conf.get(PropertyNames.REPO_LOCATION);
        Path fRepo = new Path(strRepo);
        FileSystem fs = FileSystem.get(conf);
        this.repo = new ToolRepository(fs, fRepo);
        this.tool = repo.getTool(strTool);

        processor = new ToolProcessor(this.tool);

        this.operation = processor.findOperation(conf.get(PropertyNames.ACTIONSTRING));
        try {
            if( this.operation == null )
                throw new ToolSpecNotFoundException("operation " + strOperation + " not found");
        } catch (ToolSpecNotFoundException ex) {
            throw new IOException(ex);
        }

        processor.setOperation(this.operation);

        processor.initialize();

        // get parameters accepted by the processor.
        // this could be needed to validate input parameters 
        mapInputFileParameters = processor.getInputFileParameters(); 
        mapOutputFileParameters = processor.getOutputFileParameters(); 
        mapOtherParameters = processor.getOtherParameters();

        // create parser of command line input arguments
        parser = new ArgsParser();

        Set<String> keys = new HashSet<String>();
        keys.addAll(mapInputFileParameters.keySet());
        keys.addAll(mapOutputFileParameters.keySet());
        keys.addAll(mapOtherParameters.keySet());

        parser.setParameters(keys);
    }

    /**
     * The map gets a key and value, the latter being a single command-line with
     * execution parameters for pre-defined Processor (
     *
     * @see setup())
     *
     * 1. Parse the input command-line and read parameters and arguments.
     *
     * 2. Localize input and output file references and input stream using
     * FileLocalizer.
     *
     * 3. Run Processor.
     *
     * 4. "De-localize" output files and output streams
     *
     * @param key TODO what type?
     * @param value command-line with parameters and values for the tool
     * @param context Job context
     */

    @Override
    public void map(Object key, Text value, Context context ) throws IOException {
        LOG.info("Mapper.map key:" + key.toString() + " value:" + value.toString());

        parser.parse(value.toString());

        Map<String, String> mapArgs = parser.getArguments();
        String strStdinFile = parser.getStdinFile();
        String strStdoutFile = parser.getStdoutFile();

        // map parsed arguments to the processor's maps
        for(Entry<String, String> entry: mapArgs.entrySet() ) 
            if( mapInputFileParameters.containsKey(entry.getKey()))
                mapInputFileParameters.put(entry.getKey(), entry.getValue());
            else if( mapOutputFileParameters.containsKey(entry.getKey()))
                mapOutputFileParameters.put(entry.getKey(), entry.getValue());
            else if( mapOtherParameters.containsKey(entry.getKey()))
                mapOtherParameters.put(entry.getKey(), entry.getValue());

        Map<String, String> mapTempInputFileParameters = 
            new HashMap<String, String>(mapInputFileParameters);
        Map<String, String> mapTempOutputFileParameters = 
            new HashMap<String, String>(mapOutputFileParameters);

        for( Entry<String, String> entry : mapInputFileParameters.entrySet()) {
            LOG.debug("input = " + entry.getValue());
            Filer filer = Filer.create(entry.getValue());
            filer.localize();
            mapTempInputFileParameters.put( entry.getKey(), filer.getFileRef());
        }

        for( Entry<String, String> entry : mapOutputFileParameters.entrySet()) {
            LOG.debug("output = " + entry.getValue());
            Filer filer = Filer.create(entry.getValue());
            mapTempOutputFileParameters.put( entry.getKey(), filer.getFileRef());
        }

        processor.setInputFileParameters(mapTempInputFileParameters);
        processor.setOutputFileParameters(mapTempOutputFileParameters);

        //FileProcessor fileProcessorIn = null;
        if( strStdinFile != null ) {
            Filer filer = Filer.create(strStdinFile);
            InputStream isStdin = filer.getInputStream();

            processor.setInputStream(isStdin);
            //fileProcessorIn = new FileProcessor(isStdin);
        }

        if( strStdoutFile != null ) {
            Filer filer = Filer.create(strStdoutFile);
            OutputStream osStdout = filer.getOutputStream();

            processor.setOutputStream(osStdout);
            //FileProcessor fileProcessorOut = new FileProcessor(osStdout);
            //processor.next(fileProcessorOut);
        }

        try {
            //if( fileProcessorIn != null ) {
                //fileProcessorIn.next(processor);
                //fileProcessorIn.execute();
            //} else
                processor.execute();
                if( processor.getInputStream() != null)
                    processor.getInputStream().close();
                if( processor.getOutputStream() != null)
                    processor.getOutputStream().close();

        } catch (Exception ex) {
            LOG.error(ex.getStackTrace());
        }


        for( Entry<String, String> entry : mapOutputFileParameters.entrySet()) {
            Filer filer = Filer.create(entry.getValue());
            filer.delocalize();
        }
        try {
            // write processor output to map context
            context.write(
                    new Text(System.currentTimeMillis()+""), new Text("dummy"));
        } catch (InterruptedException ex) {
            Logger.getLogger(ToolspecExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Deprecated
    public void map2(Object key, Text value, Context context) {
        HashMap mapInputs = null;
        OldArgsParser parser = null;
        LOG.info("MyMapper.map key:" + key.toString() + " value:" + value.toString());

        HashMap<String, String> inFiles = new HashMap<String, String>();
        HashMap<String, String> outFiles = new HashMap<String, String>();

        // Unix-style parsing 

        FileSystem fs = null;

        HashMap<String, Stream> pContext = new HashMap<String, Stream>();
        String[] args = OldArgsParser.makeCLArguments(value.toString());
        parser.parse(args);
        for (Iterator it = mapInputs.entrySet().iterator(); it.hasNext();) {
            Entry<String, ParamSpec> entry = (Entry<String, ParamSpec>) it.next();
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
                            inFiles.put(entry.getKey(), strValue);
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
                 * if (entry.getValue().getDirection() ==
                 * ParamSpec.Direction.IN) { inFiles.put(entry.getKey(),
                 * strValue); } else if (entry.getValue().getDirection() ==
                 * ParamSpec.Direction.OUT) { outFiles.put(entry.getKey(),
                 * strValue); }
                 *
                 */
            }
        }

        // TODO need to tell FileProcessor if parameters are to be used as streams

        // if mapInputs are not known, the paramters could be parsed that way:
        //HashMap<String, String> pContext = OldArgsParser.readParameters(
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
         * for (Entry<String, String> entry : pContext.entrySet()) {
         * LOG.debug("pContext.key = " + entry.getKey() + "; value = " +
         * entry.getValue()); }
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
            //processor.setContext(pContext);
            processor.execute();
        } catch (Exception e_exec) {
            LOG.error("Exception in execution phase: "
                    + e_exec.getMessage(), e_exec);
            e_exec.printStackTrace();
        }

        // bring output files in exec-dir back to the locations on hdfs 
        // as defined in the parameter value
        /*
         * try { fileProcessor.resolvePostcondition(); } catch (Exception
         * e_post) { LOG.error("Exception in postprocessing phase: " +
         * e_post.getMessage(), e_post); e_post.printStackTrace(); }
         *
         */
        try {

            for (Entry<String, String> entry : outFiles.entrySet()) {
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
         * //Process processor = new ProcessBuilder(cmds[0],cmds[1],cmds[2]).start();
         * Process processor = new ProcessBuilder(cmds[0],cmds[1],cmds[1]).start();
         *
         * //opening file FSDataInputStream hdfs_in = hdfs.open(inFile);
         * FSDataOutputStream hdfs_out = hdfs.create(outFile);
         * //FileOutputStream fs_out = new
         * FileOutputStream(fs_outFile.toString());
         *
         * //pipe(process.getErrorStream(), System.err);
         *
         * OutputStream p_out = processor.getOutputStream(); InputStream p_in =
         * processor.getInputStream(); //TODO copy outstream and send to log file
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
