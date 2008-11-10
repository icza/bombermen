
/*
 * Created on August 3, 2004
 */

package classes.client;

import classes.utils.TimedIterableControlledThread;
import classes.options.*;
import classes.options.Consts.NetworkLatencies;
import classes.options.Consts.SceneRefreshModes;
import classes.options.model.ClientOptions;
import classes.options.model.PublicClientOptions;
import classes.options.model.ServerOptions;
import classes.MainFrame;
import static classes.MainMenuBar.GameStates;
import java.net.*;
import java.io.IOException;
import classes.utils.ConnectionStub;
import classes.server.PlayerCollector;
import static classes.Consts.*;
import classes.MessageHandler;
import classes.server.Server;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_STRING;
import classes.MainComponentHandler;
import classes.GameManager;
import classes.AbstractAnimationMainComponentHandler;
import classes.MainMenuBar;
import classes.client.graphics.GraphicsManager;
import classes.client.graphics.AnimationDatas;
import static classes.options.ServerComponentOptions.RANDOMLY_GENERATED_LEVEL_NAME;
import java.util.Random;

import classes.client.gamecore.control.GameCoreHandler;
import classes.client.gamecore.model.level.LevelModel;
import classes.client.gamecore.view.GameSceneMainComponentHandler;

import java.util.Vector;

/**
 * A client of the game.<br>
 * The client must provide:
 * <ul>
 *   <li> handling (sending and receiving) local messages, participating in games by
 *   <li> calculating game (interpret as calculating levels, bombs, items, bombermen),
 *   <li> handling user interactions etc.
 * </ul>
 * To provide these services, uses -of course- lots of other classes
 * (in generally the classes.client package).<br>
 * All computer (host) playing in a Bombermen game must have exactly 1 Client object,
 * or none if a Bombermen station is not connected to a game.<br>
 * <br>
 * The client is also TimedIterableControlledThread, because in case of not low network latency
 * client times a few iterations by himself.<br>
 * <br>
 * The joining protocol is described at the PlayerCollector javadoc.
 *
 * @author Andras Belicza
 */
public class Client extends TimedIterableControlledThread implements MessageHandler, OptionsChangeListener< ClientOptions > {

    /**
     * Commands to be sent to the client, interpreted by us.
     * @author Andras Belicza
     */
    public enum Commands {
        /** Message command.                                                             */
        MESSAGE,
        /** Server is going down command.                                                */
        SHUTDOWN,
        /** Sending server options command.                                              */
        SENDING_SERVER_OPTIONS,
        /** Starting game command.                                                       */
        STARTING_GAME,
        /** Ending game command.                                                         */
        ENDING_GAME,
        /** Starting next round command.                                                 */
        STARTING_NEXT_ROUND,
        /** A client has joined the game command.                                        */
        A_CLIENT_HAS_JOINED_THE_GAME,
        /** A client has left the game command.                                          */
        A_CLIENT_HAS_LEFT_THE_GAME,
        /** Sending public cilent options command.
         * (if somebody changed its public client options, will be forwarded to us too.) */
        SENDING_PUBLIC_CLIENT_OPTIONS,
        /** Starting next iteration command.                                             */
        STARTING_NEXT_ITERATION
    }

    /** Identification string of the Bombermen client. */
    public static final String CLIENT_IDENTIFICATION_STRING = APPLICATION_NAME + " client";

    /** Reference to the game manager.                 */
    private final GameManager                     gameManager;
    /** Reference to the main frame.                   */
    private final MainFrame                       mainFrame;
    /** Reference to the client options manager.       */
    private final OptionsManager< ClientOptions > clientOptionsManager;
    /** Reference to the server options manager.       */
    private final OptionsManager< ServerOptions > globalServerOptionsManager;
    /** Server stub to communicate through.            */
    private ConnectionStub                        serverStub;
    /** Public client options of the clients.          */
    private final Vector< PublicClientOptions >   clientsPublicClientOptions = new Vector< PublicClientOptions >();
    /** Our index in the public client options vector. */
    private int                                   ourIndex;

    /** Handler of the main component being the waiting animation component. */
    private final MainComponentHandler            waitingAnimationMainComponentHandler;
    /** Handler of the main component being the game scene component.        */
    private final GameSceneMainComponentHandler   gameSceneMainComponentHandler;

