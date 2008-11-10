
/*
 * Created on August 3, 2004
 */

package classes.server;

import classes.utils.TimedIterableControlledThread;
import java.util.Vector;
import classes.MainFrame;
import classes.options.*;
import classes.utils.GeneralStringTokenizer;
import classes.client.Client;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_STRING;
import java.io.IOException;
import classes.options.OptionsChangeListener;
import classes.options.model.PublicClientOptions;
import classes.options.model.ServerOptions;
import static classes.MainMenuBar.GameStates;
import java.util.Random;
import static classes.options.ServerComponentOptions.RANDOMLY_GENERATED_LEVEL_NAME;
import classes.GameManager;
import static classes.options.Consts.NetworkLatencies;

/**
 * The server of the game.<br>
 * In generally the server must provide:
 * <ul>
 *     <li>player collecting
 *     <li>handling and forwarding chat messages and commands
 *     <li>timing of iterations, user events and game states
 *     <li>etc.
 * </ul>
 * To provide these services uses -of course- lots of other classes
 * (in generally the classes.server package).<br>
 * All game has exactly one server which is not neccessarily on the local host.
 * Game is not possible without Server object, if a player plays alone,
 * he must have created a Server (object).<br>
 * <br>
 * <br>
 * The communication between the server and the clients is text command oriented based on TCP/IP.<br>
 * Each command is terminated with the new line character '\n'. Each line is a command.<br>
 * Each command sent by the server is one of Client.Commands.<br>
 * Each command sent to the server must be one of Server.Commands.<br>
 * Sending a command means sending its ordinal not its string representation.
 * Each command must be followed by GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR,
 * and can be followed optional parameters (until the terminating '\n' of course).<br>
 * The joining and game starting protocols may be exceptions.<br> 
 * <br>
 * The game starting protocol is the following:
 * <ol>
 *     <li>The server sends the Client.Commands.STARTING_GAME command.
 *     <li>The server sends a long random number.
 *     <li>The server sends the server options.
 *     <li>OPTIONAL: if the level what is set is not the random level, the server sends the level.
 *     <li>The server sends the Client.Commands.STARTING_NEXT_ROUND command.
 * </ol>
 * 
 *
 * @author Andras Belicza
 */
public class Server extends TimedIterableControlledThread implements OptionsChangeListener< ServerOptions > {

    /**
     * Commands to be sent to the server, interpreted by us.
     * @author Andras Belicza
     */
    public enum Commands {
        /** Message command.                                      */
        MESSAGE,
        /** Quit command.                                         */
        QUIT,
        /** Sending public client options command.                */
        SENDING_PUBLIC_CLIENT_OPTIONS,
        /** Requesting server options command.                    */
        REQUESTING_SERVER_OPTIONS,
        /** Signing that client is ready for next iteration,
         * and sends the actions of its players with the command. */
        READY_FOR_NEXT_ITERATION
    }
    
    /** Name of the server as a chat client whithout extra signs. */
    public static final String BASE_SERVER_CHAT_NAME = "<Server>";
    /** Name of the server as a chat client included a ':' and a space at the end. */
    public static final String SERVER_CHAT_NAME      = BASE_SERVER_CHAT_NAME + ": ";
    

    /** Reference to the server options manager.           */
    private final OptionsManager< ServerOptions > serverOptionsManager;
    /** Reference to the main frame.                       */
    private final MainFrame                       mainFrame;
    /** Reference to the game manager.                     */
    private final GameManager                     gameManager;
    /** The player collector.                              */
    private volatile PlayerCollector              playerCollector;
    /** Vector of client contacts.                         */
    private final Vector< ClientContact >         clientContacts       = new Vector< ClientContact > ();
    /** Tells whether starting of game has been requested. */
    private volatile boolean                      requestedToStartGame = false;
    /** Tells whether ending of game has been requested.   */
    private volatile boolean                      requestedToEndGame   = false;
    /** The state of the game. The clients state (stored at MainMenuBar) will be synchronized to this by commands. */
    private volatile GameStates                   gameState;
    
	/** Counter of iterations. Used to determine whether we have to send STARTING_NEXT_ITERATION command
	 * or we can start next iteration without it based on the network latency.                                     */
	private int									  iterationCounter;

    /**
     * Creates a new Server.
     * @param serverOptionsManager reference to the server options manager
     * @param mainFrame            reference to the main frame
     * @param gameManager          reference to the game manager
     */
    public Server( final OptionsManager< ServerOptions > serverOptionsManager, final MainFrame mainFrame, final GameManager gameManager ) {
		super( 20 );   // This frequency will not be used, will be overwritten when game starts
        this.serverOptionsManager = serverOptionsManager;
        this.mainFrame            = mainFrame;
        this.gameManager          = gameManager;
        this.serverOptionsManager.registerOptionsChangeListener( this );
        iterationTimer.start();
    }

