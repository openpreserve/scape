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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

/**
 * GenericSubstitutor
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public abstract class Substitutor {

    private static Logger logger = LoggerFactory.getLogger(Substitutor.class.getName());
    private VelocityContext context;
    protected VelocityEngine ve;

    /**
     * Constructor of the generic substitutor which initialises the velocity
     * engine and the context containing the key value pairs.
     */
    public Substitutor() {
        //pValPairs = new HashMap<String, String>();
        context = new VelocityContext();
        ve = new VelocityEngine();
        logger.debug("Note: The dot (.) in the velocity variable name is replaced by "
                + "underscore (_) because the dot (.) is used for accessing "
                + "sub-properties, therefore a property defined as foo.bar will "
                + "become foo_bar in the velocity context.");
    }

    /**
     * Add a new key-value pair to the velocity context, note that the dot (.)
     * in the variable is replaced by underscore (_) because this character
     * is used for accessing sub-properties in the Velocity VTL.
     * @param key Key
     * @param val Value
     */
    public void putKeyValuePair(String key, String val) {
        key = key.replaceAll("\\.", "_");
        this.getContext().put(key, val);
        logger.debug("Adding variable \""+key+"\" with value \""+val+"\" to substitutor's velocity context");
    }

    /**
     * Apply substitution
     * @param srcFileAbsPath Source file
     * @param trgtFileAbsPath Target file
     * @throws GeneratorException 
     */
    public void applySubstitution(String srcFileAbsPath, String trgtFileAbsPath) throws GeneratorException {
        File srcFile = new File(srcFileAbsPath);
        if (srcFile.exists()) {
            logger.debug("Processing \"" + srcFile.getPath() + "\"");
        } else {
            logger.error("Unable to find template file " + srcFile.getPath() + "");
        }
        String content;
        File targetFile = null;
        try {

            content = org.apache.commons.io.FileUtils.readFileToString(srcFile);
            String result = replaceVars(content);

            targetFile = new File(trgtFileAbsPath);

            org.apache.commons.io.FileUtils.writeStringToFile(targetFile, result);
        } catch (IOException ex) {
            throw new GeneratorException("An IOException occurred");
        }
        if (targetFile != null && targetFile.exists()) {
            logger.debug("Target file " + targetFile.getPath() + " created.");
        } else {
            throw new GeneratorException("Unable to create target file because it is null");
        }
    }

    /**
     * Apply substitution
     * @param content Content string
     * @return Result
     */
    public String applySubstitution(String content) {
        String result = replaceVars(content);
        return result;
    }

    /**
     * Process directory recursively, only if a file is found, the subsitution
     * is applied.
     * @param path Path to start with
     * @throws IOException
     * @throws GeneratorException 
     */
    public void processDirectory(File path) throws IOException, GeneratorException {
        if (path.isDirectory()) {
            String[] children = path.list();
            for (int i = 0; i < children.length; i++) {
                processDirectory(new File(path, children[i]));
            }
        } else {
            if (path.isFile()) {
                processFile(path);
            }
        }
    }

    /**
     * Replace variables
     * @param inputText Text where substitution will be applied
     * @return Result text
     */
    protected String replaceVars(String inputText) {
        String log = "substitution";
        StringWriter sw = new StringWriter();
        Velocity.evaluate(getContext(), sw, log, inputText);
        return sw.toString();
    }

    protected abstract void processFile(File path) throws GeneratorException;

    /**
     * @return the context
     */
    public VelocityContext getContext() {
        return context;
    }

}