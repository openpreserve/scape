/**
 * 
 */
package eu.scape_project.pc.cc.nanite;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.domesdaybook.reader.ByteReader;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class InputStreamIdentificationRequest extends ByteArrayIdentificationRequest {

	private InputStream in;
	private InputStreamByteReader isReader;
	
	public InputStreamIdentificationRequest(RequestMetaData metaData,
			RequestIdentifier identifier, InputStream in) {
		this.metaData = metaData;
		this.identifier = identifier;
		try {
			this.size = in.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.isReader = new InputStreamByteReader(in);
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getByte(long)
	 */
	@Override
	public byte getByte(long position) {
		return this.isReader.readByte(position);
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getReader()
	 */
	@Override
	public ByteReader getReader() {
		return this.isReader;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#close()
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getSourceInputStream()
	 */
	@Override
	public InputStream getSourceInputStream() throws IOException {
		return in;
	}


}
