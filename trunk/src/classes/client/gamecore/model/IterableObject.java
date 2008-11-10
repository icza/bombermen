
/*
 * Created on November 28, 2005
 */

package classes.client.gamecore.model;

/**
 * This class represents an iterable object. An iterable object can be stepped to the next iteration,
 * and we can access and modify its iteration counter.
 * 
 * @author Andras Belicza
 */
public class IterableObject {

	/** The number of iterations of this iterable object. */
	private int iterationCounter;
	
	/**
	 * Returns the iteration counter.
	 * @return the iteration counter
	 */
	public int getIterationCounter() {
		return iterationCounter;
	}
	
	/**
	 * Sets the iteration counter.
	 * @param iterationCounter iteration counter to be set
	 */
	public void setIterationCounter( final int iterationCounter ) {
		this.iterationCounter = iterationCounter;
	}
	
	/**
	 * Performs operations which are requried by passing the time.
	 * Steps this object into the next iteration.
	 */
	public void nextIteration() {
		iterationCounter++;
	}
	
}
