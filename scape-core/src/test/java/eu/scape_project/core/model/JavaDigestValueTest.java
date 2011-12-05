package eu.scape_project.core.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Test;

import eu.scape_project.core.AllCoreTest;
import eu.scape_project.core.api.DigestValue.DigestAlgorithm;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl"
 *         >carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a> Test
 *         class for the digest value class, using apache.commons.codec to test
 *         Java implementation.
 */
public class JavaDigestValueTest {

	/**
	 * Test method for
	 * {@link eu.scape_project.core.model.JavaDigestValue#hashCode()}.
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
			JavaDigestValue apacheValue = JavaDigestValue
					.getInstance(DigestAlgorithm.MD5,
							DigestUtils.md5(new FileInputStream(file)));
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.MD5, file);
			JavaDigestValue testSha256Value = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA256, file);
			assertEquals(
					"apacheValue.hash() and testValue.hash() should be equal",
					apacheValue.hashCode(), testValue.hashCode());
			assertTrue(
					"apacheValue.hash() and testSha256Value.has() shouldn't be equal",
					apacheValue.hashCode() != testSha256Value.hashCode());
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for
	 * {@link eu.scape_project.core.model.JavaDigestValue#getAlgorithmId()}.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	@Test
	public void testGetAlgorithmId() throws FileNotFoundException,
			URISyntaxException, IOException {
		boolean dataTested = false;
		for (File file : AllCoreTest
				.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
			JavaDigestValue apacheValue = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA1,
					DigestUtils.sha(new FileInputStream(file)));
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA1, file);
			JavaDigestValue testSha256Value = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA256, file);
			assertEquals("apacheValue.id() and testValue.id() should be equal",
					apacheValue.getAlgorithmId(), testValue.getAlgorithmId());
			assertFalse(
					"apacheValue.id() and testSha256Value.id() shouldn't be equal",
					apacheValue.getAlgorithmId().equals(
							testSha256Value.getAlgorithmId()));
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for
	 * {@link eu.scape_project.core.model.JavaDigestValue#getHexDigest()}.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	@Test
	public void testGetHexDigest() throws FileNotFoundException,
			URISyntaxException, IOException {
		boolean dataTested = false;
		for (File file : AllCoreTest
				.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
			JavaDigestValue apacheValue = JavaDigestValue
					.getInstance(DigestAlgorithm.MD5,
							DigestUtils.md5(new FileInputStream(file)));
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.MD5, file);
			JavaDigestValue testSha1Value = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA1, file);
			assertEquals(
					"apacheValue.hexDigest() and testValue.hexDigest() should be equal",
					apacheValue.getHexDigest(), testValue.getHexDigest());
			assertFalse(
					"apacheValue.hexDigest() and testSha1Value.hexDigest() shouldn't be equal",
					apacheValue.getHexDigest().equals(
							testSha1Value.getHexDigest()));
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for
	 * {@link eu.scape_project.core.model.JavaDigestValue#getDigest()}.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	@Test
	public void testGetDigest() throws FileNotFoundException,
			URISyntaxException, IOException {
		boolean dataTested = false;
		for (File file : AllCoreTest
				.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
			JavaDigestValue apacheValue = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA1,
					DigestUtils.sha(new FileInputStream(file)));
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA1, file);
			JavaDigestValue testSha256Value = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA256, file);
			assertArrayEquals(
					"apacheValue.digest() and testValue.digest() should be equal",
					apacheValue.getDigest(), testValue.getDigest());
			assertThat(
					"apacheValue.digest() and testSha256Value.digest() shouldn't be equal",
					apacheValue.getDigest(),
					IsNot.not(IsEqual.equalTo(testSha256Value.getDigest())));
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for
	 * {@link eu.scape_project.core.model.JavaDigestValue#toXml()}, and
	 * {@link eu.scape_project.core.model.JavaDigestValue#getInstance(String)}.
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
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.MD5, file);
			JavaDigestValue xmlTestValue = JavaDigestValue
					.getInstance(testValue.toXml());
			assertEquals(
					"testValue.hash() and xmlTestValue.hash() should be equal",
					testValue.hashCode(), xmlTestValue.hashCode());
			assertFalse(
					"apache256Value.hash() and xmlTestValue.hash() shouldn't be equal",
					apache256Value.hashCode() == xmlTestValue.hashCode());
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}

	/**
	 * Test method for {@link eu.scape_project.core.model.JavaDigestValue}.
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	@Test
	public void testEqualsObject() throws FileNotFoundException,
			URISyntaxException, IOException, JAXBException {
		boolean dataTested = false;
		for (File file : AllCoreTest
				.getFilesFromResourceDir(AllCoreTest.TEST_DATA_ROOT)) {
			JavaDigestValue apache256Value = JavaDigestValue.getInstance(
				DigestAlgorithm.SHA256,
					DigestUtils.sha256(new FileInputStream(file)));
			JavaDigestValue testValue = JavaDigestValue.getInstance(
				DigestAlgorithm.MD5, file);
			JavaDigestValue xmlTestValue = JavaDigestValue
					.getInstance(testValue.toXml());
			assertTrue("testValue and xmlTestValue should be equal",
					testValue.equals(xmlTestValue));
			assertFalse("apache256Value and xmlTestValue shouldn't be equal",
					apache256Value.equals(xmlTestValue));
			dataTested = true;
		}
		assertTrue("No data tested as flag no set", dataTested);
	}
}
