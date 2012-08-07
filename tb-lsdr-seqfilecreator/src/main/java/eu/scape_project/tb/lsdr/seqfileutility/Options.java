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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line interface options.
 * 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class Options {

    private static Logger logger = LoggerFactory.getLogger(Options.class.getName());
    // Statics to set up command line arguments
    public static final String HELP_FLG = "h";
    public static final String HELP_OPT = "help";
    public static final String HELP_OPT_DESC = "print this message.";
    public static final String HADOOPMAPMODE_FLG = "m";
    public static final String HADOOPMAPMODE_OPT = "mapmode";
    public static final String HADOOPMAPMODE_OPT_DESC = "Hadoop map mode: A text "
            + "file containing all absolute paths of the input directory "
            + "(parameter -d) is created and the sequence file is created using "
            + "a map/reduce job. The sequence file is direcly stored in HDFS. "
            + "If this parameter is omitted, the sequence file creation runs "
            + "as a batch process, adding all files of a directory to a "
            + "sequence file (one per directory) in a separate thread. (Optional)";
    public static final String HDFSINPUTPATH_FLG = "p";
    public static final String HDFSINPUTPATH_OPT = "path";
    public static final String HDFSINPUTPATH_OPT_DESC = "Hadoop map mode: "
            + "HDFS input path where the text files containing input paths is "
            + "available. If this parameter is provided, the -d parameter is "
            + "not required (Optional)";
    public static final String HADOOPJOBNAME_FLG = "n";
    public static final String HADOOPJOBNAME_OPT = "name";
    public static final String HADOOPJOBNAME_OPT_DESC = "Hadoop map mode: "
            + "Hadoop job name (Optional)";
    public static final String DIR_FLG = "d";
    public static final String DIR_OPT = "dir";
    public static final String DIR_OPT_DESC = "Local directory (or directories - "
            + "commaseparated) containing files to be added. Note "
            + "that in hadoop map mode (parameter -m) each tasktracker must be "
            + "able to access the files at the same path. (Required)";
    public static final String EXT_FLG_FLG = "e";
    public static final String EXT_FLG_OPT = "ext";
    public static final String EXT_FLG_OPT_DESC = "Extension filter(s) - commaseparated (Optional).";
    public static final String COMPR_FLG = "c";
    public static final String COMPR_OPT = "compr";
    public static final String COMPR_OPT_DESC = "Compression, one of "
            + "NONE,RECORD,BLOCK, default: BLOCK (Optional).";
    public static final String TEXTLINE_FLG = "t";
    public static final String TEXTLINE_OPT = "textline";
    public static final String TEXTLINE_OPT_DESC = "Text line mode, input files are"
            + "text files and each line of the text files should be added as a "
            + "record in the sequence file (Optional).";
    public static final String USAGE = "{hadoop jar|java -jar}"
            + " target/tb-lsdr-seqfileutility-0.1-SNAPSHOT-jar-with-dependencies.jar";
    // Static for command line option parsing
    public static org.apache.commons.cli.Options OPTIONS = new org.apache.commons.cli.Options();
    // Logger instance

    static {
        OPTIONS.addOption(HELP_FLG, HELP_OPT, false, HELP_OPT_DESC);
        OPTIONS.addOption(HADOOPMAPMODE_FLG, HADOOPMAPMODE_OPT, false, HADOOPMAPMODE_OPT_DESC);
        OPTIONS.addOption(HDFSINPUTPATH_FLG, HDFSINPUTPATH_OPT, true, HDFSINPUTPATH_OPT_DESC);
        OPTIONS.addOption(HADOOPJOBNAME_FLG, HADOOPJOBNAME_OPT, true, HADOOPJOBNAME_OPT_DESC);
        OPTIONS.addOption(DIR_FLG, DIR_OPT, true, DIR_OPT_DESC);
        OPTIONS.addOption(EXT_FLG_FLG, EXT_FLG_OPT, true, EXT_FLG_OPT_DESC);
        OPTIONS.addOption(COMPR_FLG, COMPR_OPT, true, COMPR_OPT_DESC);
        OPTIONS.addOption(TEXTLINE_FLG, TEXTLINE_OPT, false, TEXTLINE_OPT_DESC);
    }

    public static void initOptions(CommandLine cmd, ProcessParameters pc) {

        String dirStr;
        String pathStr;
        String jobName;
        String seqFileStr;
        String extStr;
        String compressionType;
        boolean textlinemode = false;
        boolean mapmode = false;
       

        // map mode
        if ((cmd.hasOption(HADOOPMAPMODE_OPT))) {
            logger.info("Hadoop map mode, one sequence file will be created as map/reduce job in hdfs for all input directories (comma-separated, parameter -d)");
            mapmode = true;
        } else {
            
            logger.info("Batch mode, one sequence file per input directory (comma-separated, parameter -d) in separate threads for each input directory.");
        }
        pc.setHadoopmapmode(mapmode);
        
        
        if ( !(cmd.hasOption(DIR_OPT)) && !(cmd.hasOption(HDFSINPUTPATH_OPT)) ) {
             exit("Either input directory (-d) or hdfs input paths (-p) must be given.", 1);
        }
        if ( cmd.hasOption(DIR_OPT) && cmd.hasOption(HDFSINPUTPATH_OPT) ) {
             exit("Set either input directory (-d) or hdfs input paths (-p), not both!", 1);
        }
        
        // hdfs input path
        if (!(cmd.hasOption(HDFSINPUTPATH_OPT) && cmd.getOptionValue(HDFSINPUTPATH_OPT) != null)) {
           
        } else {
            pathStr = cmd.getOptionValue(HDFSINPUTPATH_OPT);
            pc.setHdfsInputPath(pathStr);
            logger.info("HDFS input path: " + pathStr);
        }

        // dirs
        if (!(cmd.hasOption(DIR_OPT) && cmd.getOptionValue(DIR_OPT) != null)) {
           
        } else {
            dirStr = cmd.getOptionValue(DIR_OPT);
            pc.setDirsStr(dirStr);
            logger.info("Directory: " + dirStr);
        }

        // dirs
        if (!(cmd.hasOption(HADOOPJOBNAME_OPT) && cmd.getOptionValue(HADOOPJOBNAME_OPT) != null)) {
            logger.info("No job name given.", 1);
        } else {
            jobName = cmd.getOptionValue(HADOOPJOBNAME_OPT);
            pc.setHadoopJobName(jobName);
            logger.info("Hadoop job name: " + jobName);
        }

        // compression
        if (!(cmd.hasOption(COMPR_OPT) && cmd.getOptionValue(COMPR_OPT) != null)) {
            compressionType = "BLOCK";
            logger.info("No compression type given. Default BLOCK compression is used.");
        } else {
            compressionType = cmd.getOptionValue(COMPR_OPT);
            logger.info("Compression type: " + compressionType);
        }
        pc.setCompressionType(compressionType);

        // extension filter
        if (!(cmd.hasOption(EXT_FLG_OPT) && cmd.getOptionValue(EXT_FLG_OPT) != null)) {
            extStr = "";
            logger.info("No extension filter given.");
        } else {
            extStr = cmd.getOptionValue(EXT_FLG_OPT);
            pc.setExtStr(extStr);
            logger.info("Extension filter: " + extStr);
        }

        // textline mode
        if (!(cmd.hasOption(TEXTLINE_OPT))) {
            logger.info("Default mode, filename is key as Text, file content "
                    + "is value as BytesWritable.");
        } else {
            textlinemode = true;
            logger.info("Text line mode, key is empty as NullWritable, text "
                    + "lines as Text are values");
        }
        pc.setTextlinemode(textlinemode);
    }

    public static void exit(String msg, int status) {
        if (status > 0) {
            logger.error(msg);
        } else {
            logger.info(msg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(USAGE, OPTIONS, true);
        System.exit(status);
    }
}
