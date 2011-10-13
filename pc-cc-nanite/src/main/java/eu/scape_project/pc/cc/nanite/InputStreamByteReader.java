/**
 * 
 */
package eu.scape_project.pc.cc.nanite;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.domesdaybook.reader.ByteReader;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class InputStreamByteReader implements ByteReader {
	
	private BufferedInputStream in;
	private long nextpos = 0;

	public InputStreamByteReader( InputStream in ) {
		this.in = new BufferedInputStream(in);
		this.nextpos = 0;
		// The 'reset' logic will fail if this is not big enough.
		// Not sure this is a good idea for v large files!
		this.in.mark(1024*1024*1024);
	}

	@Override
	public byte readByte(long position) {
		//System.out.println("Reading "+position);
		try {
			// If skipping back, skip back.
			if( position < this.nextpos ) {
				in.reset();
				in.skip(position);
			} else if( position > this.nextpos ) {
				in.skip( position - this.nextpos );
			}
			this.nextpos = position+1;
			return (byte)in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
