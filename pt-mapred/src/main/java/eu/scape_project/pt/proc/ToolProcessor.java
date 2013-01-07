package eu.scape_project.pt.proc;

import eu.scape_project.pt.pit.ToolRepository;
import eu.scape_project.pt.pit.invoke.Stream;
import eu.scape_project.pt.tool.Input;
import eu.scape_project.pt.tool.Operation;
import eu.scape_project.pt.tool.Operations;
import eu.scape_project.pt.tool.Output;
import eu.scape_project.pt.tool.Tool;
import eu.scape_project.pt.util.ParamSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Creates processes for a Tool.
 *
 * @author Matthias Rella [my_rho]
 */
public class ToolProcessor implements eu.scape_project.pt.proc.Processor {

    private static Log LOG = LogFactory.getLog(ToolProcessor.class);
    /**
     * Name of the Operation of a Tool to use.
     */
    private String strOperation;
    private Operation operation;
    /**
     * Name of the Tool to use.
     */
    private String strTool;
    private Tool tool;
    /**
     * In this Processor context is used as the Processor input parameters set.
     */
    private Map<String, Stream> context;
    private String strRepo;
    private ToolRepository repo;
    private InputStream stdin;
    private OutputStream stdout;

    /**
     * Constructs the processor with a toolspec name, an action name of the
     * toolspec and a location of the repository. 
     * 
     * Creates the Toolspec, Operation and Repository instances.
     *
     * @param strTool
     * @param strAction
     * @param strRepo
     */
    public ToolProcessor(String strTool, String strOperation, String strRepo) {
        this.strTool = strTool;
        this.strOperation = strOperation;
        this.strRepo = strRepo;

        Path fRepo = new Path(strRepo);
        FileSystem fs;
        try {
            fs = FileSystem.get(new Configuration());
            this.repo = new ToolRepository(fs, fRepo);
            this.tool = repo.getTool(strTool);
            Operations operations = this.tool.getOperations();
            for (Operation operation : operations.getOperation()) {
                if (operation.getName().equals(strOperation)) {
                    this.operation = operation;
                }
            }

        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }

    @Override
    public int execute() throws Exception {
        // FIXME update this processor to use Stream class!
        throw new Exception("update ToolProcessor to use Stream class");

        /*
         * LOG.debug("execute");          *
         * // get default values for input parameters Map<String, String>
         * inputs = getDefaults();
         *
         * // replace default values by given parameters in the context if(
         * this.context != null ) inputs.putAll(this.context);
         *
         * for( String key : inputs.keySet() ) { LOG.debug("Key: "+key+" =
         * "+inputs.get(key)); }
         *
         * ToolInvoker invoker = new ToolInvoker();
         *
         * // Now invoke the command: return
         * invoker.runCommand(this.operation.getCommand(), inputs, null,
         * this.stdout);
         *
         */
    }

    @Override
    public void initialize() {
    }

    @Override
    public void setContext(Map<String, Stream> streamMap) {
        this.context = streamMap;
    }

    @Override
    public void setStdout(OutputStream out) {
        this.stdout = out;
    }

    @Override
    public Map<String, ParamSpec> getParameters() {
        Map<String, ParamSpec> parameters = new HashMap<String, ParamSpec>();

        if (operation.getInputs() != null) {
            for (Input input : operation.getInputs().getInput()) {
                ParamSpec param = new ParamSpec();
                param.setDirection(ParamSpec.Direction.IN);
                param.setRequired(input.isRequired());
                parameters.put(input.getName(), param);
            }
        }

        if (operation.getOutputs() != null) {
            for (Output output : operation.getOutputs().getOutput()) {
                ParamSpec param = new ParamSpec();
                param.setDirection(ParamSpec.Direction.OUT);
                param.setRequired(output.isRequired());
                parameters.put(output.getName(), param);
            }
        }

        return parameters;
    }

    private Map<String, String> getDefaults() {
        Map<String, String> defaults = new HashMap<String, String>();
        if (operation.getInputs() != null) {
            for (Input input : operation.getInputs().getInput()) {
                defaults.put(input.getName(), input.getDefaultValue());
            }
        }
        return defaults;
    }
}
