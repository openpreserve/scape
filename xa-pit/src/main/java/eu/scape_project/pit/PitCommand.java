/**
 * 
 */
package eu.scape_project.pit;

import java.io.IOException;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;

/**
 * @author anj
 *
 */
public class PitCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PitInvoker.main(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ToolSpecNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
