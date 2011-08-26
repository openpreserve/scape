/*
 * Copyright ${global_year} The ${global_project_prefix} Project Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package ${global_package_name}.proc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines a command which can be executed on the command line. The
 * command is constructed on the basis of a pattern that is provided as a string
 * which contains variables of the syntax #variable#. There are required
 * variables which are passed to the builder constructor and other optional
 * variables that can be set using the corresponding build methods, like
 * outFormat, for example. After setting the variable values the will be
 * substituted in the pattern string. The getCommand method then provides a
 * command string list which can be passed to a ProcessBuilder object. The
 * typical use of this class is
 *
 * 1) Define command substitution variables:
 *
 * HashMap paramValuePairs = new HashMap<String, String>();
 * paramValuePairs.put(CommandPatternVariables.INFILE, "C:\test\infile.tif");
 * paramValuePairs.put(CommandPatternVariables.OUTFILE,"C:\test\outfile.tif");
 *
 * 2) Define command pattern:
 *
 * String cliCmdPattern = "cp $INFILE $OUTFILE";
 *
 * 2) Create CommandLineProcessObject:
 *
 * CommandLineProcess clp = new CommandLineProcess(cliCmdPattern,
 * paramValuePairs);
 *
 * 3) Initialise and execute process:
 *
 * clp.init(); clp.execute();
 *
 * @author ${global_project_prefix} Project Consortium
 * @version ${global_wrapper_version}
 */
public class CommandLineProcess {

    public static final int MIN_QUERYTOKEN_LENGTH = 4;
    /* Logger */
    private static Logger logger = LoggerFactory.getLogger(CommandLineProcess.class.getName());
    /* Required - Command line interface command pattern */
    private String pattern;
    /* Command list that can be passed to the process builder */
    private HashMap<String, String> paramValuePairs;
    private List<String> commands;
    private ProcessRunner pr = null;
    /* Return code of the process */
    private int code; // -1 means "undefined", 0 success, >0 error
    private String processingLog = "";
    private boolean sh;
    private String output;
    private VelocityEngine ve;

    public String getProcessingLog() {
        return processingLog;
    }

    {
        commands = new ArrayList<String>();
        paramValuePairs = null;
        pr = new ProcessRunner();
        code = -1; // -1 means "undefined", 0 success, >0 error
    }

    private CommandLineProcess() {
        ve = new VelocityEngine();
        ve.init();
    }

    /**
     *
     * @param pattern
     * @param paramValuePairs
     * @param sh
     *         invoke the shell with the command line as the parameter
     */
    public CommandLineProcess(String pattern, HashMap paramValuePairs, boolean sh) {
        this();
        this.sh = sh;
        this.pattern = pattern;
        infolog("Pattern (before substitution): " + pattern);
        this.paramValuePairs = paramValuePairs;
        this.replaceVars();
        this.createCommandList();
    }

    public CommandLineProcess(List<String> commands) {
        this();
        for (String command : commands) {
            this.commands.add(command);
        }
    }

    /**
     * Creates the command string list which can be used by the Process builder
     * object.
     */
    private void createCommandList() {
        if (sh) {

            // commands.add("/bin/sh");
            commands.add("sh");
            commands.add("-c");
            commands.add(pattern);
        } else {
            StringTokenizer st = new StringTokenizer(pattern);
            while (st.hasMoreTokens()) {
                commands.add(st.nextToken());
            }
        }
    }

    /**
     * Get the command string list which can be used by the Process builder object.
     */
    public List<String> getCommand() {
        return this.commands;
    }

    private void replaceVars() {
        for (String key : paramValuePairs.keySet()) {
            String value = paramValuePairs.get(key);
            infolog(key + " substitution variable value: " + value);
            try {
                pattern = pattern.replace("${" + key + "}", value);
            } catch (NullPointerException ex) {
                errorlog("Variable " + key + " is not defined.");
            }
        }
        infolog("Command (after substitution): " + pattern);
    }

    /**
     * Initialise process.
     *
     * @throws java.io.IOException
     */
    public void init() throws IOException {
        List<String> cmd = getCommand();
        if (cmd == null) {
            errorlog("No command defined. Unable to start command line process");
        } else {
            pr.setCommandList(cmd);
        }
    }

    /**
     * Execute process withouth further input
     */
    public int execute() {
        pr.run();
        // assign return code from process controller
        code = pr.getCode();
        //String toolMsg = FileUtils.getStringFromInputStream(pr.getStdInputStream());
        StringWriter writer = new StringWriter();
        if (pr.getStdInputStream() != null) {
            try {
                IOUtils.copy(pr.getStdInputStream(), writer);
            } catch (IOException ex) {
                logger.warn("Unable to read standard output of tool message");
            }
            String toolMsg = writer.toString();
            output = toolMsg;
            if (toolMsg != null && !toolMsg.equals("")) {
                debuglog(toolMsg);
            }
        }
        infolog("Assigned exit code: " + code + "");
        if (pr.getErrInputStream() != null) {
            try {
                IOUtils.copy(pr.getStdInputStream(), writer);
            } catch (IOException ex) {
                logger.warn("Unable to read standard output of tool message");
            }
            String toolMsg = writer.toString();
            if (output == null || output.equals("")) {
                output = toolMsg;
            } else {
                output += toolMsg;
            }
            if (toolMsg != null && !toolMsg.equals("")) {
                debuglog(toolMsg);
            }
        }
        return code;
    }

    public int getCode() {
        return code;
    }

    public String getOutput() {
        return output;
    }

    private void debuglog(String msg) {
        logger.debug(msg);
        processingLog += msg + ".\n";
    }

    private void infolog(String msg) {
        logger.info(msg);
        processingLog += msg + ".\n";
    }

    private void errorlog(String msg) {
        logger.error(msg);
        processingLog += "ERROR: " + msg + ".\n";
    }
}
