package eu.scape_project.pt.proc;

import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import java.net.URI;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps the eu.scape_project.pit.invoke.Processor
 * 
 * @author Matthias Rella [myrho]
 */
public class PitProcessor implements Processor {

	private static Log LOG = LogFactory.getLog(PitProcessor.class);

    /**
     * Processor of PIT (used for command-line invocation).
     */
    eu.scape_project.pit.invoke.Processor p;

    /**
     * Name of the Action of a Tool to use.
     */
    private String strAction;

    /**
     * Name of the Tool to use.
     */
    private String strTool;

    /**
     * In this Processor context is used as the Processor input parameters set.
     */
    private HashMap<String, String> context;

    /**
     * Sets toolstring and actionstring.
     * 
     * @param strTool
     * @param strAction
     */
    public PitProcessor( String strTool, String strAction ) {
        this.strTool = strTool;
        this.strAction = strAction;
    }

    /**
     * Sets context which is the parameters set.
     * 
     * @param context
     */
    public void setContext( HashMap<String, String> context ) {
        this.context = context;
    }

    /**
     * Executes the PitProcessor with the given context as processor parameters.
     * 
     * TODO wrap execute exception
     * 
     * @return
     * @throws Exception
     */
    @Override
    public final int execute() throws Exception {
        p.execute(this.context);
        return 0;
    }

    @Override
    public void initialize() {
        try {
            p = eu.scape_project.pit.invoke.Processor.createProcessor(
                    this.strTool, this.strAction);
        } catch (ToolSpecNotFoundException ex) {
            LOG.error(ex);
        } catch (CommandNotFoundException ex) {
            LOG.error(ex);
        }
    }

    /**
     * Just a workaround to get some form of representation of Toolspec Inputs.
     * Should be replaced by an appropriate method in pit.invoke.Processor.  
     * 
     * TODO get parameter keys (inputs) used in the toolspec action 
     * 
     * @return inputs in a HashMap
     */
    public HashMap<String, HashMap> getInputs( ) {
        HashMap<String, HashMap> mapInputz = new HashMap<String,HashMap>();
        if( strTool.equals("ghostscript") && strAction.equals("gs-to-pdfa")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put( "required", false );
            mapInputParam.put( "datatype", URI.class );
            mapInputParam.put( "direction", "input");
            mapInputz.put("input", mapInputParam);

            HashMap<String, Object> mapOutputParam = new HashMap<String, Object>();
            mapOutputParam.put( "required", false );
            mapOutputParam.put( "datatype", URI.class );
            mapOutputParam.put( "direction", "output");
            mapInputz.put("output", mapOutputParam);
        }
        else if( strTool.equals("file") && strAction.equals("file")) {
            HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
            mapInputParam.put( "required", true );
            mapInputParam.put( "datatype", URI.class );
            mapInputz.put("input", mapInputParam);
        }
        return mapInputz;
    }
    
}
