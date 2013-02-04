package eu.scape_project.pt.fs.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Handles the transportation of files from the local filesystem to HDFS and vice-versa.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 * @author Martin Schenck [schenck]
 */
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
	
    @Override
	public File copyFile(String strSrc, String strDest) throws IOException {
		Path path = new Path(strSrc);
		if(!hdfs.exists(path)) throw new IOException("file does not exist! "+strSrc);
		//File temp = File.createTempFile(path.getName(), "", tempDir);		
        File temp = new File( strDest );
		hdfs.copyToLocalFile(path, new Path(strDest));
		return temp;
	}
	
	@Override
	public void depositDirectoryOrFile(String strSrc, String strDest) throws IOException {
        File file = new File( strSrc );
		if(file.isDirectory()) {
			depositDirectory(strSrc, strDest);
		} else {
			depositFile(strSrc, strDest);
		}
	}
	
	@Override
	public void depositDirectory(String strSrc, String strDest) throws IOException {
		// Get output directory name from strSrc
        File dir = new File( strSrc );
		
		if(!dir.isDirectory()) {
			LOG.error("Could not find correct local output directory: " + dir );
			return;
		}
		
		LOG.info("Local directory is: " + dir );
		
        // FIXME if strSrc is a directory then strDest should be a directory too
		for(File file : dir.listFiles()) {
			depositDirectoryOrFile(file.getCanonicalPath(), strDest + File.separator + file.getName());
		}
	}

	@Override
	public void depositFile(String strSrc, String strDest) throws IOException {
		Path src = new Path(strSrc);
		Path dest = new Path(strDest);
		
		LOG.info("local file name is: "+src+" destination path is:" +dest);
		hdfs.copyFromLocalFile(src, dest);
	}

}
