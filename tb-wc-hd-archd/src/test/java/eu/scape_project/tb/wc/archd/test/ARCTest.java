/*
 *  Copyright 2012 The SCAPE Project Consortium.
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

package eu.scape_project.tb.wc.archd.test;

import eu.scape_project.tb.wc.archd.hdreader.ArcInputFormat;
import eu.scape_project.tb.wc.archd.hdreader.ArcRecord;
import java.io.*;
import java.util.Date;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author onbram
 */
public class ARCTest extends TestCase {

    private ArcInputFormat myArcF;
    private FileSplit split;
    private TaskAttemptContext tac;

    public ARCTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        
        InputStream is = ARCTest.class.getResourceAsStream("ONBdevSample.arc.gz");
        File file = eu.scape_project.tb.wc.archd.tools.FileUtils.getTmpFile("archd", "arc.gz");
        OutputStream out = new FileOutputStream(file);
        int read = 0;
	byte[] bytes = new byte[1024];
 
	while ((read = is.read(bytes)) != -1) {
		out.write(bytes, 0, read);
	}
 
	is.close();
	out.flush();
	out.close();
        
        
        Configuration conf = new Configuration();
        Job job = new Job(conf);


        split = new FileSplit(new Path(file.getAbsolutePath()), 0, file.length(), null);
        
        myArcF = new ArcInputFormat();
        tac = new TaskAttemptContext(conf, new TaskAttemptID());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of nextKeyValue method, of class ArcRecordReader.
     */
    public void testNextKeyValue() throws Exception {
        RecordReader<Text, ArcRecord> recordReader = myArcF.createRecordReader(split, tac);
        recordReader.initialize(split, tac);
        int start = 1;
        while (recordReader.nextKeyValue()) {
            Text currKey = recordReader.getCurrentKey();
            ArcRecord currValue = recordReader.getCurrentValue();
            
            String currMIMEType = currValue.getMimeType();
            String currType = currValue.getType();
            String currURL = currValue.getUrl();
            InputStream currStream = currValue.getContents();
            String currContent;
            String myContentString;
            int myContentStringIndex;
            Date currDate = currValue.getDate();
            int currHTTPrc = currValue.getHttpReturnCode();
            int currLength = currValue.getLength();
            
            System.out.println("KEY " + start + ": " + currKey + " MIME Type: " + currMIMEType + " Type: " + currType + " URL: " + currURL + " Date: " + currDate.toString() + " HTTPrc: " + currHTTPrc + " Length: " + currLength);
                
            // check example record 1 (first one and the header of the ARC file)
            if (start == 1) {
                //"myContentString" is arbitrary sting snipped of which we know that it exists in the content stream and of which we know the position in the stream.
                //We will search for the string int the content we read and compare it to the values we know.                
                currContent = content2String(currStream);
                myContentString = "<dcterms:isPartOf>";
                myContentStringIndex = currContent.indexOf(myContentString);
                //System.out.println("Search for: " + myContentString + "=> Index is: " + myContentStringIndex);
                
                assertEquals("ID not equal","20120619072058/filedesc://8839-51-20120619072058-00000-testcrawler02.onb.ac.at.arc", currKey.toString());
                assertEquals("MIME Type not equal","text/plain", currMIMEType);    
                assertEquals("Response type not equal","response", currType);
                assertEquals("URL not equal","filedesc://8839-51-20120619072058-00000-testcrawler02.onb.ac.at.arc", currURL);
                assertEquals("Date not equal","Tue Jun 19 07:20:58 CEST 2012", currDate.toString());
                assertEquals("HTTPrc not equal",-1, currHTTPrc);
                assertEquals("Record length not equal",1209, currLength);
                assertEquals("Content seems not to be correct",530, myContentStringIndex);
            }
            // check example record 105 (in the middle)
            if (start == 105) {
                //"myContentString" is arbitrary sting snipped of which we know that it exists in the content stream and of which we know the position in the stream.
                //We will search for the string int the content we read and compare it to the values we know.   
                currContent = content2String(currStream);
                myContentString = "background-position:495px";
                myContentStringIndex = currContent.indexOf(myContentString);
                //System.out.println("Search for: " + myContentString + "=> Index is: " + myContentStringIndex);

                assertEquals("ID not equal","20120619072140/http://www.onb.ac.at/sammlungen/CSS_template_literaturarchiv_small.css", currKey.toString());
                assertEquals("MIME Type not equal","text/css", currMIMEType);
                assertEquals("Response type not equal","response", currType);
                assertEquals("URL not equal","http://www.onb.ac.at/sammlungen/CSS_template_literaturarchiv_small.css", currURL);
                assertEquals("Date Type not equal","Tue Jun 19 07:21:40 CEST 2012", currDate.toString());
                assertEquals("HTTPrc not equal",200, currHTTPrc);
                assertEquals("Length Type not equal",185, currLength);
                assertEquals("Content seems not to be correct",117, myContentStringIndex);
            } 
            // check example record 121 (last one)
            if (start == 121) {
                //"myContentString" is arbitrary sting snipped of which we know that it exists in the content stream and of which we know the position in the stream.
                //We will search for the string int the content we read and compare it to the values we know.   
                currContent = content2String(currStream);
                myContentString = "enYf*X";
                myContentStringIndex = currContent.indexOf(myContentString);
                //System.out.println("Search for: " + myContentString + "=> Index is: " + myContentStringIndex);

                assertEquals("20120619072145/http://www.onb.ac.at/images/HAN/HAN_Hauptbild_225_120.jpg", currKey.toString());
                assertEquals("MIME Type not equal","image/jpeg", currMIMEType);
                assertEquals("Response type not equal","response", currType);
                assertEquals("URL not equal","http://www.onb.ac.at/images/HAN/HAN_Hauptbild_225_120.jpg", currURL);
                assertEquals("Date Type not equal","Tue Jun 19 07:21:45 CEST 2012", currDate.toString());
                assertEquals("HTTPrc not equal",200, currHTTPrc);
                assertEquals("Length Type not equal",49443, currLength);
                assertEquals("Content seems not to be correct",45770, myContentStringIndex);
            }            
            start++;
        }
    }
    
    
    
        private String content2String(InputStream contents) throws IOException {
        StringWriter myWriter = new StringWriter();
        IOUtils.copy(contents, myWriter, null);
        String out = myWriter.toString();
        //System.out.print("CONTENT: " + out);  //uncomment this line to print the inputsream (e.g. to find new "myContentString"
        return out;
    }
}
