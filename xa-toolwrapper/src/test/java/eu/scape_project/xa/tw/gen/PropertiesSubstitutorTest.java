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

import eu.scape_project.xa.tw.conf.Configuration;
import eu.scape_project.xa.tw.util.PropertyUtil;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author onbscs
 */
public class PropertiesSubstitutorTest {

    private PropertiesSubstitutor st;

    public PropertiesSubstitutorTest() {
    }

    @Before
    public void setUp() throws GeneratorException {
        Configuration ioc = new Configuration();
        ioc.setXmlConf("default.xml");
        ioc.setProjConf("toolwrapper.properties");
        st = new PropertiesSubstitutor(ioc.getProjConf());
    }

    /**
     * Test of addVariable method, of class PropertiesSubstitutor.
     */
    @Test
    public void testAddVariable() {
        st.addVariable("testvar", "testval");
        String val = st.getContextProp("testvar");
        assertTrue("Test variable not created successfully.",val.equals("testval"));
    }

    /**
     * Test of deriveVariables method, of class PropertiesSubstitutor.
     */
    @Test
    public void testDeriveVariables() throws Exception {

        String projectTitle = "Test Project";
        double toolVersion = 1.0D;

        ServiceDef sdef = new ServiceDef(projectTitle, Double.toString(toolVersion));
        st.setServiceDef(sdef);

        st.addVariable("project_title", sdef.getName());
        st.addVariable("tool_version", sdef.getVersion());
        st.addVariable("global_package_name", "org.apache.axis2");

        st.deriveVariables();
        
        assertTrue("Variable is not derived correctly.",
                st.getContextProp("project_midfix").equals("TestProject10"));
        assertTrue("Variable is not derived correctly.",
                st.getContextProp("project_midfix_lc").equals("testproject10"));
        assertTrue("Variable is not derived correctly.",
                st.getContextProp("project_package_path").equals("org/apache/axis2"));
        assertTrue("Variable is not derived correctly.",
                st.getContextProp("project_namespace").equals("http://apache.org/axis2"));
    }

}