
/*
 * Created on July 5, 2004
 */

package classes.options.model;

import classes.options.Consts.GameTypes;
import classes.options.Consts.KillsBelongTos;
import classes.options.Consts.NetworkLatencies;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;

/**
 * Holds all the server options; contains a reference to a level options object.
 *
 * @author Andras Belicza
 */
public class ServerOptions extends Options< ServerOptions > {

    /** Level options for randomly generated levels.                                */
    public LevelOptions     levelOptions = new LevelOptions();

    /** Which level to play on. This is the name of the level file without extension.
     * Empty string indicates random level                                          */
    public String           levelName    = new String();
    /** Type of the game.                                                           */
    public GameTypes        gameType     = GameTypes.values()[ 0 ];
    /** Round time limit in seconds.                                                */
    public int              roundTimeLimit;
    /** Game point limit. Reaching this means end of game.                          */
    public int              gamePointLimit;
    /** Password for password protected games.                                      */
    public String           password     = new String();

    /** Damage of the whole fire of a bomb in percent. 100 % means to kill exactly a healthy bomberman.             */
    public int              damageOfWholeBombFire;
    /** Tells whether explosion ahhihilates diseases when reaches them. (If not, they'll be replaced on the level.) */
    public boolean          explosionAnnihilatesDiseases;
    /** Tells whether fire doesn't hurt teammates.                                                                  */
    public boolean          fireDoesntHurtTeammates;
    /** Tells whether we have to determine new random positions after each round.                                   */
    public boolean          newRandomPositionsAfterRounds;
    /** Tells whether bombs should explode after one remained (team or player).                                     */
    public boolean          bombsExplodeAfterOneRemained;
    /** Tells whether building up walls should stop after one remained (team or player).                            */
    public boolean          buildingUpWallsStopsAfterOneRemained;
    /** Tells whether items stop rolling bombs (if false, items disappear when bombs roll over them).               */
    public boolean          itemsStopRollingBombs;
    /** Tells whether punched or thrown away bombs come back at the opposite end of the level.                      */
    public boolean          punchedBombsComeBackAtTheOppositeEnd;
    /** Tells whether fire damaging to players is multiple in case of multiple fire.                                */
    public boolean          multipleFire;
    /** Tells who gets the killing points when bombermen die.                                                       */
    public KillsBelongTos   killsBelongTo = KillsBelongTos.values()[ 0 ];
    
    /** Amount of brick walls in percent of non-concrete walls.                     */
    public int              amountOfBrickWalls;
    /** Probability of getting itme when a wall brick has been exploded.            */
    public int              gettingItemProbability;
    
    /** Game cycle frequency in 1/s (Hz).                                           */
    public int              gameCycleFrequency;
    /** Game server port.                                                           */
    public int              gamePort;
	/** The network latency.                                                        */
	public NetworkLatencies networkLatency;

    /**
     * Packs this object to a String so it can be transferred or stored.
     * Enums are packed by their ordinals.
     * @return a compact string representing this server options
     */
    public String packToString() {
        final StringBuilder buffer = new StringBuilder();
        
        buffer.append( levelName                            ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( gameType.ordinal()                   ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( roundTimeLimit                       ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( gamePointLimit                       ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( password                             ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( damageOfWholeBombFire                ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( explosionAnnihilatesDiseases         ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( fireDoesntHurtTeammates              ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( newRandomPositionsAfterRounds        ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( bombsExplodeAfterOneRemained         ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( buildingUpWallsStopsAfterOneRemained ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( itemsStopRollingBombs                ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( punchedBombsComeBackAtTheOppositeEnd ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( multipleFire                         ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( killsBelongTo.ordinal()              ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( amountOfBrickWalls                   ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( gettingItemProbability               ).append( GENERAL_SEPARATOR_CHAR );
        
        buffer.append( gameCycleFrequency                   ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( gamePort                             ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( networkLatency.ordinal()             ).append( GENERAL_SEPARATOR_CHAR );

        buffer.append( levelOptions.packToString() );       // This ends with GENERAL_SEPARATOR_CHAR
        
        return buffer.toString();
    }
    
    /**
     * Parses a server options object from a string.
     * @param source the String representing the parsable server options
     * @return a new ServerOptions created from the source string
     */
    public static ServerOptions parseFromString( final String source ) {
        final ServerOptions          serverOptions    = new ServerOptions();
        final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer( source );

        serverOptions.levelName                            = optionsTokenizer.nextStringToken();
        serverOptions.gameType                             = GameTypes.values()[ optionsTokenizer.nextIntToken() ];
        serverOptions.roundTimeLimit                       = optionsTokenizer.nextIntToken();
        serverOptions.gamePointLimit                       = optionsTokenizer.nextIntToken();
        serverOptions.password                             = optionsTokenizer.nextStringToken();

        serverOptions.damageOfWholeBombFire                = optionsTokenizer.nextIntToken();
        serverOptions.explosionAnnihilatesDiseases         = optionsTokenizer.nextBooleanToken();
        serverOptions.fireDoesntHurtTeammates              = optionsTokenizer.nextBooleanToken();
        serverOptions.newRandomPositionsAfterRounds        = optionsTokenizer.nextBooleanToken();
        serverOptions.bombsExplodeAfterOneRemained         = optionsTokenizer.nextBooleanToken();
        serverOptions.buildingUpWallsStopsAfterOneRemained = optionsTokenizer.nextBooleanToken();
        serverOptions.itemsStopRollingBombs                = optionsTokenizer.nextBooleanToken();
        serverOptions.punchedBombsComeBackAtTheOppositeEnd = optionsTokenizer.nextBooleanToken();
        serverOptions.multipleFire                         = optionsTokenizer.nextBooleanToken();
        serverOptions.killsBelongTo                        = KillsBelongTos.values()[ optionsTokenizer.nextIntToken() ];

        serverOptions.amountOfBrickWalls                   = optionsTokenizer.nextIntToken();
        serverOptions.gettingItemProbability               = optionsTokenizer.nextIntToken();
        
        serverOptions.gameCycleFrequency                   = optionsTokenizer.nextIntToken();
        serverOptions.gamePort                             = optionsTokenizer.nextIntToken();
        serverOptions.networkLatency                       = NetworkLatencies.values()[ optionsTokenizer.nextIntToken() ];

        serverOptions.levelOptions                         = LevelOptions.parseFromString( optionsTokenizer.remainingString() );

        return serverOptions;
    }

    /**
     * Parses a server options object from a string.
     * Simply returns the object created by parseFromString().
     * @param source the String representing the parsable server options
     * @return a new ServerOptions created from the source string
     */
    public ServerOptions dynamicParseFromString( final String source ) {
        return parseFromString( source );
    }
    
}
