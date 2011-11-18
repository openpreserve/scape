package eu.scape_project.pt.pit.invoke;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.fs.util.PtFileUtil;
import eu.scape_project.pt.pit.ToolSpec;
import eu.scape_project.pt.proc.Processor;

/*
 * Class to invoke tools as native processes. Supports IO via files and streams.
 * @author Rainer Schmidt [rschmidt13]
 */ 
public class ToolInvoker {
	
	private static Log LOG = LogFactory.getLog(ToolInvoker.class);
	
	private ToolSpec toolSpec = null;
	
	public ToolInvoker(ToolSpec tool) {
		this.toolSpec = tool;
	}
	
	public int execute() throws IOException, InterruptedException {
		
		File execDir = PtFileUtil.getExecDir();
		String newExecDir = toolSpec.getContext().get(ToolSpec.EXEC_DIR);
		
		//has the execution dir been overwritten?
		if(newExecDir != null) {
			execDir = new File(newExecDir);
		}
		//otherwise use the default value
		else {
			toolSpec.getContext().put(ToolSpec.EXEC_DIR, execDir.toString());
		}
			
		LOG.info("Is execDir a file: "+execDir.isFile() + " and a dir: "+execDir.isDirectory());
		
		
		//TODO check if a simple split(" ") is sufficient for complexer parameter lists
		//Perhaps a specific delimiter for parameters will be required 
		String[] cmds = toolSpec.getCmd().split(Pattern.quote(" "));
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmds);
		processBuilder.directory(execDir);
		Process p = processBuilder.start();
		p.waitFor();
		return p.exitValue();
	}	
	
	/** STREAMING works but we'll integrate that later
	//Path inFile = new Path("hdfs://"+value.toString());
	//Path outFile = new Path("hdfs://"+value.toString()+".pdf");
	//Path fs_outFile = new Path("/home/rainer/tmp/"+inFile.getName()+".pdf");

	 

	
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
	toHdfs.join();
	
	*/
	
  	private Thread pipe(final InputStream src, final PrintStream dest, final char debugToken) throws IOException {
		System.out.println("Starting piping "+ debugToken);
	    Thread t = new Thread(new Runnable() {
	        public void run() {
	            try {
	                byte[] buffer = new byte[1024];
	                for (int n = 0; n != -1; n = src.read(buffer)) {
	                	System.out.print(debugToken);
	                    dest.write(buffer, 0, n);
	                    System.out.println("/"+debugToken);
	                }
	                //reader at EOF, flush writer, and close streams
	                System.out.print("EOF, flushing, and closing \n");
	                dest.flush();
	                src.close();
	                dest.close();
	                return;
	            } catch (IOException e) { // just exit
	            	System.out.println(e);
	            	return;
	            }
	        }
	    });
	    t.start();
	    return t;
	}

	public ToolSpec getToolSpec() {
		return toolSpec;
	}

	public void setTool(ToolSpec tool) {
		this.toolSpec = tool;
	}

}
