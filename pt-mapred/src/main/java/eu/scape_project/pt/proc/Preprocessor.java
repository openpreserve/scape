package eu.scape_project.pt.proc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;

import eu.scape_project.pt.fs.util.Filer;
import eu.scape_project.pt.fs.util.HDFSFiler;
import eu.scape_project.pt.fs.util.PtFileUtil;
import eu.scape_project.pt.mapred.SimpleWrapper;

public class Preprocessor {
	
	private static Log LOG = LogFactory.getLog(Preprocessor.class);
	
	protected String[] inFiles = null;
	protected FileSystem hdfs = null;
	
	public Preprocessor(String[] inFiles) {
		this.inFiles = inFiles;
	}
	
	public Preprocessor(String[] inFiles, FileSystem hdfs) {
		this.inFiles = inFiles;
		this.hdfs = hdfs;
	}
	
	public void setHadoopFS(FileSystem hdfs) {
		this.hdfs = hdfs;
	}
	
	public int retrieveFiles() throws IOException, URISyntaxException {
		int i=0;
		for(String file : inFiles) {			
			LOG.info("trying to retrieve file: "+file);
			Filer filer = getFiler(file);
			if(filer == null) continue;
	    	File inFile = filer.createTempFileFromReference(file);
	    	i++;
	    	System.out.println("tempFile: "+inFile.getCanonicalPath()+" with name: "+inFile.getName());
		}	
		return i;
	}
	
	private Filer getFiler(String file) throws URISyntaxException {
		if(PtFileUtil.isHdfsUri(file)) {
			if(hdfs == null) {
				LOG.error("Cannot create HDFSFiler. Hadoop FileSystem not set!");
				return null;
			}
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
