package eu.scape_project.pt.proc;

import java.io.IOException;

import java.io.OutputStream;
import java.util.HashMap;
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

    @Override
    public void setContext(HashMap<String, String> hashMap) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, HashMap> getInputs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setStdout(OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	

}
