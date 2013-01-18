package eu.scape_project.pt.proc;

import eu.scape_project.pt.pit.ToolRepository;
import eu.scape_project.pt.pit.invoke.Stream;
import eu.scape_project.pt.pit.invoke.ToolInvoker;
import eu.scape_project.pt.tool.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
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
public class ToolProcessor extends Processor {

    private static Log LOG = LogFactory.getLog(ToolProcessor.class);
    /**
     * Operation of a Tool to use.
     */
    private Operation operation;
    /**
     * Tool to use.
     */
    private Tool tool;
    /**
     * Map data input/output.
     */
    private Map<String, Stream> context;
    /**
     * ToolRepository to fetch tools from.
     */
    private ToolRepository repo;
    /**
     * Parameters referring to input files.
     */
    private Map<String, String> mapInputFileParameters;
    /**
     * Parameters referring to output files.
     */
    private Map<String, String> mapOutputFileParameters;

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
        LOG.debug("execute");

        Map<String, String> allInputs = new HashMap<String, String>();
        allInputs.putAll(getInputFileParameters());
        allInputs.putAll(getOutputFileParameters());
        allInputs.putAll(getOtherParameters());

        for (String key : allInputs.keySet()) {
            LOG.debug("Key: " + key + " = " + allInputs.get(key));
        }

        String strCmd = replaceAll(this.operation.getCommand(), allInputs);

        ToolInvoker invoker = new ToolInvoker();

        return invoker.runCommand( strCmd, isIn, osOut);
    }

    @Override
    public void initialize() {
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

    public Map<String, String> getInputFileParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getInputs() != null) {
            for (Input input : operation.getInputs().getInput()) {
                parameters.put(input.getName(), input.getDefaultValue());
            }
        }
        return parameters;

    }

    public Map<String, String> getOutputFileParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getOutputs() != null) {
            for (Output output : operation.getOutputs().getOutput()) {
                parameters.put(output.getName(), null);
            }
        }
        return parameters;

    }

    public Map<String, String> getOtherParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getInputs() != null) {
            for (Parameter param : operation.getInputs().getParameter()) {
                parameters.put(param.getName(), param.getDefaultValue());
            }
        }
        return parameters;

    }

    public void setInputFileParameters(Map<String, String> mapTempInputFileParameters) {
        this.mapInputFileParameters = mapTempInputFileParameters;
    }

    public void setOutputFileParameters(Map<String, String> mapTempOutputFileParameters) {
        this.mapOutputFileParameters = mapTempOutputFileParameters;
    }

    /**
     * Replaces ${key}s in given command strCmd by values.
     * 
     * @param strCmd
     * @param mapInputs 
     */
	 private String replaceAll(String strCmd, Map<String,String> mapInputs) {
        // create the pattern joining the keys with '|'
        String regexp = StringUtils.join(
                mapInputs.keySet().toArray( new String[mapInputs.size()]), "|");
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(strCmd);

        while (m.find())
            m.appendReplacement(sb, mapInputs.get(m.group()));
        m.appendTail(sb);

        return sb.toString();

	}
}
