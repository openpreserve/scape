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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HocrParser {

    // Logger instance
    private static Logger logger = LoggerFactory.getLogger(HocrParser.class.getName());

    public static class HocrParserReducer
            extends Reducer<Text, Text, Text, LongWritable> {

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String keyStr = key.toString();
            if (keyStr != null && !keyStr.equals("")) {
                Iterator<Text> it = values.iterator();
                int blockCount = 0;
                int sumBlockWidth = 0;

                while (it.hasNext()) {
                    Text value = it.next();
                    String strVal = value.toString();
                    if (strVal != null && !strVal.equals("")) {
                        StringTokenizer st = new StringTokenizer(strVal, ";");
                        while (st.hasMoreTokens()) {
                            String var = st.nextToken();
                            if (var != null && !var.equals("") && var.contains("w:")) {
                                String cleaned = var.replace("w:", "").trim();
                                try {
                                    int blockWidth = Integer.parseInt(cleaned);
                                    sumBlockWidth += blockWidth;
                                    blockCount++;
                                } catch (NumberFormatException _) {
                                }

                            }
                        }
                    }

                }
                int avbw = 0;
                try {
                    if (blockCount != 0) {
                        avbw = sumBlockWidth / blockCount;
                    }
                } catch (java.lang.ArithmeticException _) {
                    // averageBlockWidth is 0
                }

                context.write(key, new LongWritable(avbw));
            }
        }
    }

    /**
     * The map class of HocrParser.
     */
    public static class HocrParserMapper
            extends Mapper<Text, BytesWritable, Text, Text> {

        @Override
        public void map(Text key, BytesWritable value, Mapper.Context context)
                throws IOException, InterruptedException {

            // Attention: Hadoop versions < 0.22.0 return a padded byte array
            // with arbitrary data chunks and zero bytes using BytesWritable.getBytes.
            // BytesWritable.getBytes.getLength() returns the real size of the
            // BytesWritable content. The name of BytesWritable.getBytes is
            // misleading, which has been fixed in Hadoop version 0.22.0.
            // See https://issues.apache.org/jira/browse/HADOOP-6298
            byte[] bytes = value.getBytes();
            int bytesLen = value.getLength();
            byte[] slicedBytes = new byte[bytesLen];
            System.arraycopy(bytes, 0, slicedBytes, 0, bytesLen);

            InputStream hocrInputStream = new ByteArrayInputStream(slicedBytes);

            Document doc = Jsoup.parse(hocrInputStream, "UTF-8", "http://books.google.com");
            String tag = "div";

            String type = "ocrx_block";

            List<Element> spans = doc.getElementsByTag(tag);
            for (Element span : spans) {
                String attr_class = span.attr("class");
                if (attr_class != null && attr_class.equals(type)) {
                    String attr_title = span.attr("title");
                    if (attr_title != null) {
                        if (attr_title.startsWith("bbox ")) {
                            String lines = attr_title.substring(5);
                            StringTokenizer st = new StringTokenizer(lines);
                            int i = 0;
                            int x1 = 0, y1 = 0, x2 = 0, y2 = 0, w = 0, h = 0;
                            while (st.hasMoreElements()) {
                                String coord = st.nextToken();
                                int c = Integer.parseInt(coord);
                                if (i == 0) {
                                    x1 = c;
                                }
                                if (i == 1) {
                                    y1 = c;
                                }
                                if (i == 2) {
                                    x2 = c;
                                }
                                if (i == 3) {
                                    y2 = c;
                                }
                                i++;
                            }
                            w = x2 - x1;
                            h = y2 - y1;
                            String format = "x1: %d; y1: %d; x2: %d; y2: %d; w: %d; h: %d";
                            String line = String.format(format, x1, y1, x2, y2, w, h);
                            Pattern pattern = Pattern.compile("[0-9]{3,15}");
                            Matcher matcher = pattern.matcher(key.toString());
                            String outKeyStr = "Z";
                            while (matcher.find()) {
                                outKeyStr += matcher.group(0);
                                outKeyStr += ('/');
                            }
                            outKeyStr = outKeyStr.substring(0, outKeyStr.length() - 1);
                            Text outkey = new Text(outKeyStr);
                            Text outvalue = new Text(line);
                            context.write(outkey, outvalue);
                        }
                    }
                }
            }
        }
    }

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws ParseException {
        Configuration conf = new Configuration();

        //conf.setBoolean("mapreduce.client.genericoptionsparser.used", true);
        GenericOptionsParser gop = new GenericOptionsParser(conf, args);
        HocrParserCliConfig pc = new HocrParserCliConfig();
        CommandLineParser cmdParser = new PosixParser();
        CommandLine cmd = cmdParser.parse(HocrParserOptions.OPTIONS, gop.getRemainingArgs());
        if ((args.length == 0) || (cmd.hasOption(HocrParserOptions.HELP_OPT))) {
            HocrParserOptions.exit("Usage", 0);
        } else {
            HocrParserOptions.initOptions(cmd, pc);
        }
        String dir = pc.getDirStr();

        String name = pc.getHadoopJobName();
        if (name == null || name.equals("")) {
            name = "hocr_parser";
        }

        try {
            Job job = new Job(conf, name);
            job.setJarByClass(HocrParser.class);

            job.setMapperClass(HocrParserMapper.class);
            //job.setCombinerClass(HocrParserReducer.class);
            job.setReducerClass(HocrParserReducer.class);

            job.setInputFormatClass(SequenceFileInputFormat.class);

            job.setOutputFormatClass(TextOutputFormat.class);
            //SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.NONE);

            //conf.setMapOutputKeyClass(Text.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);



            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(LongWritable.class);

            SequenceFileInputFormat.addInputPath(job, new Path(dir));
            String outpath = "output/" + System.currentTimeMillis() + "hop";
            FileOutputFormat.setOutputPath(job, new Path(outpath));
            job.waitForCompletion(true);
            System.out.print(outpath);
            System.exit(0);
        } catch (Exception e) {
            logger.error("IOException occurred", e);
        }
    }
}