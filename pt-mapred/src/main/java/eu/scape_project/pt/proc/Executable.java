package eu.scape_project.pt.proc;

import java.util.HashMap;

@Deprecated
public abstract class Executable {
	
	public static int NATIVE_EXEC = 0;
	
	abstract public int getExecutableType();
	
	private HashMap<String, String> context = null;
	
	public HashMap<String, String> getContext() {
		return context;
	}

	public void setContext(HashMap<String, String> context) {
		this.context = context;
	}

	public Executable() {
		context = new HashMap<String, String>();
	}
	
}
