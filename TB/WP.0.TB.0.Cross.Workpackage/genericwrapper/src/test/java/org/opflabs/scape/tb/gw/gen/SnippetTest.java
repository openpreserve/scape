/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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