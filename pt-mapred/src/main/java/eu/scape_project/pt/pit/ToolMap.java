package eu.scape_project.pt.pit;

import java.util.HashMap;

public class ToolMap {
	
	protected HashMap<String, Tool> tools = new HashMap<String, Tool>();
	
	public ToolMap() {
		initialize();
	}
	
	private void initialize() {
		for(Tool t : Tool.cmds) {
			tools.put(t.getName(), t);
		}
	}
	
	public Tool get(String name) {
		return tools.get(name);
	}
}
