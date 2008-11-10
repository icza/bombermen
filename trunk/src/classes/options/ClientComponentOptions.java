
/*
 * Created on July 6, 2004
 */

package classes.options;

import javax.swing.*;
import java.awt.*;
import static classes.options.Consts.*;
import static classes.options.Consts.SceneRefreshModes;
import java.awt.event.*;
import classes.utils.GeneralStringTokenizer;
import classes.client.graphics.GraphicsManager;
import classes.client.sound.SoundManager;
import classes.utils.GeneralUtilities;
import classes.utils.ColorIcon;
import classes.options.model.ClientOptions;
import classes.server.Server;

/**
 * This class makes the client options available for viewing and changing
 * on the screen.<br>
 * The manageable client options is passed to the constructor.
 *
 * @author Andras Belicza
 */
public class ClientComponentOptions extends ComponentOptions< ClientOptions > implements ActionListener {

    /** Default value of client name.                          */
    private static final String         DEFAULT_CLIENT_NAME                     = "UNNAMED CLIENT";
    /** Default values of player names.                        */
    private static final String[]       DEFAULT_PLAYER_NAMES                    = new String      [ MAX_PLAYERS_FROM_A_COMPUTER ];
    /** Default values of colors of players.                   */
    private static final PlayerColors[] DEFAULT_PLAYER_COLORS                   = new PlayerColors[ MAX_PLAYERS_FROM_A_COMPUTER ];
    /** Default value of movement sensitivites of the players. */
    private static final int            MINIMUM_MOVEMENT_CORRECTION_SENSITIVITY =   0;
    /** Default value of movement sensitivites of the players. */
    private static final int            DEFAULT_MOVEMENT_CORRECTION_SENSITIVITY =  85;
    /** Default value of movement sensitivites of the players. */
    private static final int            MAXIMUM_MOVEMENT_CORRECTION_SENSITIVITY = 100;

    /*
     * Static initializer to initialize DEFAULT_PLAYER_NAMES and DEFAULT_PLAYER_COLORS fields.
     */
    static {
        for ( int i = 0; i < DEFAULT_PLAYER_NAMES.length; i++ )
            DEFAULT_PLAYER_NAMES[ i ]  = "Player#" + ( i + 1 );
        for ( int i = 0; i < DEFAULT_PLAYER_COLORS.length; i++ )
            DEFAULT_PLAYER_COLORS[ i ] = PlayerColors.values()[ i * 10 % PlayerColors.values().length ];
    }


    /** Default value of server URL option.            */
    private static final String                 DEFAULT_SERVER_URL              = "";
    /** Minimum value of players from host option.     */
    private static final int                    MINIMUM_PLAYERS_FROM_HOST       = 0;
    /** Default value of players from host option.     */
    private static final int                    DEFAULT_PLAYERS_FROM_HOST       = 1;
    /** Maximum value of players from host option.     */
    private static final int                    MAXIMUM_PLAYERS_FROM_HOST       = MAX_PLAYERS_FROM_A_COMPUTER;
    /** Default value of password option.              */
    private static final String                 DEFAULT_PASSWORD                = "";

    /** Default values of players control keys.        */
    private static final int[][]                DEFAULT_PLAYERS_CONTROL_KEYS    = {
        { KeyEvent.VK_UP      , KeyEvent.VK_DOWN   , KeyEvent.VK_RIGHT  , KeyEvent.VK_LEFT   , KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT    },
        { KeyEvent.VK_R       , KeyEvent.VK_F      , KeyEvent.VK_G      , KeyEvent.VK_D      , KeyEvent.VK_A      , KeyEvent.VK_Q        },
        { KeyEvent.VK_NUMPAD8 , KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD4, KeyEvent.VK_ADD    , KeyEvent.VK_SUBTRACT },
        { KeyEvent.VK_I       , KeyEvent.VK_K      , KeyEvent.VK_L      , KeyEvent.VK_J      , KeyEvent.VK_H      , KeyEvent.VK_Y        }
    };

    /** Default value of graphical theme option.       */
    private static final String                 DEFAULT_GRAPHICAL_THEME         = "test";
    /** Default value of sound theme option.           */
    private static final String                 DEFAULT_SOUND_THEME             = "<not given yet>";  // TODO: ...


