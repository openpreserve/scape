/*
 * Copyright 2012 ait.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scape_project.pt.proc;

import eu.scape_project.pt.pit.ToolRepository;
import eu.scape_project.pt.pit.invoke.CommandNotFoundException;
import eu.scape_project.pt.pit.invoke.Out;
import eu.scape_project.pt.pit.invoke.Stream;
import eu.scape_project.pt.pit.invoke.ToolInvoker;
import eu.scape_project.pt.tool.Input;
import eu.scape_project.pt.tool.Inputs;
import eu.scape_project.pt.tool.Operation;
import eu.scape_project.pt.tool.Operations;
import eu.scape_project.pt.tool.Output;
import eu.scape_project.pt.tool.Tool;
import eu.scape_project.pt.util.ParamSpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private HashMap<String, Stream> context;
    private String strRepo;
    private ToolRepository repo;
    private InputStream stdin;
    private OutputStream stdout;

    /**
     * Sets toolstring and actionstring.
     *
     * @param strTool
     * @param strAction
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
            Logger.getLogger(ToolProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int execute() throws Exception {
        // FIXME update this processor to use Stream class!
        throw new Exception("update ToolProcessor to use Stream class");

        /*
         * LOG.debug("execute");          *
         * // get default values for input parameters HashMap<String, String>
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
    public void setContext(HashMap<String, Stream> hashMap) {
        this.context = hashMap;
    }

    @Override
    public void setStdout(OutputStream out) {
        this.stdout = out;
    }

    @Override
    public HashMap<String, ParamSpec> getParameters() {
        HashMap<String, ParamSpec> parameters = new HashMap<String, ParamSpec>();

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

    private HashMap<String, String> getDefaults() {
        HashMap<String, String> defaults = new HashMap<String, String>();
        if (operation.getInputs() != null) {
            for (Input input : operation.getInputs().getInput()) {
                defaults.put(input.getName(), input.getDefaultValue());
            }
        }
        return defaults;
    }
}
