package eu.scape_project.pt.fs;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.File;
import java.io.IOException;

public class HDFSFiler {
	
	protected FileSystem hdfs = null;
	
	public HDFSFiler(FileSystem hdfs) {
		this.hdfs = hdfs;
	}
	
	public boolean exists(String file) throws IOException {
		Path path = new Path(file);
		return hdfs.exists(path);
	}
	
	public File createTempFromHDFSReference(String file) throws IOException {
		Path path = new Path(file);
		if(!hdfs.exists(path)) throw new IOException("file does not exist! "+file.toString());
		File temp = File.createTempFile(path.getName(), Long.toString(System.nanoTime()));
		hdfs.copyToLocalFile(new Path(temp.toString()), path);
		return temp;
	}

}
