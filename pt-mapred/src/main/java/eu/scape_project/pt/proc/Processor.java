package eu.scape_project.pt.proc;

import eu.scape_project.pt.pit.invoke.Stream;
import eu.scape_project.pt.util.ParamSpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public abstract class Processor {

    /**
     * Inputstream to read from. 
     */
    protected InputStream isIn;

    /**
     * Outputstream to write to. 
     */
    protected OutputStream osOut;

    /**
     * Processor to execute.
     */
    protected Processor next;

    /**
     * Processor to read output from.
     */
    protected Processor prev;

    /**
     * Executes its process and provides the InputStream for the next processor.
     * @return exit code of process (0 for success)
     * @throws Exception 
     */
	abstract public int execute() throws Exception;
	
	abstract public void initialize();

    /**
     * Gets InputStream of processor
     * @return InputStream isIn
     */
    public InputStream getInputStream( ) {
        return this.isIn;
    }

    /**
     * Gets OutputStream of processor
     * @return OutputStream osOut
     */
    public OutputStream getOutputStream( ) {
        return this.osOut;
    }

    /**
     * Sets InputStream of processor
     * @param InputStream in 
     */
    public void setInputStream(InputStream in) {
        this.isIn = in;
    }

    /**
     * Sets OutputStream of processor
     * @param outputStream in 
     */
    public void setOutputStream(OutputStream out) {
        this.osOut = out;
    }

    /**
     * Get next processor
     * @return 
     */
    public Processor next() {
        return next;
    }
    
    /**
     * Get previous processor
     * @return 
     */
    public Processor prev() {
        return prev;
    }

    /**
     * Double-link this processor to given next processor
     * @param next 
     */
    public void next(Processor next) {
        if( this.next == next ) return;
        this.next = next;
        next.prev(this);
    }

    /**
     * Double-link this processor to given previous processor
     * @param prev 
     */
    private void prev(Processor prev) {
        if( this.prev == prev ) return;
        this.prev = prev;
        prev.next(this);
    }

}
