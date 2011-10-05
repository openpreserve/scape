package eu.scape_project.pt.mapred;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import joptsimple.OptionParser;

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
import org.apache.hadoop.util.ToolRunner;

//import eu.scape_project.pit.invoke.Peu.scape_project.pt.mapreditInvoker;
import eu.scape_project.pt.pit.ToolMap;
//import eu.scape_project.pt.pit.invoke.PTInvoker;
import eu.scape_project.pt.util.ArgsParser;
import eu.scape_project.pt.util.PtRecordParser;
import eu.scape_project.pt.pit.Tool;
import eu.scape_project.pt.proc.Preprocessor;
import eu.scape_project.pt.proc.Processor;
import eu.scape_project.pt.fs.util.HDFSFiler;

/**
 * A very simple wrapper to execute cmd-line tools using mapReduce
 * @author Rainer Schmidt [rschmidt13]
 */ 
public class SimpleWrapper extends Configured implements org.apache.hadoop.util.Tool {

	private static Log LOG = LogFactory.getLog(SimpleWrapper.class);
	
	public static class MyMapper extends Mapper<Object, Text, Text, IntWritable> {
	    //Mapper<Text, Buffer, Text, IntWritable> {
		
		//TODO
		//use logger for writing to std. out
    
		private final static IntWritable one = new IntWritable(1);
	    private Text word = new Text();
			
		public void setup( Context context ) {
		}

	    public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	
	    	LOG.info("MyMapper.map key:"+key.toString()+" value:"+value.toString());
	    	PtRecordParser rparser = new PtRecordParser(value.toString());
	    	String tstr = context.getConfiguration().get(ArgsParser.TOOLSTRING);
	    	Tool tool = Tool.fromString(tstr);
	    	
	    	String pstr = context.getConfiguration().get(ArgsParser.PARAMETERLIST);
	    	String[] params = (pstr == null ? null : pstr.split(Pattern.quote(" ")));
	    	
	    	//Let's implement a simple workflow
	    	//read file (1) -> execute (2) -> write file (3)
	    	
	    	String[] inFiles = rparser.getInFiles();
	    	String[] outFiles = rparser.getOutFiles();
	    	String[] cmdArgs = rparser.getCmdArguments();
	    	
	    	//TODO
	    	//prepare execution (download files, attach pipes)
	    	//start execution 
	    	//finish execution (write output files)
	    	
	    	//pre
	    	FileSystem hdfs = FileSystem.get(new Configuration());
	    	Preprocessor preProcessor = new Preprocessor(inFiles, hdfs);
	    	
	    	try {
	    		preProcessor.retrieveFiles();
	    	} catch(Exception e_pre) {
	    		LOG.error("Exception in propocessing phase: " + e_pre.getMessage(), e_pre);
	    		e_pre.printStackTrace();
	    	}	    	
	    	
	    	tool.replaceToken(Tool.FILE, cmdArgs);
	    	tool.replaceToken(Tool.PARAM, params);
	    	Processor processor = new Processor(tool);
	    	int exitCode = 0;
	    	
	    	//exec
	    	try {
	    		processor.initialize();
	    		exitCode = processor.execute();
	    		LOG.info("Execution terminated with code: "+exitCode);
	    	} catch(Exception e_exec) {
	    		LOG.error("Exception in execution phase: " + e_exec.getMessage(), e_exec);
	    		e_exec.printStackTrace();
	    	}	    		    		
	    	
	    	//post
	    	for(String s : preProcessor.getTempDir().list()) {
	    		File f = new File(s);
	    		LOG.info("in tmp dir: "+ f.getName()+ " " +f.getTotalSpace());
	    	}
	    	
