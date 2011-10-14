package eu.scape_project.pt.proc;

import java.util.HashMap;

public abstract class Executable {
	
	public static int NATIVE_EXEC = 0;
	
	abstract public int getExecutableType();
	
	private HashMap context = null;
	
	public HashMap getContext() {
		return context;
	}

	public void setContext(HashMap context) {
		this.context = context;
	}

	public Executable() {
		context = new HashMap();
	}
	
}
