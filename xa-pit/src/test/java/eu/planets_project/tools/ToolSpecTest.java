/**
 * 
 */
package eu.planets_project.tools;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Jackson
 *
 */
public class ToolSpecTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.planets_project.tools.ToolSpec#toXMlFormatted()}.
	 */
	@Test
	public void testToXMlFormatted() {
		fail("Not yet implemented");
	}

	/**
	 * @throws JAXBException 
	 * @throws FileNotFoundException 
	 * 
	 */
	@Test
	public void testFromInputstream() throws FileNotFoundException, JAXBException {
		ToolSpec kakadu = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/kakadu.ptspec"));
		System.out.println("Tools "+kakadu.getTool());
		ToolSpec jhove2 = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/jhove2.ptspec"));
		System.out.println("Tools "+jhove2.getTool());
		ToolSpec isobuster = ToolSpec.fromInputStream( ToolSpec.class.getResourceAsStream("/toolspecs/isobuster.ptspec"));
		System.out.println("Tools "+isobuster.getTool());
	}

}
