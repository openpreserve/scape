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

import eu.scape_project.xa.tw.gen.types.MsgType;
import eu.scape_project.xa.tw.toolspec.Input;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Output;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class WsdlCreator {

    private static Logger logger = LoggerFactory.getLogger(WsdlCreator.class.getName());
    PropertiesSubstitutor st;
    private String wsdlAbsPath;
    private Document doc;
    private List<Operation> operations;

    public WsdlCreator(PropertiesSubstitutor st, String wsdlAbsPath, List<Operation> operations) {
        this.wsdlAbsPath = wsdlAbsPath;
        this.st = st;
        this.operations = operations;
    }

    public WsdlCreator() {
    }

    /**
     * Insert data types
     */
    public void insertDataTypes() throws GeneratorException {
        File wsdlTemplate = new File(this.wsdlAbsPath);
        if (!wsdlTemplate.canRead()) {
            throw new GeneratorException("Unable to read WSDL Template file: " + this.wsdlAbsPath);
        }
        try {

            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuildFact.newDocumentBuilder();
            doc = docBuilder.parse(this.wsdlAbsPath);

            for (Operation operation : operations) {

                createMessageDataTypes(operation);

                createSchemaDataTypes(operation);

                createOperation(doc, operation);

                createBinding(doc, operation);


            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            FileOutputStream fos = new FileOutputStream(this.wsdlAbsPath);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
        } catch (Exception ex) {
            logger.error("An exception occurred: " + ex.getMessage());
        }
    }

    private void createMessageDataTypes(Operation operation) {
        Node definitionsNode = doc.getDocumentElement();
        NodeList portTypeNodeList = doc.getElementsByTagName("wsdl:portType");
        Node portTypeNode = portTypeNodeList.item(0);
        List<Input> inputs = operation.getInputs().getInput();
        createMessageDataType(definitionsNode, portTypeNode, MsgType.REQUEST, operation.getOid());
        List<Output> outputs = operation.getOutputs().getOutput();
        createMessageDataType(definitionsNode, portTypeNode, MsgType.RESPONSE, operation.getOid());

    }

    private void createMessageDataType(Node definitionsNode, Node portTypeNode, MsgType type, int oid) {
        Element partElm = doc.createElement("wsdl:part");
        partElm.setAttribute("name", type + String.valueOf(oid));
        partElm.setAttribute("element", "tns:" + type + String.valueOf(oid));
        Element msgElm = doc.createElement("wsdl:message");
        msgElm.setAttribute("name", type + String.valueOf(oid));
        msgElm.appendChild(partElm);
        definitionsNode.insertBefore(msgElm, portTypeNode);
    }

    /**
     * Create XMLSchema data types
     * @param doc XML document
     * @param rootJsn Root Json node
     */
    private void createSchemaDataTypes(Operation operation) {
        String opid = String.valueOf(operation.getOid());
        NodeList schemaNodeList = doc.getElementsByTagName("xsd:schema");
        Node schemaNode = schemaNodeList.item(0);

        createSchemaDataType(operation, MsgType.REQUEST, schemaNode, opid);
        createSchemaDataType(operation, MsgType.RESPONSE, schemaNode, opid);

    }

    private void createSchemaDataType(Operation operation, MsgType type, Node schemaNode, String oid) {
        Element cplxReqTypeElm = doc.createElement("xsd:complexType");
        cplxReqTypeElm.setAttribute("name", type + oid + "Type");
        Element reqTypeSeqElm = doc.createElement("xsd:sequence");
        if (type.equals(MsgType.REQUEST)) {
            List<Input> inputs = operation.getInputs().getInput();
            for (Input input : inputs) {
                createMsgElm(reqTypeSeqElm, input.getName(), input.getDatatype(),
                        input.getDefault(), input.getCliMapping(), input.getRequired(),
                        input.getDocumentation());
            }
        }
        if (type.equals(MsgType.RESPONSE)) {
            List<Output> outputs = operation.getOutputs().getOutput();
            for (Output output : outputs) {
                createMsgElm(reqTypeSeqElm, output.getName(), output.getDatatype(),
                        null, output.getCliMapping(), output.getRequired(),
                        output.getDocumentation());
            }
        }
        cplxReqTypeElm.appendChild(reqTypeSeqElm);
        schemaNode.appendChild(cplxReqTypeElm);
        Element msgElm = doc.createElement("xsd:element");
        msgElm.setAttribute("name", type + oid);
        msgElm.setAttribute("type", "tns:" + type + oid + "Type");
        schemaNode.appendChild(msgElm);
    }

    /**
     * 
     * @param reqTypeSeqElm
     * @param name
     * @param dataType
     * @param defaultVal
     * @param cliMapping
     * @param required
     * @param documentation
     */
    private void createMsgElm(Node reqTypeSeqElm, String name, String dataType,
            String defaultVal, String cliMapping, String required, String documentation) {
        Element reqTypeElm = doc.createElement("xsd:element");
        if (documentation != null) {
            Element annotationElm = doc.createElement("xsd:annotation");
            Element documentationElm = doc.createElement("xsd:documentation");
            documentationElm.setTextContent(documentation);
            annotationElm.appendChild(documentationElm);
            reqTypeElm.appendChild(annotationElm);
        }
        boolean isRequired = (required != null && required.equalsIgnoreCase("true"));
        if (defaultVal != null) {
            logger.debug("Default value: " + defaultVal);
            reqTypeElm.setAttribute("default", defaultVal);
        }
        reqTypeElm.setAttribute("name", name);
        reqTypeElm.setAttribute("minOccurs", ((isRequired) ? "1" : "0"));
        reqTypeElm.setAttribute("maxOccurs", "1");
        reqTypeElm.setAttribute("type", dataType);
        reqTypeSeqElm.appendChild(reqTypeElm);
    }

    private void createOperation(Document doc, Operation operation) throws GeneratorException {

        //        <wsdl:operation name="simpleCopy">
        //            <wsdl:documentation>Copy a source file to a target file</wsdl:documentation>
        //            <wsdl:input message="tns:Request1" wsaw:Action="simpleCopy">
        //                <wsdl:documentation/>
        //            </wsdl:input>
        //            <wsdl:output message="tns:Response1" wsaw:Action="http://schemas.xmlsoap.org/wsdl/SCAPESimpleCopy10ServicePortType/Response1">
        //                <wsdl:documentation/>
        //            </wsdl:output>
        //        </wsdl:operation>
        String opid = String.valueOf(operation.getOid());
        NodeList portTypeNodeList = doc.getElementsByTagName("wsdl:portType");
        Node portTypeNode = portTypeNodeList.item(0);

        Element operationElm = doc.createElement("wsdl:operation");
        String servOpStr = operation.getName();

        operationElm.setAttribute("name", servOpStr);

        Element documentationElm = doc.createElement("wsdl:documentation");
        String servDocStr = operation.getDescription();
        if (servDocStr == null || servDocStr.equals("")) {
            throw new GeneratorException("No documentation for service operation id " + opid + " available ");
        }
        documentationElm.setTextContent(servDocStr);

        Element inputElm = doc.createElement("wsdl:input");
        inputElm.setAttribute("message", "tns:Request" + opid);
        inputElm.setAttribute("wsaw:Action", servOpStr);
        Element subDocElm = doc.createElement("wsdl:documentation");
        inputElm.appendChild(subDocElm);

        Element outputElm = doc.createElement("wsdl:output");
        outputElm.setAttribute("message", "tns:Response" + opid);
        outputElm.setAttribute("wsaw:Action", "http://schemas.xmlsoap.org/wsdl/" + st.getGlobalProjectPrefix() + st.getProjectMidfix() + "ServicePortType/Response" + opid);
        Element subDocOutElm = doc.createElement("wsdl:documentation");
        outputElm.appendChild(subDocOutElm);

        documentationElm.setTextContent(servDocStr);

        operationElm.appendChild(documentationElm);
        operationElm.appendChild(inputElm);
        operationElm.appendChild(outputElm);

        portTypeNode.appendChild(operationElm);

    }

    private void createBinding(Document doc, Operation operation) throws GeneratorException {
        String opid = String.valueOf(operation.getOid());
        NodeList bindingNodeList = doc.getElementsByTagName("wsdl:binding");
        String servOpStr = operation.getName();
        if (servOpStr == null || servOpStr.equals("")) {
            throw new GeneratorException("No service operation defined for operation " + opid);
        }

        //        <wsdl:operation name="simpleCopy">
        //            <soap:operation soapAction="urn:simpleCopy" style="document"/>
        //            <wsdl:input>
        //                <soap:body use="literal"/>
        //            </wsdl:input>
        //            <wsdl:output>
        //                <soap:body use="literal"/>
        //            </wsdl:output>
        //        </wsdl:operation>
        Node binding1Node = bindingNodeList.item(0);
        Element operation1Elm = doc.createElement("wsdl:operation");
        operation1Elm.setAttribute("name", servOpStr);
        Element soapOp1Elm = doc.createElement("soap:operation");
        soapOp1Elm.setAttribute("soapAction", "urn:" + servOpStr);
        soapOp1Elm.setAttribute("style", "document");
        operation1Elm.appendChild(soapOp1Elm);
        Element input1Elm = doc.createElement("wsdl:input");
        Element soap1Elm1 = doc.createElement("soap:body");
        soap1Elm1.setAttribute("use", "literal");
        input1Elm.appendChild(soap1Elm1);
        Element output1Elm = doc.createElement("wsdl:output");
        Element soap1Elm2 = doc.createElement("soap:body");
        soap1Elm2.setAttribute("use", "literal");
        output1Elm.appendChild(soap1Elm2);
        operation1Elm.appendChild(input1Elm);
        operation1Elm.appendChild(output1Elm);
        binding1Node.appendChild(operation1Elm);

        //        <wsdl:operation name="simpleCopy">
        //            <soap12:operation soapAction="urn:simpleCopy" style="document"/>
        //            <wsdl:input>
        //                <soap12:body use="literal"/>
        //            </wsdl:input>
        //            <wsdl:output>
        //                <soap12:body use="literal"/>
        //            </wsdl:output>
        //        </wsdl:operation>
        Node binding2Node = bindingNodeList.item(1);
        Element operation2Elm = doc.createElement("wsdl:operation");
        operation2Elm.setAttribute("name", servOpStr);
        Element soapOp2Elm = doc.createElement("soap12:operation");
        soapOp2Elm.setAttribute("soapAction", "urn:" + servOpStr);
        soapOp2Elm.setAttribute("style", "document");
        operation2Elm.appendChild(soapOp2Elm);
        Element input2Elm = doc.createElement("wsdl:input");
        Element soap2Elm1 = doc.createElement("soap12:body");
        soap2Elm1.setAttribute("use", "literal");
        input2Elm.appendChild(soap2Elm1);
        Element output2Elm = doc.createElement("wsdl:output");
        Element soap2Elm2 = doc.createElement("soap12:body");
        soap2Elm2.setAttribute("use", "literal");
        output2Elm.appendChild(soap2Elm2);
        operation2Elm.appendChild(input2Elm);
        operation2Elm.appendChild(output2Elm);
        binding2Node.appendChild(operation2Elm);

        //        <wsdl:operation name="simpleCopy">
        //            <http:operation location="SimpleCopy10/simpleCopy"/>
        //            <wsdl:input>
        //                <mime:content type="text/xml" part="simpleCopy"/>
        //            </wsdl:input>
        //            <wsdl:output>
        //                <mime:content type="text/xml" part="simpleCopy"/>
        //            </wsdl:output>
        //        </wsdl:operation>
        Node binding3Node = bindingNodeList.item(2);
        Element operation3Elm = doc.createElement("wsdl:operation");
        operation3Elm.setAttribute("name", servOpStr);
        Element soapOp3Elm = doc.createElement("http:operation");
        soapOp3Elm.setAttribute("location", st.getProjectMidfix() + "/" + servOpStr);
        operation3Elm.appendChild(soapOp3Elm);
        Element input3Elm = doc.createElement("wsdl:input");
        Element soap3Elm1 = doc.createElement("mime:content");
        soap3Elm1.setAttribute("type", "text/xml");
        soap3Elm1.setAttribute("part", servOpStr);
        input3Elm.appendChild(soap3Elm1);
        Element output3Elm = doc.createElement("wsdl:output");
        Element soap3Elm2 = doc.createElement("mime:content");
        soap3Elm2.setAttribute("type", "text/xml");
        soap3Elm2.setAttribute("part", servOpStr);
        output3Elm.appendChild(soap3Elm2);
        operation3Elm.appendChild(input3Elm);
        operation3Elm.appendChild(output3Elm);
        binding3Node.appendChild(operation3Elm);

    }
}
