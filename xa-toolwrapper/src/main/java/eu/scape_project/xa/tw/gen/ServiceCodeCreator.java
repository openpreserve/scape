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
import eu.scape_project.xa.tw.tmpl.GenericCode;
import eu.scape_project.xa.tw.tmpl.OperationCode;
import eu.scape_project.xa.tw.tmpl.OutputItemCode;
import eu.scape_project.xa.tw.tmpl.ResultElementCode;
import eu.scape_project.xa.tw.tmpl.SectionCode;
import eu.scape_project.xa.tw.tmpl.ServiceCode;
import eu.scape_project.xa.tw.tmpl.ServiceXml;
import eu.scape_project.xa.tw.tmpl.ServiceXmlOp;
import eu.scape_project.xa.tw.toolspec.InOut;
import eu.scape_project.xa.tw.toolspec.Input;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Output;
import eu.scape_project.xa.tw.toolspec.Restriction;
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

    /**
     * Constructor of the service code creator
     * @param st Substitutor, contains global project value-key pairs
     * @param sc Service code, the java code that is created for the service
     * @param sxml services xml, the axis2 web service definition file
     * @param operations The operations that are defined for the service
     */
    public ServiceCodeCreator(PropertiesSubstitutor st, ServiceCode sc, ServiceXml sxml, List<Operation> operations) {
        this.st = st;
        this.operations = operations;
        this.sc = sc;
        this.sxml = sxml;
    }

    /**
     * Constructor
     */
    private ServiceCodeCreator() {
    }

    /**
     * Create operations
     * @throws IOException
     * @throws GeneratorException
     */
    public void createOperations() throws IOException, GeneratorException {
        for (Operation operation : operations) {
            createOperationCode(operation);
        }
    }

    /**
     * Create the code for an operation
     * @param operation
     * @throws IOException
     * @throws GeneratorException
     */
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

        // generate an input data section in the service code for each input field
        List<Input> inputs = operation.getInputs().getInput();
        for (Input input : inputs) {
            addDataSection(operation, oc, input);
        }
        // generate an output data section in the service code for each output field
        List<Output> outputs = operation.getOutputs().getOutput();
        for (Output output : outputs) {
            addDataSection(operation, oc, output);
        }

        // insert input and output data sections for the operation
        oc.put("inputsection", oc.getInputSection());
        oc.put("outputsection", oc.getOutputSection());

        // name of the service and project namespace
        oc.put("servicename", st.getContextProp("project_midfix"));
        oc.put("project_namespace", st.getContextProp("project_namespace"));

        // output files that are used as a command pattern variable
        // (CliMapping is set to a variable of the command pattern) have to
        // be defined before the command line process section.
        oc.put("outfileitems", oc.getOutFileItems());
        oc.put("resultelements", oc.getResultElements());

        // operation id
        oc.put("opid", String.valueOf(opn));
        // velocity variable substitution for the operation
        oc.evaluate();

        // add the operation to the service code
        sc.addOperation(oc);

        // create services xml operation entry
        ServiceXmlOp sxmlop = new ServiceXmlOp("tmpl/servicexmlop.vm");
        sxmlop.put("operationname", oc.getOperationName());
        String clicmd = st.getProp("service.operation." + opn + ".clicmd");
        sxmlop.put("clicmd", clicmd);
        sxmlop.put("opid", String.valueOf(opn));
        // velocity variable substitution for the services xml
        sxmlop.evaluate();
        // add the operation to the services xml
        sxml.addOperation(sxmlop);
    }

    /**
     * Add an input/output data section to the operation
     * @param operation
     * @param oc
     * @param inout
     * @throws GeneratorException
     */
    protected void addDataSection(Operation operation, OperationCode oc,
            InOut inout) throws GeneratorException {

        // input or output field?
        IOType iotype = (inout instanceof Input) ? IOType.INPUT : IOType.OUTPUT;
        // input/output fields
        String nodeName = inout.getName();
        String required = inout.getRequired();
        String dataType = inout.getDatatype();
        String cliMapping = inout.getCliMapping();
        // input specific fields
        Input input = null;
        boolean isInput = false;
        if (inout instanceof Input) {
            input = (Input) inout;
            isInput = true;
        }
        String cliReplacement = (isInput) ? ((Input) inout).getDefault().getClireplacement() : null;
        Restriction restriction = (isInput) ? ((Input) inout).getRestriction() : null;
        // output specific fields
        Output output = null;
        boolean isOutput = false;
        if (inout instanceof Output) {
            output = (Output) inout;
            isOutput = true;
        }
        boolean autoExtension = (isOutput && output.isAutoExtension() != null) ? output.isAutoExtension().booleanValue() : false;
        String prefixFromInput = (isOutput) ? ((Output) inout).getPrefixFromInput() : null;
        String extension = (isOutput) ? ((Output) inout).getExtension() : null;

        boolean isRequired = (required != null && required.equalsIgnoreCase("true"));
        String opid = String.valueOf(operation.getOid());
        // code template for the the field depending on data type
        boolean isMultiple = restriction != null && restriction.isMultiple();
        String template = "tmpl/datatypes/" + iotype + "_"
                + StringConverterUtil.typeToFilename(dataType)
                + (isMultiple ? "_restricted_list" : "") // multiple string list
                + ((dataType.equals("xsd:anyURI") // URL with temporary file
                && !isRequired) ? "_opt" : "") // optional suffix
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
                    String mapping = null;
                    if (cliReplacement == null) {
                        // Simple mapping
                        mapping = getCliMapping(inout);
                    } else {
                        // Mapping of a variable with replacement of null value
                        GenericCode cliReplCode = new GenericCode("tmpl/clireplacement.vm");
                        cliReplCode.put(sectCode.getCtx());
                        cliReplCode.put("clireplacement", cliReplacement);
                        cliReplCode.evaluate();
                        mapping = cliReplCode.getCode();
                    }
                    sectCode.put("mapping", mapping);
                    String parameter = getOperationParameter(input);
                    oc.addParameter(parameter);
                    String parList = oc.getParametersCsList();
                    oc.put("parameters", parList);
                    sectCode.evaluate();
                    oc.appendInputSection(sectCode.getCode());
                } else if (iotype == IOType.OUTPUT) {
                    sectCode.put("output_variable", nodeName);
                    // some tools automatically append the extension to the
                    // result file in this case it is attached again, so the
                    // result for a file with the extension .txt would be
                    // .txt.txt in order to match the output file produced
                    // by the tool.
                    if (autoExtension) {
                        sectCode.put("autoextension", "+\"." + extension + "\"");
                    } else {
                        sectCode.put("autoextension", "");
                    }
                    sectCode.evaluate();
                    if (dataType.equals("xsd:anyURI")) {
                        OutputItemCode oic = null;
                        // should the output file get the input file name as
                        // prefix? Different templates!
                        String outfileId = ((Output) inout).getOutfileId();
                        if (prefixFromInput == null || prefixFromInput.isEmpty()) {
                            if (outfileId != null && !outfileId.equals("")) {
                                oic = new OutputItemCode("tmpl/outfileitem_id.vm");
                                oic.put("outfileid", outfileId);
                            } else {
                                oic = new OutputItemCode("tmpl/outfileitem.vm");
                            }
                        } else {
                            if (outfileId != null && !outfileId.equals("")) {
                                oic = new OutputItemCode("tmpl/outfileitem_prefix_id.vm");
                                oic.put("outfileid", outfileId);
                            } else {
                                oic = new OutputItemCode("tmpl/outfileitem_prefix.vm");
                            }
                            oic.put("prefix", prefixFromInput);
                        }
                        // put the current context
                        oic.put(oc.getCtx());
                        oic.put("varname", nodeName);

                        String mapping = cliMapping;
                        oic.put("mapping", mapping);
                        oic.put("extension", extension);
                        oic.evaluate();
                        oc.addOutFileItem(oic.getCode());
                    }
                    ResultElementCode rec = new ResultElementCode("tmpl/resultelement.vm");
                    rec.put("varname", nodeName);
                    String serviceresult = null;
                    serviceresult = nodeName + "FileUrl.toString()";
                    rec.put("serviceresult", serviceresult);
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
    private String getOperationParameter(Input input) {
        String parameter = null;
        String dataType = input.getDatatype();
        String nodeName = input.getName();
        boolean isMultiple = (input.getRestriction() != null
                && input.getRestriction().isMultiple());
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
            if (isMultiple) {
                parameter = "OMElement " + nodeName;
            } else {
                parameter = "String " + nodeName;
            }
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
    private String getCliMapping(InOut inout) {

        String cliMappingVar = inout.getCliMapping();
        String dataType = inout.getDatatype();
        String nodeName = inout.getName();
        String mappingVal = null;
        if (cliMappingVar != null) {

            if (dataType.equals("xsd:anyURI")) {
                mappingVal = nodeName + "File.getAbsolutePath()";
            }
            if (dataType.equals("xsd:int")) {
                mappingVal = "Integer.toString(" + nodeName + ")";
            }
            if (dataType.equals("xsd:boolean")) {
                mappingVal = "Boolean.toString(" + nodeName + ")";
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
            if (inout instanceof Input) {
                Input input = (Input) inout;
                Restriction restr = input.getRestriction();
                if (restr != null) {
                    boolean isMultiple = restr.isMultiple();
                    if (isMultiple) {
                        mappingVal += "Csv";
                    }
                }
                mappingKeyVal = "cliCmdKeyValPairs.put(\"" + cliMappingVar + "\", " + mappingVal + ");";
            }
            return mappingKeyVal;
        } else {
            return "// No CLI mapping defined for " + nodeName;
        }
    }
}
