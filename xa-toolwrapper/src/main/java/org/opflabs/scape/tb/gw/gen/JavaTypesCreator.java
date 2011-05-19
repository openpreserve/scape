/*******************************************************************************
 * Copyright (c) 2011 The IMPACT/SCAPE Project Partners.
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.opflabs.scape.tb.gw.gen;

import java.io.File;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.opflabs.scape.tb.gw.util.FileUtil;
import org.opflabs.scape.tb.gw.util.StringConverterUtil;

/**
 * JavaTypesCreator
 * @author IMPACT/SCAPE Project Development Team
 * @version 0.1
 */
public final class JavaTypesCreator extends JsonTraverser implements Insertable {

    private static Logger logger = Logger.getLogger(JavaTypesCreator.class);
    String srcFileAbsPath;
    String trgtFileAbsPath;
    String jsonConfig;
    StringBuilder codeSb;
    StringBuilder codeReqSb;
    IOType ioType;
    JsonNode cliMappingJsn;
    String mappingVar;
    ProjectPropertiesSubstitutor st;

    public JavaTypesCreator() {
    }

    public JavaTypesCreator(ProjectPropertiesSubstitutor st, IOType msgType, String jsonConfig, String srcFileAbsPath, String trgtFileAbsPath) {
        this.srcFileAbsPath = srcFileAbsPath;
        this.trgtFileAbsPath = trgtFileAbsPath;
        this.jsonConfig = jsonConfig;
        codeSb = new StringBuilder("");
        codeReqSb = new StringBuilder("");
        this.ioType = msgType;
        this.st = st;
    }

    protected String getTypeName(String varName) {
        StringBuilder sb = new StringBuilder();
        sb.append(varName.substring(0, 1).toUpperCase());
        sb.append(varName.substring(1));
        return sb.toString();
    }

    protected String getGetterName(String varName) {
        StringBuilder sb = new StringBuilder("get");
        sb.append(getTypeName(varName));
        return sb.toString();
    }

    protected String getSetterName(String varName) {
        StringBuilder sb = new StringBuilder("set");
        sb.append(getTypeName(varName));
        return sb.toString();
    }

    /**
     * Apply substitution
     * @param srcFileAbsPath Source file
     * @param trgtFileAbsPath Target file
     */
    @Override
    public void insert() throws GeneratorException {
        File srcFile = new File(srcFileAbsPath);
        if (srcFile.exists()) {
            logger.debug("Template file " + srcFile.getPath() + " exists.");
        } else {
            logger.error("Unable to find template file " + srcFile.getPath() + "");
        }
        // Service source
        String serviceSource = FileUtil.readTxtFileIntoString(srcFile);
        if (serviceSource == null) {
            logger.error("Service source does not exist.");
            throw new GeneratorException();
        }
        String codePoint = "// <!-- " + ioType + " code java types - do not remove! -->\n";
        codeSb.append(codePoint);
        codeSb.append("\n");

        apply(this.jsonConfig);

        serviceSource = serviceSource.replaceAll(codePoint, codeSb.toString());

        serviceSource = serviceSource.replaceAll("// <!-- " + ioType.INPUT + " code java types - do not remove! -->\n", "// <!-- " + ioType.INPUT + " code java types - do not remove! -->\n" + codeReqSb.toString());

        File targetFile = FileUtil.writeStringToFile(serviceSource, (trgtFileAbsPath));
        if (targetFile.exists()) {
            logger.debug("Target file " + targetFile.getPath() + " created.");
        } else {
            logger.error("Unable to create target file " + targetFile.getPath() + "");
        }
    }

    @Override
    protected void processNode(String nodeName, JsonNode currJsn) {
    }

    @Override
    protected void processNodeFirstLevel(String nodeName, JsonNode currJsn) {
    }

    @Override
    protected void processLeaf(String nodeName, JsonNode currJsn, String dataType) {
    }

