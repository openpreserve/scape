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
package eu.scape_project.xa.tw.conf;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.gen.GeneratorException;

/**
 * Configuration files

 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class Configuration {

    private static Logger logger = LoggerFactory.getLogger(Configuration.class.getName());
    private String projConf;
    private String xmlConf;

    public Configuration() {
        
    }

    public String getProjectConfigurationFile() throws GeneratorException {
        if(projConf == null) {
            throw new GeneratorException("Project configuration file is not defined");
        }
        return projConf;
    }

    /**
     * @return the projConf
     */
    public String getProjConf() {
        return projConf;
    }

    /**
     * @param projConf the projConf to set
     */
    public void setProjConf(String projConf) throws GeneratorException {
        File ocf = new File(projConf);
        if (ocf.canRead()) {
            logger.info("Project configuration file: " + projConf);
        } else {
            throw new GeneratorException("Unable to read project configuration properties file: " + projConf);
        }
        this.projConf = projConf;
    }

    public void setXmlConf(String xmlConf) throws GeneratorException {
        File xmlCfgFile = new File(xmlConf);
        if (xmlCfgFile.canRead()) {
            logger.info("XML toolspec configuration file: " + xmlConf);
        } else {
            throw new GeneratorException("Unable to read XML toolspec configuration file: " + xmlConf);
        }
        this.xmlConf = xmlConf;
    }

    public boolean hasXmlConf() {
        return (getXmlConf() != null);
    }

    public boolean hasConfig() {
        return (getXmlConf() != null && projConf != null);
    }

    /**
     * @return the xmlConf
     */
    public String getXmlConf() {
        return xmlConf;
    }
}
