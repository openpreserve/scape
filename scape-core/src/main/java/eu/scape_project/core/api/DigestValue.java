/**
 * 
 */
package eu.scape_project.core.api;

import java.net.URI;

import eu.scape_project.core.Constants;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 * 
 * @version 0.1 Created Nov 21, 2011:1:54:25 PM
 */

public interface DigestValue {
    /**
     * Enum to identify the java supported digest algorithms. The name field is used to identify the algorithm for
     * java.security.MessageDigest. The list isn't supposed to be authoritative but was grabbed by the code in the
     * DigestUtilities class that lists the providers and algorithms.
     * 
     * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
     *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a> <a
     *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
     * 
     */
    public enum DigestAlgorithm {
	/** MD5 algorithm identifier */
	MD2("MD2"),
	/** MD5 algorithm identifier */
	MD5("MD5"),
	/** SHA algorithm identifier */
	SHA("SHA"),
	/** SHA1 algorithm identifier */
	SHA1("SHA-1"),
	/** SHA256 algorithm identifier */
	SHA256("SHA-256"),
	/** SHA384 algorithm identifier */
	SHA384("SHA-384"),
	/** SHA512 algorithm identifier */
	SHA512("SHA-512");

	/** SCAPE algorithm id URI scheme prefix */
	public final static String ALGID_URI_PREFIX = Constants.SCAPE_URI_SCHEME
		+ "digest/";
	private final String javaName; // java.security.MessageDigest string identifier

	DigestAlgorithm(String javaName) {
	    this.javaName = javaName;
	}

	/**
	 * @return the java name for the enum instance
	 */
	public final String getJavaName() {
	    return this.javaName;
	}

	/**
	 * @return the scape digest algorithm id
	 */
	public final URI getId() {
	    return URI.create(ALGID_URI_PREFIX + this.javaName);
	}
    }

    /**
     * @return the algorithm id for the digest as a java.net.URI
     */
    public URI getAlgorithmId();

    /**
     * @return the hex value of the digest as a java.lang.String
     */
    public String getHexDigest();

    /**
     * @return the digest value as a byte[]
     */
    public byte[] getDigest();
}
