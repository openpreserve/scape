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

    /**
     * Default constructor
     */
    public Configuration() {
        
    }

    /**
     * @return string with the path to the project configuration file
     * @throws GeneratorException
     */
    public String getProjectConfigurationFile() throws GeneratorException {
        if(this.projConf == null) {
            throw new GeneratorException("Project configuration file is not defined");
        }
        return this.projConf;
    }

    /**
     * @return the projConf
     */
    public String getProjConf() {
        return this.projConf;
    }

    /**
     * @param confFile a java.io.File which should be a file of project configuration properties.
     * @throws GeneratorException 
     */
    public void setProjConf(File confFile) throws GeneratorException {
	// Check args
	if (confFile == null) throw new IllegalArgumentException("Project configuration file is null.");
        if (confFile.canRead()) {
            logger.info("Project configuration file: " + confFile.getName());
        } else {
            throw new GeneratorException("Unable to read project configuration properties file: " + confFile.getAbsolutePath());
        }
        this.projConf = confFile.getAbsolutePath();
    }

    /**
     * @param toolspecFile
     * @throws GeneratorException
     */
    public void setXmlConf(File toolspecFile) throws GeneratorException {
	// Check args
	if (toolspecFile == null) throw new IllegalArgumentException("Project configuration file is null.");
        if (toolspecFile.canRead()) {
            logger.info("XML toolspec configuration file: " + toolspecFile.getName());
        } else {
            throw new GeneratorException("Unable to read XML toolspec configuration file: " + toolspecFile.getAbsolutePath());
        }
        this.xmlConf = toolspecFile.getAbsolutePath();
    }

    /**
     * @return true if the toolspec config is set
     */
    public boolean hasXmlConf() {
        return (this.getXmlConf() != null);
    }

    /**
     * @return true if toolspec co
     */
    public boolean hasConfig() {
        return (this.hasXmlConf() && this.getProjConf() != null);
    }

    /**
     * @return the xmlConf
     */
    public String getXmlConf() {
        return this.xmlConf;
    }
}
