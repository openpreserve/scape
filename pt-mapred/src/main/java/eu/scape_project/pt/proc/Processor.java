package eu.scape_project.pt.proc;

import java.util.HashMap;

public interface Processor {

	//TODO change error code to result object
	public int execute() throws Exception;
	
	public void initialize();

    public void setContext( HashMap<String, String> hashMap);

    /**
     * Get input parameters to the processor.
     * 
     * TODO use a Inputs Object instead of HashMap
     * @return 
     */
    public HashMap<String, HashMap> getInputs();
	
}
