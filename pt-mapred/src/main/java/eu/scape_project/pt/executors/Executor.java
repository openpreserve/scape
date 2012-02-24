/**
 * 
 */
package eu.scape_project.pt.executors;

import java.io.IOException;

import org.apache.hadoop.io.Text;

/**
 * An Interface for execution engines of the wrapper. E.g. Toolspec or Taverna.
 * 
 * @author Martin Schenck [schenck]
 *
 */
public interface Executor {
	public void setup();
	public void map(Object key, Text value) throws IOException;
}
