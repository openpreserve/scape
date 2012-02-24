/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Out {

	public enum Type { OUTPUTSTREAM, URI, FILE };
	private Type type = null;
	private OutputStream os;
	private URI uri;
	private File file;
	
	public Out( OutputStream is ) {
		type = Type.OUTPUTSTREAM;
		this.os = is;
	}
	
	public Out( URI uri ) {
		type = Type.URI;
		this.uri = uri;
	}
	
	public Out( File file ) {
		type = Type.FILE;
		this.file = file;
	}
	
	public Type getType() {
		return type;
	}
	
	public File getFile() {
		if( file != null ) return file;
		// FIXME support the others via a Temp file.
		return null;
	}

    public OutputStream getOutputStream() throws IOException {
		if( os != null ) return os;
		if( file != null) return new FileOutputStream(file);
		//if( uri != null ) 
        return null;
    }

}
