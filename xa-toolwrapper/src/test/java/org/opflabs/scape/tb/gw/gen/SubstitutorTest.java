/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opflabs.scape.tb.gw.gen;

import org.opflabs.scape.tb.gw.util.StringConverterUtil;
import java.util.StringTokenizer;
import org.opflabs.scape.tb.gw.util.PropertyUtil;
import org.opflabs.scape.tb.gw.util.FileUtil;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SubstitutorTest {

    private ProjectPropertiesSubstitutor st = null;
    private PropertyUtil pu = null;

    public SubstitutorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            st = new ProjectPropertiesSubstitutor("default.properties");
            pu = st.getPropertyUtils();
        } catch (GeneratorException ex) {
            fail("Unable to create substitutor.");
        }
    }

    @After
    public void tearDown() {
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
     * Test of getProjectLibDir method, of class Substitutor.
     */
    @Test
    public void testGetProjectLibDir() {
        assertTrue("Library directory is not correct.",st.getProjectLibDir().equals("lib"));
    }

    /**
     * Test of getTemplateDir method, of class Substitutor.
     */
    @Test
    public void testGetTemplateDir() {
        assertTrue("Template directory is not correct.",st.getTemplateDir().equals("template"));
    }

    /**
     * Test of getGenerateDir method, of class Substitutor.
     */
    @Test
    public void testGetGenerateDir() {
        assertTrue("Generated directory is not correct.",st.getGenerateDir().equals("generated"));
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



    @Test
    public void testGetProjectPackagePath() {
        String ppn = pu.getProp("project.package.name");
        String expected = StringConverterUtil.packageNameToPackagePath(ppn);
        String is = st.getProjectPackagePath();
        assertTrue("Project package path is not correct.",is.equals(expected));

    }

}