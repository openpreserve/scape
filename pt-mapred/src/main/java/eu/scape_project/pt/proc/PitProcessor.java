package eu.scape_project.pt.proc;

//import eu.scape_project.pit.invoke.CommandNotFoundException;
//import eu.scape_project.pit.invoke.Out;
//import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
//import eu.scape_project.pit.tools.Action;
//import eu.scape_project.pit.tools.ToolSpec;
//import eu.scape_project.pt.pit.ToolSpecRepository;
import eu.scape_project.pt.pit.invoke.CommandNotFoundException;
import eu.scape_project.pt.pit.invoke.Out;
import eu.scape_project.pt.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pt.pit.tools.Action;
import eu.scape_project.pt.pit.tools.ToolSpec;
import eu.scape_project.pt.pit.invoke.Processor;

import eu.scape_project.pt.pit.ToolSpecRepository;
import eu.scape_project.pt.pit.invoke.*;
import eu.scape_project.pt.util.ParamSpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Wraps the eu.scape_project.pit.invoke.Processor
 * 
 * @author Matthias Rella [myrho]
 */
public class PitProcessor implements eu.scape_project.pt.proc.Processor {

	private static Log LOG = LogFactory.getLog(PitProcessor.class);

    /**
     * Processor of PIT (used for command-line invocation).
     */
    Processor p;

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
    private HashMap<String, Stream> context;
    private final String strRepo;

    /**
     * Sets toolstring and actionstring.
     * 
     * @param strTool
     * @param strAction
     */
    public PitProcessor( String strTool, String strAction, String strRepo ) {
        this.strTool = strTool;
        this.strAction = strAction;
        this.strRepo = strRepo;
    }

    /**
     * Sets context which is the parameters set.
     * 
     * @param context
     */
    public void setContext( HashMap<String, Stream> context ) {
        this.context = context;
    }

    public void setStdout( OutputStream out ) {
        p.setStdout(new Out( out ));
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
            Path fRepo = new Path( strRepo );
            FileSystem fs = FileSystem.get( new Configuration() );
            ToolSpecRepository repo = new ToolSpecRepository(fs, fRepo);
            ToolSpec toolSpec = repo.getToolSpec( this.strTool);

            Action action = Processor.findTool(toolSpec, strAction);
            p = new Processor( toolSpec, action);
        } catch( FileNotFoundException ex ) {
            LOG.error(ex);
        } catch( IOException ex ) {
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
    public HashMap<String, ParamSpec> getParameters( ) {
        HashMap<String, ParamSpec> mapInputz = new HashMap<String,ParamSpec>();
        if( strTool.equals("ghostscript") && strAction.startsWith("ps-to-pdfa") ||
            strTool.equals("convert") ) {

            ParamSpec param = new ParamSpec();
            param.setDirection(ParamSpec.Direction.IN);
            param.setRequired(false);
            param.setType(URI.class);
            mapInputz.put( "input", param );

            ParamSpec param2 = new ParamSpec();
            param2.setDirection(ParamSpec.Direction.OUT);
            param2.setRequired(false);
            param2.setType(URI.class);
            mapInputz.put( "output", param2 );
        }
        else if( strTool.equals("file") && strAction.equals("identify") ||
                 strTool.equals("fits") && strAction.equals("identify")) {
            ParamSpec param = new ParamSpec();
            param.setDirection(ParamSpec.Direction.IN);
            param.setRequired(true);
            param.setType(URI.class);
            mapInputz.put( "input", param );
        }
        return mapInputz;
    }
    
}
