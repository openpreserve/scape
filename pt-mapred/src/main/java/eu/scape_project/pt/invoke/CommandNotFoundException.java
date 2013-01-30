package eu.scape_project.pt.invoke;
/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
public class CommandNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4405067564500792267L;

	/**
	 * 
	 */
	public CommandNotFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CommandNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public CommandNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CommandNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
