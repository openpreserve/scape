/**
 * 
 */
package eu.scape_project.pc.qa.bitwiser;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 */
public class XORShiftRandom extends Random {
	/** */
	private static final long serialVersionUID = -96188754726659250L;

	private AtomicLong seed = new AtomicLong(System.nanoTime());

	public XORShiftRandom() {
	}
	public XORShiftRandom( long seed ) {
		this.seed.set( seed );
	}

	protected synchronized int next(int nbits) {
		// N.B. Not thread-safe?
		long x = this.seed.get();
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		this.seed.set(x);
		x &= ((1L << nbits) -1);
		return (int) x;
	}
}
