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
package eu.scape_project.tb.lsdr.seqfileutility.batch;

import eu.scape_project.tb.lsdr.seqfileutility.Job;
import eu.scape_project.tb.lsdr.seqfileutility.ProcessParameters;
import eu.scape_project.tb.lsdr.seqfileutility.SequenceFileWriter;
import eu.scape_project.tb.lsdr.seqfileutility.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch job that runs the sequence file creation as a batch process, adding 
 * all files of a directory to a sequence file (one per directory in a separate
 * thread).
 * 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class BatchJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(BatchJob.class.getName());
    private static ExecutorService executor;
    ProcessParameters pc;

    /**
     * Empty constructor
     */
    private BatchJob() {
    }

    /**
     * Constructor initialising the process configuration
     * @param pc Process configuration
     */
    public BatchJob(ProcessParameters pc) {
        this.pc = pc;
    }

    /**
     * Run the batch process
     * @return Success indicator
     */
    @Override
    public int run() {
        List<SequenceFileWriter> sfws = new ArrayList<SequenceFileWriter>();
        List<String> dirs = StringUtils.getStringListFromString(pc.getDirsStr(), ",");
        executor = Executors.newFixedThreadPool(dirs.size());
        logger.info("Threadpool of size " + dirs.size() + " created");
        Integer i = 1;
        long startMillis = System.currentTimeMillis();
        for (String dir : dirs) {
            String threadId = i.toString();
            ProcessParameters pcThread = pc.clone();
            pcThread.setThreadId(threadId);
            pcThread.setThreadSeqFile(startMillis + "_" + threadId + ".seq");
            pcThread.setThreadDir(dir);
            SequenceFileWriter sfw = new SequenceFileWriter(pcThread);
            executor.execute(sfw);
            sfws.add(sfw);
            i++;
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                ;
            }
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        long timeMillis = System.currentTimeMillis() - startMillis;
        report(sfws, timeMillis);
        return 0;
    }

    /**
     * Create report
     * @param sfws Thread object
     * @param timeMillis Milliseconds
     */
    private void report(List<SequenceFileWriter> sfws, long timeMillis) {
        for (SequenceFileWriter sfw : sfws) {

            File sequenceFile = new File(sfw.getSequenceFileName());
            String msg = "";
            if (sfw.isTextlinemode()) {
                String linesPlrSng = (sfw.getLinecount() == 1) ? "line" : "lines";
                String filesPlrSng = (sfw.getFilecount() == 1) ? "file" : "files";
                msg = sfw.getId() + ": " + sfw.getLinecount() + " " + linesPlrSng + " from " + sfw.getFilecount()
                        + " " + filesPlrSng + " added to sequence file "+ sfw.getSequenceFileName()  +" ("
                        + StringUtils.humanReadableByteCount(sequenceFile.length(), true) +")";
            } else {
                String filesPlrSng = (sfw.getFilecount() == 1) ? "file" : "files";
                msg = sfw.getId() + ": " + sfw.getFilecount() + " " + filesPlrSng + " added to sequence file "
                       + sfw.getSequenceFileName()  + " (" + StringUtils.humanReadableByteCount(sequenceFile.length(), true) +")";
            }
            logger.info(msg);
        }
        logger.info("Process finished after " + StringUtils.humanReadableMillis(timeMillis));
    }
}
