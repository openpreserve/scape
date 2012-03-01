package eu.scape_project.pt.proc;

import java.io.IOException;
import java.net.URISyntaxException;
/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
public interface PostProcessor {
	
	/**
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void resolvePostcondition() throws IOException, URISyntaxException;

}