	    	/** STREAMING works but we'll integrate that later
	    	//Path inFile = new Path("hdfs://"+value.toString());
	    	//Path outFile = new Path("hdfs://"+value.toString()+".pdf");
	    	//Path fs_outFile = new Path("/home/rainer/tmp/"+inFile.getName()+".pdf");

	    	 
	    	String[] cmds = {"ps2pdf", "-", "/home/rainer/tmp"+fn+".pdf"};
	    	//Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[2]).start();
	    	Process p = new ProcessBuilder(cmds[0],cmds[1],cmds[1]).start();
	    	
	    	//opening file
	    	FSDataInputStream hdfs_in = hdfs.open(inFile);
	    	FSDataOutputStream hdfs_out = hdfs.create(outFile);
	    	//FileOutputStream fs_out = new FileOutputStream(fs_outFile.toString());
	    		    	
	    	//pipe(process.getErrorStream(), System.err);
	    	
	    	OutputStream p_out = p.getOutputStream();
	    	InputStream p_in = p.getInputStream();
	    	//TODO copy outstream and send to log file
	    	
	    	byte[] buffer = new byte[1024];
	    	int bytesRead = -1;
	    	
	    	System.out.println("streaming data to process");
	    	Thread toProc = pipe(hdfs_in, new PrintStream(p_out), '>');
	    	
	    	System.out.println("streaming data to hdfs");()
	    	Thread toHdfs = pipe(p_in, new PrintStream(hdfs_out), 'h'); 
	    	
	    	//pipe(process.getErrorStream(), System.err);
	    	
	    	toProc.join();	    	
	    	toHdfs.join();
	    	
	    	*/


	    }
	}
	  

	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
		private IntWritable result = new IntWritable();
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
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

		job.setInputFormatClass(PtInputFormat.class);
		//job.setOutputFormatClass(FileOutputFormat.class);
		
		//job.setOutputFormatClass(MultipleOutputFormat.class);
		
		//FileInputFormat.addInputPath(job, new Path(args[0])); ArgsParser.INFILE
		//FileOutputFormat.setOutputPath(job, new Path(args[1])); ArgsParser.OUTDIR
		FileInputFormat.addInputPath(job, new Path(conf.get(ArgsParser.INFILE)));
		String outDir = (conf.get(ArgsParser.OUTDIR) == null) ? "out/"+System.currentTimeMillis()%1000 : conf.get(ArgsParser.OUTDIR); 
		FileOutputFormat.setOutputPath(job, new Path(outDir) ); 
				
		//add command to job configuration
		//conf.set(TOOLSPEC, args[2]);
		
		//job.setNumReduceTasks(Integer.parseInt(args[2]));

		//FileInputFormat.setInputPaths(job, s.toString());
		//FileOutputFormat.setOutputPath(job, new Path("output"));

		//FileInputFormat.setMaxInputSplitSize(job, 1000000);

		job.waitForCompletion(true);
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		
		int res = 1;
		SimpleWrapper mr = new SimpleWrapper();
        Configuration conf = new Configuration();
		ToolMap toolMap = new ToolMap();
        		
		try {
			ArgsParser pargs = new ArgsParser("i:o:t:p:x", args);
			//input file
			LOG.info("input: "+ pargs.getValue("i"));
			//hadoop's output 
			LOG.info("output: "+pargs.getValue("o"));
			//tool to select
			LOG.info("tool: "+pargs.getValue("t")+" ...lookup returned: "+toolMap.get(pargs.getValue("t")));		
			//defined parameter list
			LOG.info("parameters: "+pargs.getValue("p"));
			
			conf.set(ArgsParser.INFILE, pargs.getValue("i"));			
			Tool tool = toolMap.get(pargs.getValue("t"));
			if(tool != null) conf.set(ArgsParser.TOOLSTRING, tool.toString());
	        if (pargs.hasOption(ArgsParser.OUTDIR)) conf.set(ArgsParser.OUTDIR, pargs.getValue("o"));
	        if (pargs.hasOption(ArgsParser.PARAMETERLIST)) conf.set(ArgsParser.PARAMETERLIST, pargs.getValue("p"));
	        
			if(tool == null) {
				System.out.println("Cannot find tool: "+pargs.getValue("t"));
				System.exit(-1);
			}
	        //don't run hadoop
	        if(pargs.hasOption("x")) {
	        	
	        	System.out.println("option x detected.");	        	
	        	System.exit(1);
	        }
		} catch (Exception e) {
			System.out.println("usage: SimpleWrapper -i inFile [-o outFile] [-p \"parameterList\"] -t cmd");
			LOG.info(e);
			System.exit(-1);
		}
				
        try {
			LOG.info("Running MapReduce ..." );
			res = ToolRunner.run(conf, mr, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(res);
	}		
		
}
	
	
