/**
 * 
 */
package eu.scape_project.core.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import eu.scape_project.core.AllCoreTest;
import eu.scape_project.core.api.DigestValue;

/**
 * @author  <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a>
 *          <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl AT SourceForge</a>
 *          <a href="https://github.com/carlwilson-bl">carlwilson-bl AT github</a>
 * @version 0.1
 * 
 * Created Dec 5, 2011:2:49:51 PM
 */

public class ByteStreamInfoTest {

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#hashCode()}.
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws FileNotFoundException 
	 */
	@Test
	public void testHashCode() throws FileNotFoundException, URISyntaxException, IOException {
		boolean dataTested = false;
		for (File file : AllCoreTest
			.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
		    JavaDigestValue apacheMd5 = JavaDigestValue
			    .getInstance(DigestValue.MD5,
				    DigestUtils.md5(new FileInputStream(file)));
		    JavaDigestValue apacheSha1 = JavaDigestValue
				    .getInstance(DigestValue.SHA1,
					    DigestUtils.sha(new FileInputStream(file)));
		    // Create a byte sequence from the MD5
		    ByteStreamInfo bsiMd5 = new ByteStreamInfo(file.length(), apacheMd5);
		    ByteStreamInfo bsiSha1 = new ByteStreamInfo(file.length(), apacheSha1);
		    assertFalse(
			    "bsiMd5.hash() and bsiSha1.hash() should be equal",
			    bsiMd5.hashCode() == bsiSha1.hashCode());
		    // Adding a sha1 value to the md5 and an md 5 to the sha1 should make them equal...
		    bsiMd5.addChecksum(apacheSha1);
		    bsiSha1.addChecksum(apacheMd5);
		    assertEquals(
			    "bsiMd5.hash() and bsiSha1.hash() should be equal",
			    bsiMd5.hashCode(), bsiSha1.hashCode());
		    System.out.println(bsiMd5.toString());
		    System.out.println(bsiSha1.toString());
		    dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#addChecksum(eu.scape_project.core.model.JavaDigestValue)}.
	 */
	@Test
	public void testAddChecksum() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#getLength()}.
	 */
	@Test
	public void testGetLength() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#getChecksums()}.
	 */
	@Test
	public void testGetChecksums() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#getMimeType()}.
	 */
	@Test
	public void testGetMimeType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.ByteStreamInfo#toXml()}.
	 */
	@Test
	public void testToXml() {
		fail("Not yet implemented");
	}

}