    /**
     * Waits until the player collector tries to create server socket, and returns the successfulness of the operation.
     * @return true, if the player collector created the server socket successfully; false otherwise
     */
    public boolean waitForAndCheckServerSocket() {
        try {
            while ( playerCollector == null )
                Thread.sleep( 1l );   // Very small because if server socket is created, we should join first
        }
        catch ( final InterruptedException ie ) {
        }
        if ( playerCollector != null )
            return playerCollector.isServerSocketCreated();
        return false;
    }
    
    /**
     * The run() method of the server controlled thread.
     * Provides the services of the server.
     */
    public void run() {
        gameState = GameStates.PLAYER_COLLECTING_NOT_CONNECTED;
        while ( true ) { // The game loop: every iteration of this loop is one game.

            if ( requestedToCancel )
                break;
            collectPlayers();

            if ( requestedToCancel )
                break;
            gameState = GameStates.PLAYING;
            handleGame();
            gameState = GameStates.PLAYER_COLLECTING_CONNECTED;
                
        }
    }

    /**
     * Collects players until game starting or cancel is requested.
     */
    private void collectPlayers() {
        playerCollector = new PlayerCollector( this, serverOptionsManager, clientContacts, mainFrame );
        while ( !requestedToCancel && !requestedToStartGame ) {
            playerCollector.nextIteration();
            checkForNewCommands();
            try {
                sleep( 1l );
            }
            catch ( final InterruptedException ie ) {
            }
        }
        playerCollector.close();
        playerCollector = null;
        requestedToStartGame = false;
    }

    /**
     * Handles the game until it ends or cancel is requested.
     */
    private void handleGame() {

		// Game starting protocol
        broadcastCommand( Client.Commands.STARTING_GAME.ordinal() + GENERAL_SEPARATOR_STRING );
		
		// Sending all required options and datas for a new game...
        broadcastCommand( "" + new Random().nextLong() );
        final ServerOptions serverOptions = serverOptionsManager.getOptions();
        broadcastCommand( serverOptions.packToString() );
        if ( !serverOptions.levelName.equals( RANDOMLY_GENERATED_LEVEL_NAME ) )
            broadcastCommand( gameManager.getLevel().packToString() );
        
		iterationTimer.setFrequency( serverOptions.gameCycleFrequency );
		nextIterationMayBegin = false;
		iterationTimer.setReadyForNextIteration();

		for ( final ClientContact clientContact : clientContacts )
			clientContact.newClientActions = "";                    // Simulating that all the clients are ready for next iteration

		startNextRound();
		broadcastStartingNextIterationCommand();
		
		// If network latency is LOW, we send STARTING_NEXT_ITERATION command in every iteration, if it's HIGH, we send in every 2, and if it's EXTRA_HIGH, we send in every 4.
		final int ITERATION_NETWORK_LATENCY_MASK = serverOptions.networkLatency == NetworkLatencies.LOW ? 0 : ( serverOptions.networkLatency == NetworkLatencies.HIGH ? 1 : 3 );
		while ( !requestedToCancel && !requestedToEndGame ) {
			
			if ( nextIterationMayBegin ) {
				if ( ( iterationCounter & ITERATION_NETWORK_LATENCY_MASK ) == 0 ) { // We time the clients
					if ( areAllClientsReadyForNextIteration() ) {
						broadcastStartingNextIterationCommand();
						startNextIteration();
					}
				}
				else          // The clients time themselves
					startNextIteration();
			}

			checkForNewCommands();
            try {
                sleep( 1l );
            }
            catch ( final InterruptedException ie ) {
            }
        }
		
        broadcastCommand( Client.Commands.ENDING_GAME.ordinal() + GENERAL_SEPARATOR_STRING );
        requestedToEndGame = false;
    }

	/**
	 * Starts next iteration which does not include sending STARTING_NEXT_ITERATION command.
	 */
	private void startNextIteration() {
		nextIterationMayBegin = false;
		iterationTimer.setReadyForNextIteration();
		iterationCounter++;
	}
	
	/**
	 * Broadcasts the starting next iteration.<br>
	 * This includes sending and clearing all the new client actions.
	 */
	private void broadcastStartingNextIterationCommand() {
        String newClientsActions = "";
		
		for ( int i = 0; i < clientContacts.size(); i++ ) {
			final ClientContact clientContact = clientContacts.get( i ); 
			if ( clientContact.newClientActions.length() > 0 )
				newClientsActions += i + " " + clientContact.newClientActions;
			clientContact.newClientActions = null;
		}

		broadcastCommand( Client.Commands.STARTING_NEXT_ITERATION.ordinal() + GENERAL_SEPARATOR_STRING + newClientsActions );  // We append ALL new clients actions
	}

	/**
	 * Tests whether all the clients are ready for the next iteration.
	 * @return true if all the clients are ready for the next iteration; false otherwise
	 */
	private boolean areAllClientsReadyForNextIteration() {
		for ( final ClientContact clientContact : clientContacts )
			if ( clientContact.newClientActions == null )
				return false;
		
		return true;
	}
	
