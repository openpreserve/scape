/**
 * 
 */
package eu.scape_project.services.characterise;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import eu.scape_project.core.api.DigestValue.DigestAlgorithm;
import eu.scape_project.core.model.ByteStreamInfo;


/**
 * Simple characterisation service for byte streams.  Simply records the length and calculates
 * checksums.  Intended as a example for experimenting with REST implementations.
 * 
 * @author  <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a>
 *          <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl AT SourceForge</a>
 *          <a href="https://github.com/carlwilson-bl">carlwilson-bl AT github</a>
 * @version 0.1
 * 
 * Created Dec 5, 2011:2:27:06 PM
 */
public class ByteStreamCharacterisation {
    /**
     * @param is the java.io.InputStream to characterise
     * @return a ByteStreamInfo object with characterisation data calced from the stream
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public ByteStreamInfo characterise(InputStream is) throws NoSuchAlgorithmException, IOException {
    	return ByteStreamInfo.getInstance(DigestAlgorithm.values(), is);
    }
}
