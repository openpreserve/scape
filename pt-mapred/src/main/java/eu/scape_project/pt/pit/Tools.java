package eu.scape_project.pt.pit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Maps tools to commands.
 * 
 * @author Matthias Rella [my_rho]
 * @deprecated Using the cmd specification in Toolspec files
 */
@Deprecated
public class Tools {
	
	public static String TOOL_FILE = "tools.properties";
	
	private Properties tools = null;
	
	public static Tools createInstance() throws FileNotFoundException, IOException {
		return new Tools();
	}
	
	protected Tools() throws FileNotFoundException, IOException {
		tools = new Properties();
		//FileInputStream in = new FileInputStream(TOOL_FILE);
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(TOOL_FILE);
		tools.load(in);
		in.close();		
	}
	
	public String find(String tool) {
		if (!tools.containsKey(tool)) return null;
		return (String)tools.get(tool);
	}

	
	public Properties getTools() {
			return tools;
		}

}