    /** Reference to the game core handler.            */
    private GameCoreHandler                       gameCoreHandler;
	
	/** Counter of iterations. Used to determine whether we have to wait STARTING_NEXT_ITERATION command
	 * or we can start next iteration without it based on the network latency.                                 */
	private int									  iterationCounter;
	/** Iteration mask to determine whether next iteraion will be timed by us or the server.
	 * Capital letters indicates that its value is constant for the whole time of a game.                      */
	private int                                   ITERATION_NETWORK_LATENCY_MASK;
	/** Iteration mask to determine whether we have to redraw the game scene after calculating next iteration. */
	private int                                   iterationSceneRefreshMask;

	/** New, unprocessed actions of all clients (including ours).              */
	public String                                 newClientsActions;

    /**
     * Creates a new Client.
     * @param gameManager          reference to the game manager
     * @param mainFrame            reference to the main frame
     * @param clientOptionsManager reference to the client options manager
     * @param serverOptions        OPTIONAL reference to the server options<br>
     *                                  null means we're not hosting the game we're about to connect to, and
     *                                  if not null, we have to connect to our game, and connection options can be taken from this server options
     *                                  (for example game port, password)
     * @throws ConnectingToServerFailedException if connecting to server fails
     */
    public Client( final GameManager gameManager, final MainFrame mainFrame, final OptionsManager< ClientOptions > clientOptionsManager, ServerOptions serverOptions ) throws ConnectingToServerFailedException {
		super( 20 );   // This frequency will not be used, will be overwritten when game starts
		this.gameManager           = gameManager;
        this.mainFrame             = mainFrame;
        this.clientOptionsManager  = clientOptionsManager;
        globalServerOptionsManager = new OptionsManager< ServerOptions >( new ServerComponentOptions( new ServerOptions(), true ), "Global server options", this.mainFrame, true );

        connectToServer( serverOptions );
        clientOptionsManager.registerOptionsChangeListener( this );

        waitingAnimationMainComponentHandler = new AbstractAnimationMainComponentHandler( this.mainFrame ) {
            protected AnimationDatas getNewAnimationDatas() {
                return GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
            }
        };
        this.gameManager.setMainComponentHandler( waitingAnimationMainComponentHandler );
		
		gameSceneMainComponentHandler = new GameSceneMainComponentHandler( this.mainFrame, clientOptionsManager );

		// Part of the joining protocol: sending our public client options and receiving others' 
		final ClientOptions clientOptions = clientOptionsManager.getOptions();
        sendPublicClientOptions( clientOptions.publicClientOptions, clientOptions.playersFromHost );
        try {
            final int publicClientOptionsCount = Integer.parseInt( serverStub.receiveMessage() );
            for ( int i = 0; i < publicClientOptionsCount; i++ )
            	clientsPublicClientOptions.add( PublicClientOptions.parseFromString( serverStub.receiveMessage() ) );
        }
        catch ( final IOException ie ) {
        }
		// End of joining protocol
        ourIndex = clientsPublicClientOptions.size() - 1;  // We are placed always to the last position
		
		// If scene refresh mode is NORMAL, we refresh scene in every iteration, if it's SLOW, we refersh scene in every 2, and if it's EXTRA_SLOW, we refresh it in every 4. 
		iterationSceneRefreshMask = clientOptions.sceneRefreshMode == SceneRefreshModes.NORMAL ? 0 : ( clientOptions.sceneRefreshMode == SceneRefreshModes.SLOW ? 1 : 3 );

		iterationTimer.start();
    }

