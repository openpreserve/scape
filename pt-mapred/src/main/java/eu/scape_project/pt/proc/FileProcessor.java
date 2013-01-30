package eu.scape_project.pt.proc;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;

public class FileProcessor extends Processor {

    private static Log LOG = LogFactory.getLog(FileProcessor.class);

    /**
     * Creates a FileProcessor that functions a reader for the given InputStream.
     * @param InputStream isIn 
     */
    public FileProcessor(InputStream isIn) {
        this.isIn = isIn;
        this.osOut = null;
    }

    /**
     * Creates a FileProcessor that functions a writer for the given OutputStream.
     * @param OutputStream osOut 
     */
    public FileProcessor(OutputStream osOut) {
        this.osOut = osOut;
        this.isIn = null;
    }

    @Override
    public int execute() throws Exception {
        if( osOut != null )  {
            IOUtils.copy(isIn, osOut );
            osOut.close();
        }
        if( this.next != null )
        {
            this.next.setInputStream(isIn);
            return this.next.execute();
        }
        return 0;
    }

    @Override
    public void initialize() {
    }

}