    /**
     * Checks all clients whether they sent new messages, and process them if they did.
     */
    private void checkForNewCommands() {
        for ( int i = 0; i < clientContacts.size(); i++ ) {  // Can't use enhanced for because elements can be removed (QUIT)
            final ClientContact clientContact = clientContacts.get( i );
            messageLoop:
            while ( clientContact.connectionStub.hasNewMessage() )
                try {
                    final GeneralStringTokenizer commandTokenizer = new GeneralStringTokenizer( clientContact.connectionStub.receiveMessage() );
                    switch ( Commands.values()[ commandTokenizer.nextIntToken() ] ) {
				        // The message loop checks Commands.READY_FOR_NEXT_ITERATION and Commands.MESSAGE first,
				        // because these are the most frequent commands.
						case READY_FOR_NEXT_ITERATION:
							clientContact.newClientActions = commandTokenizer.hasRemainingString() ? commandTokenizer.remainingString() : "";
							break;
                        case MESSAGE :
                            broadcastMessage( clientContact.publicClientOptions.clientName + ": " + commandTokenizer.remainingString() );
                            break;
                        case QUIT : 
                            handleClientLeaving( clientContact );
                            break messageLoop;    // We're not trying read more message (would not be error/exception without this because connectionStub would simply return that no more message is available)
                        case SENDING_PUBLIC_CLIENT_OPTIONS :
                            clientContact.publicClientOptions = PublicClientOptions.parseFromString( commandTokenizer.remainingString() );
                            broadcastCommand( Client.Commands.SENDING_PUBLIC_CLIENT_OPTIONS.ordinal() + GENERAL_SEPARATOR_STRING + i + GENERAL_SEPARATOR_STRING + clientContact.publicClientOptions.packToString() );
                            break;
                        case REQUESTING_SERVER_OPTIONS :
                            clientContact.connectionStub.sendMessage( Client.Commands.SENDING_SERVER_OPTIONS.ordinal() + GENERAL_SEPARATOR_STRING + serverOptionsManager.getOptions().packToString() );
                            break;
                    }
                }
                catch ( final Exception e ) {
                }
            }
    }

    /**
     * Broadcasts a message to all the clients.
     * @param message message to be broadcasted
     */
    public void broadcastMessage( final String message ) {
        broadcastCommand( Client.Commands.MESSAGE.ordinal() + GENERAL_SEPARATOR_STRING + message );
    }

    /**
     * Broadcasts a command to all the clients.
     * Protected because PlayerCollector calls it.
     * @param command command to be broadcasted
     */
    protected void broadcastCommand( final String command ) {
        for ( final ClientContact clientContact : clientContacts )
            try {
                clientContact.connectionStub.sendMessage( command );
            }
            catch ( final IOException ie ) {
            }
    }

    /** 
     * Starts current game.
     */
    public void startCurrentGame() {
        iterationCounter = 0;
		requestedToStartGame = true;
        try {
            while ( gameState != GameStates.PLAYING )
                Thread.sleep( 1l );
        }
        catch ( final InterruptedException ie ) {
        }
    }

    /** 
     * Ends current game.
     */
    public void endCurrentGame() {
        requestedToEndGame = true;
        try {
            while ( gameState == GameStates.PLAYING )
                Thread.sleep( 1l );
        }
        catch ( final InterruptedException ie ) {
        }
    }

    /**
     * Starts next round of the game.
     */
    public void startNextRound() {
        broadcastCommand( Client.Commands.STARTING_NEXT_ROUND.ordinal() + GENERAL_SEPARATOR_STRING );
    }

    /**
     * Handles a client when it leaves the game.
     * @param clientContact client contact of client who is about to leave
     */
    private void handleClientLeaving( final ClientContact clientContact ) {
        clientContacts.remove( clientContact );
        clientContact.connectionStub.close();
        broadcastCommand( Client.Commands.A_CLIENT_HAS_LEFT_THE_GAME.ordinal() + GENERAL_SEPARATOR_STRING );
        broadcastMessage( SERVER_CHAT_NAME + clientContact.publicClientOptions.clientName + " has left the game." );
    }

    /**
     * Method to be called when server options may have been changed.
     * @param oldOptions the old options before the change signed by calling this method
     * @param newOptions the new options are about to become effective
     * TODO CONSIDER IF THIS NEEDS HERE AT ALL! SETTING GAME FREQUENCY MUST NOT BE DONE, ITS CONSTANT FOR A GAME, AND IS SET AT THE STARTING OF ALL GAME!
     */
    public void optionsChanged( final ServerOptions oldOptions, ServerOptions newOptions ) {
    }

    /**
     * Closes the server.
     * Invoked at the end of shutdown.
     * Must not (not needed to) be invoked at the end of the run() method.
     */
    protected void close() {
        iterationTimer.shutDown();
        serverOptionsManager.unregisterOptionsChangeListener( this );
        broadcastMessage( SERVER_CHAT_NAME + "Server is going for a shutdown..." );
        broadcastCommand( Client.Commands.SHUTDOWN + GENERAL_SEPARATOR_STRING );
        for ( final ClientContact clientContact : clientContacts )
            clientContact.connectionStub.close();
    }
    
}
