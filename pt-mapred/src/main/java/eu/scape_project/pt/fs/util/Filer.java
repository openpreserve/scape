package eu.scape_project.pt.fs.util;

import java.io.File;
import java.io.IOException;

/**
 * Implementing classes of this interface handle the transportation of files and
 * directories from local to remote filesystem and vice-versa. A remote filesystem
 * may be HDFS as for example.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 */
public interface Filer {
	
    /**
     * Copies a file from a remote filesystem to the local one.
     * 
     * @param strSrc
     * @param strDest
     * @return
     * @throws IOException 
     */
	public File copyFile(String strSrc, String strDest) throws IOException;

    /**
     * Copies a file or directory from the local filesystem to a remote one.
     * 
     * @param srcSrc
     * @param strDest
     * @throws IOException 
     */
	public void depositDirectoryOrFile(String srcSrc, String strDest) throws IOException; 

    /**
     * Copies a directory from the local filesystem to a remote one.
     * 
     * @param strSrc
     * @param strDest
     * @throws IOException 
     */
	public void depositDirectory(String strSrc, String strDest) throws IOException;
	
    /**
     * Copies a file from the local filesystem to a remote one.
     * 
     * @param strSrc
     * @param strDest
     * @throws IOException 
     */
	public void depositFile(String strSrc, String strDest) throws IOException;

}
