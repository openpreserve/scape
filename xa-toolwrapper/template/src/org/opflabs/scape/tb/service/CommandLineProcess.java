/*
 *  Copyright (c) #GLOBAL_YEAR# The #GLOBAL_PROJECT_PREFIX# Project Partners.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package #GLOBAL_PACKAGE_NAME#.service;

import #GLOBAL_PACKAGE_NAME#.util.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 * This class defines a command which can be executed on the command line. The
 * command is constructed on the basis of a pattern that is provided as a
 * string which contains variables of the syntax @variable@. There are
 * required variables which are passed to the builder constructor and
 * other optional variables that can be set using the corresponding build
 * methods, like outFormat, for example. After setting the variable values
 * the will be substituted in the pattern string. The getCommand method then
 * provides a command string list which can be passed to a ProcessBuilder
 * object.
 * The typical use of this class is
 *
 * 1) Define command substitution variables:
 *
 *    HashMap paramValuePairs = new HashMap<String, String>();
 *    paramValuePairs.put(CommandPatternVariables.INFILE, "C:\test\infile.tif");
 *    paramValuePairs.put(CommandPatternVariables.OUTFILE,"C:\test\outfile.tif");
 *
 * 2) Define command pattern:
 *
 *    String cliCmdPattern
 *      = "C:\someapplication.exe @INFILE@ @OUTFILE@";
 *
 * 2) Create CommandLineProcessObject:
 *
 *    CommandLineProcess clp =
 *       new CommandLineProcess(cliCmdPattern, paramValuePairs);
 *
 * 3) Initialise and execute process:
 *
 *    clp.init();
 *    clp.execute();
 *
 * @author #GLOBAL_PROJECT_PREFIX# Project Development Team
 * @version #GLOBAL_WRAPPER_VERSION#
 */
public class CommandLineProcess {

    public static final int MIN_QUERYTOKEN_LENGTH = 4;
    /** Logger */
    private static Logger logger =
            Logger.getLogger(CommandLineProcess.class);
    /** Required - Command line interface command pattern */
    private String pattern;
    /** Command list that can be passed to the process builder */
    private HashMap<String, String> paramValuePairs;
    private List<String> command;
    String[] tokenArr;
    boolean[] isQueryTokenArr;
    private ArrayList<String> outputList;
    private ProcessRunner pr = null;
    /** Return code of the process */
    private int code; // -1 means "undefined", 0 success, >0 error
    String[][] queryResult = null;

    private String processingLog = "";
    public String getProcessingLog() {
        return processingLog;
    }


    {
        command = new ArrayList<String>();
        outputList = new ArrayList<String>();
        paramValuePairs = null;
        pr = new ProcessRunner();
        code = -1; // -1 means "undefined", 0 success, >0 error
    }

    private CommandLineProcess() {
    }

    public CommandLineProcess(String pattern, HashMap paramValuePairs) {
        this.pattern = pattern;
        infolog("Pattern (before substitution): " + pattern);
        this.paramValuePairs = paramValuePairs;
        this.replaceVars();
        this.createCommandList();
    }

    /**
     * Creates the command string list which can be used by the Process builder
     * object.
     */
    private void createCommandList() {
        StringTokenizer st = new StringTokenizer(pattern);
        while (st.hasMoreTokens()) {
            command.add(st.nextToken());
        }
    }

    /**
     * Get the command string list which can be used by the Process builder
     * object.
     */
    public List<String> getCommand() {
        return this.command;
    }

    private void replaceVars() {
        for (String key : paramValuePairs.keySet()) {
            String value = (String) paramValuePairs.get((String) key);
            infolog(key + " substitution variable value: " + value);
            try {
                pattern = pattern.replace("@" + key + "@", value);
            } catch (NullPointerException ex) {
                errorlog("Variable " + key + " is not defined.");
            }
        }
        infolog("Command (after substitution): " + pattern);
    }

    /**
     * Initialise process.
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
    public void execute() {
        pr.run();
        // assign return code from process controller
        code = pr.getCode();
        infolog("Assigned exit code: " + code + "");
    }

    public void submitQueries(final String[] cmdArr) throws IOException, InterruptedException {
        if (pr == null) {
            errorlog("There is no active command line process");
        } else {

            this.tokenArr = cmdArr;
            this.isQueryTokenArr = new boolean[cmdArr.length];

            //errorCatcher = new StreamCatcher(p.getErrorStream())

            StringBuffer sb = new StringBuffer();
            // Submit query for tokens that exceed the minimum length, the other
            // tokens are marked as false in the boolean isQueryTokenArr array.
            int k = 0;
            int m = 0;
            for (int i = 0; i < cmdArr.length; i++) {
                if (tokenArr[i].length() > MIN_QUERYTOKEN_LENGTH) {
                    sb.append(tokenArr[i] + "\n");
                    this.isQueryTokenArr[i] = true;
                    k++;
                } else {
                    this.isQueryTokenArr[i] = false;
                    m++;
                }
            }
            infolog("List of " + k + " query tokens with more than " + MIN_QUERYTOKEN_LENGTH + " characters (" + m + " items skipped, " + (k + m) + " total):\n" + sb.toString());
            File f = FileUtils.writeStringToFile(sb.toString(), FileUtils.getTmpFile("commands", "tmp"));
            InputStream is = FileUtils.getInputStreamFromFile(f);

            pr.setProcessInputStream(is);
            pr.run();

            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getStdInputStream()));
            String currentLine = null;
            int l = 0;
            while ((currentLine = br.readLine()) != null) {
                outputList.add(currentLine);
                infolog("Output " + l + ": " + currentLine);
                l++;
            }
            // assign return code from process controller
            code = pr.getCode();
            infolog("Assigned exit code: " + code + "");

            infolog("Output list has " + outputList.size() + " items");
        }
    }

    public String[][] getResultOutput() throws InterruptedException {

        queryResult = new String[tokenArr.length][];
        infolog("Result array for " + tokenArr.length + " tokens created.");

        // TODO create output array
        for (int i = 0, j = 0; i < tokenArr.length; i++) {
            String[] queryResultPair = new String[2];
            queryResultPair[0] = tokenArr[i];
            if (isQueryTokenArr[i]) {
                try {
                    queryResultPair[1] = outputList.get(j);
                } catch (IndexOutOfBoundsException ex) {
                    errorlog("Error accessing element at " + j);
                    System.exit(0);
                }
                j++;
            } else {
                queryResultPair[1] = "--NONE--";
            }
            queryResult[i] = queryResultPair;
        }
        return queryResult;
    }

    public int getCode() {
        return code;
    }

    public InputStream getErrorInputStream() {
        return pr.getErrInputStream();
    }

    public InputStream getStdInputStream() {
        return pr.getStdInputStream();
    }


    private void infolog(String msg) {
        processingLog += msg +".\n";
        logger.info(msg);
    }

    private void errorlog(String msg) {
        processingLog += "ERROR: "+msg +".\n";
        logger.error(msg);
    }
}
