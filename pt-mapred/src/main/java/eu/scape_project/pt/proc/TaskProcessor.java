package eu.scape_project.pt.proc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.pit.ToolSpec;
import eu.scape_project.pt.pit.invoke.ToolInvoker;

@Deprecated
public class TaskProcessor implements Processor {
	
	private static Log LOG = LogFactory.getLog(TaskProcessor.class);
	
	protected Executable executable = null;
	protected ToolInvoker invoker = null;
	
	public TaskProcessor(Executable executable) {
		this.executable = executable;
	}

	@Override
	public void initialize() {
		if(executable.getExecutableType() == Executable.NATIVE_EXEC) {
			invoker = new ToolInvoker((ToolSpec)executable);
		}
	}

	@Override
	public int execute() throws Exception {
		int code = invoker.execute();
		return code;
	}
	

}
