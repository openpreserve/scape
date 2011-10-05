package eu.scape_project.pt.pit.invoke;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.pit.Tool;

/*
 * Class to invoke tools as native processes. Supports IO via files and streams.
 * @author Rainer Schmidt [rschmidt13]
 */ 
public class ToolInvoker implements Invoker {
	
	private static Log LOG = LogFactory.getLog(ToolInvoker.class);
	
	private Tool tool = null;
	
	public ToolInvoker(Tool tool) {
		this.tool = tool;
	}
	
	@Override
	public int execute() throws IOException {
		//String[] cmds = {"ps2pdf", "-", "/home/rainer/tmp"+fn+".pdf"};
		Process p = new ProcessBuilder(tool.getCmd()).start();
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

	public Tool getTool() {
		return tool;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

}
