/**
 * 
 */
package eu.scape_project.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import eu.scape_project.core.AllCoreTest;
import eu.scape_project.core.api.DigestValue.DigestAlgorithm;

/**
 * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl AT SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl AT github</a>
 * @version 0.1
 * 
 *          Created Dec 5, 2011:2:49:51 PM
 */

public class ByteStreamInfoTest {

    /**
     * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#hashCode()}, and for
     * {@link eu.scape_project.core.model.ByteStreamInfo#addChecksum(eu.scape_project.core.model.JavaDigestValue)}.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    @Test
    public void testHashCode() throws FileNotFoundException,
	    URISyntaxException, IOException {
	boolean dataTested = false;
	for (File file : AllCoreTest
		.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
	    JavaDigestValue apacheMd5 = JavaDigestValue.getInstance(
		    DigestAlgorithm.MD5,
		    DigestUtils.md5(new FileInputStream(file)));
	    JavaDigestValue apacheSha1 = JavaDigestValue.getInstance(
		    DigestAlgorithm.SHA1,
		    DigestUtils.sha(new FileInputStream(file)));
	    // Create a byte sequence from the MD5
	    ByteStreamInfo bsiMd5 = new ByteStreamInfo(file.length(), apacheMd5);
	    ByteStreamInfo bsiSha1 = new ByteStreamInfo(file.length(),
		    apacheSha1);
	    assertFalse("bsiMd5.hash() and bsiSha1.hash() should not be equal",
		    bsiMd5.hashCode() == bsiSha1.hashCode());
	    // Adding a sha1 value to the md5 and an md 5 to the sha1 should make them equal...
	    bsiMd5.addChecksum(apacheSha1);
	    bsiSha1.addChecksum(apacheMd5);
	    assertEquals("bsiMd5.hash() and bsiSha1.hash() should be equal",
		    bsiMd5.hashCode(), bsiSha1.hashCode());
	    dataTested = true;
	}
	assertTrue("No data tested as flag no set", dataTested);
    }

    /**
     * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#equals(Object)}.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    @Test
    public void testEqualsObject() throws FileNotFoundException,
	    URISyntaxException, IOException {
	boolean dataTested = false;
	for (File file : AllCoreTest
		.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
	    JavaDigestValue apacheSha256 = JavaDigestValue.getInstance(
		    DigestAlgorithm.SHA256,
		    DigestUtils.sha256(new FileInputStream(file)));
	    JavaDigestValue apacheSha1 = JavaDigestValue.getInstance(
		    DigestAlgorithm.SHA1,
		    DigestUtils.sha(new FileInputStream(file)));
	    // Create a byte sequence from the MD5
	    ByteStreamInfo bsiSha256 = new ByteStreamInfo(file.length(),
		    apacheSha256);
	    ByteStreamInfo bsiSha1 = new ByteStreamInfo(file.length(),
		    apacheSha1);
	    assertFalse(
		    "bsiSha256.equals(bsiSha1.hash()) should return false.",
		    bsiSha256.equals(bsiSha1.hashCode()));
	    // Adding a sha1 value to the md5 and an md 5 to the sha1 should make them equal...
	    bsiSha256.addChecksum(apacheSha1);
	    bsiSha1.addChecksum(apacheSha256);
	    assertTrue("bsiSha256.equals(bsiSha1.hash()) should return true.",
		    bsiSha256.equals(bsiSha1));
	    dataTested = true;
	}
	assertTrue("No data tested as flag no set", dataTested);
    }

    /**
     * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#getMimeType()}.
     */
    @Test
    public void testGetMimeType() {
	/**
	 * TODO: Think about whether this is required and how
	 */
    }

    /**
     * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#toXml()}, and
     * {@link eu.scape_project.core.model.ByteStreamInfo#getInstance(String)}.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    @Test
    public void testXmlSerialization() throws FileNotFoundException,
	    URISyntaxException, IOException, JAXBException {
	boolean dataTested = false;
	for (File file : AllCoreTest
		.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
	    JavaDigestValue apache256Value = JavaDigestValue.getInstance(
		    DigestAlgorithm.SHA256,
		    DigestUtils.sha256(new FileInputStream(file)));
	    ByteStreamInfo bsiApache256 = new ByteStreamInfo(file.length(),
		    apache256Value);
	    JavaDigestValue javaMd5Value = JavaDigestValue.getInstance(
		    DigestAlgorithm.MD5, file);
	    ByteStreamInfo bsiFromXml = ByteStreamInfo
		    .getInstance(new ByteStreamInfo(file.length(), javaMd5Value)
			    .toXml());
	    assertFalse("bsiApache256 and bsiFromXml should not be equal",
		    bsiApache256.equals(bsiFromXml));
	    // Add a the other's checksum to each and they should be the same
	    bsiApache256.addChecksum(javaMd5Value);
	    bsiFromXml.addChecksum(apache256Value);
	    assertTrue("bsiApache256 and bsiFromXml should be equal",
		    bsiApache256.equals(bsiFromXml));
	    dataTested = true;
	}
	assertTrue("No data tested as flag no set", dataTested);
    }

}
