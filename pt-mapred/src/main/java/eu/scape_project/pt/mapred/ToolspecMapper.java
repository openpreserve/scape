package eu.scape_project.pt.mapred;

import eu.scape_project.pt.util.fs.Filer;
import eu.scape_project.pt.proc.Processor;
import eu.scape_project.pt.proc.StreamProcessor;
import eu.scape_project.pt.proc.ToolProcessor;
import eu.scape_project.pt.repo.ToolRepository;
import eu.scape_project.pt.tool.Operation;
import eu.scape_project.pt.tool.Tool;
import eu.scape_project.pt.util.*;
import eu.scape_project.pt.util.PipedArgsParser.Command;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * The Toolspec executor.
 *
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 *
 */
public class ToolspecMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

    private static Log LOG = LogFactory.getLog(ToolspecMapper.class);

    private PipedArgsParser parser;
    private ToolRepository repo;
    private Tool tool;
    private Operation operation;

    /**
     * Sets up toolspec repository and parser.
     */
    @Override
    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        String strRepo = conf.get(PropertyNames.REPO_LOCATION);
        Path fRepo = new Path(strRepo);
        FileSystem fs = FileSystem.get(conf);
        this.repo = new ToolRepository(fs, fRepo);

        // create parser of command line input arguments
        parser = new PipedArgsParser();
    }

    /**
     * The map gets a key and value, the latter being a line describing
     * stdin and stdout file references and (a pipe of) tool/action pair(s) with
     * parameters.
     *
     * 1. Parse the input command-line and read parameters and arguments.
     * 2. Localize input and output file references and input stream.
     * 3. Chain piped commands to a simple-chained list of processors and run them.
     * 4. "De-localize" output files and output streams
     *
     * @param key TODO what type?
     * @param value line describing the (piped) command(s) and stdin/out file refs
     * @param context Job context
     */
    @Override
    public void map(LongWritable key, Text value, Context context ) throws IOException {
        LOG.info("Mapper.map key:" + key.toString() + " value:" + value.toString());

        Text text = null;
        try {
            // parse input line for stdin/out file refs and tool/action commands
            parser.parse(value.toString());

            final Command[] commands = parser.getCommands();
            final String strStdinFile = parser.getStdinFile();
            final String strStdoutFile = parser.getStdoutFile();

            ToolProcessor firstProcessor = null;
            ToolProcessor lastProcessor = null; 

            Map<String, String>[] mapInputFileParameters = new HashMap[commands.length];
            Map<String, String>[] mapOutputFileParameters = new HashMap[commands.length];

            for(int c = 0; c < commands.length; c++ ) {
                Command command = commands[c];

                this.tool = repo.getTool(command.tool);

                lastProcessor = new ToolProcessor(this.tool);

                this.operation = lastProcessor.findOperation(command.action);
                if( this.operation == null )
                    throw new IOException(
                            "operation " + command.action + " not found");

                lastProcessor.setOperation(this.operation);

                lastProcessor.initialize();

                lastProcessor.setParameters(command.pairs);

                // get parameters accepted by the lastProcessor.
                mapInputFileParameters[c] = lastProcessor.getInputFileParameters(); 
                mapOutputFileParameters[c] = lastProcessor.getOutputFileParameters(); 

                // copy parameters to temporal map
                Map<String, String> mapTempInputFileParameters = 
                    new HashMap<String, String>(mapInputFileParameters[c]);
                Map<String, String> mapTempOutputFileParameters = 
                    new HashMap<String, String>(mapOutputFileParameters[c]);

                // localize parameters
                for( Entry<String, String> entry : mapInputFileParameters[c].entrySet()) {
                    LOG.info("input = " + entry.getValue());
                    String[] filelist = entry.getValue().split(",");
                    LOG.info("filelist length = "+filelist.length);
                    for(String file : filelist) {
                    	//Filer filer = Filer.create(entry.getValue());
                    	Filer filer = Filer.create(file);
                    	filer.setDirectory(context.getTaskAttemptID().toString());
                    	filer.localize();
                    	mapTempInputFileParameters.put( entry.getKey(), filer.getFileRef());
                    }
                }

                for( Entry<String, String> entry : mapOutputFileParameters[c].entrySet()) {
                    LOG.info("output = " + entry.getValue());
                    String[] filelist = entry.getValue().split(",");
                    LOG.info("filelist length = "+filelist.length);
                    for(String file : filelist) {
                    	//Filer filer = Filer.create(entry.getValue());
                    	Filer filer = Filer.create(file);
                    	filer.setDirectory(context.getTaskAttemptID().toString());
                    	mapTempOutputFileParameters.put( entry.getKey(), filer.getFileRef());
                    }
                }

                // feed processor with localized parameters
                lastProcessor.setInputFileParameters(mapTempInputFileParameters);
                lastProcessor.setOutputFileParameters(mapTempOutputFileParameters);

                // chain processor
                if(firstProcessor == null )
                    firstProcessor = lastProcessor;
                else {
                    Processor help = firstProcessor;  
                    while(help.next() != null ) help = help.next();
                    help.next(lastProcessor);
                }
            }

            // Processors for stdin and stdout
            StreamProcessor streamProcessorIn = null;
            if( strStdinFile != null ) {
                InputStream iStdin = Filer.create(strStdinFile).getInputStream();
                streamProcessorIn = new StreamProcessor(iStdin);
            }

            OutputStream oStdout = null;
            if( strStdoutFile != null ) 
                oStdout = Filer.create(strStdoutFile).getOutputStream();
            else // default: output to bytestream
                oStdout = new ByteArrayOutputStream();

            StreamProcessor streamProcessorOut = new StreamProcessor(oStdout);
            lastProcessor.next(streamProcessorOut);

            if( streamProcessorIn != null ) {
                streamProcessorIn.next(firstProcessor);
                streamProcessorIn.execute();
            } else
                firstProcessor.execute();


            // delocalize output parameters
            for(int i = 0; i < mapOutputFileParameters.length; i++ ) 
                for( String strFile : mapOutputFileParameters[i].values())
                {
                	LOG.info("delocalizing file: "+strFile);
                    String[] filelist = strFile.split(",");
                    LOG.info("filelist length = "+filelist.length);
                    for(String file : filelist) {
                    	//Filer filer = Filer.create(strFile);
                    	Filer filer = Filer.create(file);
                    	LOG.info("dir = " + context.getTaskAttemptID().toString() );
                    	filer.setDirectory(context.getTaskAttemptID().toString());
                    	filer.delocalize();
                    }
                }

                if( oStdout instanceof ByteArrayOutputStream )
                    text = new Text( ((ByteArrayOutputStream)oStdout).toByteArray() );
                else
                    text = new Text( strStdoutFile );

        } catch (Exception ex) {
            LOG.error(ex);
            text = new Text( "ERROR: " + ex.getMessage() );
        } finally {
            try {
                context.write( key, text);
            } catch (InterruptedException ex) {
                LOG.error(ex);
                throw new IOException(ex);
            }
        }
    }
}
