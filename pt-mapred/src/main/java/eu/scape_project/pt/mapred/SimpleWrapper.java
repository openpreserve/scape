package eu.scape_project.pt.mapred;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.WritableRenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import eu.planets_project.services.utils.ProcessRunner;

public class SimpleWrapper extends Configured implements Tool {

	private static Log LOG = LogFactory.getLog(SimpleWrapper.class);
	
	public static class MyMapper extends Mapper<Object, Text, Text, IntWritable> {
    
	    //Mapper<Text, Buffer, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
	    private Text word = new Text();
			
		public void setup( Context context ) {
		}
			      
	    public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	
	    	
	    	System.out.println("MyMapper.map key:"+key.toString()+" value:"+value.toString());
	    	FileSystem hdfs = FileSystem.get(new Configuration());
	    	Path inFile = new Path("hdfs://"+value.toString());
	    	Path outFile = new Path("hdfs://"+value.toString()+".pdf");
	    	
	    	
	    	if (!hdfs.exists(inFile)) {
	    	  System.out.println("Input file not found");
	    	  return;
	    	}
	    	
	    	String fn = inFile.getName();
	    	System.out.println("procsssing file: "+fn);
	    	
	    	//opening file
	    	FSDataInputStream in = hdfs.open(inFile);
	    	    	
	    	//read from stdin and write to local file system
	    	//Process p = Runtime.getRuntime( ).exec ("ps2pdf - /home/root/hadoop-0.21.0/bin/"+fn+".pdf");
	    	
	    	//read from stdin and write to stdout
	    	Process p = Runtime.getRuntime( ).exec ("ps2pdf - ");
	    	//Writer w = new java.io.OutputStreamWriter(p.getOutputStream( )) ;
	    	
	    	//pass input stream to process
	    	OutputStream out = p.getOutputStream();
	    	byte[] buffer = new byte[1024];
	    	int bytesRead = -1;
	   	
	    	System.out.println("writing to stdin");
	    	
	    	while ( ( bytesRead = in.read(buffer) ) > 0 ) {
	    		System.out.print(".");
	    		out.write(buffer, 0, bytesRead);
	    	}
	    	System.out.println("\n input written");
	    	
	    	//out.flush();
	    	in.close();
	    	out.close();
	    	
	    	//log stderr
	    	InputStream stderr = p.getErrorStream();
	    	InputStreamReader isr = new InputStreamReader(stderr);
	    	BufferedReader br = new BufferedReader(isr);
	    	String line = null;
	    	System.out.println("<ERROR>");
	    	while ( (line = br.readLine()) != null) System.out.println(line);
	    	System.out.println("</ERROR>");
	    	stderr.close();
	    	isr.close();    	
	      
	    	InputStream stdout = p.getInputStream();
	      
		    //print stdout
		    /*
		    InputStreamReader isr2 = new InputStreamReader(stdout);
		    br = new BufferedReader(isr2);
		    line = null;
		
		    System.out.println("<Stdout>");
		    while ( (line = br.readLine()) != null)
		    	System.out.println(line);
		      	System.out.println("</Stdout>");
		      	//isr2.close();
		     */
	
	    	//write stdout to hdfs
	    	System.out.println("reading from stdout");
	    	FSDataOutputStream hdfs_out = hdfs.create(outFile);  
	    	while ((bytesRead = stdout.read(buffer)) > 0) {
	    		System.out.print(".");
	    		hdfs_out.write(buffer, 0, bytesRead);
	    	}
	    	System.out.println("output written");
	
	    	//hdfs_out.flush();
	    	stdout.close();
	    	hdfs_out.close();
	      
	    	System.out.println("Waiting for Process to exit...");
	    	int exitVal = p.waitFor();
	    	System.out.println("Process exitValue: " + exitVal);
    	
	    }
	}
  

	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
		private IntWritable result = new IntWritable();
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
			/*
			Iterator<IntWritable> all = values.iterator();
			int max = 0;
			while( all.hasNext() )
			{
				int thismax = all.next().get();
				if( thismax > max ) max = thismax;
			}
			
			LOG.debug("reducing, max = " + max );
			context.write(key, new IntWritable( max ) );
			*/
			
			/*
			LOG.error("REDUCER with Key:"+key.toString());
			int sum = 0;
	      	for (IntWritable val : values) {
	        	sum += val.get();
	      	}
	      	result.set(sum);
	      	context.write(key, result);
			*/
		}
	}
	
	

	public int run(String[] args) throws Exception {

		Configuration conf = getConf();

		Job job = new Job(conf);
		
		job.setJobName("mrexample");
		job.setJarByClass(SimpleWrapper.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setMapperClass(MyMapper.class);
		//job.setReducerClass(MyReducer.class);

		//job.setInputFormatClass(VideoInputFormat.class);
		//job.setOutputFormatClass(FileOutputFormat.class);
		
		//job.setOutputFormatClass(MultipleOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path("output/"+args[1]));
		
		//job.setNumReduceTasks(Integer.parseInt(args[2]));

		//FileInputFormat.setInputPaths(job, s.toString());
		//FileOutputFormat.setOutputPath(job, new Path("output"));

		//FileInputFormat.setMaxInputSplitSize(job, 1000000);

		job.waitForCompletion(true);
		return 0;
	}


	public static void main(String[] args) throws Exception {

		boolean windows = ((System.getProperty("os.name").toLowerCase().indexOf("windows") < 0) ? false : true);
		LOG.debug("OS: "+System.getProperty("os.name"));
		
		SimpleWrapper mr = new SimpleWrapper();
		
		int res =-1;
		try {
			LOG.info("Running MapReduce ..." );
			res = ToolRunner.run(new Configuration(), mr, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(res);
	}		
		
}
	
	
