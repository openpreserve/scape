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
 * @author onbscs
 * @version 0.1
 */
public class ProjectPropertiesSubstitutor extends GenericSubstitutor {

    private static Logger logger = Logger.getLogger(ProjectPropertiesSubstitutor.class);
    private PropertyUtil pu;
    private String templateDir;
    private String generateDir;

    private Tool tool;

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
            tool = new Tool(pu.getProp("project.title"),pu.getProp("tool.version"));
        } catch (GeneratorException ex) {
            throw new GeneratorException("Unable to load properties.");
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
     * Add variables that can be de derived from property values
     * @param key property where value is derived from
     * @param val derived value
     */
    private void addDerivedVariables(String key, String val) {
        if (key.equals("project.title")) {
            pValPairs.put("PROJECT_MIDFIX", tool.getMidfix());
            pValPairs.put("PROJECT_MIDFIX_LC", tool.getDirectory());
        } else if (key.equals("global.package.name")) {
            String projectPackagePath = StringConverterUtil.packageNameToPackagePath(val);
            pValPairs.put("PROJECT_PACKAGE_PATH", projectPackagePath);
        } else if (key.equals("global.project.prefix")) {
            String globalProjectPrefixLc = val.toLowerCase();
            pValPairs.put("GLOBAL_PROJECT_PREFIX_LC", globalProjectPrefixLc);
        }
    }

    public void addKeyValuePair(String key, String val) {
        pValPairs.put(key, val);
    }

    @Override
    public void processFile(File path) {
        logger.debug("Source: " + path);
        String trgtFilePath = path.getPath().replaceAll(pu.getProp("project.template.dir"), pu.getProp("project.generate.dir") + "/" + tool.getDirectory());
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
        return tool.getMidfix();
    }

    /**
     * Getter for the project midfix (e.g. SomeTool)
     * @return project midfix
     */
    public String getProjectDirectory() {
        return tool.getDirectory();
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

    /**
     * @return the tool
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * @param tool the tool to set
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }
}
