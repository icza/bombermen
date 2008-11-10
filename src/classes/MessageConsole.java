
/*
 * Created on July 8, 2004
 */

package classes;

/**
 * Defines methods must be known by a message console.
 *
 * @author Andras Belicza
 */
interface MessageConsole {

    /**
     * Receives a message, appends it to the messages.
     * @param message message to be appended
     */
    void receiveMessage( final String message );

    /**
     * Clears all messages from the console.
     */
    void clearMessages();
    
}
