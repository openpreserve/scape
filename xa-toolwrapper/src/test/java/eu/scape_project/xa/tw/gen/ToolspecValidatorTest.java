/*
 *  Copyright 2011 The SCAPE Project Consortium.
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
package eu.scape_project.xa.tw.gen;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scape_project.xa.tw.conf.Configuration;
import eu.scape_project.xa.tw.toolspec.Toolspec;

/**
 *
 * @author onbscs
 */
public class ToolspecValidatorTest {

    private ArrayList<String> toolspecs;

    private static Logger logger = LoggerFactory.getLogger(ToolspecValidatorTest.class.getName());

    public ToolspecValidatorTest() {
    }

    @Before
    public void setUp() throws GeneratorException {
        toolspecs = new ArrayList<String>();
        toolspecs.add("default.xml");
        // All tool specification instances from the examples directory
        // will be validated
        addToolspecFilesFromDir("examples");
        addToolspecFilesFromDir("production");
    }

    private ToolspecValidator getToolspecValidator(String toospecXml) throws GeneratorException {
        ToolspecValidator tv;
        try {
            Configuration ioc = new Configuration();
            ioc.setXmlConf(toospecXml);
            ioc.setProjConf("toolwrapper.properties");
            JAXBContext context;
            context = JAXBContext.newInstance("eu.scape_project.xa.tw.toolspec");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Toolspec toolspec = (Toolspec) unmarshaller.unmarshal(new File(ioc.getXmlConf()));
            tv = new ToolspecValidator(toolspec, ioc);
            return tv;
        } catch (JAXBException ex) {
            logger.error("JAXBException", ex);
            throw new GeneratorException("JAXBException occurred.");
        }
    }


     /**
     * Test of validate method, of class ToolspecValidator.
     */
    @Test
    public void testValidate() throws Exception {
        for(String toolspec : toolspecs) {
            ToolspecValidator tv = getToolspecValidator(toolspec);
            tv.validateWithXMLSchema();
            tv.validate();
        }
    }

    private void addToolspecFilesFromDir(String directory) throws GeneratorException {
        File dir = new File(directory);
        String[] children = dir.list();
        if (children == null) {
            throw new GeneratorException("examples directory not available.");
        } else {
            for (int i=0; i<children.length; i++) {
                String filename = children[i];
                if(filename.endsWith(".xml")) {
                    logger.info("Tool specification file \""+filename+"\" found");
                    toolspecs.add(directory+"/"+filename);
                }
            }
        }
    }

}
