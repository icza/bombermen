
/*
 * Created on July 8, 2004
 */

package classes;

/**
 * Defines a method to handle new messages.
 *
 * @author Andras Belicza
 */
public interface MessageHandler {

    /**
     * Handles a messages.
     * @param message message to be handled
     */
    void handleMessage( final String message );
    
}
