package eu.scape_project.pt.mapred;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import eu.scape_project.pt.executors.Executor;
import eu.scape_project.pt.executors.ToolspecExecutor;
import eu.scape_project.pt.util.ArgsParser;
/**
 * A command-line interaction wrapper to execute cmd-line tools with MapReduce.
 * Code based on SimpleWrapper.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 */ 
public class CLIWrapper extends Configured implements org.apache.hadoop.util.Tool {

	private static Log LOG = LogFactory.getLog(CLIWrapper.class);
	
	public static class CLIMapper extends Mapper<Object, Text, Text, IntWritable> {

		// Executes the setup and map jobs.
		Executor executor;
		
        /**
         * Parser for the parameters in the command-lines (records).
         */
        static ArgsParser parser = null;
			
        /**
         * Sets up stuff which needs to be created only once and can be used in all maps this Mapper performs.
         * 
         * @param context
         */
        @Override
		public void setup( Context context ) {
        	executor = new ToolspecExecutor(context.getConfiguration().get(ArgsParser.TOOLSTRING), context.getConfiguration().get(ArgsParser.ACTIONSTRING));
	    	executor.setup();
		}

        /**
         * The map gets a key and value.
         * 
         * @param key key of the map job
         * @param value value of the map job
         * @param context Job context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
		public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	executor.map(key, value);
	    }	  
	}


	public static class CLIReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
        @Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
		}
	}
	
    /**
     * Sets up, initializes and starts the Job.
     * 
     * @param args
     * @return
     * @throws Exception
     */
    @Override
	public int run(String[] args) throws Exception {

		Configuration conf = getConf();
		Job job = new Job(conf);
		
		job.setJarByClass(CLIWrapper.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setMapperClass(CLIMapper.class);
	
		
		//job.setReducerClass(MyReducer.class);

		job.setInputFormatClass(PtInputFormat.class);
		//job.setOutputFormatClass(FileOutputFormat.class);
		
		//job.setOutputFormatClass(MultipleOutputFormat.class);
		
		//FileInputFormat.addInputPath(job, new Path(args[0])); ArgsParser.INFILE
		//FileOutputFormat.setOutputPath(job, new Path(args[1])); ArgsParser.OUTDIR
		FileInputFormat.addInputPath(job, new Path(conf.get(ArgsParser.INFILE)));
		String outDir = (conf.get(ArgsParser.OUTDIR) == null) ? "out/"+System.nanoTime()%10000 : conf.get(ArgsParser.OUTDIR); 
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
		CLIWrapper mr = new CLIWrapper();
        Configuration conf = new Configuration();
        		
		try {
			ArgsParser pargs = new ArgsParser("i:o:t:a:p:x", args);
			//input file
			LOG.info("input: "+ pargs.getValue("i"));
			//hadoop's output 
			LOG.info("output: "+pargs.getValue("o"));
			//tool to select
			LOG.info("tool: "+pargs.getValue("t"));
            //action to select
            LOG.info("action: "+pargs.getValue("a"));
			//defined parameter list
			LOG.info("parameters: "+pargs.getValue("p"));
			
			conf.set(ArgsParser.INFILE, pargs.getValue("i"));			
			//toolMap.initialize();
			//ToolSpec tool = toolMap.get(pargs.getValue("t"));
			//if(tool != null) conf.set(ArgsParser.TOOLSTRING, tool.toString());
            conf.set(ArgsParser.TOOLSTRING, pargs.getValue("t"));
            conf.set(ArgsParser.ACTIONSTRING, pargs.getValue("a"));
	        if (pargs.hasOption("o")) conf.set(ArgsParser.OUTDIR, pargs.getValue("o"));
	        if (pargs.hasOption("p")) conf.set(ArgsParser.PARAMETERLIST, pargs.getValue("p"));

            // TODO validate input parameters (eg. look for toolspec, action, ...)
	        
            /*
			if(tool == null) {
				System.out.println("Cannot find tool: "+pargs.getValue("t"));
				System.exit(-1);
			}
             */
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
			System.out.println("usage: CLIWrapper -i inFile [-o outFile] [-p \"parameterList\"] -t cmd");
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
	
	
