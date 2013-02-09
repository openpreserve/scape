package eu.scape_project.pt.proc;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamProcessor extends Processor {

    private static Log LOG = LogFactory.getLog(StreamProcessor.class);
    private Thread t;

    /**
     * Creates a StreamProcessor that functions as a reader for the 
     * given InputStream.
     * 
     * @param InputStream iStdOut 
     */
    public StreamProcessor(InputStream iIn) {
        this.iStdOut = iIn;
        this.oStdIn = null;
    }

    /**
     * Creates a StreamProcessor that functions as a writer for the 
     * given OutputStream.
     * 
     * @param OutputStream oStdIn 
     */
    public StreamProcessor(OutputStream osOut) {
        this.oStdIn = osOut;
        this.iStdOut = null;
    }

    /**
     * Reads a previous processor's stream and writes to it's own or
     * simply executes the next processor. 
     * @return
     * @throws Exception 
     */
    @Override
    public int execute() throws Exception {
        debugToken = 'S';
        LOG.debug("execute");
        t = new Thread(this);
        t.start();
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
        int r = this.prev.waitFor();
        t.join();
        return r;
    }

}
