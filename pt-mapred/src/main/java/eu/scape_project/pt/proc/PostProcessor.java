package eu.scape_project.pt.proc;

import java.io.IOException;
import java.net.URISyntaxException;

public interface PostProcessor {
	
	public void resolvePostcondition() throws IOException, URISyntaxException;

}
