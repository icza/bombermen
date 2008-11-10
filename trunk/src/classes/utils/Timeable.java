
/*
 * Created on August 28, 2004
 */

package classes.utils;

/**
 * Defines a method which can be called to time the object if it is timeable
 * (usually calling it frequently with a frequency).
 *
 * @author Andras Belicza
 */
public interface Timeable {

    /**
     * Method to be called when the timeable object must be timed.
     * Signs that new iteration may begin now.
     */
    void signalingNextIteration();
    
}
