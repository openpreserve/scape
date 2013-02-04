package eu.scape_project.pt.pit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class ToolMap {
	
	private static Log LOG = LogFactory.getLog(ToolMap.class);
	
	protected Tools tools = null;
	protected HashMap<String, ToolSpec> toolSpecs = new HashMap<String, ToolSpec>();
	
	public ToolMap() {		
	}
	
	public void initialize() throws FileNotFoundException, IOException {
		//get available tools
		tools = Tools.createInstance();		
		readToolSpecs();
	}
	
	private void readToolSpecs() {
		for(ToolSpec t : ToolSpec.SPECS) {
			if (t.toolNameToPath(tools) < 0) {
				LOG.warn("couldn't resolve tool for toolspec: "+t);
				continue;
			}
			toolSpecs.put(t.getName(), t);
		}
	}
	
	public ToolSpec get(String name) {
		return toolSpecs.get(name);
	}
}
