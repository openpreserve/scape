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

package eu.scape_project.xa.tw.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.gen.GeneratorException;

/**
 * PropertyUtils
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class PropertyUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertyUtil.class.getName());
    private Properties properties;
    private HashMap<String, String> map;

    public PropertyUtil(String propertiesFile) throws GeneratorException {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
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
        return val;
    }
}
