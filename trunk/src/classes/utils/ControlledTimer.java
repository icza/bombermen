
/*
 * Created on August 28, 2004
 */

package classes.utils;

/**
 * Controlled timer.<br>
 * This class provides a confirmable timing service. The timed object must implement the Timeable interface.
 * The timing is based on the timing frequency. If next iteration is about to come and confirmation has been
 * arrived that the timeable object is ready for the next iteration, signalingNextIteration() will be called.<br>
 * So by default, we call signalingNextIteration() method in every T=1/f seconds (if the timeable object is ready for it).
 * If the timeable object is not ready, we wait until it'll be ready, and the next signalingNextIteration() call
 * will be not sooner then T after the last call of signalingNextIteration().
 * When all is said and done: calls of signalingNextIteration() will be not more frequently than f, but as frequently as possible,
 * as frequently as the timeable object is ready for it.
 *
 * @author Andras Belicza
 */
public class ControlledTimer extends ControlledThread {

    /** Object to be timed.                                                */
    private final Timeable   timeable;
    /** Period time of timing in ms.                                       */
    private volatile long    periodTime;
    /** Tells whether the timeable object is ready for the next iteration. */
    private volatile boolean readyForNextIteration = false;

    /**
     * Creates a new ControlledTimer.
     * @param timeable  object to be timed
     * @param frequency the desirabled timing frequency
     */
    public ControlledTimer( final Timeable timeable, final int frequency ) {
        this.timeable = timeable;
        setFrequency( frequency );
    }

    /**
     * The run method of the timer: this will time the timeable object.
     */
    public void run() {
        try {
            while ( !requestedToCancel ) {
                final long timeToWaitBeforeNextCheck = periodTime / 25l + 1l;
                while ( !requestedToCancel && !readyForNextIteration )
                    sleep( timeToWaitBeforeNextCheck );
                sleep( periodTime );
                if ( !requestedToCancel ) {
                    readyForNextIteration = false;
                    timeable.signalingNextIteration();
                }
            }
        }
        catch ( final InterruptedException ie ) {
        }
    }

    /**
     * Sets the desireabled timing frequency.
     * @param frequency the desirabled timing frequency
     */
    public void setFrequency( final int frequency ) {
        periodTime = 1000 / frequency;
    }
    
    /**
     * Sets that timeable object is ready for next iteration.
     */
    public void setReadyForNextIteration() {
        readyForNextIteration = true;
    }

}
