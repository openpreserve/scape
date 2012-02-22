/*
 * Copyright 2012 ait.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scape_project.pt.proc;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ait
 */
public class FileProcessorTest {
    
    public FileProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of getTempLocation method, of class FileProcessor.
     * Output must be absolute.
     * 
     */
    @Test
    public void testGetTempLocation() {
        System.out.println("getTempLocation");
        String fileRef = "hdfs:///bla/bla/bla";
        String result = FileProcessor.getTempLocation(fileRef);
        System.out.println(result);
        if( !result.startsWith("/"))
            fail("Temporary location is not absolute.");
    }

}
