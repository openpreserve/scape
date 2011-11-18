/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	public enum Type { INPUTSTREAM, URI, FILE };
	private Type type = null;
	private InputStream is;
	private URI uri;
	private File file;
	
	public In( InputStream is ) {
		type = Type.INPUTSTREAM;
		this.is = is;
	}
	
	public In( URI uri ) {
		type = Type.URI;
		this.uri = uri;
	}
	
	public In( File file ) {
		type = Type.FILE;
		this.file = file;
	}
	
	public Type getType() {
		return type;
	}
	
	public InputStream getInputStream() throws IOException {
		if( is != null ) return is;
		if( file != null) return new FileInputStream(file);
		//if( uri != null ) 
		return uri.toURL().openStream();
	}
	
	public File getFile() {
		if( file != null ) return file;
		// In other cases, take the input stream and push it into a temp file.
		// And delete the temp file on finalize (?)
		return null;
	}
	
}
