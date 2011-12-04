/**
 * 
 */
package eu.scape_project.core.utils;

import java.math.BigInteger;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 * 
 */
public class DigestUtilities {
    private DigestUtilities() {
	/** static class */
    }

    /**
     * @param digest
     *        The digest value as a byte array
     * @return the digest as a hex java.lang.String
     * @since 1.1
     */
    public static final String byteDigestToHexString(byte[] digest) {
	// OK use the BigInt trick to format this correctly
	BigInteger bi = new BigInteger(1, digest);
	return String.format("%0" + (digest.length << 1) + "x", bi);
    }

    /**
     * @param hexVal
     *        a java.lang.String hex representation of a digest
     * @return the byte[] representation of the digest
     */
    public static byte[] hexStringToByteArray(String hexVal) {
	int len = hexVal.length();
	byte[] data = new byte[len / 2];
	for (int i = 0; i < len; i += 2) {
	    data[i / 2] = (byte) ((Character.digit(hexVal.charAt(i), 16) << 4) + Character
		    .digit(hexVal.charAt(i + 1), 16));
	}
	return data;
    }
}
