/*******************************************************************************
 * Copyright (c) 2011 The SCAPE Project Partners.
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
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
 * @author SCAPE Project Development Team
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
            st = new ProjectPropertiesSubstitutor("projectconfig.properties");
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