    /**
     * Connects to the server.
     * @param serverOptions OPTIONAL reference to the server options<br>
     *                           null means we're not hosting the game we're about to connect to, and
     *                           if not null, we have to connect to our game, and connection options can be taken from this server options
     *                           (for example game port, password)
     * @throws ConnectingToServerFailedException if connecting to server fails
     */
    private void connectToServer( final ServerOptions serverOptions ) throws ConnectingToServerFailedException {
        final ClientOptions clientOptions = clientOptionsManager.getOptions();
        Socket socket;
        try {
            socket = serverOptions == null ? new Socket( clientOptions.serverURL, clientOptions.gamePort )
                                           : new Socket( "localhost"            , serverOptions.gamePort );
        }
        catch ( final UnknownHostException ue ) {
            throw new ConnectingToServerFailedException( "Unknown server host!" );
        }
        catch ( final IOException ie ) {
            throw new ConnectingToServerFailedException( "Server not running on destination host!" );
        }
        
        try {
            try {
                serverStub = new ConnectionStub( socket );
                serverStub.sendMessage( CLIENT_IDENTIFICATION_STRING );
                if ( !serverStub.receiveMessage().equals( PlayerCollector.SERVER_IDENTIFICATION_STRING ) )
                    throw new ConnectingToServerFailedException( "Destination server is not a " + APPLICATION_NAME + " server!" );
                serverStub.sendMessage( APPLICATION_VERSION );
                final String serverVersion = serverStub.receiveMessage();
                if ( !serverVersion.equals( APPLICATION_VERSION ) )
                    throw new ConnectingToServerFailedException( "Incompatible " + APPLICATION_NAME + " server (ver. " + serverVersion + ")!" );
                serverStub.sendMessage( serverOptions == null ? clientOptions.password : serverOptions.password );
                if ( !serverStub.receiveMessage().equals( PlayerCollector.PASSWORD_ACCEPTED ) )
                    throw new ConnectingToServerFailedException( "Incorrect game password!" );
                
            }
            catch ( final IOException ie ) {
                throw new ConnectingToServerFailedException( "Network error!" );
            }
        }
        catch ( final ConnectingToServerFailedException ce ) {  // Before throwing this exception we have to close the server stub (this is the right thing to do)!
            if ( serverStub != null )
				serverStub.close();
            throw ce;
        }
    }

    /**
     * The run() method of the client controlled thread.
     * Provides the services of the client.
     */
    public void run() {
		final MainMenuBar mainMenuBar = mainFrame.getMainMenuBar();
		while ( !requestedToCancel ) {
			
			if ( mainMenuBar.getGameState() == GameStates.PLAYING ) {               // If we playing, we check whether next iteration should begin now
				if ( ( iterationCounter & ITERATION_NETWORK_LATENCY_MASK ) == 0 ) { // Next iteration is timed by the server now
					if ( newClientsActions != null )
						startNextIteration();
				}
				else {                                                              // Next iteration is timed by us
					if ( nextIterationMayBegin ) {  
//						nextIterationMayBegin = false;
						startNextIteration();
					}
				}
			}

			checkForNewCommands();
            try {
                sleep( 1l );
            }
            catch ( final InterruptedException ie ) {
            }
        }
    }

    /**
     * Checks whether the server sent new messages, and process them if it did.
     */
    private void checkForNewCommands() {
        while ( serverStub.hasNewMessage() )
            try {
                final GeneralStringTokenizer commandTokenizer = new GeneralStringTokenizer( serverStub.receiveMessage() );
                switch ( Commands.values()[ commandTokenizer.nextIntToken() ] ) {
			        // The message loop checks Commands.STARTING_NEXT_ITERATION and Commands.MESSAGE first,
			        // because these are the most frequent commands.
				    case STARTING_NEXT_ITERATION:
						newClientsActions = commandTokenizer.hasRemainingString() ? commandTokenizer.remainingString() : "";
						break;
					case MESSAGE  :
                        mainFrame.receiveMessage( commandTokenizer.remainingString() );
                        break;
                    case SENDING_SERVER_OPTIONS:
                        globalServerOptionsManager.setOptions( ServerOptions.parseFromString( commandTokenizer.remainingString() ) );
                        new Thread() {   // Client thread (and checking commands) cannot be blocked, we show options dialog in a new thread!
                            public void run() {
                                globalServerOptionsManager.showOptionsDialog();
                            }
                        }.start();
                        break;
                    case STARTING_GAME:
                        handleGameStarting();
                        break;
                    case ENDING_GAME:
						handleGameEnding();
                        break;
                    case STARTING_NEXT_ROUND :
                        gameCoreHandler.initNextRound();
                        break;
                    case A_CLIENT_HAS_JOINED_THE_GAME :
                    	clientsPublicClientOptions.add( PublicClientOptions.parseFromString( commandTokenizer.remainingString() ) );
                        break;
                    case A_CLIENT_HAS_LEFT_THE_GAME :
                        final int clientIndex = commandTokenizer.nextIntToken();
                        clientsPublicClientOptions.removeElementAt( clientIndex );
                        if ( ourIndex > clientIndex )
                            ourIndex--;
                        if ( gameCoreHandler != null )
                            gameCoreHandler.aClientHasLeftTheGame( clientIndex );
                        break;
                    case SENDING_PUBLIC_CLIENT_OPTIONS :
                        final int clientIndex_ = commandTokenizer.nextIntToken();   // Index of client whose public client options is being received
                        clientsPublicClientOptions.setElementAt( PublicClientOptions.parseFromString( commandTokenizer.remainingString() ), clientIndex_ );
                        break;
                    case SHUTDOWN :
                        serverStub.close();
                        break;
                }
            }
            catch ( final Exception e ) {
            }
    }
	