    /** Default value of show player names option.     */
    private static final boolean                DEFAULT_SHOW_PLAYER_NAMES       = true;
    /** Default value of show bombermen lives option.  */
    private static final boolean                DEFAULT_SHOW_BOMBERMEN_LIVES    = true;
    
	/** Default value of scene refresh mode.           */
    private static final SceneRefreshModes      DEFAULT_SCENE_REFRESH_MODE      = SceneRefreshModes.NORMAL;
	/** Default value of image scaling algorithm.      */
    private static final ImageScalingAlgorithms DEFAULT_IMAGE_SCALING_ALGORITHM = ImageScalingAlgorithms.FAST;
    
    // Port constants are imported from classes.options.Consts!


    /** Component for client name option.                                    */
    private final JTextField   clientName_c                       = new JTextField( 15 );
    /** Components for the names of the players options.                     */
    private final JTextField[] playerNames_cs                     = new JTextField[ MAX_PLAYERS_FROM_A_COMPUTER ];
    /** Components for the colors of the players options.                    */
    private final JComboBox[]  playerColors_cs                 = new JComboBox [ MAX_PLAYERS_FROM_A_COMPUTER ];
    /** Components for the movement correction sensitivities of the players. */
    private final JSpinner[]   movementCorrectionSensitivities_cs = new JSpinner  [ MAX_PLAYERS_FROM_A_COMPUTER ];

    /** Component for server URL option.                                     */
    private final JTextField   serverURL_c                        = new JTextField( 15 );
    /** Component for players from host option.                              */
    private final JSpinner     playersFromHost_c                  = new JSpinner  ( new SpinnerNumberModel( DEFAULT_PLAYERS_FROM_HOST, MINIMUM_PLAYERS_FROM_HOST, MAXIMUM_PLAYERS_FROM_HOST, 1 ) );
    /** Component for password option.                                       */
    private final JTextField   password_c                         = new JTextField( 15 );    
    
    /** Components for players control keys options.                         */
    private final JButton[][]  playersControlKeys_cs              = new JButton[ MAX_PLAYERS_FROM_A_COMPUTER ][ PlayerControlKeys.values().length ];
    /** Values of components for players control keys.                       */
    private final int[][]      playersControlKeys_cvalues         = new int    [ MAX_PLAYERS_FROM_A_COMPUTER ][ PlayerControlKeys.values().length ];
    
    /** Component for graphical theme option.                                */
    private final JComboBox    graphicalTheme_c                   = new JComboBox( GraphicsManager.getAvailableGraphicalThemes() );
    /** Component for sound theme option.                                    */
    private final JComboBox    soundTheme_c                       = new JComboBox( SoundManager.getAvailableSoundThemes() );

    /** Component for show player names option.                              */
    private final JCheckBox    showPlayerNames_c                  = new JCheckBox( "Show player names" );
    /** Component for show bombermen lives option.                           */
    private final JCheckBox    showBombermenLives_c               = new JCheckBox( "Show bombermen lives" );
    
	/** Component for screen refresh rate option.                            */
    private final JComboBox    sceneRefreshMode_c                 = new JComboBox( SceneRefreshModes     .values() );
	/** Component for image scaling algorithm option.                        */
    private final JComboBox    imageScalingAlgorithm_c            = new JComboBox( ImageScalingAlgorithms.values() );
    
    /** Component for game port option.                                      */
    private final JSpinner     gamePort_c                         = new JSpinner ( new SpinnerNumberModel( DEFAULT_GAME_PORT, MINIMUM_GAME_PORT, MAXIMUM_GAME_PORT, 1 ) );



    /**
     * Creates a new ClientComponentOptions.
     * @param clientOptions the client options object to be handled
     */
    public ClientComponentOptions( final ClientOptions clientOptions ) {
        super( clientOptions );

        for ( int i = 0; i < playerNames_cs.length; i++ )
            playerNames_cs[ i ]  = new JTextField( 10 );
        
        final ListCellRenderer listCellRenderer = new DefaultListCellRenderer() { // We want color icons beside the names of colors
            public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus ) {
                final Component component = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
                if ( component instanceof JLabel )
                    ( (JLabel) component ).setIcon( new ColorIcon( ( (PlayerColors) value ).value ) );
                return component;
            }
        };
        for ( int i = 0; i < playerColors_cs.length; i++ ) {
            playerColors_cs[ i ] = new JComboBox( PlayerColors.values() );
            playerColors_cs[ i ].setRenderer( listCellRenderer );
        }
        
