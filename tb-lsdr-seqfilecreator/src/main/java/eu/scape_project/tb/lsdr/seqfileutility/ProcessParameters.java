/*
 *  Copyright 2012 The SCAPE Project Consortium.
 * 
 *  Licensed under the Apache License; Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing; software
 *  distributed under the License is distributed on an "AS IS" BASIS;
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package eu.scape_project.tb.lsdr.seqfileutility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process configuration 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class ProcessParameters implements Cloneable {

    String dirsStr;
    private String hdfsInputPath;
    private String hadoopJobName;
    String extStr;
    String compressionType;
    boolean textlinemode;

    // Thread specific options
    String threadId;
    String threadSeqFile;
    String threadDir;
    private boolean hadoopmapmode;
    
    // result parameters
    private String outputDirectory;

    /**
     * Empty constructor
     */
    public ProcessParameters() {
        
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    

    public String getHadoopJobName() {
        return hadoopJobName;
    }

    public void setHadoopJobName(String hadoopJobName) {
        this.hadoopJobName = hadoopJobName;
    }

    private static Logger logger = LoggerFactory.getLogger(ProcessParameters.class.getName());

    public String getHdfsInputPath() {
        return hdfsInputPath;
    }

    public void setHdfsInputPath(String hdfsInputPath) {
        this.hdfsInputPath = hdfsInputPath;
    }

    /**
     * Getter for the compression type
     * @return Compression type
     */
    public String getCompressionType() {
        return compressionType;
    }

    /**
     * Setter for the compression type
     * @param compressionType Compression type
     */
    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    /**
     * Getter for the directories parameter
     * @return Directories parameter
     */
    public String getDirsStr() {
        return dirsStr;
    }

    /**
     * Setter for the directories parameter
     * @param dirsStr Directories parameter
     */
    public void setDirsStr(String dirsStr) {
        this.dirsStr = dirsStr;
    }

    /**
     * Getter for the extensions parameter
     * @return Extensions parameter
     */
    public String getExtStr() {
        return extStr;
    }

    /**
     * Setter for the extensions parameter
     * @param extStr Extensions parameter
     */
    public void setExtStr(String extStr) {
        this.extStr = extStr;
    }

    /**
     * Getter for the text line mode parameter
     * @return Text line mode parameter
     */
    public boolean isTextlinemode() {
        return textlinemode;
    }

    /**
     * Setter for the text line mode parameter
     * @param textlinemode Text line mode parameter
     */
    public void setTextlinemode(boolean textlinemode) {
        this.textlinemode = textlinemode;
    }

    // Thread specific options

    /**
     * Getter for the thread directory
     * @return Thread directory
     */
    public String getThreadDir() {
        return threadDir;
    }

    /**
     * Setter for the thread directory
     * @param threadDir Thread directory
     */
    public void setThreadDir(String threadDir) {
        this.threadDir = threadDir;
    }

    /**
     * Getter for the thread id
     * @return Thread id
     */
    public String getThreadId() {
        return threadId;
    }

    /**
     * Setter for the thread id
     * @param threadId Thread id
     */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    /**
     * Getter for the thread sequence file
     * @return Thread sequence file
     */
    public String getThreadSeqFile() {
        return threadSeqFile;
    }

    /**
     * Setter for the thread sequence file
     * @param threadSeqFile Thread sequence file
     */
    public void setThreadSeqFile(String threadSeqFile) {
        this.threadSeqFile = threadSeqFile;
    }

    /**
     * Clone object
     * @return cloned object
     */
    @Override public ProcessParameters clone() {
        try {
            return (ProcessParameters) super.clone();
        } catch (CloneNotSupportedException ex) {
            logger.error("CloneNotSupportedException:",ex);
            throw new AssertionError();
        }
    }

    /**
     * Setter for the hadoop map mode parameter
     * @param mapmode Hadoop map mode parameter
     */
    void setHadoopmapmode(boolean mapmode) {
        this.hadoopmapmode = mapmode;
    }

    /**
     * Setter for the hadoop map mode parameter
     * @return Hadoop map mode parameter
     */
    public boolean isHadoopmapmode() {
        return hadoopmapmode;
    }

}
