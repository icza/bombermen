
/*
 * Created on July 3, 2004
 */

package classes.options.model;

import classes.options.Consts.ImageScalingAlgorithms;
import classes.options.Consts.PlayerControlKeys;
import static classes.options.Consts.MAX_PLAYERS_FROM_A_COMPUTER;
import static classes.options.Consts.SceneRefreshModes;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;

/**
 * Holds all the client options; contains a reference to the public client options.
 *
 * @author Andras Belicza
 */
public class ClientOptions extends Options< ClientOptions > {
    
    /** A reference to the public client options.               */
    public PublicClientOptions    publicClientOptions = new PublicClientOptions();

    /** IP address or URL of the server.                        */
    public String                 serverURL           = new String();
    /** Number of players playing from this host.               */
    public int                    playersFromHost;
    /** Password to use for password protected games.           */
    public String                 password            = new String();

    /** The control keys of the players.                        */
    public int[][]                playersControlKeys  = new int[ MAX_PLAYERS_FROM_A_COMPUTER ][ PlayerControlKeys.values().length ];

    /** 
     * The graphical theme.                         
     * We store theme by name (not by index),
     * because the list of themes can change during the game
     */
    public String                 graphicalTheme      = new String();
    /** The sound theme. (for note, see grapicalTheme)          */
    public String                 soundTheme          = new String();

    /** Tells whether we have to show the names of the players. */
    public boolean                showPlayerNames;
    /** Tells whether we have to show the lives of bombermen.   */
    public boolean                showBombermenLives;
    
	/** Screen refresh mode.                                    */
    public SceneRefreshModes      sceneRefreshMode;
	/** Image scaling algorithm.                                */
    public ImageScalingAlgorithms imageScalingAlgorithm;

    /** Game server port.                                       */
    public int                    gamePort;

    /**
     * Packs this object to a String so it can be transferred or stored.
     * @return a compact string representing this client options
     */
    public String packToString() {
        final StringBuilder buffer = new StringBuilder();

        buffer.append( serverURL                       ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( playersFromHost                 ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( password                        ).append( GENERAL_SEPARATOR_CHAR );

        for ( final int[] playerControlKeys : playersControlKeys )
            for ( final int playerControlKey : playerControlKeys ) {
                buffer.append( playerControlKey        ).append( GENERAL_SEPARATOR_CHAR );
            }

        buffer.append( graphicalTheme                  ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( soundTheme                      ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( showPlayerNames                 ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( showBombermenLives              ).append( GENERAL_SEPARATOR_CHAR );
        
		buffer.append( sceneRefreshMode     .ordinal() ).append( GENERAL_SEPARATOR_CHAR );
		buffer.append( imageScalingAlgorithm.ordinal() ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( gamePort                        ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( publicClientOptions.packToString() );   // This ends with GENERAL_SEPARATOR_CHAR

        return buffer.toString();
    }
    
    /**
     * Parses a client options object from a string.
     * @param source the String representing the parsable client options
     * @return a new ClientOptions created from the source string
     */
    public static ClientOptions parseFromString( final String source ) {
        final ClientOptions          clientOptions    = new ClientOptions();
        final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer( source );

        clientOptions.serverURL             = optionsTokenizer.nextStringToken();
        clientOptions.playersFromHost       = optionsTokenizer.nextIntToken();
        clientOptions.password              = optionsTokenizer.nextStringToken();

        for ( int i = 0; i < clientOptions.playersControlKeys.length; i++ )
            for ( int j = 0; j < clientOptions.playersControlKeys[ i ].length; j++ )
                clientOptions.playersControlKeys[ i ][ j ] = optionsTokenizer.nextIntToken();

        clientOptions.graphicalTheme        = optionsTokenizer.nextStringToken();
        clientOptions.soundTheme            = optionsTokenizer.nextStringToken();
        clientOptions.showPlayerNames       = optionsTokenizer.nextBooleanToken();
        clientOptions.showBombermenLives    = optionsTokenizer.nextBooleanToken();

        clientOptions.sceneRefreshMode      = SceneRefreshModes     .values()[ optionsTokenizer.nextIntToken() ];
        clientOptions.imageScalingAlgorithm = ImageScalingAlgorithms.values()[ optionsTokenizer.nextIntToken() ];

        clientOptions.gamePort              = optionsTokenizer.nextIntToken();

        clientOptions.publicClientOptions   = PublicClientOptions.parseFromString( optionsTokenizer.remainingString() );
        
        return clientOptions;
    }

    /**
     * Parses a client options object from a string.
     * Simply returns the object created by parseFromString().
     * @param source the String representing the parsable client options
     * @return a new ClientOptions created from the source string
     */
    public ClientOptions dynamicParseFromString( final String source ) {
        return parseFromString( source );
    }

}
