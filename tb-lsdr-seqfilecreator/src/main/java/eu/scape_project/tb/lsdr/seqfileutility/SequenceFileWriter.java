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
package eu.scape_project.tb.lsdr.seqfileutility;

import eu.scape_project.tb.lsdr.seqfileutility.util.FileUtils;
import java.io.*;
import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for writing hadoop sequence files.
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public final class SequenceFileWriter implements Runnable, Serializable {

    private Path path;
    private SequenceFile.Writer writer;
    private Configuration conf;
    ProcessConfiguration pc;
    // Logger instance
    private static Logger logger = LoggerFactory.getLogger(SequenceFileWriter.class.getName());
    //int lineCounter;
    private long filecount;
    private long linecount;
    String uri;
    File rootDir;
    File sequenceFile;

    public String getSequenceFileName() {
        return pc.getThreadSeqFile();
    }

    /**
     * Private constructur initialising the hadoop configuration.
     */
    private SequenceFileWriter() {
        conf = new Configuration();
        filecount = 0L;
        linecount = 0L;
    }

    /**
     * Constructur that takes the sequence file name as an argument.
     *
     * @param sequenceFileName Sequence file name
     */
    public SequenceFileWriter(ProcessConfiguration pc) {
        this();
        this.pc = pc;
        initialise();
    }

    /**
     * Append all files that have a defined extension from a root directory
     * recursively to the sequence file.
     *
     * @param rootDirName Root directory
     * @param extensionFilter File extension filter
     */
    public void initialise() {
        rootDir = new File(pc.getThreadDir());
        sequenceFile = new File(pc.getThreadSeqFile());
        uri = sequenceFile.getAbsolutePath();
    }

    /**
     * Traverse the root directory recursively
     *
     * @param dir Root directory
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void traverseDir(File dir) throws FileNotFoundException, IOException {
        if (writer != null) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (String child : children) {
                    traverseDir(new File(dir, child));
                }
            } else if (!dir.isDirectory()) {
                if (!dir.getAbsolutePath().contains("abo/abo")) {
                    if (pc.getExtStr() == null || pc.getExtStr().equals("") || dir.getName().endsWith(pc.getExtStr())) {
                        if (pc.isTextlinemode()) {
                            writeTextLines(dir);
                        } else {
                            writeFileContent(dir);
                        }
                    }
                }
            }
        } else {
            logger.error(this.getId() + ": " + "Writer not available");
        }
    }

    private void writeTextLines(File file) throws FileNotFoundException, IOException {
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        String line;
        Text key = new Text();
        key.set(file.getAbsolutePath());
        Text value = new Text();

        while ((line = buffer.readLine()) != null) {
            value.set(line);
            writer.append(key, value);
            logger.info(this.getId() + ": " + value);
            linecount++;
        }
        buffer.close();
    }

    private void writeFileContent(File file) throws IOException {
        Text key = new Text();
        BytesWritable value = new BytesWritable();
        String filePath = file.getAbsolutePath();
        String keyPath = FilenameUtils.separatorsToUnix(filePath);
        key.set(keyPath);
        byte[] buffer;
        long length = 0;

        length = file.length();
        buffer = FileUtils.readFileToByteArray(file.getAbsolutePath());

        value.set(buffer, 0, (int) length);
        filecount++;
        logger.info(this.getId() + ": " + filecount + ":" + key);
        writer.append(key, value);
    }

    public long getFilecount() {
        return filecount;
    }

    public long getLinecount() {
        return linecount;
    }

    public boolean isTextlinemode() {
        return pc.isTextlinemode();
    }

    @Override
    public void run() {
        try {
            FileSystem fs = FileSystem.get(URI.create(uri), conf);
            path = new Path(uri);
            Class keyClass = Text.class;
            Class valueClass = BytesWritable.class;
            if (pc.isTextlinemode()) {
                keyClass = Text.class;
                valueClass = Text.class;
            }
            writer = SequenceFile.createWriter(fs, conf, path, keyClass,
                    valueClass, CompressionType.get(pc.getCompressionType()));
            traverseDir(rootDir);
        } catch (Exception e) {
            logger.error(this.getId() + ": " + "IOException occurred", e);
        } finally {
            IOUtils.closeStream(writer);
        }
    }

    public String getId() {
        return pc.getThreadId();
    }
}
