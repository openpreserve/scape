/**
 * 
 */
package eu.scape_project.pt.executors;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * An Interface for execution engines of the wrapper. E.g. Toolspec or Taverna.
 * 
 * @author Martin Schenck [schenck]
 *
 */
public interface Executor {
	public void setup(Context context ) throws IOException;
	public void map(LongWritable key, Text value, Context context) throws IOException;
}
