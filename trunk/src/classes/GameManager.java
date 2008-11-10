
/*
 * Created on July 3, 2004
 */

package classes;

import classes.options.*;
import classes.options.model.ClientOptions;
import classes.options.model.ServerOptions;

import java.io.*;
import static classes.Consts.*;
import classes.MainMenuBar.GameStates;
import classes.server.Server;
import classes.client.Client;
import classes.client.ConnectingToServerFailedException;
import javax.swing.JOptionPane;
import classes.utils.DataTextFileReader;
import classes.client.graphics.GraphicsManager;
import classes.client.graphics.CorruptGraphicalThemeException;
import classes.client.graphics.AnimationDatas;
import classes.client.graphics.ImageHandler;
import static classes.options.ServerComponentOptions.RANDOMLY_GENERATED_LEVEL_NAME;
import classes.client.gamecore.model.level.LevelModel;


/**
 * Manages the whole game (as software), which includes:
 * <UL>
 *    <LI>Loads and initializes the options and the other managers.
 *    <LI>Handles the main GUI class, the main frame.
 *    <LI>Implements ActionEvent and handles all the menu actions (instructions from the players).
 *    <LI>Handles the game corses and states, and connects the GUI of game and the game core (game logic) (to each other)
 * </UL>
 *
 * @author Andras Belicza
 */
public class GameManager implements MainMenuHandler, OptionsChangeListener< ClientOptions > {
//public class GameManager implements MainMenuHandler, OptionsChangeListener< ClientOptions >, OptionsChangeListener< ServerOptions > { // this should be, would be nice this way... but it's not allowed...

    /** The client options manager.                                                  */
    private OptionsManager< ClientOptions > clientOptionsManager;
    /** The server options manager.                                                  */
    private OptionsManager< ServerOptions > serverOptionsManager;
    /** The main frame of the game.                                                  */
    private final MainFrame                 mainFrame;
    /** Reference to the main menu bar.                                              */
    private final MainMenuBar               mainMenuBar;
    /** The server of the game, or null if there is no server running or our host.   */
    private Server                          server;
    /** The client of the game or null if there is we are not connected to any game. */
    private Client                          client;
    /** The handler of the main component.                                           */
    private MainComponentHandler            mainComponentHandler;
    /** The selected level where game will be played on (null means randomly generated level). */
    private LevelModel                           level;

    /** Handler of the main component being the title animation component.           */
    private final MainComponentHandler      titleAnimationMainComponentHandler;

    /**
     * Server Options Change Listener. This class is a delegation class to the game manager, because
     * multiple implementation of an interface is not allowed (even with different parameters).
     */
    private final OptionsChangeListener< ServerOptions > serverOptionsChangeListener = new OptionsChangeListener< ServerOptions >() {
        public void optionsChanged( final ServerOptions oldOptions, final ServerOptions newOptions ) {
            GameManager.this.optionsChanged( oldOptions, newOptions );
        }
    };
    
    /**
     * Creates a GameManager. Does all the job needed to start the game.
     * After the constructor, the main thread ends, but the main frame remains visible.
     * First tries to load the options, then creates and makes the main frame visible.
     */
    public GameManager() {
        mainFrame   = new MainFrame( this );
        mainMenuBar = mainFrame.getMainMenuBar();
        
        mainFrame.receiveMessage( "Contact: bomber.men@freemail.hu" );

        loadOptions();
        clientOptionsManager.registerOptionsChangeListener( this );
        serverOptionsManager.registerOptionsChangeListener( serverOptionsChangeListener );
        clientOptionsManager.registerOptionsChangeListener( ImageHandler.clientOptionsChangeListener ); // This shouldn't be here, but ImageHandler is a "static" options listener, cannot be there
        ImageHandler.setImageScalingAlgorithm( clientOptionsManager.getOptions().imageScalingAlgorithm );
        
        titleAnimationMainComponentHandler = new AbstractAnimationMainComponentHandler( mainFrame ) {
            protected AnimationDatas getNewAnimationDatas() {
                return GraphicsManager.getCurrentManager().getTitleAnimationDatas();
            }
        };
        
        setMainComponentHandler( titleAnimationMainComponentHandler );   // This will also be set in closeGame(), but we need this for loadGraphicalTheme(), and it must precede closeGame().
        loadGraphicalTheme( clientOptionsManager.getOptions().graphicalTheme );
        loadLevel( RANDOMLY_GENERATED_LEVEL_NAME );

        // At the beginning we must be in the same state we're in after games.
        closeGame();
        
        mainFrame.setFullScreenMode( mainMenuBar.getFullScreenWindowMenuItemState() );
        mainFrame.setVisible( true );
    }

