/**
 * 
 */
package eu.scape_project.xa.rester;

import java.net.URI;
import java.util.Map;

/**
 * @author andy
 *
 */
public interface Convert {

	public void convert( URI input, URI targetFormat, Map<String,String> parameters );
	
}
