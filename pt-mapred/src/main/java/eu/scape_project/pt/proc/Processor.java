package eu.scape_project.pt.proc;

import eu.scape_project.pt.pit.invoke.Stream;
import eu.scape_project.pt.util.ParamSpec;
import java.io.OutputStream;
import java.util.HashMap;

public interface Processor {

	//TODO change error code to result object
	public int execute() throws Exception;
	
	public void initialize();

    public void setContext( HashMap<String, Stream> hashMap);

    public void setStdout( OutputStream out );

    /**
     * Get input parameters to the processor.
     * 
     * TODO use a Inputs Object instead of HashMap
     * @return 
     */
    public HashMap<String, ParamSpec> getParameters();
	
}