    /**
     * Tries to load the options. If it fails, creates new ones with default values.
     */
    private void loadOptions() {
        ClientComponentOptions clientComponentOptions;
        ServerComponentOptions serverComponentOptions;
        try {
            final DataTextFileReader optionsFileReader = new DataTextFileReader( OPTIONS_FILE_NAME );

            clientComponentOptions = new ClientComponentOptions( ClientOptions.parseFromString( optionsFileReader.readNextDataLine() ) );
            serverComponentOptions = new ServerComponentOptions( ServerOptions.parseFromString( optionsFileReader.readNextDataLine() ) );

            mainMenuBar.setMenuStates( optionsFileReader.readNextDataLine() );
            mainFrame.setWindowAndSplitterPositions( optionsFileReader.readNextDataLine() );
            
            optionsFileReader.close();
        }
        catch ( final Exception e ) {
            clientComponentOptions = new ClientComponentOptions( new ClientOptions() );
            clientComponentOptions.restoreDefaultValuesToComponents();
            clientComponentOptions.setOptions( clientComponentOptions.getOptionsFromComponents() );

            serverComponentOptions = new ServerComponentOptions( new ServerOptions() );
            serverComponentOptions.restoreDefaultValuesToComponents();
            serverComponentOptions.setOptions( serverComponentOptions.getOptionsFromComponents() );

            mainMenuBar.restoreDefaultMenuStates();
            mainFrame.restoreDefaultWindowAndSplitterPositions();
        }
        // We validate options by setting them to the components and then getting them back (checking options is done when getting them from components)
        // Validating is not unnecessary even when creating them from defaults (defaults are not checked, could be invalid value...)
        clientComponentOptions.synchronizeComponentsToOptions();
        serverComponentOptions.synchronizeComponentsToOptions();
        clientComponentOptions.setOptions( clientComponentOptions.getOptionsFromComponents() );
        serverComponentOptions.setOptions( serverComponentOptions.getOptionsFromComponents() );
        clientOptionsManager = new OptionsManager< ClientOptions >( clientComponentOptions, "Client options", mainFrame );
        serverOptionsManager = new OptionsManager< ServerOptions >( serverComponentOptions, "Server options", mainFrame );
        
    }

