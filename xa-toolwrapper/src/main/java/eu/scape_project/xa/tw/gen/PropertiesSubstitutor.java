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

import eu.scape_project.xa.tw.util.PropertyUtil;
import eu.scape_project.xa.tw.util.FileUtil;
import java.io.File;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.util.StringConverterUtil;

/**
 * PropertiesSubstitutor
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class PropertiesSubstitutor extends Substitutor {

    private static Logger logger = LoggerFactory.getLogger(PropertiesSubstitutor.class.getName());
    private PropertyUtil pu;
    private String templateDir;
    private String generateDir;
    private Tool tool;

    /**
     * Default constructor
     */
    public PropertiesSubstitutor() {
    }

    /**
     * Reads the properties from the project configuration properties file
     * and creates the substitution variable map.
     * @throws GeneratorException
     */
    public PropertiesSubstitutor(String propertiesFileStr) throws GeneratorException {
        super();
        try {
            pu = new PropertyUtil(propertiesFileStr);
            tool = new Tool(pu.getProp("project.title"), pu.getProp("tool.version"));
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
            String val = (String) entry.getValue();
            this.putKeyValuePair(key, val);
            addDerivedVariables(key, val);
        }
    }
    

    /**
     * Add variables that can be de derived from property values.
     * Note that velocity context variables have an underscore (_) instead of
     * the dot (.) as the string parts separator sign!
     * @param key property where value is derived from
     * @param val derived value
     */
    private void addDerivedVariables(String key, String val) {
        if (key.equals("project.title")) {
            this.putKeyValuePair("project_midfix", tool.getMidfix());
            logger.debug("Note: Velocity variable \"project_midfix\" is derived from property \"project.title\"");
            this.putKeyValuePair("project_midfix_lc", tool.getDirectory());
            logger.debug("Note: Velocity variable \"project_midfix_lc\" is derived from property \"project.title\"");
        } else if (key.equals("global.package.name")) {
            String projectPackagePath = StringConverterUtil.packageNameToPackagePath(val);
            this.putKeyValuePair("project_package_path", projectPackagePath);
            logger.debug("Note: Velocity variable \"project_package_path\" is derived from property \"global.package.name\"");
            String projectNamespace = StringConverterUtil.packageNameToNamespace(val);
            this.putKeyValuePair("project_namespace", projectNamespace);
            logger.debug("Note: Velocity velocity variable \"project_namespace\" is derived from property \"global.package.name\"");
        } else if (key.equals("global.project.prefix")) {
            String globalProjectPrefixLc = val.toLowerCase();
            this.putKeyValuePair("global_project_prefix_lc", globalProjectPrefixLc);
            logger.debug("Note: Velocity velocity variable \"global_project_prefix_lc\" is derived from property \"global.project.prefix\"");
        }
    }

    public void addKeyValuePair(String key, String val) {
        this.putKeyValuePair(key, val);
    }

    @Override
    public void processFile(File path) {
        String trgtFilePath = replaceVars(path.getPath());
        trgtFilePath = trgtFilePath.replace(pu.getProp("project.template.dir"), pu.getProp("project.generate.dir") + "/" + tool.getDirectory());
        String trgtDirStr = trgtFilePath.substring(0, trgtFilePath.lastIndexOf(File.separator));
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
    public String getProjectVersion() {
        return tool.getVersion();
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
        String ppp = (String)this.getContext().get("project_package_path");
        return ppp;
    }

    /**
     * Getter for the project midfix (e.g. SomeTool)
     * @return project midfix
     */
    public String getProjectNamespace() {
        String pn = (String)this.getContext().get("project_namespace");
        return pn;
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

    /**
     * Note that the properties are dot separated like defined in the properties
     * file (e.g. some.variable=value).
     * @param string Variable
     * @return Value
     */
    public String getProp(String string) {
        return pu.getProp(string);
    }

    /**
     * Note that the velocity context variables are underscore (_) separated,
     * e.g. use some_variable=value if you have defined some.variable=value
     * in the properties file.
     * file (e.g. some.variable=value).
     * @param string Variable
     * @return Value
     */
    public String getContextProp(String string) {
        if(this.getContext() != null) {
            return (String)this.getContext().get(string);
        } else {
            return "NULL";
        }
    }
}
