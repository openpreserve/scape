/*
 *  Copyright 2011 IMPACT (www.impact-project.eu)/SCAPE (www.scape-project.eu)
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

package org.opflabs.scape.tb.gw.gen;

import java.io.File;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.opflabs.scape.tb.gw.util.FileUtil;
import org.opflabs.scape.tb.gw.util.StringConverterUtil;

/**
 * Java Types Creator
 * @author onbscs
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
    int opid;
    private String defaultResponseValues;

    public JavaTypesCreator() {
    }

    public JavaTypesCreator(int opid, ProjectPropertiesSubstitutor st, IOType msgType, String jsonConfig, String srcFileAbsPath, String trgtFileAbsPath) {
        this.srcFileAbsPath = srcFileAbsPath;
        this.trgtFileAbsPath = trgtFileAbsPath;
        this.jsonConfig = jsonConfig;
        codeSb = new StringBuilder("");
        codeReqSb = new StringBuilder("");
        this.ioType = msgType;
        this.st = st;
        this.opid = opid;
        defaultResponseValues = "";
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
            throw new GeneratorException("Service source does not exist.");
        }
        String codePoint = "//<!-- " + ioType + "_code -->//\n";
        codeSb.append(codePoint);
        codeSb.append("\n");

        apply(this.jsonConfig);

        serviceSource = serviceSource.replaceAll(codePoint, codeSb.toString());

        serviceSource = serviceSource.replaceAll("//<!-- " + ioType.INPUT + "_code -->//\n", "//<!-- " + ioType.INPUT + "_code -->//\n" + codeReqSb.toString());

        File targetFile = FileUtil.writeStringToFile(serviceSource, (trgtFileAbsPath));
        if (targetFile.exists()) {
            logger.debug("Target file " + targetFile.getPath() + " created.");
        } else {
            logger.error("Unable to create target file " + targetFile.getPath() + "");
        }
    }

    /**
     * Apply substitution
     * @param srcFileAbsPath Source file
     * @param trgtFileAbsPath Target file
     */
    public void insertSnippet(String snipStr, String point) throws GeneratorException {
        File srcFile = new File(srcFileAbsPath);
        StringBuilder strBuf = new StringBuilder();
        if (srcFile.exists()) {
            logger.debug("Template file " + srcFile.getPath() + " exists.");
        } else {
            logger.error("Unable to find template file " + srcFile.getPath() + "");
        }
        // Service source
        String serviceSource = FileUtil.readTxtFileIntoString(srcFile);
        if (serviceSource == null) {
            throw new GeneratorException("Service source does not exist.");
        }
        strBuf.append(point);
        strBuf.append(snipStr);

        serviceSource = serviceSource.replaceAll(point, strBuf.toString());

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
            snippet.addKeyValuePair("OPID", Integer.toString(opid));
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
            String setterVal = getSpace(2)+"response"+opid+"Obj." + getSetterName(nodeName) + "(" + nodeName + ");\n";
            outMappingVal += setterVal;
            //response1Obj.setProcessingLog(processing_log);
            defaultResponseValues +=getSpace(2)+"response"+opid+"Obj." + getSetterName(nodeName) + "(" + outMappingVar + ");\n";
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
                codeReqSb.append(getSpace(2)+"URI "+ prefixVar + "TmpUri = request"+opid+"Obj."+getGetterName(prefixVar)+"();\n");
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

    public String getDefaultResponseValues() {
        return defaultResponseValues;
    }
}
