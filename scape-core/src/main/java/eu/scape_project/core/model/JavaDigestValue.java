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
@XmlRootElement
public class JavaDigestValue implements DigestValue, Serializable {
    /** Serialization ID */
    private static final long serialVersionUID = 5509099783448173265L;
    private static final int BUFFER_SIZE = (32 * 1024);
    @XmlAttribute
    URI algorithmId = null;
    @XmlAttribute
    String hexDigest = null;

    JavaDigestValue() {
	/** Disable no arg constructor */
    }

    JavaDigestValue(String algorithmId, String hexDigest) {
	this.algorithmId = URI.create(DigestValue.ALGORITHM_ID_PREFIX + algorithmId);
	this.hexDigest = hexDigest;
    }

    JavaDigestValue(String algorithmId, byte[] digest) {
	this(algorithmId, DigestUtilities.byteDigestToHexString(digest));
    }

    JavaDigestValue(MessageDigest digest) {
	this(digest.getAlgorithm(), digest.digest());
    }

    @Override
    public URI getAlgorithmId() {
	return this.algorithmId;
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
	JAXBContext jbc = JAXBContext
		.newInstance(JavaDigestValue.class);
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
		+ ((algorithmId == null) ? 0 : algorithmId.hashCode());
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
	if (algorithmId == null) {
	    if (other.algorithmId != null)
		return false;
	} else if (!algorithmId.equals(other.algorithmId))
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
	JAXBContext jbc = JAXBContext
		.newInstance(JavaDigestValue.class);
	Unmarshaller um = jbc.createUnmarshaller();
	return (JavaDigestValue) um.unmarshal(input);
    }

    /**
     * @param algorithmId
     *        the string id post-fix for the algorithm, i.e. MD5 or SHA256
     * @param digest
     *        the calculated digest byte array
     * @return a new JavaDigestValue object.
     */
    public static JavaDigestValue getInstance(String algorithmId, byte[] digest) {
	return new JavaDigestValue(algorithmId, digest);
    }

    /**
     * @param algorithmId
     *        the string id post-fix for the algorithm, i.e. MD5 or SHA256
     * @param hexDigest
     *        the calculated digest hex string
     * @return a new JavaDigestValue object.
     */
    public static JavaDigestValue getInstance(String algorithmId,
	    String hexDigest) {
	return new JavaDigestValue(algorithmId, hexDigest);
    }

    /**
     * Factory method that creates a new value from a java.security.MessageDigest.
     * 
     * @param digest
     *        the java.security.MessageDigest that's been calced
     * @return a new JavaDigestValue object
     */
    public static JavaDigestValue getInstance(MessageDigest digest) {
	return new JavaDigestValue(digest);
    }

    /**
     * @param byteStream
     * @param algorithmId
     * @return a new JavaDigestValue object
     * @throws IOException
     */
    public static JavaDigestValue getInstance(String algorithmId,
	    InputStream byteStream) throws IOException {
	try {
	    MessageDigest digest = MessageDigest.getInstance(algorithmId);
	    DigestInputStream dis = new DigestInputStream(byteStream, digest);
	    BufferedInputStream bis = new BufferedInputStream(dis);
	    byte[] buff = new byte[BUFFER_SIZE];
	    while ((bis.read(buff, 0, BUFFER_SIZE)) != -1) {
	    }
	    bis.close();
	    return new JavaDigestValue(digest);
	} catch (NoSuchAlgorithmException e) {
	    throw new IllegalArgumentException(
		    "No java.security.MessageDigest algorithm for id:"
			    + algorithmId, e);
	}
    }

    /**
     * @param file
     * @param algorithmId
     * @return a new JavaDigestValue object
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static JavaDigestValue getInstance(String algorithmId, File file)
	    throws FileNotFoundException, IOException {
	return JavaDigestValue.getInstance(algorithmId, new FileInputStream(
		file));
    }
}
