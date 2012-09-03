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

import eu.scape_project.tb.lsdr.seqfileutility.batch.BatchJob;
import eu.scape_project.tb.lsdr.seqfileutility.hadoop.HadoopJob;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;

/**
 * Utility command line application for creating hadoop sequence files.
 * usage: {hadoop jar|java -jar}
 *       target/tb-lsdr-seqfileutility-0.1-SNAPSHOT-jar-with-dependencies.ja
 *       r [-c <arg>] [-d <arg>] [-e <arg>] [-h] [-m] [-t]
 * -c,--compr <arg>   Compression, one of NONE,RECORD,BLOCK, default: BLOCK
 *                    (Optional).
 * -d,--dir <arg>     Local directory (or directories - commaseparated)
 *                    containing files to be added. Note that in hadoop map
 *                    mode (parameter -m) each tasktracker must be able to
 *                    access the files at the same path. (Optional, but either
 *                    -d or -p is required)
 * -p,-- paths        HDFS Input path where the text files containing file
 *                    path is available (but either -d or -p is required)
 * -e,--ext <arg>     Extension filter(s) - commaseparated (Optional).
 * -h,--help          print this message.
 * -m,--mapmode       Hadoop map mode: A text file containing all absolute
 *                    paths of the input directory (parameter -d) is created
 *                    and the sequence file is created using a map/reduce
 *                    job. The sequence file is direcly stored in HDFS. If
 *                    this parameter is omitted, the sequence file creation
 *                    runs as a batch process, adding all files of a
 *                    directory to a sequence file (one per directory) in a
 *                    separate thread. (Optional)
 * -t,--textline      Text line mode, input files aretext files and each
 *                    line of the text files should be added as a record in
 *                    the sequence file (Optional).
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class SequenceFileUtility {

    /**
     * Main method
     * @param args Command line arguments
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {
        
        int res = 1;
        Configuration conf = new Configuration();
        //conf.set("mapred.max.split.size", "16777216");
        
        //conf.setBoolean("mapreduce.client.genericoptionsparser.used", false);
        GenericOptionsParser gop = new GenericOptionsParser(conf, args);
        ProcessParameters pc = new ProcessParameters();
        CommandLineParser cmdParser = new PosixParser();
        CommandLine cmd = cmdParser.parse(Options.OPTIONS, gop.getRemainingArgs() );
        if ((args.length == 0) || (cmd.hasOption(Options.HELP_OPT))) {
            Options.exit("Usage", 0);
        } else {
            Options.initOptions(cmd, pc);
            eu.scape_project.tb.lsdr.seqfileutility.Job j;
            if (!pc.isHadoopmapmode()) {
                j = new BatchJob(pc);
                j.run();
                res = 0;
            } else {
                HadoopJob hj =  new HadoopJob();
                hj.setPc(pc);
                res = ToolRunner.run(conf,hj, args);
                if(res == 0) System.out.print(pc.getOutputDirectory());
                
            }
        }
        System.exit(res);
    }
}
