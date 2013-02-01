package eu.scape_project.pt.proc;

import eu.scape_project.pt.repo.ToolRepository;
import eu.scape_project.pt.invoke.Stream;
import eu.scape_project.pt.invoke.ToolInvoker;
import eu.scape_project.pt.invoke.ToolSpecNotFoundException;
import eu.scape_project.pt.tool.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * Underlying sub-process.
     */
    private Process proc;

    /**
     * Constructs the processor with a tool and an action of a
     * toolspec.
     *
     * @param Tool tool
     * @param Operation operation
     */
    public ToolProcessor(Tool tool) {
        this.tool = tool;
        debugToken = 'T';
    }

    public Operation findOperation( String strOp ) {
        LOG.debug("findOperation(" + strOp + ")");
        Operations operations = tool.getOperations();
        for (Operation op : operations.getOperation()) {
            LOG.debug("op = " + op.getName());
            if (op.getName().equals(strOp)) {
                return op;
            }
        }
        return null;
    }

    public void setOperation( Operation op ) {
        this.operation = op;
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

		proc = Runtime.getRuntime().exec(strCmd);

        this.setStdIn(proc.getOutputStream());
        this.setStdOut(proc.getInputStream());

        new Thread(this).start();

        if( this.next != null )
            return this.next.execute();

        return proc.waitFor();
    }

    /** 
     * Waits for the sub-process to terminate.
     * 
     * @return
     * @throws InterruptedException 
     */
    @Override
    public int waitFor() throws InterruptedException {
        if( proc == null ) return 0;
        LOG.debug("waitFor");
        return proc.waitFor();
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
        LOG.debug("getInputFileParameters");
        if( this.mapInputFileParameters != null )
            return this.mapInputFileParameters;
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getInputs() != null) {
            for (Input input : operation.getInputs().getInput()) {
                LOG.debug("input = " + input.getName());
                parameters.put(input.getName(), input.getDefaultValue());
            }
        }
        return this.mapInputFileParameters = parameters;

    }

    public Map<String, String> getOutputFileParameters() {
        LOG.debug("getOutputFileParameters");
        if( this.mapOutputFileParameters != null )
            return this.mapOutputFileParameters;
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getOutputs() != null) {
            for (Output output : operation.getOutputs().getOutput()) {
                LOG.debug("output = " + output.getName());
                parameters.put(output.getName(), null);
            }
        }
        return this.mapOutputFileParameters = parameters;

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
         if( mapInputs.isEmpty() ) return strCmd;
        // create the pattern wrapping the keys with ${} and join them with '|'
        String regexp = "";
        for( String input : mapInputs.keySet())
            regexp += parameterToVariable(input);
        regexp = regexp.substring(0, regexp.length()-1);

        LOG.debug("replaceAll.regexp = " + regexp );
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(strCmd);

        while (m.find())
        {
            String param = variableToParameter(m.group());
            m.appendReplacement(sb, mapInputs.get(param));
        }
        m.appendTail(sb);

        return sb.toString();

	}

    private String parameterToVariable( String strParameter ) {
        return "\\$\\{" + strParameter + "\\}|"; 
    }

    private String variableToParameter( String strVariable ) {
        return strVariable.substring(2, strVariable.length() - 1 );
    }
}
