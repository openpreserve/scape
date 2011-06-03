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

package org.opflabs.scape.tb.gw.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.gen.GeneratorException;

/**
 * PropertyUtils
 * @author onbscs
 * @version 0.1
 */
public class PropertyUtil {

    private static Logger logger = Logger.getLogger(PropertyUtil.class.getName());
    private Properties properties;
    private HashMap<String, String> map;

    public PropertyUtil(String propertiesFile) throws GeneratorException {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));

//            InputStream is = this.getClass().getResourceAsStream(propertiesFile);
//            BufferedInputStream stream = new BufferedInputStream(is);
//            properties.load(stream);
//            stream.close();

            logger.debug("Property file \"" + propertiesFile + "\" loaded.");
        } catch (IOException ex) {
            throw new GeneratorException("Unable to load properties file!");
        }
    }

    public Map<String, String> getKeyValuePairs() {
        map = new HashMap<String, String>((Map) properties);
        return map;
    }

    public String getProp(String key) {
        String val = (String) properties.getProperty(key);
        logger.debug("Property key \"" + key + "\" has value \"" + val + "\"");
        return val;
    }
}
