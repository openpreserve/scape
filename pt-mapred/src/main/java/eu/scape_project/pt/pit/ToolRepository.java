package eu.scape_project.pt.pit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.scape_project.pt.tool.Tool;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Manages toolspecs for a given physical location, eg. a directory.
 * @author Matthias Rella [my_rho]
 */
public class ToolRepository {

	private static Log LOG = LogFactory.getLog(ToolRepository.class);
    private final Path repo_dir;
    private final FileSystem fs;

	private static JAXBContext jc;
	
	static {
		try {
			jc  = JAXBContext.newInstance(Tool.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    // TODO constructor should not rely on these internal parameters
    // a parametrized factoryMethod would do better
    public ToolRepository( FileSystem fs, Path directory ) throws FileNotFoundException, IOException {
        if( !fs.exists(directory) )
            throw new FileNotFoundException();

        if( !fs.getFileStatus(directory).isDir() )
            throw new IOException( directory.toString() + "is not a directory");

        this.fs = fs;
        this.repo_dir = directory;
    }

    public boolean toolspecExists( String strTool ) {
        try {
            Path file = new Path( 
                repo_dir.toString() + System.getProperty("file.separator") 
                + getToolName( strTool ) );
            return fs.exists(file);
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return false;
    }

    public Tool getTool( String strTool ) throws FileNotFoundException {
        Path file = new Path( 
                repo_dir.toString() + System.getProperty("file.separator") 
                + getToolName( strTool ) );

        FSDataInputStream fis = null;
        try {
            fis = fs.open( file );
        } catch (IOException ex) {
            LOG.error(ex);
            return null;
        }

        try {
            return fromInputStream( fis );
        } catch (JAXBException ex) {
            LOG.error(ex);
        }
        return null;
    }

    public String[] getToolList() {
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


    private String getToolName( String strTool ) {
        return strTool + ".xml";
    }

    private Tool fromInputStream(InputStream input) throws JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (Tool) u.unmarshal(new StreamSource(input));
    }

	private String toXMlFormatted() throws JAXBException, UnsupportedEncodingException {
		//Create marshaller
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m.marshal(this, bos);
		return bos.toString("UTF-8");
	}
	

}
