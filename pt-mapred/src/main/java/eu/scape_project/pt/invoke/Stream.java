package eu.scape_project.pt.invoke;

import java.io.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps data object, an inputstream, an outputstream and/or a file.
 * TODO rename to DataObject 
 *
 * @author Matthias Rella [myrho]
 */
public class Stream {

    private static Log LOG = LogFactory.getLog(Stream.class);
    InputStream in = null;
    OutputStream out = null;
    File file = null;

    public Stream(InputStream in) {
        this.in = in;
    }

    public Stream(OutputStream out) {
        this.out = out;
    }

    public Stream(File file) {
        this.file = file;
    }

    public InputStream getInputStream() {
        if (in != null) {
            return in;
        }
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                LOG.error(ex);
            }
        }
        return null;
    }

    public OutputStream getOutputStream() {
        if (out != null) {
            return out;
        }
        if (file != null) {
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                LOG.error(ex);
            }
        }
        return null;
    }

    public File getFile() {
        if (file != null) {
            return file;
        }
        // In other cases, take the input stream and push it into a temp file.
        // And delete the temp file on finalize (?)
        if (in != null ) {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("tmp", "stream");
                
                FileOutputStream ostream = new FileOutputStream(tmpFile);
                IOUtils.copy(in, ostream);
                ostream.close();
                return tmpFile;
            } catch (IOException ex) {
                LOG.error(ex);
            }                       
            
            
        }
        if( out != null ) {
            
        }
        return null;
    }
}
