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
package org.opflabs.scape.tb.gw.cli;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.gen.GeneratorException;
import org.opflabs.scape.tb.gw.gen.IOType;
import org.opflabs.scape.tb.gw.gen.Inserter;
import org.opflabs.scape.tb.gw.gen.JavaTypesCreator;
import org.opflabs.scape.tb.gw.gen.MsgType;
import org.opflabs.scape.tb.gw.util.FileUtil;
import org.opflabs.scape.tb.gw.gen.ProjectPropertiesSubstitutor;
import org.opflabs.scape.tb.gw.gen.Snippet;
import org.opflabs.scape.tb.gw.gen.XMLSchemaDatatypesCreator;

/**
 * Command line interface of the generic soap web service wrapper.
 * This program generates an ant-based project that can be used to deploy
 * a soap web service in an axis2 web service container deployed in a servlet
 * container, like apache tomcat, for example.
 * @author onbscs
 * @version 0.1
 */
public class GenericWrapper {

    private static Logger logger = Logger.getLogger(GenericWrapper.class);
    private static Configuration ioc;

    /**
     * Default constructor
     */
    public GenericWrapper() {
    }

    /**
     * Initialise parameters from command line arguments
     * @param args command line arguments
     * @throws GeneratorException Exception if project generation fails
     */
    private static void initialiseParametersFromArgs(String[] args) throws GeneratorException {
        
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
            logger.info("Reading default configuration files from working directory ...");
            ioc.setCurrOpid(1);
            ioc.setCurrInConf("default_inputconfig.json");
            ioc.setCurrOutConf("default_outputconfig.json");
            ioc.setProjConf("default.properties");
            if (ioc.hasInOutConf()) {
                ioc.addIoConfiguration();
            }
            logger.info("Use arguments:");
            logger.info("-pc <default.properties> -ic <inputconfig1.json> -oc <outputconfig1.json> [-ic <inputconfig2.json> -oc <outputconfig2.json> ...]");
            logger.info("in order to load other configuration files.");
        } else {
            initialiseParametersFromArgs(args);
        }

