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
import org.apache.log4j.Logger;

/**
 * Java code substitutor
 * @author onbscs
 * @version 0.1
 */
public class JavaCodeSubstitutor extends GenericSubstitutor {

    private static Logger logger = Logger.getLogger(ProjectPropertiesSubstitutor.class);

    public JavaCodeSubstitutor() {
        super();
    }


    public void addKeyValuePair(String key, String val) {
        this.pValPairs.put(key, val);
    }

    public void addKeyValHashMap(HashMap<String,String> hm) {
        pValPairs.putAll(hm);
    }

    @Override
    protected void processFile(File path) {
        logger.debug("Source: " + path);
        String trgtFilePath = path.getPath();
        applySubstitution(path.getPath(), trgtFilePath);
    }
}
