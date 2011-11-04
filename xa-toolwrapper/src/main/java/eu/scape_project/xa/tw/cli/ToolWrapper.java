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
import java.io.File;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.gen.GeneratorException;
import eu.scape_project.xa.tw.gen.DeploymentCreator;
import eu.scape_project.xa.tw.gen.PropertiesSubstitutor;
import eu.scape_project.xa.tw.gen.ServiceCodeCreator;
import eu.scape_project.xa.tw.gen.ServiceDef;
import eu.scape_project.xa.tw.gen.WsdlCreator;
import eu.scape_project.xa.tw.tmpl.ServiceCode;
import eu.scape_project.xa.tw.tmpl.ServiceXml;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Service;
import eu.scape_project.xa.tw.toolspec.Toolspec;
import eu.scape_project.xa.tw.util.FileUtil;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * Command line interface of the tool wrapper.
 * This program generates a maven project that can be used to deploy
 * a soap/rest web service.
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.3
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
                // project configuration
                if (arg.equals("-pc") && argval != null) {
                    ioc.setProjConf(argval);
                }
                // xml configuration
                if (arg.equals("-xc") && argval != null) {
                    ioc.setXmlConf(argval);
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
            ioc.setXmlConf("default.xml");
            ioc.setProjConf("toolwrapper.properties");
            logger.info("Use arguments:");
            logger.info("-pc <toolwrapper.properties> -xc <toolspec.xml>");
            logger.info("in order to load other configuration files.");
        } else {
            initParamsFromArgs(args);
        }
        if (!ioc.hasConfig()) {
            throw new GeneratorException("No configuration available.");
        }
        JAXBContext context;
        try {
            context = JAXBContext.newInstance("eu.scape_project.xa.tw.toolspec");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc.getXmlConf()));
            // general tool specification properties
            logger.info("Toolspec model: " + toolspec.getModel());
            logger.info("Tool id: " + toolspec.getId());
            logger.info("Tool name: " + toolspec.getName());
            logger.info("Tool version: " + toolspec.getVersion());

            List<Service> services = toolspec.getServices().getService();
            // for each service a different maven project will be generated
            for (Service service : services) {
                createService(service, toolspec.getVersion());
            }
        } catch (IOException ex) {
            logger.error("An IOException occurred", ex);
        } catch (JAXBException ex) {
            logger.error("JAXBException", ex);
            throw new GeneratorException("Unable to create XML binding for toolspec");
        }
    }

    private static void createService(Service service, String toolVersion) throws GeneratorException, IOException {
        logger.info("Service id: " + service.getSid());
        logger.info("Service name: " + service.getName());
        logger.info("Service type: " + service.getType());
        // Properties substitutor is created for each service
        PropertiesSubstitutor st = new PropertiesSubstitutor(ioc.getProjConf());
        // Service name is composed of Service Name and Tool Version
        ServiceDef sdef = new ServiceDef(service.getName(), toolVersion);
        st.setServiceDef(sdef);
        st.addVariable("tool_version", sdef.getVersion());
        st.addVariable("project_title", sdef.getName());
        st.addVariable("global_package_name", service.getServicepackage());
        st.deriveVariables();
        File dir = new File(st.getTemplateDir());
        st.processDirectory(dir);
        String generatedDir = st.getGenerateDir();
        String projMidfix = st.getProjectMidfix();
        String projDir = st.getProjectDirectory();

        // target service wsdl
        String wsdlSourcePath = FileUtil.makePath("tmpl")
                + "Template.wsdl";
        logger.debug("Source WSDL file: " + wsdlSourcePath);

        // target service wsdl
        String wsdlTargetPath = FileUtil.makePath(generatedDir, projDir,
                "src", "main", "webapp")
                + projMidfix + ".wsdl";
        logger.debug("Target WSDL file: " + wsdlTargetPath);

        List<Operation> operations = service.getOperations().getOperation();
        WsdlCreator wsdlCreator = new WsdlCreator(st, wsdlSourcePath, wsdlTargetPath, operations);
        wsdlCreator.insertDataTypes();

        st.processFile(new File(wsdlTargetPath));

        // service code
        String sjf = FileUtil.makePath(generatedDir, projDir,
                "src", "main", "java", st.getProjectPackagePath())
                + projMidfix
                + ".java";
        logger.debug("Initialising service file: " + sjf);
        String serviceTmpl = st.getProp("project.template.service");
        // service operation java code template
        ServiceCode sc = new ServiceCode(serviceTmpl);
        // service xml
        ServiceXml sxml = new ServiceXml("tmpl/servicexml.vm");
        sxml.put(st.getContext());
        ServiceCodeCreator scc = new ServiceCodeCreator(st, sc, sxml, operations);
        scc.createOperations();

        logger.debug("Writing service file: " + sjf);
        // add project properties velocity context
        sc.put(st.getContext());
        sc.put("operations", sc.getOperations());
        sc.create(sjf);
        String sxmlFile = FileUtil.makePath(generatedDir, projDir,
                "src/main/webapp/WEB-INF/services", st.getProjectMidfix(),
                "META-INF") + "services.xml";

        // adding operations to services.xml
        sxml.put("servxmlops", sxml.getOperations());
        logger.debug("Writing services.xml file: " + sxmlFile);
        sxml.put("url_filter", st.getProp("url.filter"));
        sxml.create(sxmlFile);

        // pom.xml
        String pomPath = FileUtil.makePath(generatedDir, projDir) + "pom.xml";
        DeploymentCreator pomCreator = new DeploymentCreator(pomPath, service, st);
        pomCreator.createPom();

        logger.info("Project created in in \"" + FileUtil.makePath(generatedDir, projDir) + "\"");
    }
}