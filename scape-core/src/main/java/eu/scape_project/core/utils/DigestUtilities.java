/**
 * 
 */
package eu.scape_project.core.utils;

import java.math.BigInteger;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 * 
 */
public enum DigestUtilities {
    /** Enforce a static instance */
    INSTANCE;

    private final static String JAVA_SECURITY_ALG_ALIAS_PREFIX = "Alg.Alias.";
    private final static String JAVA_SECURITY_JAVA_PREFIX = "MessageDigest";

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

    /**
     * @return the string names of the Message Digest algorithms supported by java
     */
    public static Set<String> getMessageDigestAlgorithmNames() {
	Set<String> algNames = new HashSet<String>();		// Set of string names to return
	for (Provider prov : Security.getProviders()) {		// Iterate through the security providers
	    Set<Object> keys = prov.keySet();			// Get the provider keys
	    for (Object objKeyPair : keys) {			// Get the Object key pair
		String strKeyPair = (String) objKeyPair;
		String key = strKeyPair.split(" ")[0];		// Split on the space
		if (key.startsWith(JAVA_SECURITY_ALG_ALIAS_PREFIX)) {
		    key = key.substring(JAVA_SECURITY_ALG_ALIAS_PREFIX.length());
		}
		if (key.startsWith(JAVA_SECURITY_JAVA_PREFIX)) {// If it's a MessageDigest identifier	
		    algNames.add(key.substring(JAVA_SECURITY_JAVA_PREFIX.length() + 1));		// Add it to the results
		}
	    }
	}
	return algNames;
    }
}
