/**
 * 
 */
package eu.scape_project.pit.invoke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;

import us.monoid.web.Resty;

import eu.scape_project.pit.tools.Parameter;
import eu.scape_project.pit.tools.Action;
import eu.scape_project.pit.tools.ToolSpec;
import eu.scape_project.pit.tools.Template;

/**
 * @author Andrew.Jackson@bl.uk [AnJackson]
 *
 */
public class PitInvoker {
	
	public void compare(String command_is, URL input1, URL input2 ) {
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ToolSpecNotFoundException 
	 * @throws CommandNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ToolSpecNotFoundException, CommandNotFoundException {
		Resty r = new Resty();
		try {
			Object name = r.json("http://ws.geonames.org/postalCodeLookupJSON?postalcode=66780&country=DE")
			  .get("postalcodes[0].placeName");
			System.out.println("Name: "+name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/
		// The Mozilla Rhino engine for the JavaScript programming language, however, is currently included as a feature in the JDK 6 and JRE 6 libraries
		ScriptEngineManager mgr = new ScriptEngineManager();
		for( ScriptEngineFactory sef  : mgr.getEngineFactories() ) {
			System.out.println("SEF: "+sef.getEngineName()+" "+sef.getEngineVersion());
			System.out.println("SEF: "+sef.getLanguageName()+" "+sef.getLanguageVersion());
			for( String mime : sef.getMimeTypes() ) { 
				System.out.println("MIME: "+mime);
			}
		}
	    ScriptEngine jsEngine = mgr.getEngineByMimeType("application/javascript");//application/x-ruby application/x-python
	    //jsEngine.getContext().getWriter();
		try {
			jsEngine.eval("print('Hello, world!')");
		} catch (ScriptException ex) {
			ex.printStackTrace();
		}
		
		/* Parse arguments */
		/* First argument specifies the toolspec to load. */
		String toolspec = args[0];
		/* Second argument specifies the action to invoke. */
		String action = args[1];
		Identify ib = (Identify) new Processor(toolspec, action);
		/* Third argument specifies the input file. */
		String inputFile = args[2];

		HashMap<String,String> par = new HashMap<String,String>();
		par.put("input", inputFile);
		
		/* For identification actions, we invoke like this. */
		ib.execute( par );
		
		//ib.identify(action, new File( inputFile ) );
				//, 
				//new File("test.jp2") );
//				File.createTempFile("DISC_1",".iso") );
		
		/* Other actions, like migrations, would have different parameters. */

	}
	
}
