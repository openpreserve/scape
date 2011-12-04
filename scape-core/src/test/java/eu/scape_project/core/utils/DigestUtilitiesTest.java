/**
 * 
 */
package eu.scape_project.core.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import eu.scape_project.core.AllCoreTest;
import org.apache.commons.codec.digest.DigestUtils;
/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a>
 *	   <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a>
 *	   <a href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 *
 */
public class DigestUtilitiesTest {

    /**
     * Test method for {@link eu.scape_project.core.utils.DigestUtilities#byteDigestToHexString(byte[])}.
     * 
     * Each are a different alg so use one to test the others
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test
    public void testByteDigestToHexString() throws URISyntaxException, FileNotFoundException, IOException {
	boolean dataTested = false;
	for (File file : AllCoreTest.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
	    String apacheHex = DigestUtils.md5Hex(new FileInputStream(file));
	    String testHexVal = DigestUtilities.byteDigestToHexString(DigestUtils.md5(new FileInputStream(file)));
	    assertEquals("apacheHex and testHextVal should be equal", apacheHex, testHexVal);
	    dataTested = true;
	}
	assertTrue("No data tested as flag no set", dataTested);
    }

    /**
     * Test method for {@link eu.scape_project.core.utils.DigestUtilities#hexStringToByteArray(java.lang.String)}.
     * 
     * Each are a different alg so use one to test the others
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test
    public void testHexStringToByteArray() throws URISyntaxException, FileNotFoundException, IOException {
	boolean dataTested = false;
	for (File file : AllCoreTest.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
	    byte[] apacheBytes = DigestUtils.md5(new FileInputStream(file));
	    byte[] testBytes = DigestUtilities.hexStringToByteArray(DigestUtils.md5Hex(new FileInputStream(file)));
	    
	    assertArrayEquals("apacheBytes and testBytes should be equal", apacheBytes, testBytes);
	    dataTested = true;
	}
	assertTrue("No data tested as flag no set", dataTested);
    }
}
