package eu.scape_project.pt.fs.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSFiler implements Filer{
	
	private static Log LOG = LogFactory.getLog(HDFSFiler.class);
	
	protected FileSystem hdfs = null;
	
	public HDFSFiler(FileSystem hdfs) {
		super();
		this.hdfs = hdfs;
	}
	
	public boolean exists(String file) throws IOException {
		Path path = new Path(file);
		return hdfs.exists(path);
	}
	
	public File createTempFileFromReference(String hdfsRef) throws IOException {
		Path path = new Path(hdfsRef);
		if(!hdfs.exists(path)) throw new IOException("file does not exist! "+hdfsRef.toString());
		//File temp = File.createTempFile(path.getName(), "", tempDir);		
		File temp = new File(PtFileUtil.getExecDir(), path.getName());
		hdfs.copyToLocalFile(path, new Path(temp.toString()));
		return temp;
	}
	
	@Override
	public void depositTempDirectoryOrFile(String hdfsRef) throws IOException {
		// TODO have better means to identify directory
		if(hdfsRef.endsWith("/")) {
			depositTempDirectory(hdfsRef);
		} else {
			depositTempFile(hdfsRef);
		}
	}
	
	@Override
	public void depositTempDirectory(String hdfsRef) throws IOException {
		// Get output directory name from hdfsRef
		String[] splits = hdfsRef.split("/");
		
		// Execution directory plus the new folder
		File directory = new File(PtFileUtil.getExecDir().toString() + "/" + splits[splits.length - 1]);
		
		if(!directory.isDirectory()) {
			LOG.error("Could not find correct local output directory: " + directory);
			return;
		}
		
		LOG.info("Local directory is: " + directory);
		
		for(File file : directory.listFiles()) {
			depositTempFile(hdfsRef + file.getName(), directory);
		}
	}

	private void depositTempFile(String hdfsRef, File localDirectory) throws IOException {
		Path dest = new Path(hdfsRef);
		Path src = new Path(localDirectory.getAbsolutePath(), dest.getName());
		
		LOG.info("local file name is: "+src+" destination path is:" +dest);
		hdfs.copyFromLocalFile(src, dest);
	}

	@Override
	public void depositTempFile(String hdfsRef) throws IOException {
		Path dest = new Path(hdfsRef);
		Path src = new Path( new Path(PtFileUtil.getExecDir().toString()), new Path(dest.getName()) );
		
		LOG.info("local file name is: "+src+" destination path is:" +dest);
		hdfs.copyFromLocalFile(src, dest);
	}

}
