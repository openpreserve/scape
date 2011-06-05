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

package org.opflabs.scape.tb.gw.gen;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opflabs.scape.tb.gw.util.PropertyUtil;
import org.opflabs.scape.tb.gw.util.StringConverterUtil;

/**
 * JavaTypesCreatorTest
 * @author onbscs
 * @version 0.1
 */
public class JavaTypesCreatorTest extends TestCase {

    private ProjectPropertiesSubstitutor st = null;
    private PropertyUtil pu = null;

    JavaTypesCreator jtc = null;

    /** Logger */
    private static Logger logger =
            Logger.getLogger(JavaTypesCreatorTest.class);



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
            jtc = new JavaTypesCreator();
        } catch (GeneratorException ex) {
            fail("Unable to create substitutor.");
        }
    }

    @After
    @Override
    public void tearDown() {
    }

    @Test
    public void testGetGetterName() {
        String getterName = jtc.getGetterName("inputFile");
        assertTrue(getterName + " is not correct.",
                getterName.equals("getInputFile"));
    }

    @Test
    public void testGetCode() {
//        String result = jtc.getCodeSnippet("ctmpl/test");
//
//        String expected = "Replace this variable.\n";
//        logger.info(result);
//        assertTrue("Replacement is not correct.",result.equals(expected));
    }
}