        for ( int i = 0; i < movementCorrectionSensitivities_cs.length; i++ )
        	movementCorrectionSensitivities_cs[ i ] = new JSpinner(  new SpinnerNumberModel( DEFAULT_MOVEMENT_CORRECTION_SENSITIVITY, MINIMUM_MOVEMENT_CORRECTION_SENSITIVITY, MAXIMUM_MOVEMENT_CORRECTION_SENSITIVITY, 1 ) );

        for ( int i = 0; i < playersControlKeys_cs.length; i++ )
            for ( int j = 0; j < playersControlKeys_cs[ i ].length; j++ ) {
                playersControlKeys_cs[ i ][ j ] = new JButton();
                playersControlKeys_cs[ i ][ j ].addActionListener( this );
            }
        
        buildOptionsTabbedPane();
    }

    /**
     * Builds the options tabbed pane.
     */
    private void buildOptionsTabbedPane() {
        JPanel panel;
        Box    box;
        
        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( new JLabel( "Server URL:" ) );
            panel.add( serverURL_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( new JLabel( "Name of client:" ) );
            panel.add( clientName_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( new JLabel( "Players from host:" ) );
            playersFromHost_c.setToolTipText( "0 means not to play but to observe the game." );
            panel.add( playersFromHost_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( new JLabel( "Password:" ) );
            panel.add( password_c );
        box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Main", panel );
        
        for ( int i = 0; i < MAX_PLAYERS_FROM_A_COMPUTER; i++ ) {
            box = Box.createVerticalBox();
                panel = new JPanel();
                panel.add( new JLabel( "Player name:" ) );
                panel.add( playerNames_cs[ i ] );
            box.add( panel );
                panel = new JPanel();
                panel.add( new JLabel( "Player color:" ) );
                panel.add( playerColors_cs[ i ] );
            box.add( panel );
	            panel = new JPanel();
	            panel.add( new JLabel( "Movement correction sensitivity:" ) );
	            panel.add( movementCorrectionSensitivities_cs[ i ] );
	            panel.add( new JLabel( "%." ) );
	        box.add( panel );
            for ( final PlayerControlKeys playerControlKey : PlayerControlKeys.values() ) {
                    panel = new JPanel();
                    panel.add( new JLabel( "Key for " + playerControlKey + ":" ) );
                    panel.add( playersControlKeys_cs[ i ][ playerControlKey.ordinal() ] );
                box.add( panel );
            }
            panel = new JPanel();
            panel.add( box );
            optionsTabbedPane.addTab( "Player " + ( i + 1 ), panel );
        }

        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( new JLabel( "Graphical theme:" ) );
            panel.add( graphicalTheme_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( new JLabel( "Sound theme:" ) );
            panel.add( soundTheme_c );
        box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Themes", panel );

        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( showPlayerNames_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( showBombermenLives_c );
        box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "What to show", panel );

        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( new JLabel( "Scene refresh mode:" ) );
            sceneRefreshMode_c.setToolTipText( "Determines how frequently the game scene will be redrawn." );
			panel.add( sceneRefreshMode_c );
        box.add( panel );
	        panel = new JPanel();
	        panel.add( new JLabel( "Image scaling algorithm:" ) );
			panel.add( imageScalingAlgorithm_c );
	    box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Graphics", panel );

        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( new JLabel( "Game port:" ) );
            gamePort_c.setToolTipText( "Must be equal to the value set by the server!" );
            panel.add( gamePort_c );
        box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Extra", panel );
    }
    

    /**
     * Restores the default values of the client options to the option components.
     */
    public void restoreDefaultValuesToComponents() {
        clientName_c           .setText        ( GeneralStringTokenizer.checkString( DEFAULT_CLIENT_NAME ) );
        for ( int i = 0; i < playerNames_cs.length; i++ )
            playerNames_cs[ i ].setText( GeneralStringTokenizer.checkString( DEFAULT_PLAYER_NAMES[ i ] ) );
        for ( int i = 0; i < playerColors_cs.length; i++ )
            playerColors_cs[ i ].setSelectedItem( DEFAULT_PLAYER_COLORS[ i ] );
        for ( int i = 0; i < movementCorrectionSensitivities_cs.length; i++ )
        	movementCorrectionSensitivities_cs[ i ].setValue( DEFAULT_MOVEMENT_CORRECTION_SENSITIVITY );

        serverURL_c            .setText        ( GeneralStringTokenizer.checkString( DEFAULT_SERVER_URL ) );
        playersFromHost_c      .setValue       ( DEFAULT_PLAYERS_FROM_HOST );
        password_c             .setText        ( GeneralStringTokenizer.checkString( DEFAULT_PASSWORD ) );

        for ( int i = 0; i < playersControlKeys_cs.length; i++ )
            for ( int j = 0; j < playersControlKeys_cs[ i ].length; j++ ) {
                playersControlKeys_cs[ i ][ j ].setText( KeyEvent.getKeyText( DEFAULT_PLAYERS_CONTROL_KEYS[ i ][ j ] ) );
                playersControlKeys_cvalues[ i ][ j ] = DEFAULT_PLAYERS_CONTROL_KEYS[ i ][ j ];
            }
        
        graphicalTheme_c       .setSelectedItem( DEFAULT_GRAPHICAL_THEME );
        soundTheme_c           .setSelectedItem( DEFAULT_SOUND_THEME );
        
        showPlayerNames_c      .setSelected    ( DEFAULT_SHOW_PLAYER_NAMES );
        showBombermenLives_c   .setSelected    ( DEFAULT_SHOW_BOMBERMEN_LIVES );
		
		sceneRefreshMode_c     .setSelectedItem( DEFAULT_SCENE_REFRESH_MODE );
		imageScalingAlgorithm_c.setSelectedItem( DEFAULT_IMAGE_SCALING_ALGORITHM );

        gamePort_c             .setValue       ( DEFAULT_GAME_PORT );
    }
    
    /**
     * Creates a new client options object, stores into that the actual states of the option components,
     * and returns it.
     * @return the ClientOptions object holding the values/states of the option components
     */
    public ClientOptions getOptionsFromComponents() {
        final ClientOptions clientOptions = new ClientOptions();

        clientOptions.publicClientOptions.clientName = GeneralStringTokenizer.checkString( clientName_c.getText() );
        // Client name cannot equal to the chat name of the server...
        if ( clientOptions.publicClientOptions.clientName.equalsIgnoreCase( Server.BASE_SERVER_CHAT_NAME ) )
            clientOptions.publicClientOptions.clientName = GeneralStringTokenizer.checkString( '_' + clientOptions.publicClientOptions.clientName );
        for ( int i = 0; i < clientOptions.publicClientOptions.playerNames.length; i++ )
            clientOptions.publicClientOptions.playerNames[ i ] = GeneralStringTokenizer.checkString( playerNames_cs[ i ].getText() );
        for ( int i = 0; i < clientOptions.publicClientOptions.playerColors.length; i++ )
            clientOptions.publicClientOptions.playerColors[ i ] = PlayerColors.values()[ playerColors_cs[ i ].getSelectedIndex() ];
        for ( int i = 0; i < clientOptions.publicClientOptions.movementCorrectionSensitivities.length; i++ )
            clientOptions.publicClientOptions.movementCorrectionSensitivities[ i ] = (Integer) movementCorrectionSensitivities_cs[ i ].getValue();

        clientOptions.serverURL             = GeneralStringTokenizer.checkString( serverURL_c.getText() );
        clientOptions.playersFromHost       = (Integer) playersFromHost_c.getValue();
        clientOptions.password              = GeneralStringTokenizer.checkString( password_c.getText() );

        for ( int i = 0; i < clientOptions.playersControlKeys.length; i++ )
            for ( int j = 0; j < clientOptions.playersControlKeys[ i ].length; j++ )
                clientOptions.playersControlKeys[ i ][ j ] = playersControlKeys_cvalues[ i ][ j ];

        clientOptions.graphicalTheme        = (String) graphicalTheme_c.getSelectedItem();
        clientOptions.soundTheme            = (String) soundTheme_c    .getSelectedItem();
        
        clientOptions.showPlayerNames       = showPlayerNames_c   .isSelected();
        clientOptions.showBombermenLives    = showBombermenLives_c.isSelected();
        
		clientOptions.sceneRefreshMode      = (SceneRefreshModes)      sceneRefreshMode_c.getSelectedItem();
		clientOptions.imageScalingAlgorithm = (ImageScalingAlgorithms) imageScalingAlgorithm_c.getSelectedItem();

        clientOptions.gamePort              = (Integer) gamePort_c.getValue();

        return clientOptions;
    }
    
    /**
     * Stores the values of the client option attributes to the appropriate components.
     */
    public void synchronizeComponentsToOptions() {
        clientName_c           .setText        ( options.publicClientOptions.clientName );
        for ( int i = 0; i < playerNames_cs.length; i++ )
            playerNames_cs[ i ].setText        ( options.publicClientOptions.playerNames[ i ] );
        for ( int i = 0; i < playerColors_cs.length; i++ )
            playerColors_cs[ i ].setSelectedIndex( options.publicClientOptions.playerColors[ i ].ordinal() );
        for ( int i = 0; i < movementCorrectionSensitivities_cs.length; i++ )
            movementCorrectionSensitivities_cs[ i ].setValue( options.publicClientOptions.movementCorrectionSensitivities[ i ] );

        serverURL_c            .setText        ( options.serverURL             );
        playersFromHost_c      .setValue       ( options.playersFromHost       );
        password_c             .setText        ( options.password              );

        for ( int i = 0; i < playersControlKeys_cs.length; i++ )
            for ( int j = 0; j < playersControlKeys_cs[ i ].length; j++ ) {
                playersControlKeys_cs[ i ][ j ].setText( KeyEvent.getKeyText( options.playersControlKeys[ i ][ j ] ) );
                playersControlKeys_cvalues[ i ][ j ] = options.playersControlKeys[ i ][ j ];
            }

        graphicalTheme_c       .setSelectedItem( options.graphicalTheme        );
        soundTheme_c           .setSelectedItem( options.soundTheme            );
        
        showPlayerNames_c      .setSelected    ( options.showPlayerNames       );
        showBombermenLives_c   .setSelected    ( options.showBombermenLives    );

		sceneRefreshMode_c     .setSelectedItem( options.sceneRefreshMode      );
		imageScalingAlgorithm_c.setSelectedItem( options.imageScalingAlgorithm );

        gamePort_c             .setValue       ( options.gamePort              );
    }

    /**
     * Handles the action events of the button components of players control keys.
     * Makes a new dialog, and handles pressed key as the new key for the selected action.
     * @param ae details of the action event
     */
    public void actionPerformed( final ActionEvent ae ) {
        final Object source = ae.getSource();
        int i = 0, j = 0;
        
        outerCycle:
        for ( i = 0; i < playersControlKeys_cs.length; i++ )
            for ( j = 0; j < playersControlKeys_cs[ i ].length; j++ )
                if ( playersControlKeys_cs[ i ][ j ] == source )
                    break outerCycle;

        
        final Container parent       = ( (JComponent) source ).getTopLevelAncestor();
        final JDialog   newKeyDialog = parent instanceof Frame ? new JDialog( (Frame ) parent, "Define new key", true )
                                                               : new JDialog( (Dialog) parent, "Define new key", true );
        final JButton messageButton = new JButton( "Press a key for the selected action..."  );
        final int final_i = i, final_j = j;   // Final variables for the inner class
        messageButton.addKeyListener( new KeyAdapter() {
            public void keyPressed( final KeyEvent ke ) {
                playersControlKeys_cs[ final_i ][ final_j ].setText( KeyEvent.getKeyText( ke.getKeyCode() ) );
                playersControlKeys_cvalues[ final_i ][ final_j ] = ke.getKeyCode();
                newKeyDialog.dispose();
            }
        } );
        newKeyDialog.getContentPane().add( messageButton, BorderLayout.CENTER );
        newKeyDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        newKeyDialog.pack();
        
        Container parentContainer = optionsTabbedPane.getParent();
        while ( !( parentContainer instanceof Window ) )
            parentContainer = parentContainer.getParent();
        
        GeneralUtilities.centerWindowToWindow( newKeyDialog, (Window) parentContainer );
        newKeyDialog.setVisible( true );
    }
    
}
