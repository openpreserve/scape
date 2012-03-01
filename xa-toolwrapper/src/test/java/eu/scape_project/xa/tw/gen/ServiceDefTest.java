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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author onbscs
 */
public class ServiceDefTest {

    ServiceDef servicedef1;
    ServiceDef servicedef2;
    ServiceDef servicedef3;
    ServiceDef servicedef4;

    /**
     * Set up required service definitions.
     */
    @Before
    public void setUp() {
        servicedef1 = new ServiceDef("OpenJPEG", "1.4");
        servicedef2 = new ServiceDef("Border Removal", "1.3.5");
        servicedef3 = new ServiceDef("Open Jpeg Conversion", "1.3.5");
        servicedef4 = new ServiceDef("Fits", "0.5.0");
    }

    /**
     * Reset the service definition references.
     */
    @After
    public void tearDown() {
        servicedef1 = null;
        servicedef2 = null;
        servicedef3 = null;
        servicedef4 = null;
    }

    /**
     * Test of getName method, of class Tool.
     */
    @Test
    public void testGetName() {
        String expResult1 = "OpenJPEG";
        String result1 = servicedef1.getName();
        assertEquals(expResult1, result1);
        String expResult2 = "BorderRemoval";
        String result2 = servicedef2.getName();
        assertEquals(expResult2, result2);
        String expResult3 = "OpenJpegConversion";
        String result3 = servicedef3.getName();
        assertEquals(expResult3, result3);
        String expResult4 = "Fits";
        String result4 = servicedef4.getName();
        assertEquals(expResult4, result4);
    }

    /**
     * Test of getVersion method, of class Tool.
     */
    @Test
    public void testGetVersion() {
        String expResult1 = "14";
        String result1 = servicedef1.getVersion();
        assertEquals(expResult1, result1);
        String expResult2 = "135";
        String result2 = servicedef2.getVersion();
        assertEquals(expResult2, result2);
        String expResult3 = "135";
        String result3 = servicedef3.getVersion();
        assertEquals(expResult3, result3);
        String expResult4 = "050";
        String result4 = servicedef4.getVersion();
        assertEquals(expResult4, result4);
    }

    /**
     * Test of getDirectory method, of class Tool.
     */
    @Test
    public void testGetDirectory() {
        String expResult1 = "openjpeg14";
        String result1 = servicedef1.getDirectory();
        assertEquals(expResult1, result1);
        String expResult2 = "borderremoval135";
        String result2 = servicedef2.getDirectory();
        assertEquals(expResult2, result2);
        String expResult3 = "openjpegconversion135";
        String result3 = servicedef3.getDirectory();
        assertEquals(expResult3, result3);
        String expResult4 = "fits050";
        String result4 = servicedef4.getDirectory();
        assertEquals(expResult4, result4);
    }

    /**
     * Test of getMidfix method, of class Tool.
     */
    @Test
    public void testGetMidfix() {
        String expResult1 = "OpenJPEG14";
        String result1 = servicedef1.getMidfix();
        assertEquals(expResult1, result1);
        String expResult2 = "BorderRemoval135";
        String result2 = servicedef2.getMidfix();
        assertEquals(expResult2, result2);
        String expResult3 = "OpenJpegConversion135";
        String result3 = servicedef3.getMidfix();
        assertEquals(expResult3, result3);
        String expResult4 = "Fits050";
        String result4 = servicedef4.getMidfix();
        assertEquals(expResult4, result4);
    }

}