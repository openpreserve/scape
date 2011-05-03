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
package org.opflabs.scape.tb.gw.gen;

import java.util.logging.Level;
import org.opflabs.scape.tb.gw.util.PropertyUtil;
import org.opflabs.scape.tb.gw.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.util.StringConverterUtil;

/**
 * Substitutor
 * @author SCAPE Project Development Team
 * @version 0.1
 */
public class ProjectPropertiesSubstitutor extends GenericSubstitutor {

    private static Logger logger = Logger.getLogger(ProjectPropertiesSubstitutor.class);
    private PropertyUtil pu;
    private String templateDir;
    private String generateDir;
    private String cleanProjTitle;
    private String projectMidfix;

    /**
     * Default constructor
     */
    public ProjectPropertiesSubstitutor() {
    }

    /**
     * Reads the properties from the project configuration properties file
     * and creates the substitution variable map.
     * @throws GeneratorException
     */
    public ProjectPropertiesSubstitutor(String propertiesFileStr) throws GeneratorException {
        super();
        try {
            pu = new PropertyUtil(propertiesFileStr);
            initDerivedVariables();
        } catch (GeneratorException ex) {
            logger.error("Unable to load properties.");
            throw new GeneratorException();
        }

        templateDir = pu.getProp("project.template.dir");
        generateDir = pu.getProp("project.generate.dir");

        // Substitution variables
        Map<String, String> map = pu.getKeyValuePairs();
        Set propertySet = map.entrySet();
        for (Object o : propertySet) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            String keyVar = key.toUpperCase();
            keyVar = StringConverterUtil.propToVar(key);
            String val = (String) entry.getValue();
            pValPairs.put(keyVar, val);
            addDerivedVariables(key, val);
        }

    }

    /**
     * Initialises basic variables, like the project title and the midfix.
     */
    private void initDerivedVariables() {
        String projectTitle = pu.getProp("project.title");
        cleanProjTitle = projectTitle.replaceAll("[^A-Za-z0-9 ]", "");
        StringTokenizer st = new StringTokenizer(cleanProjTitle, " ");
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String nameItem = st.nextToken();
            String midfixPart = nameItem.substring(0, 1).toUpperCase() + nameItem.substring(1).toLowerCase();
            sb.append(midfixPart);
        }
        // PROJECT_MIDFIX
        projectMidfix = sb.toString();
    }

    /**
     * Add variables that can be de derived from property values
     * @param key property where value is derived from
     * @param val derived value
     */
    private void addDerivedVariables(String key, String val) {
        if (key.equals("project.title")) {
            pValPairs.put("PROJECT_MIDFIX", projectMidfix);
            String projectMidfixLc = projectMidfix.toLowerCase();
            pValPairs.put("PROJECT_MIDFIX_LC", projectMidfixLc);
        } else if (key.equals("project.package.name")) {
            String projectPackagePath = StringConverterUtil.packageNameToPackagePath(val);
            pValPairs.put("PROJECT_PACKAGE_PATH", projectPackagePath);
        } else if (key.equals("global.project.prefix")) {
            String globalProjectPrefixLc = val.toLowerCase();
            pValPairs.put("GLOBAL_PROJECT_PREFIX_LC", globalProjectPrefixLc);
        }
    }

    @Override
    public void processFile(File path) {
        logger.debug("Source: " + path);
        String trgtFilePath = path.getPath().replaceAll(pu.getProp("project.template.dir"), pu.getProp("project.generate.dir") + "/" + this.projectMidfix);
        logger.debug("Target: " + trgtFilePath);
        String trgtDirStr = trgtFilePath.replaceAll(path.getName(), "");
        FileUtil.mkdirs(new File(trgtDirStr));
        trgtFilePath = replaceVars(trgtFilePath);
        applySubstitution(path.getPath(), trgtFilePath);
    }

    /**
     * Getter for the global project prefix
     * @return global project prefix
     */
    public String getGlobalProjectPrefix() {
        return pu.getProp("global.project.prefix");
    }

    /**
     * Getter for the resources directory
     * @return resources directory
     */
    public String getProjectResourcesDir() {
        return pu.getProp("project.resources.dir");
    }

    /**
     * Getter for the library directory
     * @return library directory
     */
    public String getProjectLibDir() {
        return pu.getProp("project.lib.dir");
    }

    /**
     * Getter for the template directory
     * @return template directory
     */
    public String getTemplateDir() {
        return templateDir;
    }

    /**
     * Getter for the generated directory
     * @return the generated directory
     */
    public String getGenerateDir() {
        return generateDir;
    }

    /**
     * Getter for the project midfix (e.g. SomeTool)
     * @return project midfix
     */
    public String getProjectMidfix() {
        return this.projectMidfix;
    }

    /**
     * Getter for the property utils
     * @return property utils
     */
    public PropertyUtil getPropertyUtils() {
        return pu;
    }

    /**
     * Getter for the project midfix (e.g. SomeTool)
     * @return project midfix
     */
    public String getProjectPackagePath() {
        String ppp = pValPairs.get("PROJECT_PACKAGE_PATH");
        return ppp;
    }
}
