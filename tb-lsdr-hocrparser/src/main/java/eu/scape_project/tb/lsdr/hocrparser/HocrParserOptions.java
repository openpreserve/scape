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
package eu.scape_project.tb.lsdr.hocrparser;

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
public class HocrParserOptions {

    private static Logger logger = LoggerFactory.getLogger(HocrParserOptions.class.getName());
    // Statics to set up command line arguments
    public static final String HELP_FLG = "h";
    public static final String HELP_OPT = "help";
    public static final String HELP_OPT_DESC = "print this message.";

    public static final String DIR_FLG = "d";
    public static final String DIR_OPT = "dir";
    public static final String DIR_OPT_DESC = "HDFS directory containing sequence files.";
    
    public static final String NAME_FLG = "n";
    public static final String NAME_OPT = "name";
    public static final String NAME_OPT_DESC = "Job name.";
    
    public static org.apache.commons.cli.Options OPTIONS = new org.apache.commons.cli.Options();
    public static final String USAGE = "hadoop jar"
            + " target/tb-lsdr-hocrparser-0.1-SNAPSHOT-jar-with-dependencies.jar";
    static {
        OPTIONS.addOption(HELP_FLG, HELP_OPT, false, HELP_OPT_DESC);
        
        OPTIONS.addOption(DIR_FLG, DIR_OPT, true, DIR_OPT_DESC);
        OPTIONS.addOption(NAME_FLG, NAME_OPT, true, NAME_OPT_DESC);
    }
    
    public static void initOptions(CommandLine cmd, HocrParserCliConfig pc) {

        
        // dir
        String dirStr;
        if (!(cmd.hasOption(DIR_OPT) && cmd.getOptionValue(DIR_OPT) != null)) {
            exit("No directory given.", 1);
        } else {
            dirStr = cmd.getOptionValue(DIR_OPT);
            pc.setDirStr(dirStr);
            logger.info("Directory: " + dirStr);
        }
        
        // name
        String nameStr;
        if (!(cmd.hasOption(NAME_OPT) && cmd.getOptionValue(NAME_OPT) != null)) {
            //exit("No hadoop job name given.", 1);
        } else {
            nameStr = cmd.getOptionValue(NAME_OPT);
            pc.setHadoopJobName(nameStr);
            logger.info("Hadoop job name: " + nameStr);
        }
       
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
