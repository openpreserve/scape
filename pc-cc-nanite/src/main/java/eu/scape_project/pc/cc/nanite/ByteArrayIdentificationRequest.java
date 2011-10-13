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
import uk.gov.nationalarchives.droid.core.interfaces.resource.CachedByteArray;
import uk.gov.nationalarchives.droid.core.interfaces.resource.CachedByteArrays;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ByteArrayIdentificationRequest implements IdentificationRequest {

	private RequestMetaData metaData;
	private RequestIdentifier identifier;
	private byte[] data;
	private int size;
	private CachedByteArray byteArray;

	public ByteArrayIdentificationRequest(RequestMetaData metaData,
			RequestIdentifier identifier, byte[] data ) {
		this.metaData = metaData;
		this.identifier = identifier;
		// Set up the byte array based on the 
		this.data = data;
        this.size = data.length;
        this.byteArray = new CachedByteArray(data, 0);
        //cachedBinary.setSourceFile(null);
	}
	
	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getByte(long)
	 */
	@Override
	public byte getByte(long position) {
		return byteArray.readByte(position);
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getReader()
	 */
	@Override
	public ByteReader getReader() {
		return byteArray;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getFileName()
	 */
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#size()
	 */
	@Override
	public long size() {
		return size;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getExtension()
	 */
	@Override
	public String getExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getSourceInputStream()
	 */
	@Override
	public InputStream getSourceInputStream() throws IOException {
		return new ByteArrayInputStream(data);
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getSourceFile()
	 */
	@Override
	public File getSourceFile() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#open(java.io.InputStream)
	 */
	@Override
	public void open(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getRequestMetaData()
	 */
	@Override
	public RequestMetaData getRequestMetaData() {
		return this.metaData;
	}

	/* (non-Javadoc)
	 * @see uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest#getIdentifier()
	 */
	@Override
	public RequestIdentifier getIdentifier() {
		return this.identifier;
	}

	
}
