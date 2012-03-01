/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Out {

	/**
	 * Enumeration identifying the output type
	 */
	public enum Type {
		/** Stream based output */
		OUTPUTSTREAM,
		/** URI Output */
		URI,
		/** File based output */
		FILE };
	private Type type = null;
	private OutputStream os;
	private URI uri;
	private File file;
	
	/**
	 * Constructor for output stream output
	 * @param is the output stream
	 */
	public Out( OutputStream is ) {
		type = Type.OUTPUTSTREAM;
		this.os = is;
	}
	
	/**
	 * Constructor for ouput to a URI
	 * @param uri the URI for the output
	 */
	public Out( URI uri ) {
		type = Type.URI;
		this.uri = uri;
	}
	
	/**
	 * Constructor for file based output
	 * @param file the file to output to
	 */
	public Out( File file ) {
		type = Type.FILE;
		this.file = file;
	}
	
	/**
	 * Get the type of the output object
	 * @return enumeration identifying the output type 
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * @return the output file object 
	 */
	public File getFile() {
		if( file != null ) return file;
		// FIXME support the others via a Temp file.
		return null;
	}

}
