package eu.scape_project.pt.mapred;

import eu.scape_project.pt.executors.Executor;
import eu.scape_project.pt.executors.TavernaCLExecutor;
import eu.scape_project.pt.executors.ToolspecExecutor;
import eu.scape_project.pt.repo.Repository;
import eu.scape_project.pt.repo.ToolRepository;
import eu.scape_project.pt.invoke.ToolSpecNotFoundException;
import eu.scape_project.pt.util.PropertyNames;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
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

		/** 
         * Executes the setup and map jobs.
         */
		Executor executor;
		
        /**
         * Sets up the appropriate executor for the job. 
         * 
         * @param context
         */
        @Override
		public void setup( Context context ) {
            Configuration conf = context.getConfiguration();
        	if(conf.get(PropertyNames.TAVERNA_WORKFLOW) != null 
               && conf.get(PropertyNames.TAVERNA_WORKFLOW) != "") {
        		executor = new TavernaCLExecutor(
                        conf.get(PropertyNames.TAVERNA_HOME),
        				conf.get(PropertyNames.TAVERNA_WORKFLOW),
        				conf.get(PropertyNames.OUTDIR));
        	} else {
        		executor = new ToolspecExecutor(
                        conf.get(PropertyNames.TOOLSTRING), 
                        conf.get(PropertyNames.ACTIONSTRING),
                        conf.get(PropertyNames.REPO_LOCATION));
        	}
	    	executor.setup();
		}

        @Override
		public void map(Object key, Text value, Context context) 
                throws IOException, InterruptedException {
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
        
        if(conf.get(PropertyNames.NUM_LINES_PER_SPLIT) != null) {
        	NLineInputFormat.setNumLinesPerSplit(
                job, 
                Integer.parseInt(conf.get(PropertyNames.NUM_LINES_PER_SPLIT)));
            job.setInputFormatClass(NLineInputFormat.class);
        } else
            job.setInputFormatClass(PtInputFormat.class);
        
		FileInputFormat.addInputPath(job, new Path(conf.get(PropertyNames.INFILE)));
		FileOutputFormat.setOutputPath(job, new Path(conf.get(PropertyNames.OUTDIR)) ); 
				
		job.waitForCompletion(true);
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		
		int res = 1;
		CLIWrapper mr = new CLIWrapper();
        Configuration conf = new Configuration();
        Repository repo;

        Map<String, String> parameters = new HashMap<String, String>() {{
            put("i", PropertyNames.INFILE );
            put("o", PropertyNames.OUTDIR );
            put("j", "mapred.job.reuse.jvm.num.tasks" );
            put("n", PropertyNames.NUM_LINES_PER_SPLIT );
            put("t", PropertyNames.TOOLSTRING );
            put("a", PropertyNames.ACTIONSTRING );
            put("r", PropertyNames.REPO_LOCATION);
            put("v", PropertyNames.TAVERNA_HOME );
            put("w", PropertyNames.TAVERNA_WORKFLOW );
        }};
        		
		try {
            String pStrings = "";
            for( String i : parameters.values() )
                pStrings += i + ":";

			OptionParser parser = new OptionParser(pStrings);
            OptionSet options = parser.parse(args);

            // default values:
            conf.set(PropertyNames.NUM_LINES_PER_SPLIT, "10");
            conf.set(PropertyNames.OUTDIR, "out/"+System.nanoTime()%10000 );

            // store parameter values:
            for( Entry<String, String> param : parameters.entrySet() )
                if(options.hasArgument(param.getKey()))
                    conf.set(param.getValue(), 
                             options.valueOf(param.getKey()).toString());

			//input file
			LOG.info("Input: " + conf.get(PropertyNames.INFILE));
			//hadoop's output 
			LOG.info("Output: " + conf.get(PropertyNames.OUTDIR));
			//tool to select
			LOG.info("ToolSpec: " + conf.get(PropertyNames.TOOLSTRING));
            //action to select
            LOG.info("Action: " + conf.get(PropertyNames.ACTIONSTRING));
			//toolspec directory
            LOG.info("Toolspec Directory: " 
                    + conf.get(PropertyNames.REPO_LOCATION));
            //jvm reuse
            LOG.info("JVM reuse: " 
                    + conf.get("mapred.job.reuse.jvm.num.tasks"));
            //NInputFormat
            LOG.info("Number of Lines: " 
                    + conf.get(PropertyNames.NUM_LINES_PER_SPLIT));
            // taverna workflow location
			LOG.info("Taverna: " + conf.get(PropertyNames.TAVERNA_HOME));
            // taverna home
			LOG.info("Workflow: " + conf.get(PropertyNames.TAVERNA_WORKFLOW));

            // check if enough parameters:

		    if ( conf.get(PropertyNames.INFILE) == null )
                throw new Exception("Input file needed");

            if( conf.get(PropertyNames.TOOLSTRING) == null ||
                conf.get(PropertyNames.ACTIONSTRING) == null ) 
                throw new Exception("Toolspec and Action needed");


            // check if toolspec exists:

            Path fRepo = new Path( conf.get(PropertyNames.REPO_LOCATION) );
            repo = new ToolRepository(FileSystem.get( conf ),fRepo );

            String[] astrToolspecs = repo.getToolList();
            LOG.info( "Available ToolSpecs: ");
            for( String strToolspec: astrToolspecs ) 
                LOG.info( strToolspec );
            
            // TODO also check if action exists
            if( !repo.toolspecExists( conf.get(PropertyNames.TOOLSTRING)))
                throw new ToolSpecNotFoundException( 
                    "Toolspec " + conf.get(PropertyNames.TOOLSTRING) + " not found" );

		} catch (Exception e) {
            printUsage();
			LOG.error(e);
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

    public static void printUsage() {
        System.out.println("usage: CLIWrapper -i inFile [-o outFile] [-j mapred.job.reuse.jvm.num.tasks] [-n num lines of inFile per task]");
        System.out.println("    execution of ToolSpec: -t toolspec -a action [-r toolspec repository on hdfs]");
        System.out.println("    execution of Taverna workflow: -w workflow [-v tavernaDir]");
    }
		
}
