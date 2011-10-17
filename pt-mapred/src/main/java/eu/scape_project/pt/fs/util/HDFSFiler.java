package eu.scape_project.pt.fs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import eu.scape_project.pt.proc.TaskProcessor;

import java.io.File;
import java.io.IOException;

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
	
	public void depositeTempFile(String hdfsRef) throws IOException {
		Path dest = new Path(hdfsRef);
		Path src = new Path( new Path(PtFileUtil.getExecDir().toString()), new Path(dest.getName()) );
		LOG.info("local file name is: "+src+" destination path is:" +dest);
		hdfs.copyFromLocalFile(src, dest);
	}

}
