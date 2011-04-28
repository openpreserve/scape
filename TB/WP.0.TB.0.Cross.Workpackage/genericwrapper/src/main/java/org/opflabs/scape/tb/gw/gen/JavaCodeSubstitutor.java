/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opflabs.scape.tb.gw.gen;

import java.io.File;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author onbscs
 */
public class JavaCodeSubstitutor extends GenericSubstitutor {

    private static Logger logger = Logger.getLogger(ProjectPropertiesSubstitutor.class);

    public JavaCodeSubstitutor() {
        super();
    }


    public void addKeyValuePair(String key, String val) {
        this.pValPairs.put(key, val);
    }

    @Override
    protected void processFile(File path) {
        logger.debug("Source: " + path);
        String trgtFilePath = path.getPath();
        applySubstitution(path.getPath(), trgtFilePath);
    }
}
