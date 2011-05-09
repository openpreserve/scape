/*******************************************************************************
 * Copyright (c) 2011 The SCAPE Project Partners.
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
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
 * @author SCAPE Project Development Team
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
            logger.error("Unable to load properties file!");
            throw new GeneratorException();
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
