/*
 * Copyright 2011 The SCAPE Project Consortium
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
package eu.scape_project.xa.tw.gen;

import eu.scape_project.xa.tw.gen.types.IOType;
import eu.scape_project.xa.tw.tmpl.OperationCode;
import eu.scape_project.xa.tw.tmpl.SectionCode;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonNode;
import eu.scape_project.xa.tw.util.StringConverterUtil;
import java.io.File;

/**
 * Input/output configuration parser which reads the configuration files and
 * creates the code generation objects.
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public final class InOutConfigParser extends JsonTraverser {

   
    private static Logger logger = LoggerFactory.getLogger(InOutConfigParser.class.getName());
    protected OperationCode currentOp;

    /**
     * Initialise the service code creator with the project properties substitutor
     * @param opid Operation id
     * @param propSubst Properties subsitutor
     * @param inputJsonConfig Json configuration
     * @param jsf Java service file
     */
    public InOutConfigParser() throws IOException {
    }

    @Override
    protected void processNode(String nodeName, JsonNode currJsn) {
        logger.debug("No node processing of json tree");
    }

    @Override
    protected void processLeaf(String nodeName, JsonNode currJsn, String dataType) {
        logger.debug("No leaf processing of json tree");
    }

    @Override
    protected void processNodeFirstLevel(String nodeName, JsonNode currJsn) {
        logger.debug("No node processing for first level of json tree");
    }

    /**
     * Processing the first level of the JSON tree. Each leaf node is a variable
     * with the name "nodeName". The corresponding template is loaded and the
     * key-value pairs for substitution added.
     * @param nodeName Name of the node which is the input/output variable name
     * @param currJsn Current json node
     * @param dataType Data type (xsd:string, xsd:integer, etc.)
     * @throws GeneratorException
     */
    @Override
    protected void processLeafFirstLevel(String nodeName, JsonNode currJsn, String dataType) throws GeneratorException {

        // code template for the current leaf node
        String template = "tmpl/datatypes/" + this.iotype + "_"
                + StringConverterUtil.typeToFilename(dataType)
                + ((this.isRestrCurrJsn) ? "_restricted" : "")
                + ((this.isListCurrJsn) ? "_list" : "")
                + ".vm";
        logger.debug("Using template \"" + template + "\" for node \"" + nodeName
                + "\" in operation " + currentOp.getOpid());
        File templateFile = new File(template);
        if (templateFile.canRead()) {
            try {
                SectionCode sectCode = new SectionCode(template);


                sectCode.put("opid", Integer.toString(currentOp.getOpid()));
                if (this.iotype == IOType.INPUT) {
                    sectCode.put("input_variable", nodeName);
                    String mapping = getCliMapping(currJsn, dataType, nodeName);
                    sectCode.put("mapping", mapping);
                    String parameter = getOperationParameter(dataType, nodeName);
                    currentOp.addParameter(parameter);
                    String parList = currentOp.getParametersCsList();
                    currentOp.put("parameters", parList);
                    sectCode.evaluate();
                    currentOp.appendInputSection(sectCode.getCode());
                } else if (this.iotype == IOType.OUTPUT) {
                    // TODO: output code section
                    sectCode.put("output_variable", nodeName);
                    sectCode.evaluate();
                    this.currentOp.appendOutputSection(sectCode.getCode());
                }
                currentOp.evaluate();
            } catch (IOException ex) {
                logger.error("Unable to create code for template: " + template);
            }
        } else {
            throw new GeneratorException("Unable to read code template: " + template);
        }

    }

    /**
     * Get java data type definition for input/output datatype definition
     * @param dataType Input/output datatype definition
     * @param nodeName Current node name
     * @return Java data type definition
     */
    private String getOperationParameter(String dataType, String nodeName) {
        String parameter = null;
        if (dataType.equals("xsd:anyURI")) {
            parameter = "String " + nodeName;
        }
        if (dataType.equals("xsd:int")) {
            parameter = "Integer " + nodeName;
        }
        if (dataType.equals("xsd:boolean")) {
            parameter = "Boolean " + nodeName;
        }
        if (dataType.equals("xsd:string")) {
            parameter = "String " + nodeName;
        }
        if (parameter == null) {
            parameter = "null";
        }
        return parameter;
    }


    /**
     * Get CLI mapping expression that assigns a value to the CLI replacement
     * variable.
     * @param currJsn Current Json node
     * @param dataType Data type
     * @param nodeName Node name
     * @return CLI mapping
     */
    private String getCliMapping(JsonNode currJsn, String dataType, String nodeName) {
        JsonNode cliMappingJsn = currJsn.findValue("CliMapping");
        String mappingVar = null;
        String mappingVal = null;
        if (cliMappingJsn != null) {
            mappingVar = cliMappingJsn.getTextValue();
            if (dataType.equals("xsd:anyURI")) {
                mappingVal = nodeName + "File.getAbsolutePath()";
            }
            if (dataType.equals("xsd:int")) {
                mappingVal = "Integer.toString(" + nodeName + ")";
            }
            if (dataType.equals("xsd:boolean")) {
                mappingVal = nodeName;
            }
            if (dataType.equals("xsd:string")) {
                mappingVal = nodeName;
            }
            if (mappingVal == null) {
                mappingVal = "\"TODO: Set value\"";
            }
            // assign the variable value to a command line interface pattern
            // variable if it is defined by the CliMapping type (only INPUT
            // types are mapped to command line interface pattern variables
            String mappingKeyVal = "";
            if (this.iotype == IOType.INPUT) {
                mappingKeyVal = "cliCmdKeyValPairs.put(\"" + mappingVar + "\", " + mappingVal + ");";
            }
            return mappingKeyVal;
        } else {
            return "// No CLI mapping defined for " + nodeName;
        }
    }

    public void setCurrentOperation(OperationCode oc) {
        this.currentOp = oc;
    }

}
