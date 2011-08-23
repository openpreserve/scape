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
package eu.scape_project.xa.tw.gen;

import eu.scape_project.xa.tw.gen.types.IOType;
import eu.scape_project.xa.tw.tmpl.OperationCode;
import eu.scape_project.xa.tw.tmpl.OutputItemCode;
import eu.scape_project.xa.tw.tmpl.ResultElementCode;
import eu.scape_project.xa.tw.tmpl.SectionCode;
import eu.scape_project.xa.tw.tmpl.ServiceCode;
import eu.scape_project.xa.tw.tmpl.ServiceXml;
import eu.scape_project.xa.tw.tmpl.ServiceXmlOp;
import eu.scape_project.xa.tw.toolspec.Input;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Output;
import eu.scape_project.xa.tw.util.StringConverterUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class ServiceCodeCreator {

    private static Logger logger = LoggerFactory.getLogger(ServiceCodeCreator.class.getName());
    PropertiesSubstitutor st;
    private List<Operation> operations;
    private ServiceCode sc;
    private ServiceXml sxml;

    public ServiceCodeCreator(PropertiesSubstitutor st, ServiceCode sc, ServiceXml sxml, List<Operation> operations) {
        this.st = st;
        this.operations = operations;
        this.sc = sc;
        this.sxml = sxml;
    }

    private ServiceCodeCreator() {
    }

    public void createOperations() throws IOException, GeneratorException {
        for (Operation operation : operations) {
            createOperationCode(operation);
        }
    }

    private void createOperationCode(Operation operation) throws IOException, GeneratorException {

        // Create service operation
        int opn = operation.getOid();
        String operationTmpl = st.getProp("project.template.operation");
        OperationCode oc = new OperationCode(operationTmpl, opn);
        String operationName = operation.getName();
        oc.setOperationName(operationName);
        oc.put("operationname", oc.getOperationName());
        // add main project properties velocity context
        oc.put(st.getContext());


        List<Input> inputs = operation.getInputs().getInput();
        for (Input input : inputs) {
            addDataSection(operation, oc, IOType.INPUT, input.getDatatype(),
                    input.getName(), input.getCliMapping(), null, null);
        }
        List<Output> outputs = operation.getOutputs().getOutput();
        for (Output output : outputs) {
            addDataSection(operation, oc, IOType.OUTPUT, output.getDatatype(),
                    output.getName(), output.getCliMapping(),
                    output.getPrefixFromInput(), output.getExtension());
        }

        oc.put("inputsection", oc.getInputSection());
        oc.put("outputsection", oc.getOutputSection());

        oc.put("servicename", st.getContextProp("project_midfix"));
        oc.put("project_namespace", st.getContextProp("project_namespace"));

        oc.put("outfileitems", oc.getOutFileItems());
        oc.put("resultelements", oc.getResultElements());

        oc.put("opid", String.valueOf(opn));
        oc.evaluate();

        sc.addOperation(oc);

        ServiceXmlOp sxmlop = new ServiceXmlOp("tmpl/servicexmlop.vm");
        sxmlop.put("operationname", oc.getOperationName());
        String clicmd = st.getProp("service.operation." + opn + ".clicmd");
        sxmlop.put("clicmd", clicmd);
        sxmlop.put("opid", String.valueOf(opn));

        sxmlop.evaluate();

        sxml.addOperation(sxmlop);
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
    protected void addDataSection(Operation operation, OperationCode oc, IOType iotype, String dataType, String nodeName, String cliMapping, String prefixFromInput, String extension) throws GeneratorException {
        String opid = String.valueOf(operation.getOid());
        // code template for the current leaf node
        String template = "tmpl/datatypes/" + iotype + "_"
                + StringConverterUtil.typeToFilename(dataType)
                + ".vm";
        logger.debug("Using template \"" + template + "\" for node \"" + nodeName
                + "\" in operation " + opid);
        File templateFile = new File(template);
        if (templateFile.canRead()) {
            try {
                SectionCode sectCode = new SectionCode(template);
                sectCode.put("opid", opid);
                sectCode.put("operationname", operation.getName());
                if (iotype == IOType.INPUT) {
                    sectCode.put("input_variable", nodeName);
                    String mapping = getCliMapping(iotype,cliMapping, dataType, nodeName);
                    sectCode.put("mapping", mapping);
                    String parameter = getOperationParameter(dataType, nodeName);
                    oc.addParameter(parameter);
                    String parList = oc.getParametersCsList();
                    oc.put("parameters", parList);
                    sectCode.evaluate();
                    oc.appendInputSection(sectCode.getCode());
                } else if (iotype == IOType.OUTPUT) {
                    sectCode.put("output_variable", nodeName);
                    sectCode.evaluate();
                    if (dataType.equals("xsd:anyURI")) {
                        OutputItemCode oic = null;
                        // should the output file get the input file name as prefix? Different templates!
                        
                        if (prefixFromInput == null || prefixFromInput.isEmpty()) {
                            oic = new OutputItemCode("tmpl/outfileitem.vm");
                        } else {
                            oic = new OutputItemCode("tmpl/outfileitem_prefix.vm");
                            oic.put("prefix", prefixFromInput);
                        }
                        // put the current context
                        oic.put(oc.getCtx());
                        oic.put("varname", nodeName);

                        String mapping = cliMapping;
                        oic.put("mapping",mapping);

                        oic.put("extension", extension);
                        oic.evaluate();
                        oc.addOutFileItem(oic.getCode());
                    }
                    ResultElementCode rec = new ResultElementCode("tmpl/resultelement.vm");
                    rec.put("varname", nodeName);
                    String serviceresult = null;

                    
                    serviceresult = nodeName+"FileUrl.toString()";
                    
                    rec.put("serviceresult",serviceresult);
                    rec.evaluate();
                    oc.addResultElement(rec.getCode());
                    oc.appendOutputSection(sectCode.getCode());
                }
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
    private String getCliMapping(IOType iotype, String cliMappingVar, String dataType, String nodeName) {

        String mappingVal = null;
        if (cliMappingVar != null) {
            
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
            if (iotype == IOType.INPUT) {
                mappingKeyVal = "cliCmdKeyValPairs.put(\"" + cliMappingVar + "\", " + mappingVal + ");";
            }
            return mappingKeyVal;
        } else {
            return "// No CLI mapping defined for " + nodeName;
        }
    }
}
