package eu.scape_project.pt.proc;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamProcessor extends Processor {

    private static Log LOG = LogFactory.getLog(StreamProcessor.class);

    /**
     * Creates a StreamProcessor that functions a reader for the given InputStream.
     * @param InputStream iStdOut 
     */
    public StreamProcessor(InputStream iIn) {
        this.iStdOut = iIn;
        this.oStdIn = null;
    }

    /**
     * Creates a StreamProcessor that functions a writer for the given OutputStream.
     * @param OutputStream oStdIn 
     */
    public StreamProcessor(OutputStream osOut) {
        this.oStdIn = osOut;
        this.iStdOut = null;
    }

    @Override
    public int execute() throws Exception {
        debugToken = 'S';
        LOG.debug("execute");
        new Thread(this).start();
        if( this.next != null )
        {
            //this.next.setStdIn(oStdIn);
            return this.next.execute();
        }
        return this.waitFor();
    }

    @Override
    public void initialize() {
    }

    /**
     * Waits for the previous processor to terminate.
     * 
     * @return
     * @throws InterruptedException 
     */
    @Override
    public int waitFor() throws InterruptedException {
        if( this.prev == null ) return 0;
        LOG.debug("waitFor");
        return this.prev.waitFor();
    }

}
