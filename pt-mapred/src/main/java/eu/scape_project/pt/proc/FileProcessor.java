package eu.scape_project.pt.proc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;

import eu.scape_project.pt.fs.util.Filer;
import eu.scape_project.pt.fs.util.HDFSFiler;
import eu.scape_project.pt.fs.util.PtFileUtil;
import eu.scape_project.pt.pit.invoke.Stream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;

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
