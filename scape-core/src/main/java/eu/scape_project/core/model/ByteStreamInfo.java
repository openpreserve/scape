/**
 * 
 */
package eu.scape_project.core.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.scape_project.core.api.DigestValue.DigestAlgorithm;

/**
 * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl AT SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl AT github</a>
 * @version 0.1
 * 
 *          Created Dec 5, 2011:2:34:00 PM
 */
@XmlRootElement
public final class ByteStreamInfo {
	private static final int BUFFER_SIZE = (32 * 1024);
	@XmlAttribute
	private long length = 0L;
	@XmlAttribute
	private String mimeType = "application/octet-stream";
	@XmlElement
	private Set<JavaDigestValue> digests = new HashSet<JavaDigestValue>();

	@SuppressWarnings("unused")
	private ByteStreamInfo() {
		/** Intentionally blank */
	}

	/**
	 * @param length
	 *            the length of the byte sequence in bytes
	 * @param checksum
	 *            details of a checksum
	 */
	ByteStreamInfo(long length, JavaDigestValue checksum) {
		this.length = length;
		this.digests.add(checksum);
	}

	/**
	 * @param length
	 *            the length of the byte sequence in bytes
	 * @param checksum
	 *            details of a checksum
	 * @param mime
	 *            the mime type of the item
	 */
	ByteStreamInfo(long length, JavaDigestValue checksum, String mime) {
		this(length, checksum);
		this.mimeType = mime;
	}

	ByteStreamInfo(long length, Set<JavaDigestValue> digests) {
		this.length = length;
		this.digests = digests;
	}

	/**
	 * @param digest
	 *            a digest value
	 */
	public void addDigest(JavaDigestValue digest) {
		this.digests.add(digest);
	}

	/**
	 * @return the size of the byte stream in bytes
	 */
	public long getLength() {
		return this.length;
	}

	/**
	 * @return the set of digests known for this byte sequence
	 */
	public Set<JavaDigestValue> getDigests() {
		return this.digests;
	}

	/**
	 * @return the recorded MIME type of the file
	 */
	public String getMimeType() {
		return this.mimeType;
	}

	/**
	 * @return a String xml representation of the object
	 * @throws JAXBException
	 */
	public String toXml() throws JAXBException {
		JAXBContext jbc = JAXBContext.newInstance(ByteStreamInfo.class);
		Marshaller m = jbc.createMarshaller();
		m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(this, sw);
		return sw.toString();
	}

	@Override
	public String toString() {
		String retVal = String.valueOf(this.length) + "|" + this.mimeType + "|";
		for (JavaDigestValue checksum : this.digests) {
			retVal += "[" + checksum.toString() + "]";
		}
		return retVal += "|";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.digests == null) ? 0 : this.digests.hashCode());
		result = prime * result + (int) (this.length ^ (this.length >>> 32));
		result = prime * result
				+ ((this.mimeType == null) ? 0 : this.mimeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByteStreamInfo other = (ByteStreamInfo) obj;
		if (this.digests == null) {
			if (other.digests != null)
				return false;
		} else if (!this.digests.equals(other.digests))
			return false;
		if (this.length != other.length)
			return false;
		if (this.mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!this.mimeType.equals(other.mimeType))
			return false;
		return true;
	}

	/**
	 * @param xml
	 * @return a new JavaDigestValue object serialized from XML
	 * @throws JAXBException
	 */
	public static ByteStreamInfo getInstance(String xml) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
		JAXBContext jbc = JAXBContext.newInstance(ByteStreamInfo.class);
		Unmarshaller um = jbc.createUnmarshaller();
		return (ByteStreamInfo) um.unmarshal(input);
	}

	/**
	 * @param length
	 *            the length of the byte sequence in bytes
	 * @param checksum
	 *            details of a checksum
	 * @return a new ByteStreamInfo instance created from the params
	 */
	public static ByteStreamInfo getInstance(long length,
			JavaDigestValue checksum) {
		return new ByteStreamInfo(length, checksum);
	}

	/**
	 * @param length
	 *            the length of the byte sequence in bytes
	 * @param checksum
	 *            details of a checksum
	 * @param mime
	 *            the mime type of the item
	 * @return a new ByteStreamInfo instance created from the params
	 */
	public static ByteStreamInfo getInstance(long length,
			JavaDigestValue checksum, String mime) {
		return new ByteStreamInfo(length, checksum, mime);
	}

	/**
	 * @param algorithms
	 * @param is
	 * @return a new ByteStream information instance containing the requested digests, calculated from the stream.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static ByteStreamInfo getInstance(DigestAlgorithm[] algorithms,
			InputStream is) throws IOException, NoSuchAlgorithmException {
		InputStream lastStream = is; // OK last stream is the first stream
		Map<DigestAlgorithm, MessageDigest> digests = new HashMap<DigestAlgorithm, MessageDigest>();
		// iterate through the passed algorithms
		for (DigestAlgorithm algorithm : algorithms) {
			// Skip it if it's in the map already
			if (digests.containsKey(algorithm))
				continue;
			MessageDigest digest = MessageDigest.getInstance(algorithm
					.getJavaName()); // Create a digest
			// Create a stream, add digest to set and record last stream
			DigestInputStream dis = new DigestInputStream(lastStream, digest);
			digests.put(algorithm, digest);
			lastStream = dis;
		}
		// Wrap it all in a Buffered Input Stream, and calculate the digests
		BufferedInputStream bis = new BufferedInputStream(lastStream);
		byte[] buff = new byte[BUFFER_SIZE];
		long length = 0L;
		int count;
		while ((count = bis.read(buff, 0, BUFFER_SIZE)) != -1) {
			length += count;
		}
		bis.close();
		Set<JavaDigestValue> results = new HashSet<JavaDigestValue>();
		for (DigestAlgorithm algorithm : digests.keySet()) {
			results.add(new JavaDigestValue(algorithm, digests.get(algorithm)
					.digest()));
		}
		return new ByteStreamInfo(length, results);
	}

}
