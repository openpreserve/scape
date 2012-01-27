/**
 * 
 */
package eu.scape_project.pit.actions;

import java.net.URL;
import java.util.HashMap;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public interface Identify extends Operation {
	
	public String identify( URL inputURL, HashMap<String,String> inputs );

}
