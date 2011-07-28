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
package eu.scape_project.xa.tw.cli;

import eu.scape_project.xa.tw.conf.Configuration;
import eu.scape_project.xa.tw.gen.OperationsIterator;
import java.io.File;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.gen.GeneratorException;
import eu.scape_project.xa.tw.gen.types.IOType;
import eu.scape_project.xa.tw.gen.types.MsgType;
import eu.scape_project.xa.tw.util.FileUtil;
import eu.scape_project.xa.tw.gen.PropertiesSubstitutor;
import eu.scape_project.xa.tw.gen.InOutConfigParser;
import eu.scape_project.xa.tw.gen.WsdlCreator;
import eu.scape_project.xa.tw.tmpl.OperationCode;
import eu.scape_project.xa.tw.tmpl.ServiceCode;
import eu.scape_project.xa.tw.tmpl.ServiceXml;
import eu.scape_project.xa.tw.tmpl.ServiceXmlOp;

/**
 * Command line interface of the tool wrapper.
 * This program generates a maven project that can be used to deploy
 * a soap/rest web service.
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class ToolWrapper {

    private static Logger logger = LoggerFactory.getLogger(ToolWrapper.class.getName());
    private static Configuration ioc;

    /**
     * Default constructor
     */
    public ToolWrapper() {
    }

    /**
     * Initialise parameters from command line arguments
     * @param args command line arguments
     * @throws GeneratorException Exception if project generation fails
     */
    private static void initParamsFromArgs(String[] args) throws GeneratorException {

        int currOpid = 0;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = null;
                String argval = null;
                arg = args[i];
                // make sure that access to argument value is not
                // out of bound of the arguments array
                if ((i + 1) < args.length) {
                    argval = args[i + 1];
                }

                // input configuration
                if (arg.equals("-ic") && argval != null) {
                    currOpid++;
                    ioc.setCurrOpid(currOpid);
                    ioc.setCurrInConf(argval);
                }
                // output configuration
                if (arg.equals("-oc") && argval != null) {
                    ioc.setCurrOutConf(argval);
                }
                // project configuration
                if (arg.equals("-pc") && argval != null) {
                    ioc.setProjConf(argval);
                }
                if (ioc.hasInOutConf()) {
                    ioc.addIoConfiguration();
                }
            }
        }
    }

    /**
     * Main method of the command line application
     * @param args Arguments of the command line application
     * @throws GeneratorException Exception if project generation fails 
     */
    public static void main(String[] args) throws GeneratorException {

        ioc = new Configuration();

        // configuration files
        if (args.length == 0) {
            logger.info("Reading default configuration files from working "
                    + "directory ...");
            ioc.setCurrOpid(1);
            ioc.setCurrInConf("default_inputconfig.json");
            ioc.setCurrOutConf("default_outputconfig.json");
            ioc.setProjConf("default.properties");
            if (ioc.hasInOutConf()) {
                ioc.addIoConfiguration();
            }
            logger.info("Use arguments:");
            logger.info("-pc <default.properties> -ic <inputconfig1.json> -oc "
                    + "<outputconfig1.json> [-ic <inputconfig2.json> -oc "
                    + "<outputconfig2.json> ...]");
            logger.info("in order to load other configuration files.");
        } else {
            initParamsFromArgs(args);
        }

        logger.debug("Initialising project properties and substitution variable "
                + "context ...");
        PropertiesSubstitutor st = new PropertiesSubstitutor(ioc.getProjConf());
        File dir = new File(st.getTemplateDir());
        try {
            st.processDirectory(dir);
            String generatedDir = st.getGenerateDir();
            String projMidfix = st.getProjectMidfix();
            String projDir = st.getProjectDirectory();

            // wsdl file
            String wsdlAbsPath = FileUtil.makePath(generatedDir, projDir,
                    "src", "main", "webapp")
                    + projMidfix
                    + ".wsdl";
            logger.debug("WSDL file: " + wsdlAbsPath);

            // service file
            String sjf = FileUtil.makePath(generatedDir, projDir,
                    "src", "main", "java", st.getProjectPackagePath())
                    + projMidfix
                    + ".java";
            logger.debug("Initialising service file: " + sjf);
            InOutConfigParser ioCfgParser = new InOutConfigParser();

            // service java code template
            String serviceTmpl = st.getProp("project.template.service");
            // service operation java code template
            String operationTmpl = st.getProp("project.template.operation");
            ServiceCode sc = new ServiceCode(serviceTmpl);

            // service xml template
            ServiceXml sxml = new ServiceXml("tmpl/servicexml.vm");
            sxml.put(st.getContext());

            OperationsIterator oi = new OperationsIterator(st);
            Iterator itr = oi.iterator();

            while (itr.hasNext()) {

                // operation number
                Integer opnum = ((Integer) itr.next());
                int opn = opnum.intValue();

                // create WSDL
                logger.debug("Inserting data types in WSDL ...");
                WsdlCreator respSdc = new WsdlCreator(st,
                        MsgType.REQUEST, wsdlAbsPath, ioc.getInCfgFile(opn), wsdlAbsPath, opn);
                respSdc.insertDataTypes();
                WsdlCreator reqSdc = new WsdlCreator(st,
                        MsgType.RESPONSE, wsdlAbsPath, ioc.getOutCfgFile(opn), wsdlAbsPath, opn);
                reqSdc.insertDataTypes();

                if (st.getProjectPackagePath() == null || st.getProjectPackagePath().equals("")) {
                    throw new GeneratorException("Project package path is not defined!");
                }

                // Create service operation
                OperationCode oc = new OperationCode(operationTmpl, opn);
                String operationName = st.getProp("service.operation." + opn);
                oc.setOperationName(operationName);
                oc.put("operationname", oc.getOperationName());
                // add main project properties velocity context
                oc.put(st.getContext());
               
                ioCfgParser.setCurrentOperation(oc);

                // Input code section
                logger.debug("Creating input code section ...");
                ioCfgParser.apply(ioc.getInCfgFile(opn), IOType.INPUT);

                // Output code section
                logger.debug("Creating output code section ...");
                ioCfgParser.apply(ioc.getOutCfgFile(opn), IOType.OUTPUT);

                oc.put("inputsection", oc.getInputSection());
                oc.put("outputsection", oc.getOutputSection());
                
                oc.put("servicename", st.getContextProp("project_midfix"));
                oc.put("project_namespace", st.getContextProp("project_namespace"));

                oc.put("outfileitems", oc.getOutFileItems());
                oc.put("resultelements", oc.getResultElements());

                String ops = opnum.toString();
                oc.put("opid", ops);
                oc.evaluate();

                sc.addOperation(oc);

                ServiceXmlOp sxmlop = new ServiceXmlOp("tmpl/servicexmlop.vm");
                sxmlop.put("operationname", oc.getOperationName());
                String clicmd = st.getProp("service.operation." + opn+ ".clicmd");
                sxmlop.put("clicmd", clicmd);
                sxmlop.put("opid", String.valueOf(opn));

                sxmlop.evaluate();

                sxml.addOperation(sxmlop);
            }

            logger.debug("Writing service file: " + sjf);
            // add project properties velocity context
            sc.put(st.getContext());
            sc.put("operations", sc.getOperations());            
            sc.create(sjf);
            String sxmlFile = FileUtil.makePath(generatedDir, projDir,
                    "src/main/webapp/WEB-INF/services",st.getProjectMidfix(),
                    "META-INF")+"services.xml";

            // adding operations to services.xml
            sxml.put("servxmlops", sxml.getOperations());
            logger.debug("Writing services.xml file: " + sxmlFile);

            sxml.create(sxmlFile);
            
            logger.info("Project created in in \"" + FileUtil.makePath(generatedDir, projDir) + "\"");

        } catch (Exception ex) {
            logger.error("Unable to generate project: " + ex.getMessage(), ex);
            throw new GeneratorException("An error occurred, the project has not been created successfully.");
        }
    }
}
