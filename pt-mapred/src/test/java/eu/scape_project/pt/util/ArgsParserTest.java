/*
 * Copyright 2013 ait.
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
package eu.scape_project.pt.util;

import java.io.IOException;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

/**
 *
 * @author ait
 */
public class ArgsParserTest {
    
    private static Log LOG = LogFactory.getLog(ArgsParserTest.class);
    public ArgsParserTest() {
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
     * Test of parse method, of class ArgsParser.
     */
    @Test
    public void testParse() throws IOException {
        LOG.info("TEST parse");
        ArgsParser parser = new ArgsParser();
        Set<String> parameters = new HashSet<String>();
        parameters.add("input1");
        parameters.add("input3");
        parser.setParameters(parameters);

        LOG.info("TEST good input");
        String strCmdLine = "--input1=\"bla\" --input3=\"5\" < file.in > \"file with spaces.out\"";
        parser.parse(strCmdLine);
        Map<String, String> mapArgsExpected = new HashMap<String, String>();
        mapArgsExpected.put("input1", "bla");
        mapArgsExpected.put("input3", "5");

        assertEquals(mapArgsExpected, parser.getArguments());
        assertEquals("file.in", parser.getStdinFile() );
        assertEquals("file with spaces.out", parser.getStdoutFile() );

        LOG.info("TEST empty input");
        strCmdLine = "--input1=\"bla\" --input3=\"\"";
        parser.parse(strCmdLine);
        mapArgsExpected = new HashMap<String, String>();
        mapArgsExpected.put("input1", "bla");
        mapArgsExpected.put("input3", "");

        assertEquals(mapArgsExpected, parser.getArguments());

        LOG.info("TEST input with quotes");
        strCmdLine = "--input1=\"bla \\\"quoted\\\" bla\"";
        parser.parse(strCmdLine);
        mapArgsExpected = new HashMap<String, String>();
        mapArgsExpected.put("input1", "bla \"quoted\" bla");

        assertEquals(mapArgsExpected, parser.getArguments());

        LOG.info("TEST bad input");
        strCmdLine = "--input1=\"bla\" --input3 \"";
        parser.parse(strCmdLine);
        mapArgsExpected = new HashMap<String, String>();
        mapArgsExpected.put("input1", "bla");

        assertEquals(mapArgsExpected, parser.getArguments());

        LOG.info("TEST other bad input");
        strCmdLine = "-input1=\"bla\" --input3 \"";
        parser.parse(strCmdLine);
        mapArgsExpected = new HashMap<String, String>();

        assertEquals(mapArgsExpected, parser.getArguments());

        LOG.info("TEST other bad input concerning stdin");
        strCmdLine = "< > file.out";
        parser.parse(strCmdLine);

        assertEquals(null, parser.getStdinFile());
        assertEquals(null, parser.getStdoutFile());

        LOG.info("TEST other bad input concerning stdout");
        strCmdLine = "< file.in >";
        parser.parse(strCmdLine);

        assertEquals("file.in", parser.getStdinFile());
        assertEquals(null, parser.getStdoutFile());

    }

    /**
     * Test of setParameters method, of class ArgsParser.
     */
    @Test
    public void testSetParameters() {
    }
}
