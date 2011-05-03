/*******************************************************************************
 * Copyright (c) 2011 The SCAPE Project Partners.
 *
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package eu.impact_project.iif.gw.cli;

import java.io.File;



import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.gen.GeneratorException;
import org.opflabs.scape.tb.gw.gen.IOType;
import org.opflabs.scape.tb.gw.gen.JavaTypesCreator;
import org.opflabs.scape.tb.gw.gen.MsgType;
import org.opflabs.scape.tb.gw.util.FileUtil;
import org.opflabs.scape.tb.gw.gen.ProjectPropertiesSubstitutor;
import org.opflabs.scape.tb.gw.gen.XMLSchemaDatatypesCreator;

/**
 * Command line interface of the generic soap web service wrapper.
 * This program generates an ant-based project that can be used to deploy
 * a soap web service in an axis2 web service container deployed in a servlet
 * container, like apache tomcat, for example.
 * @author SCAPE Project Development Team
 * @version 0.1
 */
public class GenericWrapper {

    private static String inputConfigurationFile;
    private static String outputConfigurationFile;
    private static String projectPropertiesFile;
    private static Logger logger = Logger.getLogger(GenericWrapper.class);

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
                    File icf = new File(argval);
                    if (icf.canRead()) {
                        logger.info("Input configuration file: " + argval);
                        inputConfigurationFile = argval;
                    } else {
                        logger.error("Unable to read input configuration file: " + argval);
                        throw new GeneratorException();
                    }
                }
                // output configuration
                if (arg.equals("-oc") && argval != null) {
                    File ocf = new File(argval);
                    if (ocf.canRead()) {
                        logger.info("Output configuration file: " + argval);
                        outputConfigurationFile = argval;
                    } else {
                        logger.error("Unable to read output configuration file: " + argval);
                        throw new GeneratorException();
                    }
                }
                // project configuration
                if (arg.equals("-pc") && argval != null) {
                    File ocf = new File(argval);
                    if (ocf.canRead()) {
                        logger.info("Project configuration file: " + argval);
                        projectPropertiesFile = argval;
                    } else {
                        logger.error("Unable to read project configuration properties file: " + argval);
                        throw new GeneratorException();
                    }
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

        // configuration files
         if (args.length == 0) {
            logger.info("Trying to read default configuration files from working directory.");
            inputConfigurationFile = "inputconfig.json";
            outputConfigurationFile = "outputconfig.json";
            projectPropertiesFile = "genericwrapper.properties";
            logger.info("Use arguments:");
            logger.info("-ic <inputconfig.json> -oc <outputconfig.json> -pc <projectconfig.properties>");
            logger.info("in order to load other configuration files.");
        } else {
            initialiseParametersFromArgs(args);
        }

        ProjectPropertiesSubstitutor st = new ProjectPropertiesSubstitutor(projectPropertiesFile);
        File dir = new File(st.getTemplateDir());
        try {
            st.processDirectory(dir);
            String generatedDir = st.getGenerateDir();
            String projMidfix = st.getProjectMidfix();

            String wsdlAbsPath = FileUtil.makePath(generatedDir, projMidfix,
                    st.getProjectResourcesDir())
                    + st.getGlobalProjectPrefix()
                    + projMidfix
                    + "Service.wsdl";
            logger.debug("WSDL file: " + wsdlAbsPath);
            //File wsdlFile = new File();
            XMLSchemaDatatypesCreator respSdc = new XMLSchemaDatatypesCreator(
                    MsgType.REQUEST, wsdlAbsPath, inputConfigurationFile, wsdlAbsPath);
            respSdc.insert();
            XMLSchemaDatatypesCreator reqSdc = new XMLSchemaDatatypesCreator(
                    MsgType.RESPONSE, wsdlAbsPath, outputConfigurationFile, wsdlAbsPath);
            reqSdc.insert();

            String sjf = FileUtil.makePath(generatedDir, projMidfix,
                    "src",st.getProjectPackagePath(),"service")
                    + st.getGlobalProjectPrefix()
                    + projMidfix
                    + "ServiceSkeleton.java";
            logger.debug("Service Java file: " + sjf);
            JavaTypesCreator jtcInput = new JavaTypesCreator(IOType.INPUT,inputConfigurationFile,sjf,sjf);
            jtcInput.insert();
            JavaTypesCreator jtcOutput = new JavaTypesCreator(IOType.OUTPUT,outputConfigurationFile,sjf,sjf);
            jtcOutput.insert();


            String libDirStr = st.getProjectLibDir();
            File libPath = new File(FileUtil.makePath(libDirStr));
            String generatedDirStr = FileUtil.makePath(generatedDir, projMidfix, libDirStr);
            logger.info("Copying directory " + libPath + " ...");
            FileUtil.copyDirectory(libPath, FileUtil.makePath(libDirStr), generatedDirStr);
        } catch (Exception ex) {
            logger.error("Unable to generate project: " + ex.getMessage());
            throw new GeneratorException();
        }
    }
}
