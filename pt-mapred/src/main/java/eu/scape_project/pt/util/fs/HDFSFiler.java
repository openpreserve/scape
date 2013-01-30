package eu.scape_project.pt.util.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Handles the transportation of files from the local filesystem to HDFS and vice-versa.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 * @author Martin Schenck [schenck]
 */
public class HDFSFiler extends Filer{
	
	private static Log LOG = LogFactory.getLog(HDFSFiler.class);
	
    /**
     * Hadoop Filesystem handle
     */
	protected FileSystem hdfs = null;

    /**
     * File to handle by this filer
     */
    private final Path file;
	
    HDFSFiler(String value) throws IOException {
        this.file = new Path(value);
        hdfs = file.getFileSystem(new Configuration());
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
			throw new IOException("Could not find correct local output directory: " + dir );
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

    @Override
    public void localize() throws IOException {
        Path localfile = new Path( getFileRef() );
        hdfs.copyToLocalFile(file, localfile);
    }

    @Override
    public void delocalize() throws IOException {
        Path localfile = new Path( getFileRef() );
        hdfs.copyFromLocalFile(localfile, file);
    }

    @Override
    public String getFileRef() {
        // TODO introduce a namespace for temp files so that
        // other running tasks on the machine don't interfere
        return System.getProperty("java.io.tmpdir") 
                + System.getProperty("file.separator")
                    + "hdfsfiler_" + file.hashCode() + "-" + file.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return hdfs.open(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return hdfs.create(file);
    }

}
