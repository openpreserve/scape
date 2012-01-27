package eu.scape_project.pt.proc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;

import eu.scape_project.pt.fs.util.Filer;
import eu.scape_project.pt.fs.util.HDFSFiler;
import eu.scape_project.pt.fs.util.PtFileUtil;

public class FileProcessor implements PreProcessor, PostProcessor {
	
	private static Log LOG = LogFactory.getLog(FileProcessor.class);
	
	protected String[] inRefs = null;
	protected String[] outRefs = null;
	protected FileSystem hdfs = null;
	
	private File[] inputFiles = null;
	
	
	public FileProcessor(String[] inFiles) {
		this.inRefs = inFiles;
	}
	
	public FileProcessor(String[] inRefs, String[] outRefs, FileSystem hdfs) {
		this.inRefs = inRefs;
		this.outRefs = outRefs;
		this.hdfs = hdfs;
	}
	
	public void setInRefs(String[] inRefs) {
		this.inRefs = inRefs;
	}
	
	public void setOutRefs(String[] outRefs) {
		this.outRefs = outRefs;
	}

	public void setHadoopFS(FileSystem hdfs) {
		this.hdfs = hdfs;
	}
	
	@Override
	public void resolvePrecondition() throws IOException, URISyntaxException {
		// Any inRefs?
		if(inRefs == null) {
			LOG.info("no preprocessing required");
			return;
		}
		
		ArrayList<File> files = new ArrayList<File>();
		for(String file : inRefs) {			
			LOG.info("trying to retrieve file: "+file);
			Filer filer = getFiler(file);
			if(filer == null) continue;
	    	File inFile = filer.createTempFileFromReference(file);
	    	files.add(inFile);
	    	LOG.info("retrieving file: "+inFile.getName());
		}	
		this.inputFiles = files.toArray(new File[0]);
	}
	
	public File[] getInputFiles() {
		return inputFiles;
	}
	
	public void resolvePostcondition() throws IOException, URISyntaxException {
		// Any outRefs?
		if(outRefs == null) {
			LOG.info("no postprocessing required");
			return;
		}
		
		for(String file : outRefs) {			
			LOG.info("trying to deposit file: "+file);			
			Filer filer = getFiler(file);
			if(filer == null) continue;
			filer.depositTempDirectoryOrFile(file);
		}
	}
		
	private Filer getFiler(String file) throws URISyntaxException {
		if(PtFileUtil.isHdfsUri(file)) {
			if(hdfs == null) {
				LOG.error("Cannot create HDFSFiler. Hadoop FileSystem not set!");
				return null;
			}
			//TODO don't do this for each file
			return new HDFSFiler(hdfs);
		} else if(PtFileUtil.isFileUri(file)) {
			LOG.error("Cannot create FileFiler. Not implemented!");
				return null;
		} else {
			LOG.error("Unknown Schema in file: "+file);
			return null;
		}
	}
	
}
