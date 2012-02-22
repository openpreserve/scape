/**
 * Copyright (c) 2007, 2008, 2009, 2010 The Planets Project Partners.
 * 
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of
 * the Apache License version 2.0 which accompanies
 * this distribution, and is available at:
 *   http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 */
package eu.planets_project.ifr.core.servreg.utils.client;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * * http://127.0.0.1:8080/pserv-pa-java-se/JavaImageIOMigrate?wsdl
 * http://127.0.0.1:8080/pserv-pa-java-se/JavaImageIOIdentify?wsdl
 * http://testbed.planets-project.eu/pserv-pa-java-se/JavaImageIOIdentify?wsdl
 * http://testbed.planets-project.eu/pserv-pa-java-se/JavaImageIOMigrate?wsdl
 * https://testbed.planets-project.eu/pserv-pa-java-se/JavaImageIOIdentify?wsdl
 * http://planets.dialogika.de/planets.webservice/DIaLOGIKa.wsdl
 * 
 * tests/test-files/images/bitmap/test_png/png_small/2274192346_4a0a03c5d6.png
 * 
 * http://planets.dialogika.de/planets.webservice/DIaLOGIKa.wsdl
 * tests/test-files/documents/test_word_6.0/SETUP.DOC planets:fmt/ext/doc
 * planets:fmt/ext/docx
 * 
 * 
 * e.g.
 * http://testbed.planets-project.eu/pserv-pa-java-se/JavaImageIOMigrate?wsdl
 * tests/test-files/images/bitmap/test_png/png_small/2274192346_4a0a03c5d6.png
 * planets:fmt/ext/png planets:fmt/ext/jpg
 * 
 * @author AnJackson
 * 
 */
public class PlanetsCommandTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link eu.planets_project.ifr.core.servreg.utils.client.PlanetsCommand#main(java.lang.String[])}.
     */
    @Test
    public void testMain() {        
        
        String[] testbedTest = { "http://testbed.planets-project.eu/pserv-pa-java-se/JavaImageIOMigrate?wsdl",
                "tests/test-files/images/bitmap/test_png/png_small/2274192346_4a0a03c5d6.png",
                "planets:fmt/ext/png", "planets:fmt/ext/jpg"};
        PlanetsCommand.main(testbedTest);
        
        System.out.println("---- TEST -------------------------------------------------------------------------------------");
           
        String[] dialogikaTest = { "http://planets.dialogika.de/planets.webservice/DIaLOGIKa.wsdl",
                "tests/test-files/documents/test_word_6.0/SETUP.DOC",
                "planets:fmt/ext/doc", "planets:fmt/ext/docx"};
        
        PlanetsCommand.main(dialogikaTest);
        
    }

}
