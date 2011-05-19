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
import org.opflabs.scape.tb.gw.util.FileUtil;

/**
 * Code snippet template with its substitution object that is used to create
 * the source snippet.
 * @author IMPACT/SCAPE Project Development Team
 * @version 0.1
 */
public class Snippet {

    private File snippetFile;
    JavaCodeSubstitutor jcs;

    /**
     * Default constructor is not accessible
     */
    private Snippet() {}

    /**
     * Constructor
     * @param path Path to snippet template
     */
    public Snippet(String path) {
        this.snippetFile = new File(path);
        jcs = new JavaCodeSubstitutor();
    }

    /**
     * Get code, substitution is applied
     * @return Code snippet
     */
    public String getCode() {
        String snippet = FileUtil.readTxtFileIntoString(snippetFile);
        return jcs.applySubstitution(snippet);
    }

    public boolean canRead() {
        return snippetFile.canRead();
    }

    /**
     * Add key value pair to substitution object
     * @param key Key
     * @param val Value
     */
    public void addKeyValuePair(String key, String val) {
        jcs.addKeyValuePair(key, val);
    }

}
