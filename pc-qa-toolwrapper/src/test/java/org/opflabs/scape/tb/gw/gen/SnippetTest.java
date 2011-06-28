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
public class SnippetTest {

    Snippet snippet;

    public SnippetTest() {
    }

    @Before
    public void setUp() {
        // Snippet
        snippet = new Snippet("ctmpl/test");
        snippet.addKeyValuePair("VAR", "variable");
    }

    /**
     * Test of getCode method, of class Snippet.
     */
    @Test
    public void testGetCode() {
        String current = snippet.getCode();
        String expected = "Replace this variable.\n";
        assertTrue("\""+current+"\"" + " does not match "+"\""+expected+"\"",
                current.equals(expected));
    }


}