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
package org.opflabs.scape.tb.gw.cli;

import java.io.File;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.gen.GeneratorException;

/**
 * Configuration files
 * @author onbscs
 * @version 0.1
 */
class Configuration {

    private static Logger logger = Logger.getLogger(Configuration.class);
    private static HashMap<Integer, String> inputConfigs;
    private static HashMap<Integer, String> outputConfigs;
    private String currInConf;
    private String currOutConf;
    private Integer currOpid;
    private String projConf;

    public Configuration() {
        inputConfigs = new HashMap<Integer, String>();
        outputConfigs = new HashMap<Integer, String>();
    }

    public String getInputConfigurationFile(int opid) throws GeneratorException {
        if(!inputConfigs.containsKey(opid)) {
            throw new GeneratorException("Input configuration file for operation "+opid+ "  is not defined");
        }
        return inputConfigs.get(opid);
    }

    public String getOutputConfigurationFile(int opid) throws GeneratorException {
        if(!outputConfigs.containsKey(opid)) {
            throw new GeneratorException("Output configuration file for operation "+opid+ " is not defined");
        }
        return outputConfigs.get(opid);
    }

    public String getProjectConfigurationFile() throws GeneratorException {
        if(projConf == null) {
            throw new GeneratorException("Project configuration file is not defined");
        }
        return projConf;
    }

    /**
     * @return the currInConf
     */
    public String getCurrInConf() {
        return currInConf;
    }

    /**
     * @param currInConf the currInConf to set
     */
    public void setCurrInConf(String currInConf) {
        this.currInConf = currInConf;
    }

    /**
     * @return the currOutConf
     */
    public String getCurrOutConf() {
        return currOutConf;
    }

    /**
     * @param currOutConf the currOutConf to set
     */
    public void setCurrOutConf(String currOutConf) {
        this.currOutConf = currOutConf;
    }

    /**
     * @return the currOpid
     */
    public Integer getCurrOpid() {
        return currOpid;
    }

    /**
     * @param currOpid the currOpid to set
     */
    public void setCurrOpid(Integer currOpid) {
        this.currOpid = currOpid;
    }

    public boolean hasInOutConf() {
        return (currInConf != null && currOutConf != null && currOpid != 0);
    }

    public void addIoConfiguration() throws GeneratorException {
        if (currInConf == null || currOutConf == null || currOpid == 0
                || currInConf.equals("") || currInConf.equals("")) {
            String msg = "Invalid sequence of configuration files. Each input "
                    + "configuration must have a corresponding output "
                    + "configuration. A valid sequence is, for example: "
                    + "-ic inputconfig1.json -oc outputconfig1.json -ic inputconfig2.json -oc outputconfig2.json";
            throw new GeneratorException(msg);
        }
        File icf = new File(currInConf);
        if (icf.canRead()) {
            logger.info("Input configuration file: " + currInConf);
        } else {
            throw new GeneratorException("Unable to read input configuration file: " + currInConf);
        }
        File ocf = new File(currOutConf);
        if (ocf.canRead()) {
            logger.info("Output configuration file: " + currOutConf);
        } else {
            throw new GeneratorException("Unable to read output configuration file: " + currOutConf);
        }
        inputConfigs.put(currOpid, currInConf);
        outputConfigs.put(currOpid, currOutConf);
        currOpid = 0;
        currInConf = null;
        currOutConf = null;

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
}
