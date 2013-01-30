package eu.scape_project.pt.util.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementing classes of this interface handle the transportation of files and
 * directories from local to remote filesystem and vice-versa. A remote filesystem
 * may be HDFS as for example.
 * 
 * @author Rainer Schmidt [rschmidt13]
 * @author Matthias Rella [myrho]
 */
public abstract class Filer {

    /**
     * Abstract factory method to create appropriate file for given uri
     * @param value
     * @return
     * @throws IOException 
     */
    public static Filer create(String strUri) throws IOException{
        if( strUri.startsWith("hdfs")) {
            return new HDFSFiler(strUri.toString());
        }
        throw new IOException("no appropriate filer for URI " + strUri + " found");
    };
	
    /**
     * Copies a file from a remote filesystem to the local one.
     * 
     * @param strSrc
     * @param strDest
     * @return
     * @throws IOException 
     */
	abstract public File copyFile(String strSrc, String strDest) throws IOException;

    /**
     * Copies a file or directory from the local filesystem to a remote one.
     * 
     * @param srcSrc
     * @param strDest
     * @throws IOException 
     */
	abstract public void depositDirectoryOrFile(String srcSrc, String strDest) throws IOException; 

    /**
     * Copies a directory from the local filesystem to a remote one.
     * 
     * @param strSrc
     * @param strDest
     * @throws IOException 
     */
	abstract public void depositDirectory(String strSrc, String strDest) throws IOException;
	
    /**
     * Copies a file from the local filesystem to a remote one.
     * 
     * @param strSrc
     * @param strDest
     * @throws IOException 
     */
	abstract public void depositFile(String strSrc, String strDest) throws IOException;

    /**
     * Copies file to local filesystem.
     */
    abstract public void localize() throws IOException;

    /**
     * Copies file from local filesystem to remote one.
     */
    abstract public void delocalize() throws IOException;

    /**
     * Gets the local file reference of the filer's file.
     * @return String fileRef
     */
    abstract public String getFileRef();

    abstract public InputStream getInputStream() throws IOException;

    abstract public OutputStream getOutputStream() throws IOException;


}
