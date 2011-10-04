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
	    	String str = context.getConfiguration().get(ArgsParser.TOOL);
	    	Tool tool = Tool.fromString(str);
	    	
	    	//Let's implement a simple workflow
	    	//read file (1) -> execute (2) -> write file (3)
	    	
	    	String[] inFiles = rparser.getInFiles();
	    	String[] outFiles = rparser.getOutFiles();
	    	String[] cmdArgs = rparser.getCmdArguments();
	    	
	    	//TODO
	    	//prepare execution (download files, attach pipes)
	    	//start execution 
	    	//finish execution (write output files)
	    	
	    	FileSystem hdfs = FileSystem.get(new Configuration());
	    	Preprocessor preProcessor = new Preprocessor(inFiles, hdfs);
	    	
	    	try {
	    		preProcessor.retrieveFiles();
	    	} catch(Exception e) {
	    		LOG.error(e.getMessage(), e);
	    		e.printStackTrace();
	    	}
	    	//TODO 
	    	//move this to Process object
	    	
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
	    
    	private Thread pipe(final InputStream src, final PrintStream dest, final char debugToken) throws IOException {
    		System.out.println("Starting piping "+ debugToken);
    	    Thread t = new Thread(new Runnable() {
    	        public void run() {
    	            try {
    	                byte[] buffer = new byte[1024];
    	                for (int n = 0; n != -1; n = src.read(buffer)) {
    	                	System.out.print(debugToken);
    	                    dest.write(buffer, 0, n);
    	                    System.out.println("/"+debugToken);
    	                }
    	                //reader at EOF, flush writer, and close streams
    	                System.out.print("EOF, flushing, and closing \n");
    	                dest.flush();
    	                src.close();
    	                dest.close();
    	                return;
    	            } catch (IOException e) { // just exit
    	            	System.out.println(e);
    	            	return;
    	            }
    	        }
    	    });
    	    t.start();
    	    return t;
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
        ToolMap tools = new ToolMap();
        tools.initialize();
        		
		try {
			ArgsParser pargs = new ArgsParser("i:o:t:x", args);
			//input file
			LOG.info("input: "+ pargs.getValue("i"));
			//hadoop's output 
			LOG.info("output: "+pargs.getValue("o"));
			//tool to select
			LOG.info("tool: "+pargs.getValue("t")+" ...lookup returned: "+tools.get(pargs.getValue("t")));		
			//TODO user defined parameter list
			
			conf.set(ArgsParser.INFILE, pargs.getValue("i"));
	        conf.set(ArgsParser.TOOL, tools.get(pargs.getValue("t")).toString());
	        if (pargs.hasOption(ArgsParser.OUTDIR)) conf.set(ArgsParser.OUTDIR, pargs.getValue("o"));
	        
	        //don't run hadoop
	        if(pargs.hasOption("x")) {

	        	/*
	        	String[] pres = null;
	        	String[] execs = null;
	        	String[] posts = null;
	        	
	        	String rec = "-exec file1 file2 file3 -pre in1 in2 -post out1 out2";
	        	String recs[] = rec.trim().split(" ");
	        	//int find = Arrays.asList(recs).indexOf("file1");
	        	List<String> lrecs = Arrays.asList(recs);
	        	
	        	Vector<Integer> vtokens = new Vector<Integer>();
	        	Integer exec = new Integer(lrecs.indexOf(PtRecordParser.EXEC_CONDITION));
	        	Integer pre = new Integer(lrecs.indexOf(PtRecordParser.PRE_CONDITION));
	        	Integer post = new Integer(lrecs.indexOf(PtRecordParser.POST_CONDITION));
	        	Integer eol = new Integer(lrecs.size());

	        	vtokens.add(exec);
	        	vtokens.add(pre);
	        	vtokens.add(post);
	        	vtokens.add(eol);
	        	Collections.sort(vtokens);
	        	
	        		        	
				if(exec > -1) 
	        		execs= lrecs.subList(exec.intValue()+1, vtokens.get(vtokens.indexOf(exec)+1)).toArray(new String[0]);
	        	if(pre > -1) 
	        		pres = (String[])lrecs.subList(pre+1, vtokens.get(vtokens.indexOf(pre)+1)).toArray(new String[0]);
	        	if(post > -1)
	        		posts = (String[])lrecs.subList(post+1, vtokens.get(vtokens.indexOf(post)+1)).toArray(new String[0]);
	        		
	        	
	        	System.out.println("exec is: "+Arrays.toString(execs));
	        	System.out.println("pre is: "+Arrays.toString(pres));
	        	System.out.println("post is: "+Arrays.toString(posts));
	        	*/
	        	
	        	System.out.println("option x detected");	        	
	        	System.exit(1);
	        }
		} catch (Exception e) {
			System.out.println("usage: SimpleWrapper -i inFile [-o outFile] -t cmd");
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
	
	
