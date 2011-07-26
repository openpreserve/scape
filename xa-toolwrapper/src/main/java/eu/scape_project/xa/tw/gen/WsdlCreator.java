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

import eu.scape_project.xa.tw.gen.types.MsgType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XMLSchemaDatatypesCreator
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class WsdlCreator {

    private static Logger logger = LoggerFactory.getLogger(WsdlCreator.class.getName());
    private final File wsdlTemplate;
    private final File configFile;
    private final File resultWsdl;
    private final String type;
    private Document doc;
    private Node schemaNode;
    private Element reqTypeSeqElm;
    private boolean isFirstLevel;
    private int opid;
    PropertiesSubstitutor st;
    private MsgType msgtype;

    /**
     * Get message type
     * @return the type
     */
    public String getType() {
        return type;
    }

    private enum Nodetype {

        NODE, LEAF
    }

    public static enum Type {

        Response, Request
    }

    /**
     * Constructor
     * @param type Type (Request/Response)
     * @param wsdlTemplate WSDL Template
     * @param configFile Json configuration file
     * @param resultWsdl Result WSDL
     */
    public WsdlCreator(PropertiesSubstitutor st, MsgType type, String wsdlTemplate, String configFile, String resultWsdl, int opid) {
        // private Constructor can only be called from Builder
        this.wsdlTemplate = new File(wsdlTemplate);
        this.configFile = new File(configFile);
        this.resultWsdl = new File(resultWsdl);
        this.type = type.toString();
        isFirstLevel = true;
        this.opid = opid;
        this.st = st;
        this.msgtype = type;
    }

    private void createMessageDataTypes(Document doc) {
        Node definitionsNode = doc.getDocumentElement();
        NodeList portTypeNodeList = doc.getElementsByTagName("wsdl:portType");
        Node portTypeNode = portTypeNodeList.item(0);
        Element partElm = doc.createElement("wsdl:part");
        partElm.setAttribute("name", this.type + opid);
        partElm.setAttribute("element", "tns:" + this.type + opid);
        Element msgElm = doc.createElement("wsdl:message");
        msgElm.setAttribute("name", this.type + opid);
        msgElm.appendChild(partElm);
        definitionsNode.insertBefore(msgElm, portTypeNode);
    }



    private void createOperation(Document doc) throws GeneratorException {

        NodeList portTypeNodeList = doc.getElementsByTagName("wsdl:portType");
        Node portTypeNode = portTypeNodeList.item(0);

        Element operationElm = doc.createElement("wsdl:operation");
        String servOpStr = st.getPropertyUtils().getProp("service.operation." + opid);
        if (servOpStr == null || servOpStr.equals("")) {
            throw new GeneratorException("No service operation defined for operation " + opid);
        }
        operationElm.setAttribute("name", servOpStr);

        Element documentationElm = doc.createElement("wsdl:documentation");
        String servDocStr = st.getPropertyUtils().getProp("service.operation." + opid+ ".description");
        if (servDocStr == null || servDocStr.equals("")) {
            throw new GeneratorException("No service operation defined for operation " + opid);
        }
        documentationElm.setTextContent(servDocStr);

        Element inputElm = doc.createElement("wsdl:input");
        inputElm.setAttribute("message", "tns:Request"+opid);
        inputElm.setAttribute("wsaw:Action", servOpStr);
        Element subDocElm =  doc.createElement("wsdl:documentation");
        inputElm.appendChild(subDocElm);

        Element outputElm = doc.createElement("wsdl:output");
        outputElm.setAttribute("message", "tns:Response"+opid);
        outputElm.setAttribute("wsaw:Action", "http://schemas.xmlsoap.org/wsdl/"+st.getGlobalProjectPrefix()+st.getProjectMidfix()+"ServicePortType/Response"+opid);
        Element subDocOutElm =  doc.createElement("wsdl:documentation");
        outputElm.appendChild(subDocOutElm);

        documentationElm.setTextContent(servDocStr);

        operationElm.appendChild(documentationElm);
        operationElm.appendChild(inputElm);
        operationElm.appendChild(outputElm);

        portTypeNode.appendChild(operationElm);

    }
    private void createBinding(Document doc) throws GeneratorException {

        NodeList bindingNodeList = doc.getElementsByTagName("wsdl:binding");
        Node bindingNode = bindingNodeList.item(0);

        Element operationElm = doc.createElement("wsdl:operation");
        String servOpStr = st.getPropertyUtils().getProp("service.operation." + opid);
        if (servOpStr == null || servOpStr.equals("")) {
            throw new GeneratorException("No service operation defined for operation " + opid);
        }
        operationElm.setAttribute("name", servOpStr);

        Element inputElm = doc.createElement("wsdl:input");
        Element soapElm =  doc.createElement("soap:body");
        soapElm.setAttribute("use", "literal");
        inputElm.appendChild(soapElm);
        Element outputElm = doc.createElement("wsdl:output");
        Element soapElm2 =  doc.createElement("soap:body");
        soapElm2.setAttribute("use", "literal");
        outputElm.appendChild(soapElm2);

        operationElm.appendChild(inputElm);
        operationElm.appendChild(outputElm);

        bindingNode.appendChild(operationElm);

    }

    /**
     * Create XMLSchema data types
     * @param doc XML document
     * @param rootJsn Root Json node
     */
    private void createSchemaDataTypes(Document doc, JsonNode rootJsn) {

        Iterator<String> itstr = rootJsn.getFieldNames();

        NodeList schemaNodeList = doc.getElementsByTagName("xsd:schema");
        schemaNode = schemaNodeList.item(0);

        Element cplxReqTypeElm = doc.createElement("xsd:complexType");
        cplxReqTypeElm.setAttribute("name", this.type + opid + "Type");
        reqTypeSeqElm = doc.createElement("xsd:sequence");
        cplxReqTypeElm.appendChild(reqTypeSeqElm);

        traverse(rootJsn);

        schemaNode.appendChild(cplxReqTypeElm);

        Element msgElm = doc.createElement("xsd:element");
        msgElm.setAttribute("name", type + opid);
        msgElm.setAttribute("type", "tns:" + type + opid + "Type");
        schemaNode.appendChild(msgElm);

    }

    /**
     * Insert data types
     */
    public void insertDataTypes() throws GeneratorException {
        if (!wsdlTemplate.canRead() || !configFile.canRead()) {
            if (!wsdlTemplate.canRead()) {
                logger.error("Unable to read WSDL Template file: " + this.getWsdlTemplatePath());
            }
            if (!configFile.canRead()) {
                logger.error("Unable to read service creation config file: " + getConfigFile().getAbsolutePath());
            }
            return;
        }
        try {

            FileInputStream fis = new FileInputStream(getConfigFile());

            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuildFact.newDocumentBuilder();
            doc = docBuilder.parse(getWsdlTemplate());

            // Json object mapper
            ObjectMapper mapper = new ObjectMapper();
            // Root node
            JsonNode rootJsn = mapper.readTree(fis);

            createMessageDataTypes(doc);

            createSchemaDataTypes(doc, rootJsn);

            if(this.msgtype == MsgType.REQUEST) {

                createOperation(doc);
                
                createBinding(doc);
            }

            fis.close();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            FileOutputStream fos = new FileOutputStream(getResultWsdl());
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
        } catch (Exception ex) {
            logger.error("An exception occurred: " + ex.getMessage());
        }
    }

    /**
     * Traverse the Json tree
     * @param jsn Root Json node
     */
    private void traverse(JsonNode jsn) {
        Iterator<JsonNode> jsnIt = jsn.getElements();
        for (Iterator<String> itstr = jsn.getFieldNames(); itstr.hasNext();) {
            String currStr = itstr.next();
            JsonNode currJsn = jsnIt.next();
            if (currJsn.has("Datatype")) {
                if (isFirstLevel) {
                    createMsgElm(currStr, currJsn, Nodetype.LEAF);
                }
                createLeaf(currStr, currJsn);
            } else {
                createNode(currStr, currJsn);
                if (isFirstLevel) {
                    createMsgElm(currStr, currJsn, Nodetype.NODE);
                }
                traverse(currJsn);
            }

        }
        isFirstLevel = false;
    }

    /**
     * Create node element
     * @param currStr
     * @param jsn
     */
    private void createNode(String currStr, JsonNode jsn) {

        Element cplxElm = doc.createElement("xsd:complexType");
        cplxElm.setAttribute("name", currStr);
        Element seqElm = doc.createElement("xsd:sequence");
        Iterator<JsonNode> jsnIt = jsn.getElements();
        for (Iterator<String> itstr = jsn.getFieldNames(); itstr.hasNext();) {
            String thisstr = itstr.next();
            logger.info(thisstr);
            JsonNode currJsn = jsnIt.next();
            if (currJsn.has("Datatype")) {
                logger.info("data");
                Element elementElm = doc.createElement("xsd:element");
                elementElm.setAttribute("name", thisstr);
                elementElm.setAttribute("type", currJsn.findValue("Datatype").getTextValue());

                seqElm.appendChild(elementElm);

            }
            cplxElm.appendChild(seqElm);
            this.schemaNode.appendChild(cplxElm);

        }
    }

    /**
     * Create data lement
     * @param variableNameString
     * @param currJsn
     */
    private void createLeaf(String variableNameString, JsonNode currJsn) {

        // Variable name
        logger.debug("Variable name: " + variableNameString);

        // Only used if if Cardinality is "list"
        String varListName = variableNameString.substring(0, 1).toUpperCase() + variableNameString.substring(1) + "s";

        // Data type
        JsonNode dataTypeJsn = currJsn.get("Datatype");
        logger.debug("Datatype: " + dataTypeJsn.getTextValue());

        // Configuration nodes
        JsonNode restrJsn = currJsn.get("Restriction");
        JsonNode cardJsn = currJsn.get("Cardinality");
        boolean isList = (cardJsn != null && cardJsn.getTextValue().equalsIgnoreCase("list"));
        JsonNode defJsn = currJsn.get("Default");
        String defaultVal = (defJsn == null) ? null : defJsn.getTextValue();

        // Restricted type
        if (restrJsn != null) {
            Element simpleTypeElm = doc.createElement("xsd:simpleType");
            simpleTypeElm.setAttribute("name", variableNameString);
            Element restrictionElm = doc.createElement("xsd:restriction");
            restrictionElm.setAttribute("base", dataTypeJsn.getTextValue());
            for (Iterator<JsonNode> it2 = restrJsn.getElements(); it2.hasNext();) {
                JsonNode restrValJsn = it2.next();
                logger.debug("Restriction value: " + restrValJsn.getTextValue());
                Element restrValEnumElm = doc.createElement("xsd:enumeration");
                restrValEnumElm.setAttribute("value", restrValJsn.getTextValue());
                restrictionElm.appendChild(restrValEnumElm);
            }
            simpleTypeElm.appendChild(restrictionElm);
            schemaNode.appendChild(simpleTypeElm);

            // Create list type if Cardinality is "list"
            if (isList) {
                Element complexTypeElm = doc.createElement("xsd:complexType");
                complexTypeElm.setAttribute("name", varListName);
                Element sequenceElm = doc.createElement("xsd:sequence");
                complexTypeElm.appendChild(sequenceElm);
                Element elementElm = doc.createElement("xsd:element");
                JsonNode defaultJsn = currJsn.get("Default");
                if (defaultVal != null) {
                    elementElm.setAttribute("default", defaultVal);
                }
                elementElm.setAttribute("maxOccurs", "unbounded");
                elementElm.setAttribute("minOccurs", "0");
                elementElm.setAttribute("name", variableNameString);
                elementElm.setAttribute("type", "tns:" + variableNameString);
                sequenceElm.appendChild(elementElm);
                schemaNode.appendChild(complexTypeElm);
            }
        }
    }

    /**
     * Create request type element
     * @param currStr Current node name
     * @param currJsn Current node
     * @param nt Node type
     */
    private void createMsgElm(String currStr, JsonNode currJsn, Nodetype nt) {
        // Only used if if Cardinality is "list"
        String varListName = currStr.substring(0, 1).toUpperCase() + currStr.substring(1) + "s";
        Element reqTypeElm = doc.createElement("xsd:element");

                if (currJsn.has("Documentation")) {
                    Element annotationElm = doc.createElement("xsd:annotation");
                    Element documentationElm = doc.createElement("xsd:documentation");
                    documentationElm.setTextContent(currJsn.findValue("Documentation").getTextValue());
                    annotationElm.appendChild(documentationElm);
                    reqTypeElm.appendChild(annotationElm);
                }
        JsonNode defaultJn = currJsn.findValue("Default");
        JsonNode cardJsn = currJsn.get("Cardinality");
        boolean isList = (cardJsn != null && cardJsn.getTextValue().equalsIgnoreCase("list"));
        JsonNode reqJsn = currJsn.findValue("Required");
        boolean isRequired = (reqJsn != null && reqJsn.getTextValue().equalsIgnoreCase("true"));
        if (defaultJn != null && nt != Nodetype.NODE && !isList) {
            logger.debug("Default value: " + defaultJn.getTextValue());
            reqTypeElm.setAttribute("default", defaultJn.getTextValue());
        }
        reqTypeElm.setAttribute("name", currStr);
        reqTypeElm.setAttribute("minOccurs", ((isRequired) ? "1" : "0"));
        reqTypeElm.setAttribute("maxOccurs", "1");

        JsonNode dataTypeJsn = currJsn.findValue("Datatype");
        JsonNode restrJsn = currJsn.findValue("Restriction");
        // Assign namespace tns to data type restricted data ypes
        if (restrJsn != null) {
            reqTypeElm.setAttribute("type", "tns:" + ((isList) ? varListName : currStr));
            // Assign user defined xsd data type
        } else {
            reqTypeElm.setAttribute("type", dataTypeJsn.getTextValue());
        }

        reqTypeSeqElm.appendChild(reqTypeElm);
    }

    /**
     * @return the wsdlTemplate
     */
    public String getWsdlTemplatePath() {
        return wsdlTemplate.getAbsolutePath();
    }

    /**
     * @return the configFile
     */
    public String getConfigFilePath() {
        return configFile.getAbsolutePath();
    }

    /**
     * @return the resultWsdl
     */
    public String getResultWsdlPath() {
        return resultWsdl.getAbsolutePath();
    }

    /**
     * @return the wsdlTemplate
     */
    public File getWsdlTemplate() {
        return wsdlTemplate;
    }

    /**
     * @return the configFile
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * @return the resultWsdl
     */
    public File getResultWsdl() {
        return resultWsdl;
    }
}
