/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

import eu.scape_project.pit.invoke.In.Type;

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
	
	

}
