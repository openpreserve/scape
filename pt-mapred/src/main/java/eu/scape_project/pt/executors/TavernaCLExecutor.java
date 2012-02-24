package eu.scape_project.pt.executors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;

import eu.scape_project.pt.fs.util.Filer;
import eu.scape_project.pt.proc.FileProcessor;

/**
 * Executes a Taverna workflow using the Taverna shell script from the installation.
 * 
 * @author Martin Schenck [schenck]
 *
 */
public class TavernaCLExecutor implements Executor {
	private static Log LOG = LogFactory.getLog(TavernaCLExecutor.class);
	
	// Locations of the Taverna installation and the workflow
	private String tavernaHome;
	private String workflowLocation;
	private String resultOutDir;
	
	// File processor to get temporary input files
	FileProcessor fileProcessor;
	
	// Taverna output directory
	private String tavernaOutput;
	
	public TavernaCLExecutor(String tavernaHome, String workflowLocation, String resultOutDir) {
		this.tavernaHome = tavernaHome;
		this.workflowLocation = workflowLocation;
		this.resultOutDir = resultOutDir;
	}

	@Override
	public void setup() {
		// Add trailing slash if missing to given directories
    	if(!tavernaHome.endsWith(File.separator))
    		tavernaHome += File.separator;
    	
    	if(!resultOutDir.endsWith(File.separator))
    		resultOutDir += File.separator;
	}

	@Override
	public void map(Object key, Text value) throws IOException {
		// Also get a local copy of the workflow
		StringBuilder stringBuilder = new StringBuilder(value.toString());
		stringBuilder.append(" ");
		stringBuilder.append(workflowLocation);
		
		// Split input to find file handles with the file processor
		String[] values = stringBuilder.toString().split(" ");
		
		// Create a file processor and assign the hdfs
		FileSystem hdfs = FileSystem.get(new Configuration());
		FileProcessor fileProcessor = new FileProcessor(values);
		fileProcessor.setHadoopFS(hdfs);
		
		// Retrieve all files
		try {
			fileProcessor.resolvePrecondition();
		} catch(Exception e) {
			LOG.error("Exception in preprocessing phase: " + e.getMessage(), e);
			e.printStackTrace();
		}	    	
		
		// Replace all file handles with the temporary local ones
		Map<String, String> retrievedFiles = fileProcessor.getRetrievedFiles();
		String workflowInput = value.toString();
		for(String retrievedFile : retrievedFiles.keySet()) {
			workflowInput = workflowInput.replaceAll(retrievedFile,
					retrievedFiles.get(retrievedFile));
		}
		
		// Workflow
	    try {
	    	LOG.info("Starting workflow with input: " + workflowInput);
	    		    	
	    	// The list of command and arguments for the process builder
	    	List<String> command = new ArrayList<String>();
	    	command.add("sh");
	    	command.add(tavernaHome + "executeworkflow.sh");
	    	
	    	List<String> inputList = new ArrayList<String>();
	    	for(String input : workflowInput.split(" ")) {
	    		inputList.add(input);
	    	}
	    	
	    	command.addAll(inputList);
	    	command.add(retrievedFiles.get(workflowLocation));
	    	
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
	    	e.printStackTrace();
	    }
	    
	    // Write the results to the corresponding directory
	    try {
			Filer filer = fileProcessor.getFiler(resultOutDir);
			filer.depositDirectoryOrFile(tavernaOutput, resultOutDir + UUID.randomUUID() + File.separator);
		} catch (URISyntaxException e) {
			LOG.error("Could not create filer for URI syntax: " + resultOutDir);
			e.printStackTrace();
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
