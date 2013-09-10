package eu.scape_project.pt.proc;

import eu.scape_project.pt.repo.ToolRepository;
import eu.scape_project.pt.tool.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * Other Parameters. 
     */
    private Map<String, String> mapOtherParameters;

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

    /**
     * Tries to find a operation of the tool.
     * 
     * @param Operation strOp
     * @return 
     */
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

    /**
     * Sets the operation to use for execution. 
     * 
     * @param op 
     */
    public void setOperation( Operation op ) {
        this.operation = op;
    }

    /**
     * Executes the tool, optionally reading from a previous process (stdin).
     * All input file parameters need to be local to the machine.
     * 
     * @return
     * @throws Exception 
     */
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

    /**
     * Fills all types of parameters with given Map of parameters.
     * 
     * @param mapParams 
     */
    public void setParameters( Map<String, String> mapParams) {
        for(Entry<String, String> entry: mapParams.entrySet() ) 
            if( getInputFileParameters().containsKey(entry.getKey()))
                getInputFileParameters().put(entry.getKey(), entry.getValue());
            else if( getOutputFileParameters().containsKey(entry.getKey()))
                getOutputFileParameters().put(entry.getKey(), entry.getValue());
            else if( getOtherParameters().containsKey(entry.getKey()))
                getOtherParameters().put(entry.getKey(), entry.getValue());
    }

    /**
     * Get input file parameters from the toolspec.
     * 
     * @return 
     */
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

    /**
     * Get output file parameters from the toolspec.
     * 
     * @return 
     */
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

    /**
     * Gets other input parameters from the toolspec.
     * 
     * @return 
     */
    public Map<String, String> getOtherParameters() {
        if( this.mapOtherParameters != null )
            return this.mapOtherParameters;
        Map<String, String> parameters = new HashMap<String, String>();

        if (operation.getInputs() != null) {
            for (Parameter param : operation.getInputs().getParameter()) {
                parameters.put(param.getName(), param.getDefaultValue());
            }
        }
        return this.mapOtherParameters = parameters;

    }

    /**
     * Sets input file parameters.
     * 
     * @param mapTempInputFileParameters 
     */
    public void setInputFileParameters(Map<String, String> mapInput) {
        this.mapInputFileParameters = mapInput;
    }

    /**
     * Sets output file parameters.
     * 
     * @param mapTempInputFileParameters 
     */
    public void setOutputFileParameters(Map<String, String> mapOutput) {
        this.mapOutputFileParameters = mapOutput;
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
            regexp += parameterToPlaceholder(input);
        regexp = regexp.substring(0, regexp.length()-1);

        LOG.debug("replaceAll.regexp = " + regexp );
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(strCmd);

        while (m.find())
        {
            String param = placeholderToParameter(m.group());
            m.appendReplacement(sb, mapInputs.get(param));
        }
        m.appendTail(sb);

        return sb.toString();

	}

    /**
     * Maps a parameter name to the placeholder's form.
     * Inverse of placeholderToParameter.
     * 
     * @param strParameter
     * @return 
     */
    private String parameterToPlaceholder( String strParameter ) {
        return "\\$\\{" + strParameter + "\\}|"; 
    }

    /**
     * Maps a placeholder to its parameter name.
     * Inverse of parameterToPlaceholder.
     * 
     * @param strVariable
     * @return 
     */
    private String placeholderToParameter( String strVariable ) {
        return strVariable.substring(2, strVariable.length() - 1 );
    }
}
