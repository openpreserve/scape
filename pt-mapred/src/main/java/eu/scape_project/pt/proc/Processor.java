package eu.scape_project.pt.proc;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.pit.Tool;
import eu.scape_project.pt.pit.invoke.Invoker;
import eu.scape_project.pt.pit.invoke.ToolInvoker;

public class Processor {
	
	private static Log LOG = LogFactory.getLog(Processor.class);
	
	protected Executable executable = null;
	protected Invoker invoker = null;
	
	public Processor(Executable executable) {
		this.executable = executable;
	}
	
	public void initialize() {
		if(executable.getExecutableType() == Executable.NATIVE_EXEC) {
			invoker = new ToolInvoker((Tool)executable);
		}
	}
	
	public int execute() throws IOException {
		int code = invoker.execute();
		return code;
	}
	

}
