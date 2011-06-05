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

import java.io.File;
import java.util.HashMap;
import org.opflabs.scape.tb.gw.util.FileUtil;

/**
 * Code snippet template with its substitution object that is used to create
 * the source snippet.
 * @author onbscs
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

    public void addKeyValHashMap(HashMap<String,String> hm) {
        jcs.addKeyValHashMap(hm);
    }

}
