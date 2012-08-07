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
package eu.scape_project.tb.lsdr.seqfileutility.hadoop;

import eu.scape_project.tb.lsdr.seqfileutility.CompressionType;
import eu.scape_project.tb.lsdr.seqfileutility.ProcessParameters;
import eu.scape_project.tb.lsdr.seqfileutility.SequenceFileUtility;
import eu.scape_project.tb.lsdr.seqfileutility.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map/recuce job creating a text file with the traverse of input paths of the
 * files and initiates the job for creating the sequence file.
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class HadoopJob implements Tool {

    ProcessParameters pc;
    Configuration conf;
    private static Logger logger = LoggerFactory.getLogger(HadoopJob.class.getName());

    /**
     * Empty constructor
     */
    public HadoopJob() {
    }

    /**
     * Set process configuration
     *
     * @param pc Process configuration
     */
    public void setPc(ProcessParameters pc) {
        this.pc = pc;
    }

    private void traverse(File file, FSDataOutputStream outputStream) throws IOException {
        File[] list = file.listFiles();
        for (File f : list) {
            if (f.isDirectory()) {
                traverse(f, outputStream);
            } else if (f.isFile()) {
                String pathStr = f.getAbsolutePath() + "\n";
                byte[] buff = pathStr.getBytes();
                outputStream.write(buff);
            }
        }
    }

    private void writeFilePaths(File directory, FSDataOutputStream outputStream) throws IOException {
        String regex = "";
        if (pc.getExtStr() != null && !pc.getExtStr().equals("")) {
            regex = " -regex .*." + pc.getExtStr();
        }
        String command = "find -L " + directory.getAbsolutePath() + regex + " -type f -print";
        logger.info("Get input paths: " + command);
        Process p = Runtime.getRuntime().exec(command);
        InputStream inStream = p.getInputStream();
        long bytecount = 0;
        byte buf[] = new byte[4096];
        int len;
        while ((len = inStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
            bytecount += len;
            // Status message at each 10MB block
            if (bytecount > 4096 && bytecount % 10485760 <= 4096) {
                String mb = StringUtils.humanReadableByteCount(bytecount, true);
                logger.info("Current size of input paths: " + mb);
            }
        }
        String mb = StringUtils.humanReadableByteCount(bytecount, true);
        logger.info("Final size of input paths: " + mb);
        outputStream.close();
        inStream.close();
    }

    /**
     * Run hadoop job
     *
     * @param strings Command line arguments
     * @return Success indicator
     * @throws Exception
     */
    @Override
    public int run(String[] strings) throws Exception {
        try {
            String hdfsInputDir = null;
            FileSystem hdfs = FileSystem.get(conf);
            
            // hdfs input path is given as command parameter
            if (pc.getHdfsInputPath() != null) {
                hdfsInputDir = pc.getHdfsInputPath();
            // hdfs input file is created
            } else {
                hdfsInputDir = "input/" + System.currentTimeMillis() + "sfu/";

                String[] extensions = null;
                if (pc.getExtStr() != null) {
                    StringTokenizer st = new StringTokenizer(pc.getExtStr(), ",");
                    extensions = new String[st.countTokens()];
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        extensions[i] = st.nextToken();
                        i++;
                    }
                }

                hdfs.mkdirs(new Path(hdfsInputDir));

                String hdfsIinputPath = hdfsInputDir + "inputpaths.txt";
                Path path = new Path(hdfsIinputPath);

                FSDataOutputStream outputStream = hdfs.create(path);

                List<String> dirs = StringUtils.getStringListFromString(pc.getDirsStr(), ",");
                for (String dir : dirs) {
                    File directory = new File(dir);
                    if (directory.isDirectory()) {
                        // Alternatively, the java traverse method can be used
                        // for creating the file paths:
                        //traverse(directory, outputStream);
                        writeFilePaths(directory, outputStream);
                    } else {
                        logger.warn("Parameter \"" + dir + "\" is not a directory "
                                + "(skipped)");
                    }
                }
                outputStream.close();
                if (hdfs.exists(path)) {
                    logger.info("Input paths created in \"" + hdfs.getHomeDirectory()
                            + "/" + path.toString() + "\"");
                } else {
                    logger.error("Input paths have not been created in hdfs.");
                    return 1;
                }
            }
            String hadoopJobName = "Hadoop_sequence_file_creation";
            if(pc.getHadoopJobName() != null && !pc.getHadoopJobName().equals(""))
                hadoopJobName = pc.getHadoopJobName();
            Job job = new Job(conf, hadoopJobName);

            job.setJarByClass(SequenceFileUtility.class);
            job.setMapperClass(SmallFilesSequenceFileMapper.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(BytesWritable.class);
            job.setOutputFormatClass(SequenceFileOutputFormat.class);
            TextInputFormat.addInputPath(job, new Path(hdfsInputDir));

            String hdfsOutputDir = "output/" + System.currentTimeMillis() + "sfu/";
            
            SequenceFileOutputFormat.setOutputPath(job, new Path(hdfsOutputDir));
            SequenceFileOutputFormat.setOutputCompressionType(job,
                    CompressionType.get(pc.getCompressionType()));

            int success = job.waitForCompletion(true) ? 0 : 1;
            boolean seqFileExists = hdfs.exists(new Path(hdfsOutputDir
                    + "part-r-00000"));
            if (success == 0 && seqFileExists) {
                logger.info("Sequence file created: \""
                        + hdfs.getHomeDirectory() + "/"
                        + hdfsOutputDir + "part-r-00000" + "\"");
                pc.setOutputDirectory(hdfsOutputDir);
                return 0;
            } else {
                logger.error("Sequence file not created in hdfs");
                return 1;
            }
        } catch (Exception e) {
            logger.error("IOException occurred", e);
        } finally {
        }
        return 0;
    }

    /**
     * Setter for hadoop job configuration
     *
     * @param c Hadoop job configuration
     */
    @Override
    public void setConf(Configuration c) {
        this.conf = c;
    }

    /**
     * Getter for hadoop job configuration
     *
     * @return Hadoop job configuration
     */
    @Override
    public Configuration getConf() {
        return conf;
    }
}
