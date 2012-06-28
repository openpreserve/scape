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

package eu.scape_project.tb.wc.archd.hd;

import eu.scape_project.tb.wc.archd.tools.App;
import eu.scape_project.tb.wc.archd.hdreader.ArcInputFormat;
import eu.scape_project.tb.wc.archd.hdreader.ArcRecord;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 *
 * @author onbram
 */
public class HDApp extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        System.out.println("HADOOP ARC reader test application.");
        long startTime = System.currentTimeMillis();
        
        int res = ToolRunner.run(new Configuration(), new HDApp(), args);
        
        long elapsedTime = System.currentTimeMillis()-startTime;
        System.out.println("Processing time (sec): " + elapsedTime/1000F);
        
        System.exit(res);
    }

    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        Job job = new Job(conf);

        for (int i = 0; i < args.length; i++) {
            System.out.println("Arg" + i + ": " + args[i]);
        }

        //**********************************************************
        // for debugging in local mode
        // comment out the 2 lines below befor switching to pseudo-distributed or fully-distributed mode
        // job.getConfiguration().set("mapred.job.tracker", "local");
        // job.getConfiguration().set("fs.default.name", "local");
        //**********************************************************

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setJarByClass(App.class);
        job.setJobName("HADOOP ARC reader test application");

        //*** Set interface data types
        // We are using LONG because this value can become very large on huge archives.
        // In order to use the combiner function, also the map output needs to be a LONG.
        //job.setMapOutputKeyClass(Text.class);
        //job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);


        //*** Set up the mapper, combiner and reducer
        job.setMapperClass(Map.class);
        job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);


        //*** Set the MAP output compression
        //job.getConfiguration().set("mapred.compress.map.output", "true");


        //*** Set input / output format
        job.setInputFormatClass(ArcInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);


        //*** Start the job and wait for it
        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    /**
     * The map class of WordCount.
     */
    public static class Map
            extends Mapper<Text, ArcRecord, Text, LongWritable> {

        String recMimeType;
        String recType;
        String recURL;
        Date recDate;
        int recLength;
        int recHTTPret;
        InputStream recContent;
        String myTIKAout = "";
        Metadata met = new Metadata();
        LongWritable one = new LongWritable(1);
        DefaultDetector detector = new DefaultDetector();

        @Override
        public void map(Text key, ArcRecord value, Context context) throws IOException, InterruptedException {

            //            recMimeType = value.getMimeType();
            //            recType = value.getType();
            //            recURL = value.getUrl();
            //            recDate = value.getDate();
            recLength = value.getLength();
            // recHTTPret = value.getHttpReturnCode();

            //excude 0 size files. Otherwise they will falsify statistics.
            // 0byte files with jpg extension => jpg detect
            // 0byte files with jpg extension => txt as http-get stored type

            if (recLength > 0) {
                recContent = value.getContents();
                met.set(Metadata.RESOURCE_NAME_KEY, key.toString());
                MediaType mediaType = detector.detect(recContent, met);
                myTIKAout = mediaType.toString().intern();                                
            } else {
                myTIKAout = "SIZE=0";
            }

            context.write(new Text(myTIKAout), one);

        }
    }

    /**
     * The reducer class of WordCount
     */
    public static class Reduce
            extends Reducer<Text, LongWritable, Text, LongWritable> {

        long sum;

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {

            sum = 0;
            for (LongWritable value : values) {
                sum += value.get();
            }
            context.write(key, new LongWritable(sum));

        }
    }
}
