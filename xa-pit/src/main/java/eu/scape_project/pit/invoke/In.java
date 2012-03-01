/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This class supports mapping between the input types the clients support and those supported by the tool.
 * 
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class In {

	/**
	 * Enumeration to represent the type of data object
	 */
	public enum Type {
		/** Input Stream to data */
		INPUTSTREAM,
		/** URI path to data */
		URI,
		/** java.io.File object containing data */
		FILE };
	private Type type = null;
	private InputStream is;
	private URI uri;
	private File file;
	
	/**
	 * Constructor for InputStream based input
	 * @param is a java.io.InputStream to the data object
	 */
	public In( InputStream is ) {
		type = Type.INPUTSTREAM;
		this.is = is;
	}
	
	/**
	 * Constructor for URI for the data object
	 * @param uri the URI identifying the object, should be a URL
	 */
	public In( URI uri ) {
		type = Type.URI;
		this.uri = uri;
	}
	
	/**
	 * Constructor for a java.io.File containing the data object
	 * @param file the file
	 */
	public In( File file ) {
		type = Type.FILE;
		this.file = file;
	}
	
	/**
	 * Get the type of the underlying data object
	 * @return a Type enum
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * @return a java.io.InputStream to the object. 
	 * @throws IOException If there's a problem obtaining the stream
	 */
	public InputStream getInputStream() throws IOException {
		if( is != null ) return is;
		if( file != null) return new FileInputStream(file);
		//if( uri != null ) 
		return uri.toURL().openStream();
	}
	
	/**
	 * @return a java.io.File object 
	 */
	public File getFile() {
		if( file != null ) return file;
		// In other cases, take the input stream and push it into a temp file.
		// And delete the temp file on finalize (?)
		return null;
	}
	
}
