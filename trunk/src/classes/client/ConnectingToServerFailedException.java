
/*
 * Created on August 14, 2004
 */

package classes.client;

/**
 * Represents a connecting to server failed exception.<br>
 * This exception occurs on the client side when a client tries to connect to a Bombermen server, but fails.<br>
 * (Some) possible causes: host not found, host does not run Bombermen server, server is locked, passwords does not match...
 *
 * @author Andras Belicza
 */
public class ConnectingToServerFailedException extends Exception {

    /**
     * Creates a new ConnectingToServerFailedException.
     * @param message message/details of the exception
     */
    public ConnectingToServerFailedException( final String message ) {
        super( message );
    }
    
}
