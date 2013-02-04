package eu.scape_project.pt.mapred;

import eu.scape_project.pt.pit.invoke.ToolSpecNotFoundException;
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
import eu.scape_project.pt.executors.TavernaCLExecutor;
import eu.scape_project.pt.executors.ToolspecExecutor;
import eu.scape_project.pt.pit.ToolRepository;
import eu.scape_project.pt.pit.ToolSpecRepository;
import eu.scape_project.pt.pit.Repository;
import eu.scape_project.pt.util.ArgsParser;
import java.io.File;
import org.apache.hadoop.fs.FileSystem;

/**
 * A command-line interaction wrapper to execute cmd-line tools with MapReduce.
 * Code based on SimpleWrapper.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 * @author Martin Schenck [schenck]
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
         * For per Job there can only be one Tool and one Action selected, this stuff is the processor and the input parameters parser.
         * @param context
         */
        @Override
		public void setup( Context context ) {
        	if(context.getConfiguration().get(ArgsParser.WORKFLOW_LOCATION) != null && context.getConfiguration().get(ArgsParser.WORKFLOW_LOCATION) != "") {
        		executor = new TavernaCLExecutor(context.getConfiguration().get(ArgsParser.TAVERNA_HOME),
        				context.getConfiguration().get(ArgsParser.WORKFLOW_LOCATION),
        				context.getConfiguration().get(ArgsParser.OUTDIR));
        	} else {
        		executor = new ToolspecExecutor(
                        context.getConfiguration().get(ArgsParser.TOOLSTRING), 
                        context.getConfiguration().get(ArgsParser.ACTIONSTRING),
                        context.getConfiguration().get(ArgsParser.REPO_LOCATION));
        	}
	    	executor.setup();
		}

        @Override
		public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	executor.map(key, value, context );
	    }	  
	}

    public static class CLIReducer extends 
            Reducer<Text, IntWritable, Text, IntWritable> {
		
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
        // TODO Output Value Class may depend on the tool invoked
        job.setOutputValueClass(Text.class);

        job.setMapperClass(CLIMapper.class);


        //job.setReducerClass(MyReducer.class);

        job.setInputFormatClass(PtInputFormat.class);
        
        //if(conf.get(ArgsParser.NUM_LINES_PER_SPLIT) != null) 
        	//NLineInputFormat.setNumLinesPerSplit(job, Integer.parseInt(conf.get(ArgsParser.NUM_LINES_PER_SPLIT)));
        //job.setInputFormatClass(NLineInputFormat.class);
        
        //job.setOutputFormatClass(FileOutputFormat.class);
		//job.setOutputFormatClass(MultipleOutputFormat.class);
		
		//FileInputFormat.addInputPath(job, new Path(args[0])); ArgsParser.INFILE
		//FileOutputFormat.setOutputPath(job, new Path(args[1])); ArgsParser.OUTDIR
		FileInputFormat.addInputPath(job, new Path(conf.get(ArgsParser.INFILE)));
		FileOutputFormat.setOutputPath(job, new Path(conf.get(ArgsParser.OUTDIR)) ); 
				
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
        Repository repo;
        		
		try {
			ArgsParser pargs = new ArgsParser("i:o:t:a:p:x:v:w:r:j:n:", args);

            String outDir = 
                (conf.get(ArgsParser.OUTDIR) == null) ? 
                    "out/"+System.nanoTime()%10000 : 
                    conf.get(ArgsParser.OUTDIR); 

            conf.set(ArgsParser.OUTDIR, outDir);
			
			conf.set(ArgsParser.INFILE, pargs.getValue("i"));			
			//toolMap.initialize();
			//ToolSpec tool = toolMap.get(pargs.getValue("t"));
			//if(tool != null) conf.set(ArgsParser.TOOLSTRING, tool.toString());
			if (pargs.hasOption("t")) conf.set(ArgsParser.TOOLSTRING, pargs.getValue("t"));
			if (pargs.hasOption("a")) conf.set(ArgsParser.ACTIONSTRING, pargs.getValue("a"));
	        if (pargs.hasOption("o")) conf.set(ArgsParser.OUTDIR, pargs.getValue("o"));
	        if (pargs.hasOption("p")) conf.set(ArgsParser.PARAMETERLIST, pargs.getValue("p"));
	        if (pargs.hasOption("r")) conf.set(ArgsParser.REPO_LOCATION, pargs.getValue("r"));
	        
	        //parameters for job fine tuning
	        if (pargs.hasOption("j")) conf.setInt("mapred.job.reuse.jvm.num.tasks", Integer.parseInt(pargs.getValue("j")));
	        if (pargs.hasOption("n")) conf.set(ArgsParser.NUM_LINES_PER_SPLIT, pargs.getValue("n"));
	        else conf.set(ArgsParser.NUM_LINES_PER_SPLIT, "10");
	        
			//input file
			LOG.info("Input: " + conf.get(ArgsParser.INFILE));
			//hadoop's output 
			LOG.info("Output: " + conf.get(ArgsParser.OUTDIR));
			//tool to select
			LOG.info("ToolSpec: " + conf.get(ArgsParser.TOOLSTRING));
            //action to select
            LOG.info("Action: " + conf.get(ArgsParser.ACTIONSTRING));
			//defined parameter list
			LOG.info("Parameters: " + conf.get(ArgsParser.PARAMETERLIST));
			//toolspec directory
            LOG.info("Toolspec Directory: " + conf.get(ArgsParser.REPO_LOCATION));
            //jvm reuse
            LOG.info("JVM reuse: " + conf.get("mapred.job.reuse.jvm.num.tasks"));
            //NInputFormat
            LOG.info("Number of Lines: " + conf.get(ArgsParser.NUM_LINES_PER_SPLIT));


            // taverna set?
			//LOG.info("taverna: " + pargs.getValue("v"));
			//LOG.info("workflow: " + pargs.getValue("w"));
	        
	        // Get Taverna home directory and workflow location
	        //if (pargs.hasOption("w")) conf.set(ArgsParser.WORKFLOW_LOCATION, pargs.getValue("w"));
	        //if (pargs.hasOption("v")) conf.set(ArgsParser.TAVERNA_HOME, pargs.getValue("v"));

            // TODO validate input parameters (eg. look for toolspec, action, ...)
            Path fRepo = new Path( conf.get(ArgsParser.REPO_LOCATION) );
            if( conf.get(ArgsParser.TOOLSTRING).startsWith("digital-preservation"))
                repo = new ToolRepository(FileSystem.get( conf ),fRepo );
            else
                repo = new ToolSpecRepository(FileSystem.get(conf), fRepo );

            String[] astrToolspecs = repo.getToolList();
            LOG.info( "Available ToolSpecs: ");
            for( String strToolspec: astrToolspecs ) 
                LOG.info( strToolspec );
            
            if( !repo.toolspecExists( conf.get(ArgsParser.TOOLSTRING)))
                throw new ToolSpecNotFoundException( "Toolspec " + conf.get(ArgsParser.TOOLSTRING) + " not found" );

	        
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
			System.out.println("usage: CLIWrapper -i inFile [-o outFile] [-p \"parameterList\"] [-r toolspec repository on hdfs] -t toolspec -a action");
			//System.out.println("   or: CLIWrapper -i inFile -o outFile [-v tavernaDir] -w workflow");
			LOG.info(e);
			e.printStackTrace();
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
