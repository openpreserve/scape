/*
 *  Copyright 2011 The SCAPE Project Consortium.
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
package eu.scape_project.xa.tw.tmpl;

import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation code that can be evaluated by the Velocity template
 * engine. Evaluation is performed by applying the Velocity context to the
 * source code content.
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class OperationCode extends Code {

    /** Logger */
    private static Logger logger = LoggerFactory.getLogger(OperationCode.class.getName());
    
    private int opid;
    private String operationName;
    private ArrayList<String> parameters;
    private StringBuilder inputSection;
    private StringBuilder outputSection;

    public OperationCode(String filePath, int opid) throws IOException {
        super(filePath);
        parameters = new ArrayList<String>();
        this.opid = opid;
        inputSection = new StringBuilder();
        outputSection = new StringBuilder();
    }

    /**
     * @param operationSnippet the operationSnippets to add
     */
    public void addParameter(String parameter) {
        parameters.add(parameter);
    }

    /**
     * @return the operationName
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * @param operationName the operationName to set
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * @return the parameters
     */
    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getParametersCsList() {
        logger.debug(parameters.size() + " parameters in parameter list of "
                + "operation \""+this.operationName + "\"");
        String result = "";
        if (parameters == null || parameters.isEmpty()) {
            result = "";
        } else {
            for (String parameter : parameters) {
                result += parameter + ", ";
            }
            result = result.substring(0, result.length() - 2);

        }
        return result;
    }

    /**
     * @return the opid
     */
    public int getOpid() {
        return opid;
    }

    /**
     * @param opid the opid to set
     */
    public void setOpid(int opid) {
        this.opid = opid;
    }

    public void appendInputSection(String code) {
        inputSection.append(code);
    }

    public void appendOutputSection(String code) {
        outputSection.append(code);
    }

    /**
     * @return the inputSection
     */
    public String getInputSection() {
        return inputSection.toString();
    }

    /**
     * @return the outputSection
     */
    public String getOutputSection() {
        return outputSection.toString();
    }
}
