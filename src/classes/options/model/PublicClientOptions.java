
/*
 * Created on July 3, 2004
 */

package classes.options.model;

import classes.options.Consts.PlayerColors;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;
import static classes.options.Consts.MAX_PLAYERS_FROM_A_COMPUTER;

/**
 * This Options class contains the public client options, what
 * will be broadcasted to the other players (for example our name and color).<br>
 * <br>
 * Can be parameterized with the number of players whoose public client options
 * will be stored in an instance of this class. This feature will be used when
 * clients sends their public client options to the server, but the number of
 * players who will play from a host is less than the maximum (this case we don't have
 * and should not send all the public client options of the players).
 *
 * @author Andras Belicza
 */
public class PublicClientOptions extends Options< PublicClientOptions > {

    /** The name of the client.                                   */
    public String         clientName = new String();
    /** The names of the players.                                 */
    public String[]       playerNames;
    /** The colors of the players.                                */
    public PlayerColors[] playerColors;
    /** Sensitivities of the movement corrections of the players. */
    public int[]          movementCorrectionSensitivities;
    
    
    /**
     * Creates a new PublicClientOptions.
     * Calls the other constructor with MAX_PLAYERS_FROM_A_COMPUTER players count
     */
    public PublicClientOptions() {
        this( MAX_PLAYERS_FROM_A_COMPUTER );
    }

    /**
     * Creates a new PublicClientOptions.
     * Creates the playerNames and playerColors arrays and
     * fills the playerNames array with empty strings.
     * @param playersCount the number of players whoose public client options will be stored in this object
     */
    PublicClientOptions( final int playersCount ) {
        playerNames                     = new String      [ playersCount ];
        playerColors                    = new PlayerColors[ playersCount ];
        movementCorrectionSensitivities = new int[ playersCount ];
        
        for ( int i = 0; i < playerNames.length; i++ )
            playerNames[ i ] = new String();
    }

    /**
     * Packs this object to a String so it can be transferred or stored.<br>
     * Simply calls the other packToString() method with the length of the playerNames array
     * and returns the value returned by the other call.
     * @return a compact string representing this public client options
     */
    public String packToString() {
        return packToString( playerNames.length );
    }
    
    /**
     * Packs this object to a String so it can be transferred or stored.
     * @param playersCount tells how many public client options of players must be packed
     * @return a compact string representing this public client options
     */
    public String packToString( final int playersCount ) {
        final StringBuilder buffer = new StringBuilder();

        buffer.append( playersCount ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( clientName   ).append( GENERAL_SEPARATOR_CHAR );
        for ( int i = 0; i < playersCount; i++ )
            buffer.append( playerNames[ i ] ).append( GENERAL_SEPARATOR_CHAR );

        for ( int i = 0; i < playersCount; i++ )
            buffer.append( playerColors[ i ].ordinal() ).append( GENERAL_SEPARATOR_CHAR );

        for ( int i = 0; i < playersCount; i++ )
        	buffer.append( movementCorrectionSensitivities[ i ] ).append( GENERAL_SEPARATOR_CHAR );

        return buffer.toString();
    }
    
    /**
     * Parses a public client options object from a string.
     * @param source the String representing the parsable public client options
     * @return a new PublicClientOptions created from the source string
     */
    public static PublicClientOptions parseFromString( final String source ) {
        final GeneralStringTokenizer optionsTokenizer    = new GeneralStringTokenizer( source );

        final PublicClientOptions    publicClientOptions = new PublicClientOptions( optionsTokenizer.nextIntToken() );

        publicClientOptions.clientName = optionsTokenizer.nextStringToken();

        for ( int i = 0; i < publicClientOptions.playerNames.length; i++ )
            publicClientOptions.playerNames[ i ]        = optionsTokenizer.nextStringToken();

        for ( int i = 0; i < publicClientOptions.playerColors.length; i++ )
            publicClientOptions.playerColors[ i ] = PlayerColors.values()[ optionsTokenizer.nextIntToken() ];

        for ( int i = 0; i < publicClientOptions.movementCorrectionSensitivities.length; i++ )
            publicClientOptions.movementCorrectionSensitivities[ i ] = optionsTokenizer.nextIntToken();
        
        return publicClientOptions;
    }
    
    /**
     * Parses a public client options object from a string.
     * Simply returns the object created by parseFromString().
     * @param source the String representing the parsable public client options
     * @return a new PublicClientOptions created from the source string
     */
    public PublicClientOptions dynamicParseFromString( final String source ) {
        return parseFromString( source );
    }

    /**
     * Tests whether this public client options is equal to the given one.
     * @param publicClientOptions the other public client options to test whether equals to this one
     * @param playersCount        tells how many public client options of players must be compared
     * @return true if the options of this PublicClientOptions instance is equals up to playersCount to the options of the given one 
     */
    public boolean equals( final PublicClientOptions publicClientOptions, final int playersCount ) {
        if ( !clientName.equals( publicClientOptions.clientName ) )
            return false;
        for ( int i = 0; i < playersCount; i++ )
            if ( !playerNames[ i ].equals( publicClientOptions.playerNames[ i ] ) )
                return false;
        for ( int i = 0; i < playersCount; i++ )
            if ( !playerColors[ i ].equals( publicClientOptions.playerColors[ i ] ) )
                return false;
        for ( int i = 0; i < playersCount; i++ )
            if ( movementCorrectionSensitivities[ i ] != publicClientOptions.movementCorrectionSensitivities[ i ] )
                return false;
        
        return true;
    }
    
}
