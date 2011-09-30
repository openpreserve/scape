package eu.scape_project.pit.tools;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;


public class PitInvokerTest {
	
	@Test
	public void testSimpleInvocations() throws ToolSpecNotFoundException, CommandNotFoundException, IOException {
		PitInvoker ib = new PitInvoker("file");
		ib.identify("file-mime", 
				new File("src/test/resources/testfiles/images/cc.png") );
				//, 
				//new File("test.jp2") );
//				File.createTempFile("DISC_1",".iso") );
		
	}

}
