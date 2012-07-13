package eu.scape_project.pt.pit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.scape_project.pt.pit.tools.ToolSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Manages toolspecs for a given physical location, eg. a directory.
 * @author ait
 */
public class ToolSpecRepository {

	private static Log LOG = LogFactory.getLog(ToolSpecRepository.class);
    private final Path repo_dir;
    private final FileSystem fs;

    public ToolSpecRepository( FileSystem fs, Path directory ) throws FileNotFoundException, IOException {
        if( !fs.exists(directory) )
            throw new FileNotFoundException();

        if( !fs.getFileStatus(directory).isDir() )
            throw new IOException( directory.toString() + "is not a directory");

        this.fs = fs;
        this.repo_dir = directory;
    }

    public boolean toolspecExists( String strToolSpec ) {
        Path file = new Path( 
                repo_dir.toString() + System.getProperty("file.separator") 
                + getToolSpecName( strToolSpec ) );
        try {
            return fs.exists(file);
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return false;
    }

    public ToolSpec getToolSpec( String strToolSpec ) throws FileNotFoundException {
        Path file = new Path( 
                repo_dir.toString() + System.getProperty("file.separator") 
                + getToolSpecName( strToolSpec ) );

        FSDataInputStream fis = null;
        try {
            fis = fs.open( file );
        } catch (IOException ex) {
            LOG.error(ex);
            return null;
        }

        try {
            return ToolSpec.fromInputStream( fis );
        } catch (JAXBException ex) {
            LOG.error(ex);
        }
        return null;
    }

    public String[] getToolSpecList() {
        FileStatus[] list = new FileStatus[0];
        try {
            list = fs.listStatus(repo_dir);
        } catch (IOException ex) {
            LOG.error(ex);
        }
        String strList[] = new String[list.length];
        for( int f = 0; f < list.length; f++ )
            strList[f] = list[f].getPath().getName();
        return strList;
    }


    private String getToolSpecName( String strToolSpec ) {
        return strToolSpec + ".ptspec.xml";
    }

}