	/**
	 * Sends ready for next iteration command to the server.
	 * This includes sending our client's new actions.
	 */
	private void sendReadyForNextIterationCommand() {
		try {
			String newClientActions = gameSceneMainComponentHandler.getGameSceneComponent().getAndClearNewActions();
			if ( newClientActions.length() > 0 )
				newClientActions += GENERAL_SEPARATOR_STRING;

			serverStub.sendMessage( Server.Commands.READY_FOR_NEXT_ITERATION.ordinal() + GENERAL_SEPARATOR_STRING + newClientActions );
		}
		catch ( final IOException ie ) {
		}
	}

    /**
     * Sends the public client options to the server.
     * @param publicClientOptions public client options to be sent
     * @param playersCount        tells how many public client options of players have to be sent
     */
    private void sendPublicClientOptions( final PublicClientOptions publicClientOptions, final int playersCount ) {
        try {
            serverStub.sendMessage( Server.Commands.SENDING_PUBLIC_CLIENT_OPTIONS.ordinal() + GENERAL_SEPARATOR_STRING + publicClientOptions.packToString( playersCount ) );
        }
        catch ( final IOException ie ) {
        }
    }

    /**
     * Handles a messages.
     * @param message message to be handled
     */
    public void handleMessage( final String message ) {
        try {
            serverStub.sendMessage( Server.Commands.MESSAGE.ordinal() + GENERAL_SEPARATOR_STRING + message );
        }
        catch ( final IOException ie ) {
        }
    }
    
    /**
     * Requests the global server options.
     * Sends a request command to the server for the server options.
     */
    public void requestGlobalServerOptions() {
        if ( mainFrame.getMainMenuBar().getGameState() == GameStates.PLAYING )
            globalServerOptionsManager.showOptionsDialog();
        else
            try {
                serverStub.sendMessage( Server.Commands.REQUESTING_SERVER_OPTIONS.ordinal() + GENERAL_SEPARATOR_STRING );
            }
            catch ( final IOException ie ) {
            }
    }

    /**
     * Handles starting of the game.
     */
    private void handleGameStarting() {
        try {
			newClientsActions = null;
			// Receiving all required options and datas for a new game...
            final Random random = new Random( Long.parseLong( serverStub.receiveMessage() ) );
            final ServerOptions globalServerOptions = ServerOptions.parseFromString( serverStub.receiveMessage() );
            globalServerOptionsManager.setOptions( globalServerOptions );
            LevelModel levelModel = null;
            if ( !globalServerOptions.levelName.equals( RANDOMLY_GENERATED_LEVEL_NAME ) )
            	levelModel = LevelModel.parseFromString( serverStub.receiveMessage() );

            // We received all required informations... we can create game core handler now, and register that game is now in GameStates.PLAYING state
            gameCoreHandler = new GameCoreHandler( gameManager, mainFrame, globalServerOptions, levelModel, random, clientsPublicClientOptions, ourIndex );
			gameSceneMainComponentHandler.getGameSceneComponent().setModelProvider( gameCoreHandler );
			gameSceneMainComponentHandler.getGameSceneComponent().handleGameStarting();
			gameManager.setMainComponentHandler( gameSceneMainComponentHandler );

			// If network latency is LOW, we wait for STARTING_NEXT_ITERATION command in every iteration, if it's HIGH, we wait for it in every 2, and if it's EXTRA_HIGH, we wait for it in every 4.
			ITERATION_NETWORK_LATENCY_MASK = globalServerOptions.networkLatency == NetworkLatencies.LOW ? 0 : ( globalServerOptions.networkLatency == NetworkLatencies.HIGH ? 1 : 3 );
			iterationCounter               = 0;
			nextIterationMayBegin          = false;
			iterationTimer.setFrequency( globalServerOptions.gameCycleFrequency );

			// We dont have to call iterationTimer.setReadyForNextIteration() here, because first iteration is always timed by the server
			
			mainFrame.getMainMenuBar().setGameState( GameStates.PLAYING );   // This can (must) be done last
        }
        catch ( final Exception e ) {
        }
    }
	
