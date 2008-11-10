
/*
 * Created on August 3, 2004
 */

package classes.utils;

/**
 * Controlled thread which provides more service-thread services/methods and 
 * assumes/requires that the run() method is based on a sign (attribute)
 * telling whether cancel has been requested.<br>
 * The run method of the inharitar classes must be constructed to
 * listen to the requestedToCancel attribute, and when it's true, must terminate
 * running as soon as possible.
 *
 * @author Andras Belicza
 */
public abstract class ControlledThread extends Thread {
    
    /** Tells whether cancel of running has been requested (whether we have to terminate running). */
    protected volatile boolean requestedToCancel = false;

    /**
     * Closes the controlled thread, frees/disposes consumed resources.
     * Invoked at the end of shutdown.
     * Must not (not needed to) invoke at the end of the run() method,
     * because the death of the thread can be later than the end of run() method.<br>
     * This is an empty implementation.
     */
    protected void close() {
    }
    
    /**
     * Requests to cancel by setting the requestedToCancel attribute.
     */
    protected void requestToCancel() {
        requestedToCancel = true;
    }

    /**
     * Shuts down correctly this controlled thread.<br>
     * This means to sign the request of cancel to the thread (to the run method)
     * and wait until it terminates the running (until the thread dies).
     */
    public void shutDown() {
        requestToCancel();
		
        try {
            join();
        }
        catch ( final InterruptedException ie ) {
        }
		
        close();
    }

    /**
     * Finalizes the object before the garbage collector would destroy it.
     * Ends the thread (or to be precise: requests it) by calling requestToCancel() (if it hasn't happened before).
     * Else the thread would not be interrupted by destroying this object.
     * The method closed() will not be invoked, because we do not shut down the controlled thead,
     * we just ends the thread.
     */
    public void finalize() {
        if ( !requestedToCancel )
            requestToCancel();
    }

}
