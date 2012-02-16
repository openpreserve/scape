package eu.scape_project.pt.proc;


public interface Processor {

	//TODO change error code to result object
	public int execute() throws Exception;
	
	public void initialize();
	
}
