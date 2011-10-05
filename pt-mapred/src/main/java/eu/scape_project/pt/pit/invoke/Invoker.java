package eu.scape_project.pt.pit.invoke;

import java.io.IOException;

public interface Invoker {

	//TODO change error code to result object
	public int execute() throws IOException;
	
}
