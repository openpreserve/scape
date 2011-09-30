/**
 * 
 */
package eu.scape_project.pit.tools;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import eu.scape_project.pit.tools.ToolSpec;

/**
 * @author Andrew Jackson
 *
 */
public class ToolSpecTest {
	
	private static final String KAKADU_SPEC = "/toolspecs/kakadu.ptspec.xml";
	private static final String JASPER_SPEC = "/toolspecs/jasper.ptspec";
	private static final String JHOVE2_SPEC = "/toolspecs/jhove2.ptspec";
	private static final String ISOBUSTER_SPEC = "/toolspecs/isobuster.ptspec";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.scape_project.pit.tools.ToolSpec#toXMlFormatted()}.
	 * @throws JAXBException 
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testToXMlFormatted() throws FileNotFoundException, JAXBException, UnsupportedEncodingException {
		ToolSpec pts = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream(KAKADU_SPEC));
		System.out.println("In = "+pts.toXMlFormatted());
		String xml = pts.toXMlFormatted();
		ToolSpec pts2 = ToolSpec.fromInputStream( new ByteArrayInputStream( xml.getBytes("UTF-8") ));
		if( ! pts.equals( pts ) ) {
			System.out.println("Out = "+pts2.toXMlFormatted());
			fail("Round-trip to XML and back lost some data.");
		}
	}

	/**
	 * @throws JAXBException 
	 * @throws FileNotFoundException 
	 * 
	 */
	@Test
	public void testFromInputstream() throws FileNotFoundException, JAXBException {
		ToolSpec kakadu = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream(KAKADU_SPEC));
		System.out.println("Tool name: "+kakadu.getTool().getName());
		/*
		ToolSpec jhove2 = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream(JHOVE2_SPEC));
		System.out.println("Tools "+jhove2.getTool().getName());
		ToolSpec isobuster = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream(ISOBUSTER_SPEC));
		System.out.println("Tools "+isobuster.getTool().getName());
		*/
	}
	
}