	/**
	 * Starts the next iteration.
	 */
	private void startNextIteration() {
		if ( gameCoreHandler != null ) {

			if ( ( ( iterationCounter + 1 ) & ITERATION_NETWORK_LATENCY_MASK ) != 0 ) {
				nextIterationMayBegin = false;
				iterationTimer.setReadyForNextIteration();    // The next iteration will NOT be timed by the server but our timer
			}

			if ( ( iterationCounter & ITERATION_NETWORK_LATENCY_MASK ) == 0 ) { // This time we were timed by the server
				gameCoreHandler.nextIteration( newClientsActions ); // Timed by server: clients actions have to be passed
				newClientsActions = null;
				sendReadyForNextIterationCommand();                 // Timed by server: we send READY_FOR_NEXT_ITERATION command back           
			}
			else
				gameCoreHandler.nextIteration( null );              // Timed by us: no clients actions have to be passed
			
			if ( ( iterationCounter & iterationSceneRefreshMask ) == 0 )
				gameSceneMainComponentHandler.getGameSceneComponent().repaint();
			
			iterationCounter++;
		}
	}
	
	/**
	 * Handles ending of the game.
	 */
	private void handleGameEnding() {
        mainFrame.getMainMenuBar().setGameState( GameStates.PLAYER_COLLECTING_CONNECTED ); // This has to be done first
		gameSceneMainComponentHandler.getGameSceneComponent().setModelProvider( null );
        gameCoreHandler = null;
        gameManager.setMainComponentHandler( waitingAnimationMainComponentHandler );
	}

    /**
     * Method to be called when client options may have been changed.
     * @param oldOptions the old client options before the change signed by calling this method
     * @param newOptions the new client options are about to become effective
     */
    public void optionsChanged( final ClientOptions oldOptions, final ClientOptions newOptions ) {
        if ( newOptions.playersFromHost != oldOptions.playersFromHost || !newOptions.publicClientOptions.equals( oldOptions.publicClientOptions, oldOptions.playersFromHost ) )
            sendPublicClientOptions( newOptions.publicClientOptions, newOptions.playersFromHost );
		
		if ( newOptions.sceneRefreshMode != oldOptions.sceneRefreshMode ) {
			// If scene refresh mode is NORMAL, we refresh scene in every iteration, if it's SLOW, we refersh scene in every 2, and if it's EXTRA_SLOW, we refresh it in every 4. 
			iterationSceneRefreshMask = newOptions.sceneRefreshMode == SceneRefreshModes.NORMAL ? 0 : ( newOptions.sceneRefreshMode == SceneRefreshModes.SLOW ? 1 : 3 );
		}

		if ( newOptions.graphicalTheme == null || !newOptions.graphicalTheme.equals( oldOptions.graphicalTheme ) ) {
			waitingAnimationMainComponentHandler.graphicalThemeChanged();
			gameSceneMainComponentHandler.graphicalThemeChanged();
        }
    }
    
    /**
     * Closes the client.
     * Invoked at the end of shutdown.
     * Must not (not needed to) be invoked at the end of the run() method.
     */
    protected void close() {
		gameSceneMainComponentHandler.getGameSceneComponent().close();
		
		clientOptionsManager.unregisterOptionsChangeListener( this );
        try {
            serverStub.sendMessage( Server.Commands.QUIT.ordinal() + GENERAL_SEPARATOR_STRING );
        }
        catch ( final IOException ie ) {
        }
        serverStub.close();
    }
    
}
