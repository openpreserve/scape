/*******************************************************************************
 * Copyright (c) 2011 The IMPACT/SCAPE Project Partners.
 *
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.opflabs.scape.tb.gw.gen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.util.FileUtil;

/**
 * GenericSubstitutor
 * @author IMPACT/SCAPE Project Development Team
 * @version 0.1
 */
public abstract class GenericSubstitutor {

    private static Logger logger = Logger.getLogger(GenericSubstitutor.class);
    protected HashMap<String, String> pValPairs;

    public GenericSubstitutor() {
        pValPairs = new HashMap<String, String>();
    }

    public void setKeyValuePairs(HashMap<String, String> pValPairs) {
        this.pValPairs = pValPairs;
    }

    /**
     * Apply substitution
     * @param srcFileAbsPath Source file
     * @param trgtFileAbsPath Target file
     */
    public void applySubstitution(String srcFileAbsPath, String trgtFileAbsPath) {
        File srcFile = new File(srcFileAbsPath);
        if (srcFile.exists()) {
            logger.debug("Template file " + srcFile.getPath() + " exists.");
        } else {
            logger.error("Unable to find template file " + srcFile.getPath() + "");
        }
        String content = FileUtil.readTxtFileIntoString(srcFile);
        content = replaceVars(content);
        File targetFile = FileUtil.writeStringToFile(content, (trgtFileAbsPath));
        if (targetFile.exists()) {
            logger.debug("Target file " + targetFile.getPath() + " created.");
        } else {
            logger.error("Unable to create target file " + targetFile.getPath() + "");
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
     */
    public void processDirectory(File path) throws IOException {
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
     * Replace variables using the key-value pairs hashmap
     * @param inputText Text where substitution will be applied
     * @return Result text
     */
    protected String replaceVars(String inputText) {
        String text = inputText;
        for (String key : pValPairs.keySet()) {
            String value = (String) pValPairs.get((String) key);

            if (value == null) {
                logger.warn(key + " substitution variable value is null");
            }
            try {
                if (text.contains("#" + key + "#")) {
                    logger.debug("Substituting #" + key + "# by " + value);
                }
                text = text.replaceAll("#" + key + "#", value);
            } catch (NullPointerException ex) {
                logger.error("Variable " + key + " is not defined.", ex);
            }
        }
        return text;
    }

    protected abstract void processFile(File path);
}