    @Override
    protected void processLeafFirstLevel(String nodeName, JsonNode currJsn, String dataType) {

        String snippetName = ioType + "_"
                + StringConverterUtil.typeToFilename(dataType)
                + ((this.isRestrCurrJsn) ? "_restricted" : "")
                + ((this.isListCurrJsn) ? "_list" : "");
        // Snippet
        Snippet snippet = new Snippet("ctmpl/" + snippetName);
        if (snippet.canRead()) {
            snippet.addKeyValuePair("INPUT_VARIABLE", nodeName);
            String getterName = this.getGetterName(nodeName);
            snippet.addKeyValuePair("GETTER_NAME", getterName);
            String typeName = this.getTypeName(nodeName);
            snippet.addKeyValuePair("TYPE_NAME", typeName);
            JsonNode defaultJn = currJsn.findValue("Default");
            if (defaultJn != null) {
                snippet.addKeyValuePair("DEFAULT", defaultJn.getTextValue());
            }
            addCliMapping(snippet, currJsn, dataType, nodeName);
            addOutMappingJsn(snippet, currJsn, nodeName);
            addOutputFile(currJsn, nodeName);
            String code = snippet.getCode();
            codeSb.append(code);
            codeSb.append("\n");
        } else {
            logger.warn("Unable to read code template: " + snippetName);
        }

    }

    private void addCliMapping(Snippet snippet, JsonNode currJsn, String dataType, String nodeName) {
        cliMappingJsn = currJsn.findValue("CliMapping");
        mappingVar = null;
        String mappingVal = null;
        if (cliMappingJsn != null) {
            mappingVar = cliMappingJsn.getTextValue();
            if (dataType.equals("xsd:anyURI")) {
                mappingVal = nodeName + ".getAbsolutePath()";
            }
            if (dataType.equals("xsd:int")) {
                mappingVal = "Integer.toString("+nodeName + ")";
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

            // mapping
            String mappingKeyVal = "";
            if (ioType == IOType.INPUT) {
                mappingKeyVal = "cliCmdKeyValPairs.put(\"" + mappingVar + "\", " + mappingVal + ");";
            }
            snippet.addKeyValuePair("MAPPING", mappingKeyVal);
        } else {
            snippet.addKeyValuePair("MAPPING", "// No CLI mapping defined for "+nodeName);
        }
    }

    private void addOutMappingJsn(Snippet snippet, JsonNode currJsn, String nodeName) {
        JsonNode outMappingJsn = currJsn.findValue("OutMapping");
        String outMappingVar = null;
        String outMappingVal = null;
        if (outMappingJsn != null) {
            outMappingVar = outMappingJsn.getTextValue();

            outMappingVal = nodeName + " = " + outMappingVar + ";\n";
            outMappingVal += getSpace(2)+"responseObj." + getSetterName(nodeName) + "(" + nodeName + ");\n";

            snippet.addKeyValuePair("OUTMAPPING", outMappingVal);
        } else {
            snippet.addKeyValuePair("OUTMAPPING", "// No OUT mapping defined for "+nodeName);
        }
    }

    private void addOutputFile(JsonNode currJsn, String nodeName) {
        if (ioType == IOType.OUTPUT && cliMappingJsn != null) {
            JsonNode extJsn = currJsn.findValue("Extension");
            String extension = null;
            extension = (extJsn == null) ? "tmp" : extJsn.getTextValue();
            boolean hasPrefix = false;
            JsonNode pfiJsn = currJsn.findValue("PrefixFromInput");
            if(pfiJsn != null) {
                codeReqSb.append(getSpace(2)+"String origFileName = \"\";\n");
                String prefixVar =  pfiJsn.getTextValue();
                codeReqSb.append(getSpace(2)+"URI "+ prefixVar + "TmpUri = requestObj."+getGetterName(prefixVar)+"();\n");
                codeReqSb.append(getSpace(2)+"origFileName = StringUtils.getFilenameFromURI("+prefixVar+"TmpUri, true);\n");
                hasPrefix = true;
            }
            // File name for output files 
            String tmpFileArg = "\"";
            if(hasPrefix) {
                tmpFileArg = "origFileName+\"_";
            }
            tmpFileArg += st.getGlobalProjectPrefix()+st.getProjectMidfix()+"Service"+"_"+ nodeName + "_\",\"" + extension + "\"";
            String ofn = getSpace(2)+"String " + nodeName + "Name = FileUtils.getTmpFile("+tmpFileArg + ").getAbsolutePath();\n";
            codeReqSb.append(ofn);
            
            String ofn2 = getSpace(2)+"cliCmdKeyValPairs.put(\"" + mappingVar + "\", " + nodeName + "Name);\n";
            codeReqSb.append(ofn2);
        }
    }

    private String getSpace(int level) {
        StringBuilder spaceSb = new StringBuilder();
        for(int i = 0; i<level; i++) {
            spaceSb.append("    ");
        }
        return spaceSb.toString();
    }
}
