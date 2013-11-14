package eu.scape_project.pt.proc;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.tool.Tool;
//import eu.scape_project.pt.tools.fits.Fits;
import edu.harvard.hul.ois.fits.Fits;

public class JavaProcessor extends ToolProcessor{
	
	private static Log LOG = LogFactory.getLog(JavaProcessor.class);
	
    public JavaProcessor(Tool tool) {
    	super(tool);
    }
    
    @Override
    public int execute() throws Exception {
        LOG.debug("execute");

        Map<String, String> allInputs = new HashMap<String, String>();
        allInputs.putAll(getInputFileParameters());
        allInputs.putAll(getOutputFileParameters());
        allInputs.putAll(getOtherParameters());

        for (String key : allInputs.keySet()) {
            LOG.debug("Key: " + key + " = " + allInputs.get(key));
        }

        String strCmd = replaceAll(this.operation.getCommand(), allInputs);

        LOG.debug("strCmd = " + strCmd );
        String[] args = strCmd.split(" ");
 
        //for(String arg : args) { LOG.debug("JavaProcessor passing args: "+arg); }
        securityManager(true);
        try {
        	LOG.debug("FITS_HOME: "+System.getenv("FITS_HOME"));
        		Fits.main(args);
        } catch (ExitException ee) {
        	LOG.debug("Application tried to exit VM", ee);
        } catch(Exception e) {
        	LOG.error("error during Fits.main(args): "+e);
        	throw e;
        }
        securityManager(false);
       	return 1;
    }
    
    protected static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) 
        {
            super("System.exit() trapped!");
            this.status = status;
        }
    }

    private static class OnExitSecurityManager extends SecurityManager {
    	/*
    	 * no restrictions here
    	 */
    	@Override
    	public void checkPermission(Permission perm) {
			/*
    		LOG.info("checking Permission: "+permission.getName());
    		if(permission.getName().contains("exitVM")) 
				throw new ExitException("System.exit() called");
			*/
		}
    	/*
    	 * no restrictions here
    	 */
    	@Override
        public void checkPermission(Permission perm, Object context) {}    
    	/*
    	 * Throws Exception if VM is being shut down
    	 */
		@Override
        public void checkExit(int status) 
        {
            super.checkExit(status);
            throw new ExitException(status);
        }
	} 

    private void securityManager(boolean on) {
    	SecurityManager securityManager = new OnExitSecurityManager();
    	if(on == true) System.setSecurityManager(securityManager);
    	else System.setSecurityManager(null);
    }

}