        ProjectPropertiesSubstitutor st = new ProjectPropertiesSubstitutor(ioc.getProjConf());
        File dir = new File(st.getTemplateDir());
        try {
            st.processDirectory(dir);
            String generatedDir = st.getGenerateDir();
            String projMidfix = st.getProjectMidfix();
            String projDir = st.getProjectDirectory();

            // wsdl file
            String wsdlAbsPath = FileUtil.makePath(generatedDir, projDir,
                    st.getProjectResourcesDir())
                    + st.getGlobalProjectPrefix()
                    + projMidfix
                    + "Service.wsdl";
            logger.debug("WSDL file: " + wsdlAbsPath);

            // service skeleton
            String sjf = FileUtil.makePath(generatedDir, projDir,
                    "src", st.getProjectPackagePath(), "service")
                    + st.getGlobalProjectPrefix()
                    + projMidfix
                    + "ServiceSkeleton.java";
            logger.debug("Service Java file: " + sjf);

            OperationsIterator oi = new OperationsIterator(st);
            Iterator itr = oi.iterator();
            while (itr.hasNext()) {

                // operation number
                Integer opnum = ((Integer) itr.next());
                int opn = opnum.intValue();
                String ops = opnum.toString();

                st.addKeyValuePair("#OPID#", ops);

                XMLSchemaDatatypesCreator respSdc = new XMLSchemaDatatypesCreator(st,
                        MsgType.REQUEST, wsdlAbsPath, ioc.getInputConfigurationFile(opn), wsdlAbsPath, opn);
                respSdc.insert();
                XMLSchemaDatatypesCreator reqSdc = new XMLSchemaDatatypesCreator(st,
                        MsgType.RESPONSE, wsdlAbsPath, ioc.getOutputConfigurationFile(opn), wsdlAbsPath, opn);
                reqSdc.insert();

                if (st.getProjectPackagePath() == null || st.getProjectPackagePath().equals("")) {
                    throw new GeneratorException("Project package path is not defined!");
                }

                // input java code
                JavaTypesCreator jtcInput = new JavaTypesCreator(opn, st, IOType.INPUT, ioc.getInputConfigurationFile(opn), sjf, sjf);

                // output java code
                JavaTypesCreator jtcOutput = new JavaTypesCreator(opn, st, IOType.OUTPUT, ioc.getOutputConfigurationFile(opn), sjf, sjf);

                // java operation snippet(s)
                HashMap kvp = new HashMap<String, String>();
                kvp.put("OPID", ops);
                insertJavaSnippet(jtcInput, "ctmpl/snippets/service_operation",
                        "// <!-- insert_mark:operations_code --> //", kvp);
                insertJavaSnippet(jtcInput, "ctmpl/snippets/service_packages",
                        "// <!-- insert_mark:response_request_packages --> //", kvp);
                st.processFile(new File(sjf));

                jtcInput.insert();
                jtcOutput.insert();

                String drv = jtcOutput.getDefaultResponseValues();
                st.addKeyValuePair("RESPONSE_DEFAULT_VALUES", drv);
                st.processFile(new File(sjf));

                // build.properties
                String bpf = FileUtil.makePath(generatedDir, projDir, "build.properties");
                logger.debug("build.properties file path: " + bpf);
                String servCliCmd = st.getPropertyUtils().getProp("service.operation." + ops + ".clicmd");
                if (servCliCmd == null || servCliCmd.equals("")) {
                    throw new GeneratorException("No command line interface command template defined for operation " + ops);
                }
                Inserter bpfIns = new Inserter("//<!-- insert_mark:command_templates -->//", "service.operation." + ops + ".clicmd=" + servCliCmd);
                bpfIns.insert(bpf, bpf);

                // build.xml
                String buildXml = FileUtil.makePath(generatedDir, projDir) + "build.xml";
                logger.debug("build.xml file path: " + buildXml);
                String scp = st.getPropertyUtils().getProp("tomcat.public.scp");
                if (scp == null || scp.equals("")) {
                    logger.warn("Not defined if cp or scp deployment should be used (tomcat.public.scp)");
                }
                String dm = (scp != null && scp.equalsIgnoreCase("true")) ? "scp" : "cp";
                Snippet cpscpSnippet = new Snippet("ctmpl/snippets/" + dm);
                Inserter cpscpIns = new Inserter("<!--// insert_mark:cp_scp_switch //-->", cpscpSnippet.getCode());
                cpscpIns.insert(buildXml, buildXml);

                // services.xml
                String servxmlfile = FileUtil.makePath(generatedDir, projDir, st.getProjectResourcesDir(), "services.xml");
                logger.debug("services.xml file path: " + servxmlfile);
                Snippet snippet = new Snippet("ctmpl/snippets/servicesxml_operations");
                snippet.addKeyValHashMap(kvp);
                Inserter servxmlIns = new Inserter("<!--// insert_mark:webservice_operations //-->", snippet.getCode());
                servxmlIns.insert(servxmlfile, servxmlfile);

                Snippet snippet2 = new Snippet("ctmpl/snippets/servicesxml_clicommands");
                snippet2.addKeyValHashMap(kvp);
                Inserter servXmlCliCmds = new Inserter("<!--// insert_mark:cli_commands //-->", snippet2.getCode());
                servXmlCliCmds.insert(servxmlfile, servxmlfile);

                st.processFile(new File(servxmlfile));
            }

            // libs
            String libDirStr = st.getProjectLibDir();
            File libPath = new File(FileUtil.makePath(generatedDir, projDir, libDirStr));
            if (!libPath.exists()) {
                String generatedDirStr = FileUtil.makePath(generatedDir, projDir, libDirStr);
                logger.info("Copying directory " + libPath + " ...");
                FileUtil.copyDirectory(libPath, FileUtil.makePath(libDirStr), generatedDirStr);
            }
            logger.info("Finished creating project in \"" + FileUtil.makePath(generatedDir, projDir) + "\"");
        } catch (Exception ex) {
            logger.error("Unable to generate project: " + ex.getMessage());
            throw new GeneratorException();
        }
    }

    private static void insertJavaSnippet(JavaTypesCreator jtc, String snippetPath,
            String insertPoint, HashMap<String, String> kvp) throws GeneratorException {
        String packagesCodeSnippet = null;
        Snippet packagesSnippet = new Snippet(snippetPath);
        if (packagesSnippet.canRead()) {
            packagesSnippet.addKeyValHashMap(kvp);
            packagesCodeSnippet = packagesSnippet.getCode();
        } else {
            logger.warn("Unable to read code template: " + snippetPath);
        }
        jtc.insertSnippet(packagesCodeSnippet, insertPoint);
    }
}
