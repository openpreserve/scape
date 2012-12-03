package eu.scape_project.pt.mapred;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import eu.scape_project.pt.pit.ToolMap;
import eu.scape_project.pt.pit.ToolSpec;
import eu.scape_project.pt.proc.FileProcessor;
import eu.scape_project.pt.proc.Processor;
import eu.scape_project.pt.proc.TaskProcessor;
import eu.scape_project.pt.util.ArgsParser;
import eu.scape_project.pt.util.PtRecordParser;

/**
 * A very simple wrapper to execute cmd-line tools using mapReduce
 * @author Rainer Schmidt [rschmidt13]
 */ 
public class SimpleWrapper extends Configured implements org.apache.hadoop.util.Tool {

	private static Log LOG = LogFactory.getLog(SimpleWrapper.class);
	
	public static class MyMapper extends Mapper<Object, Text, Text, IntWritable> {
	    //Mapper<Text, Buffer, Text, IntWritable> {
		
		//TODO use logger for writing to std. out
			
		public void setup( Context context ) {
		}

		public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	
	    	LOG.info("MyMapper.map key:"+key.toString()+" value:"+value.toString());
	    	PtRecordParser rparser = new PtRecordParser(value.toString());
	    	String tstr = context.getConfiguration().get(ArgsParser.TOOLSTRING);
	    	ToolSpec toolSpec = ToolSpec.fromString(tstr);
	    	
	    	String outDir = context.getConfiguration().get(ArgsParser.OUTDIR);
	    	toolSpec.getContext().put(ToolSpec.RESULT_DIR, outDir);
	    	
	    	String pstr = context.getConfiguration().get(ArgsParser.PARAMETERLIST);
	    	String[] params = (pstr == null ? null : pstr.split(Pattern.quote(" ")));
	    	
	    	//Let's implement a simple workflow
	    	//read file (1) -> execute (2) -> write file (3)
	    	
	    	String[] inFiles = rparser.getInFiles();
	    	String[] outFiles = rparser.getOutFiles();
	    	String[] cmdArgs = rparser.getCmdArguments();
	    	
	    	//prepare execution (download files, attach pipes)
	    	//start execution 
	    	//finish execution (write output files)
	    	
	    	//pre
	    	FileSystem hdfs = FileSystem.get(new Configuration());
	    	FileProcessor fileProcessor = new FileProcessor(inFiles, outFiles, hdfs);
	    	
	    	try {
	    		fileProcessor.resolvePrecondition();
	    	} catch(Exception e_pre) {
	    		LOG.error("Exception in preprocessing phase: " + e_pre.getMessage(), e_pre);
	    		e_pre.printStackTrace();
	    	}	    	
	    	
	    	toolSpec.replaceTokenInCmd(ToolSpec.FILE, cmdArgs);
	    	toolSpec.replaceTokenInCmd(ToolSpec.PARAM, params);
	    	//TODO use sthg. like contextObject to manage type safety
	    	//toolSpec.getContext().put(ToolSpec.EXEC_DIR, execDir);
	    	Processor processor = new TaskProcessor(toolSpec);
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
	    	
	    	//some logging
	    	//for(String s : MapSessionFiler.getExecDir().list()) {
	    	//	File f = new File(s);
	    	//	LOG.info("in tmp dir: "+ f.getName()+ " " +f.getTotalSpace());
	    	//}
	    	
	    	try {
	    		fileProcessor.resolvePostcondition();
	    	} catch(Exception e_post) {
	    		LOG.error("Exception in postprocessing phase: " + e_post.getMessage(), e_post);
	    		e_post.printStackTrace();
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
	    	
	    	*/


	    }
	}
	  

	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
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
		conf.set(ArgsParser.OUTDIR, outDir);
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
			toolMap.initialize();
			ToolSpec tool = toolMap.get(pargs.getValue("t"));
			if(tool != null) conf.set(ArgsParser.TOOLSTRING, tool.toString());
	        if (pargs.hasOption("o")) conf.set(ArgsParser.OUTDIR, pargs.getValue("o"));
	        if (pargs.hasOption("p")) conf.set(ArgsParser.PARAMETERLIST, pargs.getValue("p"));
	        
			if(tool == null) {
				System.out.println("Cannot find tool: "+pargs.getValue("t"));
				System.exit(-1);
			}
	        //don't run hadoop
	        if(pargs.hasOption("x")) {
	        	
	        	/*
	        	String t = System.getProperty("java.io.tmpdir");
	    		LOG.info("Using Temp. Directory:" + t);
	    		File execDir = new File(t);
	    		if(!execDir.exists()) {
	    			execDir.mkdir();
	    		}
	        	
	    		LOG.info("Is execDir a file: "+execDir.isFile() + " and a dir: "+execDir.isDirectory());
	    		File paper_ps = new File(execDir.toString()+"/paper.ps");
	    		LOG.info("Looking for this file: "+paper_ps);
	    		LOG.info("Is paper.ps a file: "+paper_ps.isFile());
	    		
	    		//LOG.info("trying ps2pdf in without args.....");
	    		String cmd = "/usr/bin/ps2pdf paper.ps paper.ps.pdf";
	    		String[] cmds = cmd.split(" ");
	    		System.out.println("cmds.length "+cmds.length);
	    		ProcessBuilder pb = new ProcessBuilder(cmds);
	    		pb.directory(execDir);
	    		Process p1 = pb.start();
	    		//LOG.info(".....");
	        	*/
	        	
	        	
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
	
	
