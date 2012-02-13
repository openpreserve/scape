package eu.scape_project.pt.util;

import java.util.Map.Entry;
import java.net.URI;
import eu.scape_project.pt.mapred.CLIWrapper;
import joptsimple.OptionSet;
import joptsimple.OptionParser;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Class for ArgsParser
 * @author Matthias Rella [myrho]
 */
public class ArgsParserTest {
    
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
     * Test of readParameters method, of class ArgsParser.
     */
    @Test
    public void testReadParameters() {
        System.out.println("readParameters");
        String strParameters = "-key1 value1 -key2 value2   -key3 value with space -key4 -key5 value-with-dash";
        HashMap expResult = new HashMap<String, String>();
        expResult.put("key1", "value1");
        expResult.put("key2", "value2");
        expResult.put("key3", "value with space");
        expResult.put("key5", "value-with-dash");
        HashMap result = ArgsParser.readParameters(strParameters);
        assertEquals(expResult, result);
    }

    @Test
    public void testSetArguments() {
        System.out.println("setArguments");
        HashMap<String, HashMap> mapInputs = new HashMap<String,HashMap>();
        HashMap<String, Object> mapInputParam = new HashMap<String, Object>();
        mapInputParam.put( "required", true );
        mapInputParam.put( "datatype", URI.class );
        mapInputParam.put( "direction", "input");
        mapInputs.put("input", mapInputParam);

        HashMap<String, Object> mapOutputParam = new HashMap<String, Object>();
        mapOutputParam.put( "required", true );
        mapOutputParam.put( "datatype", URI.class );
        mapOutputParam.put( "direction", "input");
        mapInputs.put("output", mapOutputParam);

        ArgsParser parser = new ArgsParser();
        for( Entry<String, HashMap> entry: mapInputs.entrySet() )
            parser.setOption(entry.getKey(), entry.getValue());

        String value = "--input bla --output bla2";
        String[] args = ArgsParser.makeCLArguments( value );
        parser.parse(args);

        if( !parser.hasOption("input") )
            fail( "Option input not set");
        if( !parser.hasOption("output"))
            fail( "Option output not set");

        assertEquals( "bla", parser.getValue("input") );
        assertEquals( "bla2", parser.getValue("output") );
        
    }

}
