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

package eu.scape_project.tb.wc.archd.tools;

import eu.scape_project.tb.wc.archd.hdreader.ArcInputFormat;
import eu.scape_project.tb.wc.archd.hdreader.ArcRecord;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * Test ARC Reader
 *
 */
public class App extends Configured {

    private static DefaultDetector detector;

    public static void main(String[] args) throws Exception {
        detector = new DefaultDetector();
        App app = new App();
        app.myRun();
    }

    public void myRun() throws Exception {

        long curCount = 0;
        Text currKey;
        ArcRecord currArcRecord;

        //*********************************************************************************
        File file = new File("/home/aLocalTest.arc.gz");
        //*********************************************************************************

        Configuration conf = new Configuration();

        FileSplit split = new FileSplit(new Path(file.getAbsolutePath()), 0, file.length(), null);
        ArcInputFormat myArcF = new ArcInputFormat();
        TaskAttemptContext tac = new TaskAttemptContext(conf, new TaskAttemptID());

        RecordReader<Text, ArcRecord> recordReader = myArcF.createRecordReader(split, tac);
        recordReader.initialize(split, tac);

        while (recordReader.nextKeyValue()) {
            currKey = recordReader.getCurrentKey();
            currArcRecord = recordReader.getCurrentValue();

            printAllInfo(curCount, currKey, currArcRecord);
            //printAllSkipNoneResponse(curCount, currKey, currArcRecord);
            //printDiffItems(curCount, currKey, currArcRecord);

            curCount++;
        }

        System.out.println("Bye!");

    }

    private String content2String(InputStream contents) throws IOException {
        StringWriter myWriter = new StringWriter();
        IOUtils.copy(contents, myWriter, null);
        return myWriter.toString();
    }

    private String getTIKAtype(Text key, InputStream contents) throws IOException {
        Metadata met = new Metadata();

        met.set(Metadata.RESOURCE_NAME_KEY, key.toString());
        MediaType mediaType = detector.detect(contents, met);

        return mediaType.toString().intern();
    }

    private void printAllInfo(long curCount, Text currKey, ArcRecord currArcRecord) throws IOException {
        System.out.println("#   : " + curCount);
        System.out.println("Key : " + currKey.toString());
        System.out.println("Size: " + currArcRecord.getLength());
        System.out.println("Type: " + currArcRecord.getType());
        System.out.println("URL : " + currArcRecord.getUrl());
        System.out.println("Date: " + currArcRecord.getDate());
        System.out.println("HTTP: " + currArcRecord.getHttpReturnCode());
        System.out.println("Stored Type   (http-get): " + currArcRecord.getMimeType());
        System.out.println("Detected Type (TIKA 1.0): " + getTIKAtype(currKey, currArcRecord.getContents()));
        System.out.println("**************************************");
    }

    private void printAllSkipNoneResponse(long curCount, Text currKey, ArcRecord currArcRecord) throws IOException {
        String myType = currArcRecord.getType();
        System.out.println("#   : " + curCount);

        if (myType.equals("response")) { //only print allInfo if this is a "response" record (WARC has request, response and metadata records)

            System.out.println("Key : " + currKey.toString());
            System.out.println("Size: " + currArcRecord.getLength());
            System.out.println("Type: " + myType);
            System.out.println("URL : " + currArcRecord.getUrl());
            System.out.println("Date: " + currArcRecord.getDate());
            System.out.println("HTTP: " + currArcRecord.getHttpReturnCode());
            System.out.println("Stored Type   (http-get): " + currArcRecord.getMimeType());
            System.out.println("Detected Type (TIKA 1.0): " + getTIKAtype(currKey, currArcRecord.getContents()));
        } else {
            System.out.println("Type: " + myType + " (for WARC containers)");
        }

        System.out.println("**************************************");
    }

    private void printDiffItems(long curCount, Text currKey, ArcRecord currArcRecord) throws IOException {
        String myTIKA = getTIKAtype(currKey, currArcRecord.getContents());
        String myMIME = currArcRecord.getMimeType();

        if (!myTIKA.equals(myMIME)) {
            System.out.println("#   :" + curCount);
            System.out.println("URL : " + currArcRecord.getUrl());
            System.out.println("Stored Type   (http-get): " + myMIME);
            System.out.println("Detected Type (TIKA 1.0): " + myTIKA);
            System.out.println("**************************************");
        }

    }
}
