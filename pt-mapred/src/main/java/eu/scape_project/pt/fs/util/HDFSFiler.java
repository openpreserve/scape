package eu.scape_project.pt.fs.util;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.File;
import java.io.IOException;

public class HDFSFiler extends MapSessionFiler implements Filer{
	
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
		File temp = new File(getExecDir(), path.getName());
		hdfs.copyToLocalFile(path, new Path(temp.toString()));
		return temp;
	}

}
