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

package eu.scape_project.xa.tw.gen;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import eu.scape_project.xa.tw.util.PropertyUtil;

/**
 *
 * @author onbscs
 */
public class SubstitutorTest {

    private PropertiesSubstitutor st = null;
    private PropertyUtil pu = null;

    /**
     * Set up substitutor and property utils
     */
    @Before
    public void setUp() {
        try {
            st = new PropertiesSubstitutor("toolwrapper.properties");
            pu = st.getPropertyUtils();
        } catch (GeneratorException ex) {
            fail("Unable to create substitutor.");
        }
    }

    /**
     * Test of getGlobalProjectPrefix method, of class Substitutor.
     */
    @Test
    public void testGetGlobalProjectPrefix() {
        String globalProjectPrefix = pu.getProp("global.project.prefix");
        assertTrue("Global project prefix is not correct.",globalProjectPrefix.equals(st.getGlobalProjectPrefix()));
        
    }

    /**
     * Test of getProjectResourcesDir method, of class Substitutor.
     */
    @Test
    public void testGetProjectResourcesDir() {
       assertTrue("Resources directory is not correct.",st.getProjectResourcesDir().equals("resources"));
    }

    /**
     * Test of getTemplateDir method, of class Substitutor.
     */
    @Test
    public void testGetTemplateDir() {
//        assertTrue("Template directory is not correct.",st.getTemplateDir().equals("template"));
    }

    /**
     * Test of getGenerateDir method, of class Substitutor.
     */
    @Test
    public void testGetGenerateDir() {
//        assertTrue("Generated directory is not correct.",st.getGenerateDir().equals("generated"));
    }

    /**
     * Test of getProjectMidfix method, of class Substitutor.
     */
//    @Test
//    public void testGetProjectMidfix() {
//        String projectTitle = pu.getProp("project.title");
//        String cleanProjTitle = projectTitle.replaceAll("[^A-Za-z0-9 ]", "");
//        StringTokenizer strtok = new StringTokenizer(cleanProjTitle, " ");
//        StringBuilder sb = new StringBuilder();
//        while (strtok.hasMoreTokens()) {
//            String nameItem = strtok.nextToken();
//            String midfixPart = nameItem.substring(0, 1).toUpperCase() + nameItem.substring(1).toLowerCase();
//            sb.append(midfixPart);
//        }
//        // PROJECT_MIDFIX
//        String projectMidfix = sb.toString();
//        assertTrue("Template directory is not correct.",st.getProjectMidfix().equals(projectMidfix));
//    }



//    @Test
//    public void testGetProjectPackagePath() {
//        String ppn = pu.getProp("project.package.name");
//        String expected = StringConverterUtil.packageNameToPackagePath(ppn);
//        String is = st.getProjectPackagePath();
//        assertTrue("Project package path is not correct.",is.equals(expected));
//
//    }

}