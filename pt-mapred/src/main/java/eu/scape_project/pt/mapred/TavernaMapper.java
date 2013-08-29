package eu.scape_project.pt.mapred;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;

import eu.scape_project.pt.util.fs.Filer;
import eu.scape_project.pt.proc.StreamProcessor;
import eu.scape_project.pt.util.PropertyNames;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * Executes a Taverna workflow using the Taverna shell script from the installation.
 * 
 * @author Martin Schenck [schenck]
 *
 */
public class TavernaMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
	private static Log LOG = LogFactory.getLog(TavernaMapper.class);
	
	// Locations of the Taverna installation and the workflow
	private String tavernaHome;
	private String workflowLocation;
	private String resultOutDir;
	
	// File processor to get temporary input files
	StreamProcessor fileProcessor;
	
	// Taverna output directory
	private String tavernaOutput;
	
	@Override
	public void setup(Context context) {
        Configuration conf = context.getConfiguration();
        this.tavernaHome = conf.get(PropertyNames.TAVERNA_HOME);
        this.workflowLocation = conf.get(PropertyNames.TAVERNA_WORKFLOW);
        this.resultOutDir = conf.get(PropertyNames.OUTDIR);
		// Add trailing slash if missing to given directories
    	if(tavernaHome != null && !tavernaHome.endsWith(File.separator))
    		tavernaHome += File.separator;
    	
    	if(!resultOutDir.endsWith(File.separator))
    		resultOutDir += File.separator;
	}

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException {

        Map<String, String> mapTmpArgs = new HashMap<String, String>();

        // localize workflow
        Filer filer = Filer.create(workflowLocation);
        filer.localize();
        String strTmpWorkflowLocation = filer.getFileRef();
		
		// localize (retrieve) input files and store local file refs in mapTmpArgs
        for( String fileRef: value.toString().split(" ") ) {
            filer = Filer.create(fileRef);
            filer.localize();
            mapTmpArgs.put(fileRef, filer.getFileRef());
        }

		// Replace all file handles with the temporary local ones
		String workflowInput = value.toString();
		for(String retrievedFile : mapTmpArgs.keySet()) {
			workflowInput = workflowInput.replaceAll(retrievedFile,
					mapTmpArgs.get(retrievedFile));
		}
		
		// Workflow
	    try {
	    	LOG.info("Starting workflow with input: " + workflowInput);
	    		    	
	    	// The list of command and arguments for the process builder
	    	List<String> command = new ArrayList<String>();
	    	
	    	// Taverna from path or given directory?
	    	if(tavernaHome == null) {
	    		command.add("executeworkflow.sh");
	    	} else {
		    	command.add("sh");
		    	command.add(tavernaHome + "executeworkflow.sh");
	    	}
	    	
	    	List<String> inputList = new ArrayList<String>();
	    	for(String input : workflowInput.split(" ")) {
	    		inputList.add(input);
	    	}
	    	
	    	command.addAll(inputList);
	    	command.add(strTmpWorkflowLocation);
	    	
	    	ProcessBuilder processBuilder = new ProcessBuilder(command);
	    	
	    	Process process = processBuilder.start();
	    	
	    	// Read the Streams
	    	InputStream errorStream = process.getErrorStream();
	    	InputStream outStream = process.getInputStream();
	    		    	
	    	// FIXME needs time out, but uncertain how long a workflow can run. Hours?!
	    	process.waitFor();
	    	
	    	// Output the streams
	    	outputStream(errorStream);
	    	outputStream(outStream);
	    } catch (Exception e) {
	    	LOG.error("Could not run workflow: " + e.getMessage(), e);
	    }
	    
	    // Write the results to the corresponding directory
	    try {
			filer = Filer.create(resultOutDir);
			filer.depositDirectoryOrFile(tavernaOutput, resultOutDir + UUID.randomUUID() + File.separator);
		} catch (IOException e) {
			LOG.error("Could not create filer for URI syntax: " + resultOutDir, e);
		}
	    
	}

	private void outputStream(InputStream stream) {
		BufferedReader bufferedStream = null;
		try {
			bufferedStream = new BufferedReader(new InputStreamReader(stream));
			String line;
			while((line = bufferedStream.readLine()) != null) {
				LOG.info("Taverna: " + line);
				
				// Get Taverna output directory
				if(line.startsWith("Outputs will be saved to the directory:"))
					tavernaOutput = line.substring(40);
			}
		} catch (IOException e) {
			LOG.error("Error while outputting stream from Taverna process.");
			e.printStackTrace();
		} finally {
			try { bufferedStream.close(); } catch (Exception e) { /* ignore */ }
		}
	}

}
