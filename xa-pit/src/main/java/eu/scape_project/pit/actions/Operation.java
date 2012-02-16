/**
 * 
 */
package eu.scape_project.pit.actions;

import java.util.HashMap;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public interface Operation {
	
	/**
	 * @param inputs
	 */
	public void run( HashMap<String,String> inputs );

}
