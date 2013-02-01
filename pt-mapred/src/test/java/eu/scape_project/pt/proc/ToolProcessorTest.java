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
package eu.scape_project.pt.proc;

import eu.scape_project.pt.repo.LocalToolRepository;
import eu.scape_project.pt.tool.Operation;
import eu.scape_project.pt.tool.Operations;
import eu.scape_project.pt.tool.Tool;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.conf.Configured;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author ait
 */
public class ToolProcessorTest extends Configured {
    private LocalToolRepository repo;
    private String toolspecsDir;
    
    private static final Log LOG = LogFactory.getLog(ToolProcessorTest.class);

    public ToolProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        try {
            URL res = this.getClass().getClassLoader().getResource("toolspecs");
            // use the file toolspec xml as the input file too (for this test)
            toolspecsDir = res.getFile();
            repo = new LocalToolRepository(res.getFile());

        } catch (IOException ex) {
            fail(ex.getMessage());
        }

    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ToolProcessor.
     */
    public void testExecuteFileIdentify() throws Exception {
        Tool tool = repo.getTool("file");

        String tmpInputFile = this.getClass().getClassLoader()
                .getResource("ps2pdf-input.ps").getFile();

        LOG.debug("tmpInputFile = " + tmpInputFile );

        LOG.info("TEST file-identify");

        ToolProcessor processor = new ToolProcessor(tool);

        Operation operation = processor.findOperation("identify");
        processor.setOperation(operation);

        Map<String, String> mapInput = new HashMap<String, String>();
        mapInput.put("input", tmpInputFile );

        processor.setInputFileParameters( mapInput );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        processor.next( new StreamProcessor(baos));
        try {
            processor.execute();
        } catch ( IOException ex ) {
            LOG.error(
                "Exception during execution (maybe unresolved system dependency?): "
                    + ex);
        }
        LOG.info("output: " + new String(baos.toByteArray()) );
    }

    public void testExecuteFileIdentifyStdin() throws Exception {

        LOG.info("TEST file-identify-stdin");

        // This test may throw an "Broken pipe" IOException 
        // because file needs not to read the whole file data from 
        // stdin and will terminate while the other thread is reading streams.

        Tool tool = repo.getTool("file");

        String tmpInputFile = this.getClass().getClassLoader()
                .getResource("ps2pdf-input.ps").getFile();

        ToolProcessor processor = new ToolProcessor(tool);
        Operation operation = processor.findOperation("identify-stdin");
        processor.setOperation(operation);

        FileInputStream fin = new FileInputStream( new File( tmpInputFile ));
        StreamProcessor in = new StreamProcessor(fin);
        in.next(processor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = new ByteArrayOutputStream();

        processor.next( new StreamProcessor(baos));
        try {
            in.execute();
        } catch ( IOException ex ) {
            LOG.error(
                "Exception during execution (maybe unresolved system dependency?): "
                    + ex);
        }
        LOG.info("output: " + new String(baos.toByteArray()) );

    }

    public void testExecutePs2pdfConvert() throws Exception {


        LOG.info("TEST ps2pdf-convert");
        Tool tool = repo.getTool("ps2pdf");

        String tmpInputFile = this.getClass().getClassLoader()
                .getResource("ps2pdf-input.ps").getFile();

        ToolProcessor processor = new ToolProcessor(tool);
        Operation operation = processor.findOperation("convert");
        processor.setOperation(operation);

        Map<String, String> mapInput = new HashMap<String, String>();

        LOG.debug("tool = " + tool.getName());

        LOG.debug("tmpInputFile = " + tmpInputFile );

        mapInput = new HashMap<String, String>();
        mapInput.put("inFile", tmpInputFile );

        String tmpOutputFile = File.createTempFile("ps2pdf", ".pdf").getAbsolutePath();
        LOG.debug("tmpOutputFile = " + tmpOutputFile );

        Map<String, String> mapOutput = new HashMap<String, String>();
        mapOutput.put("outFile", tmpOutputFile );

        processor.setInputFileParameters( mapInput );
        processor.setOutputFileParameters( mapOutput );
        try {
            processor.execute();
        } catch ( IOException ex ) {
            LOG.error(
                "Exception during execution (maybe unresolved system dependency?): "
                    + ex);
        }

    }

    public void testExecutePs2pdfConvertStreamed() throws Exception {

        LOG.info("TEST ps2pdf-convert-streamed");
        Tool tool = repo.getTool("ps2pdf");

        String tmpInputFile = this.getClass().getClassLoader()
                .getResource("ps2pdf-input.ps").getFile();
        String tmpOutputFile = File.createTempFile("ps2pdf", ".pdf").getAbsolutePath();

        ToolProcessor processor = new ToolProcessor(tool);
        Operation operation = processor.findOperation("convert-streamed");
        processor.setOperation(operation);

        LOG.debug("tmpInputFile = " + tmpInputFile );

        LOG.debug("tmpOutputFile = " + tmpOutputFile );

        FileInputStream fin = new FileInputStream( new File( tmpInputFile ));
        StreamProcessor in = new StreamProcessor(fin);
        in = new StreamProcessor( fin );
        in.next( processor );

        FileOutputStream fout = new FileOutputStream( new File( tmpOutputFile ));
        processor.next(new StreamProcessor(fout));

        try {
            in.execute();
        } catch ( IOException ex ) {
            LOG.error(
                "Exception during execution (maybe unresolved system dependency?): "
                    + ex);
        }
    }

}