    /**
     * Tries to save options.
     */
    private void saveOptions() {
        try {
            final PrintWriter optionsFileWriter = new PrintWriter( new FileWriter( OPTIONS_FILE_NAME ) );

            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " Options file holding the persistent datas of " + Consts.APPLICATION_NAME + "." );
            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " The file has a specific format, do not remove or change lines unless you know what you're doing!" );
            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " " );

            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " Client options:" );
            optionsFileWriter.println( clientOptionsManager.getOptions().packToString() );
            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " Server options:" );
            optionsFileWriter.println( serverOptionsManager.getOptions().packToString() );
            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " Menu states:" );
            optionsFileWriter.println( mainMenuBar.packMenuStates() );
            optionsFileWriter.println( DataTextFileReader.COMMENT_LINE_CHAR + " Window positions:" );
            optionsFileWriter.println( mainFrame.packWindowAndSplitterPositions() );

            optionsFileWriter.flush();
            optionsFileWriter.close();
        }
        catch ( final Exception e ) {
            System.out.println( e );
            e.printStackTrace();
        }
    }

    /**
     * Sets the handler of the main component.
     * @param mainComponentHandler handler of the main component to be set
     */
    public void setMainComponentHandler( final MainComponentHandler mainComponentHandler ) {
        if ( this.mainComponentHandler != null )
            this.mainComponentHandler.releaseMainComponent();
        this.mainComponentHandler = mainComponentHandler;
        this.mainComponentHandler.reinitMainComponent();
    }

    /**
     * Method to be called when client options may have been changed.
     * @param oldOptions the old options before the change signed by calling this method
     * @param newOptions the new options are about to become effective
     */
    public void optionsChanged( final ClientOptions oldOptions, final ClientOptions newOptions ) {
        if ( newOptions.graphicalTheme == null || !newOptions.graphicalTheme.equals( oldOptions.graphicalTheme ) )
            loadGraphicalTheme( oldOptions.graphicalTheme );
    }

    /**
     * Method to be called when server options may have been changed.
     * @param oldOptions the old options before the change signed by calling this method
     * @param newOptions the new options are about to become effective
     */
    public void optionsChanged( final ServerOptions oldOptions, final ServerOptions newOptions ) {
        if ( !newOptions.levelName.equals( oldOptions.levelName ) )
            loadLevel( oldOptions.levelName );
    }

    /**
     * Loads the graphical theme specified by the client options.
     * @param oldGraphicalTheme the old graphical theme (will be re-set if loading the new one fails)
     */
    private void loadGraphicalTheme( final String oldGraphicalTheme ) {
        try {
            final GraphicsManager graphicsManager = GraphicsManager.loadGraphicalTheme( clientOptionsManager.getOptions().graphicalTheme );
			titleAnimationMainComponentHandler.graphicalThemeChanged();
            mainFrame.setIconImage( graphicsManager.getWindowIconImage() );
        }
        catch ( final CorruptGraphicalThemeException ce ) {
            JOptionPane.showMessageDialog( mainFrame, new String[] { "Corrupt graphical theme: " + clientOptionsManager.getOptions().graphicalTheme, ce.getMessage() }, "Error loading graphical theme", JOptionPane.ERROR_MESSAGE );
            clientOptionsManager.getOptions().graphicalTheme = oldGraphicalTheme;
        }
    }
    
    /**
     * Loads the level selected in server options.
     * @param oldLevelName the old level name to restore if loading fails
     */
    public void loadLevel( final String oldLevelName ) {
        final String levelName = serverOptionsManager.getOptions().levelName;
        if ( levelName.equals( RANDOMLY_GENERATED_LEVEL_NAME ) )
            level = null;
        else {
            final String levelFileName = LEVELS_DIRECTORY_NAME + levelName + LEVEL_FILE_EXTENSION;
            try {
                final BufferedReader levelFile = new BufferedReader( new FileReader( levelFileName ) );
                level = LevelModel.parseFromString( levelFile.readLine() );
                levelFile.close();
            }
            catch ( final FileNotFoundException fe ) {
                JOptionPane.showMessageDialog( mainFrame, "Level file not found: " + levelFileName, "Error loading level", JOptionPane.ERROR_MESSAGE );
                serverOptionsManager.getOptions().levelName = oldLevelName;
            }
            catch ( final Exception e ) {
                JOptionPane.showMessageDialog( mainFrame, "Corrupt level file: " + levelFileName, "Error loading level", JOptionPane.ERROR_MESSAGE );
                serverOptionsManager.getOptions().levelName = oldLevelName;
            }
        }
    }

    /**
     * Returns the selected level where game will be played on.
     * null is returned if randomly generated level is selected.
     * @return the selected level where game will be played on; null if randomly generated level is selected
     */
    public LevelModel getLevel() {
        return level;
    }



    //******************************************************************************************//
    //*************     SERVICES TO IMPLEMENT MenuHanler interface     *************************//
    //******************************************************************************************//


    /** 
     * To handle create menu item and create a game.
     */
    public void createGame() {
        if ( server == null & client == null ) {     // Checking this condition is not neccessary, action of menu causing invoking of this method is possible only when we're in IDLE state
            server = new Server( serverOptionsManager, mainFrame, this );
            server.start();
            if ( server.waitForAndCheckServerSocket() ) {
                mainMenuBar.setOurServerRunning( true );
                mainFrame.getMainMenuBar().setGameState( GameStates.PLAYER_COLLECTING_NOT_CONNECTED );
                joinAGame( true );
            }
            else  // We can't join to our game, we close it.
                closeGame();
        }
        else
            JOptionPane.showMessageDialog( mainFrame, "Already created or joined, close game first!", "Error", JOptionPane.ERROR_MESSAGE );
    }

    /** 
     * To handle join menu item and join to a game.
     */
    public void joinAGame() {
        if ( client == null )       // Checking this condition is not neccessary, action of menu causing invoking of this method is possible only when we're not joined
            joinAGame( false );
        else
            JOptionPane.showMessageDialog( mainFrame, "Already joined, close game first!", "Error", JOptionPane.ERROR_MESSAGE );
    }

    /** 
     * Joins to a game.
     * @param joinToOurServer tells wheter we have to join to our server
     */
    public void joinAGame( final boolean joinToOurServer ) {
        try {
            client = new Client( this, mainFrame, clientOptionsManager, joinToOurServer ? serverOptionsManager.getOptions() : null );
            client.start();
            mainFrame.clearMessages();
            mainFrame.setMessageHandler( client );
            mainFrame.getMainMenuBar().setGameState( GameStates.PLAYER_COLLECTING_CONNECTED );
        }
        catch ( final ConnectingToServerFailedException ce ) {
            closeGame();
            JOptionPane.showMessageDialog( mainFrame, new String[] { "Connecting to server failed:", ce.getMessage() }, "Error", JOptionPane.ERROR_MESSAGE );
        }
    }

    /** 
     * To handle start current game menu item and start current game.
     */
    public void startCurrentGame() {
        if ( server != null )         // Checking this condition is not neccessary, action of menu causing invoking of this method is possible only when local server running...
            server.startCurrentGame();
    }

    /** 
     * To handle end current game menu item and end current game.
     */
    public void endCurrentGame() {
        if ( server != null )         // Checking this condition is not neccessary, action of menu causing invoking of this method is possible only when local server running...
            server.endCurrentGame();
    }

    /** 
     * To handle close menu item and close the game.
     */
    public void closeGame() {
        // Client will be shut down soon, no message handler will be available
        mainFrame.setMessageHandler( null );
        // Recommended to shut down the client first.
        // In this case "Server has been shut down" message won't be appeared (and it doesn't have to be either)
        if ( client != null ) {
            client.shutDown();
            client = null;
        }
        if ( server != null ) {
            server.shutDown();
            server = null;
        }
        mainMenuBar.setOurServerRunning( false );
        mainMenuBar.setGameState( GameStates.IDLE );
        setMainComponentHandler( titleAnimationMainComponentHandler );
    }

    /** 
     * To handle exit menu item and exit from the game.
     */
    public void exit() {
        closeGame();
        clientOptionsManager.unregisterOptionsChangeListener( ImageHandler.clientOptionsChangeListener );
        serverOptionsManager.unregisterOptionsChangeListener( serverOptionsChangeListener );
        clientOptionsManager.unregisterOptionsChangeListener( this );
        saveOptions();
        System.exit( 0 );
    }
    
    /** 
     * To handle client options menu item, shows the client options dialog.
     */
    public void showClientOptionsDialog() {
        clientOptionsManager.showOptionsDialog();
    }

    /** 
     * To handle server options menu item, shows the server options dialog.
     */
    public void showServerOptionsDialog() {
        serverOptionsManager.showOptionsDialog();
    }
    
    /** 
     * To handle view global server options menu item, shows the global server options dialog.
     */
    public void showGlobalServerOptionsDialog() {
        if ( client != null )              // Checking this condition is not neccessary, action of menu causing invoking of this method is possible only when we're joined
            client.requestGlobalServerOptions();
    }

    /**
     * To handle Fullscreen window menu item, sets the full screen window status.
     * @param fullScreen true indicates to be in fullscreen mode; false to be in window mode
     */
    public void setFullScreenMode( final boolean fullScreen ) {
        mainFrame.setFullScreenMode( fullScreen );
    }
    
}
