/**
 * 
 */
package eu.scape_project.core.api;

import java.net.URI;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a>
 *	   <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a>
 *	   <a href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 *
 * @version 0.1 Created Nov 21, 2011:1:54:25 PM
 */
public interface DigestValue {
    /** Prefix for digest algorithm identifiers */
    public static String ALGORITHM_ID_PREFIX = "alg:digest.";
    /** String for MD5 algorithm identifier */
    public static String MD5 = "MD5";
    /** String for SHA1 algorithm identifier */
    public static String SHA1 = "SHA-1";
    /** String for SHA256 algorithm identifier */
    public static String SHA256 = "SHA-256";
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
