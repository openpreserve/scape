/**
 * 
 */
package eu.scape_project.core.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
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
import javax.xml.bind.annotation.XmlRootElement;

import eu.scape_project.core.api.DigestValue;
import eu.scape_project.core.utils.DigestUtilities;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 * 
 */
@XmlRootElement(name = "DigestValue")
public class JavaDigestValue implements DigestValue, Serializable {
	/** Serialization ID */
	private static final long serialVersionUID = 5509099783448173265L;
	private static final int BUFFER_SIZE = (32 * 1024);
	@XmlAttribute
	DigestAlgorithm algorithm;
	@XmlAttribute
	String hexDigest;

	JavaDigestValue() {
		/** Disable no arg constructor */
	}

	JavaDigestValue(DigestAlgorithm algorithm, String hexDigest) {
		this.algorithm = algorithm;
		this.hexDigest = hexDigest;
	}

	JavaDigestValue(DigestAlgorithm algorithm, byte[] digest) {
		this(algorithm, DigestUtilities.byteDigestToHexString(digest));
	}

	@Override
	public URI getAlgorithmId() {
		return this.algorithm.getId();
	}

	@Override
	public String getHexDigest() {
		return this.hexDigest;
	}

	@Override
	public byte[] getDigest() {
		return DigestUtilities.hexStringToByteArray(this.hexDigest);

	}

	/**
	 * @return a String xml representation of the object
	 * @throws JAXBException
	 */
	public String toXml() throws JAXBException {
		JAXBContext jbc = JAXBContext.newInstance(JavaDigestValue.class);
		Marshaller m = jbc.createMarshaller();
		m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(this, sw);
		return sw.toString();
	}

	@Override
	public String toString() {
		try {
			return this.toXml();
		} catch (JAXBException e) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result
				+ ((hexDigest == null) ? 0 : hexDigest.hashCode());
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
		JavaDigestValue other = (JavaDigestValue) obj;
		if (algorithm != other.algorithm)
			return false;
		if (hexDigest == null) {
			if (other.hexDigest != null)
				return false;
		} else if (!hexDigest.equals(other.hexDigest))
			return false;
		return true;
	}

	/**
	 * @param xml
	 * @return a new JavaDigestValue object serialized from XML
	 * @throws JAXBException
	 */
	public static JavaDigestValue getInstance(String xml) throws JAXBException {
		ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
		JAXBContext jbc = JAXBContext.newInstance(JavaDigestValue.class);
		Unmarshaller um = jbc.createUnmarshaller();
		return (JavaDigestValue) um.unmarshal(input);
	}

	/**
	 * @param algorithm
	 *            the DigestAlgorithm to use
	 * @param digest
	 *            the calculated digest byte array
	 * @return a new JavaDigestValue object.
	 */
	public static JavaDigestValue getInstance(DigestAlgorithm algorithm,
			byte[] digest) {
		return new JavaDigestValue(algorithm, digest);
	}

	/**
	 * @param algorithm
	 *            the DigestAlgorithm to use
	 * @param hexDigest
	 *            the calculated digest hex string
	 * @return a new JavaDigestValue object.
	 */
	public static JavaDigestValue getInstance(DigestAlgorithm algorithm,
			String hexDigest) {
		return new JavaDigestValue(algorithm, hexDigest);
	}

	/**
	 * @param byteStream
	 * @param algorithm
	 * @return a new JavaDigestValue object
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static JavaDigestValue getInstance(DigestAlgorithm algorithm,
			InputStream byteStream) throws IOException,
			NoSuchAlgorithmException {
		Set<DigestAlgorithm> algSet = new HashSet<DigestAlgorithm>();
		algSet.add(algorithm);
		return createDigestSet(algSet, byteStream).iterator().next();
	}

	/**
	 * @param file
	 * @param algorithm
	 * @return a new JavaDigestValue object
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static JavaDigestValue getInstance(DigestAlgorithm algorithm,
			File file) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException {
		return JavaDigestValue
				.getInstance(algorithm, new FileInputStream(file));
	}

	/**
	 * @param algorithms
	 *            a set of algorithms to calculate
	 * @param is
	 *            an input stream to calc some digests from
	 * @return a set of digests values calculated from the stream
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static Set<JavaDigestValue> createDigestSet(
			Set<DigestAlgorithm> algorithms, InputStream is)
			throws NoSuchAlgorithmException, IOException {
		InputStream lastStream = is; // OK last stream is the first stream
		Map<DigestAlgorithm, MessageDigest> digests = new HashMap<DigestAlgorithm, MessageDigest>();
		// iterate through the passed algorithms
		for (DigestAlgorithm algorithm : algorithms) {
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
		while ((bis.read(buff, 0, BUFFER_SIZE)) != -1) {
		}
		bis.close();
		Set<JavaDigestValue> results = new HashSet<JavaDigestValue>();
		for (DigestAlgorithm algorithm : digests.keySet()) {
			results.add(new JavaDigestValue(algorithm, digests.get(algorithm)
					.digest()));
		}
		return results;
	}
}
