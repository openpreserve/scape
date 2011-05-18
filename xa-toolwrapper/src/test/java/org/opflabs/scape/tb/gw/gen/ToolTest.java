/*
 *  Copyright 2011 onbscs.
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.opflabs.scape.tb.gw.gen.Tool;

/**
 *
 * @author onbscs
 */
public class ToolTest {

    Tool tool1;
    Tool tool2;
    Tool tool3;

    public ToolTest() {
    }

    @Before
    public void setUp() {
        tool1 = new Tool("OpenJPEG", "1.4");
        tool2 = new Tool("Border Removal", "1.3.5");
        tool3 = new Tool("Open Jpeg Conversion", "1.3.5");
    }

    @After
    public void tearDown() {
        tool1 = null;
        tool2 = null;
        tool3 = null;
    }

    /**
     * Test of getName method, of class Tool.
     */
    @Test
    public void testGetName() {
        String expResult1 = "OpenJPEG";
        String result1 = tool1.getName();
        assertEquals(expResult1, result1);
        String expResult2 = "BorderRemoval";
        String result2 = tool2.getName();
        assertEquals(expResult2, result2);
        String expResult3 = "OpenJpegConversion";
        String result3 = tool3.getName();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getVersion method, of class Tool.
     */
    @Test
    public void testGetVersion() {
        String expResult1 = "14";
        String result1 = tool1.getVersion();
        assertEquals(expResult1, result1);
        String expResult2 = "135";
        String result2 = tool2.getVersion();
        assertEquals(expResult2, result2);
        String expResult3 = "135";
        String result3 = tool3.getVersion();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getDirectory method, of class Tool.
     */
    @Test
    public void testGetDirectory() {
        String expResult1 = "openjpeg14";
        String result1 = tool1.getDirectory();
        assertEquals(expResult1, result1);
        String expResult2 = "borderremoval135";
        String result2 = tool2.getDirectory();
        assertEquals(expResult2, result2);
        String expResult3 = "openjpegconversion135";
        String result3 = tool3.getDirectory();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getMidfix method, of class Tool.
     */
    @Test
    public void testGetMidfix() {
        String expResult1 = "OpenJPEG14";
        String result1 = tool1.getMidfix();
        assertEquals(expResult1, result1);
        String expResult2 = "BorderRemoval135";
        String result2 = tool2.getMidfix();
        assertEquals(expResult2, result2);
        String expResult3 = "OpenJpegConversion135";
        String result3 = tool3.getMidfix();
        assertEquals(expResult3, result3);
    }

}