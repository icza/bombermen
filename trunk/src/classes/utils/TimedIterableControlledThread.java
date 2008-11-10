
/*
 * Created on August 28, 2004
 */

package classes.utils;

/**
 * This class is a controlled thread which has iterations timed by a controlled timer.
 *
 * @author Andras Belicza
 */
public class TimedIterableControlledThread extends ControlledThread implements Timeable {

    /** A controlled timer which will time the iterations of this controlled thread. */
	protected final ControlledTimer iterationTimer;
    /** Tells whether next iteration may begin. 									 */
    protected volatile boolean      nextIterationMayBegin = false;

    /**
     * Creates a new TimedIterableControlledThread.
     * @param frequency the desirabled timing frequency of the timer of this iterable controlled thread
     */
    public TimedIterableControlledThread( final int frequency ) {
        iterationTimer = new ControlledTimer( this, frequency );
    }

    /**
     * Method to be called when the timeable object must be timed.
     * Signs that new iteration may begin now.
     */
    public void signalingNextIteration() {
        nextIterationMayBegin = true;
    }
    
}
