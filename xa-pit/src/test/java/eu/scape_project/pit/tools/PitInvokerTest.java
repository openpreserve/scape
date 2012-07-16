package eu.scape_project.pit.tools;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.Identify;
import eu.scape_project.pit.invoke.Processor;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
public class PitInvokerTest {
	
	/**
	 * @throws ToolSpecNotFoundException
	 * @throws CommandNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testSimpleInvocations() throws ToolSpecNotFoundException, CommandNotFoundException, IOException {
		Identify ib = (Identify) Processor.createProcessor( "file", "identify-mime" );
		ib.identify( new File("src/test/resources/testfiles/images/cc.png") );
				//, 
				//new File("test.jp2") );
//				File.createTempFile("DISC_1",".iso") );
		
	}

}
