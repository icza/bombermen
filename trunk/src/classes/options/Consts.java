
/*
 * Created on July 4, 2004
 */

package classes.options;

import java.util.EnumMap;
import java.util.EnumSet;
import java.awt.Color;

/**
 * Holds constants needed for the options and can be public.
 *
 * @author Andras Belicza
 */
public class Consts {

    /** Maximum how many players can play from a computer (from a game/client/node). */
    public static final int MAX_PLAYERS_FROM_A_COMPUTER = 4;
    /** Minimum value of game port.                                                  */
    public static final int MINIMUM_GAME_PORT           = 0;
    /** Default value of game port.                                                  */
    public static final int DEFAULT_GAME_PORT           = 43762;
    /** Maximum value of game port.                                                  */
    public static final int MAXIMUM_GAME_PORT           = 65535;
    
    
    /** The available player colors in the game. */
    public enum PlayerColors {
        /** The Black player color. */
        BLACK     ( "Black"          , new Color(   0,   0,   0 ) ),
        /** The Navy player color. */
        NAVY      ( "Navy"           , new Color(   0,   0, 128 ) ),
        /** The Blue player color. */
        BLUE      ( "Blue"           , new Color(   0,   0, 255 ) ),
        /** The Royal Blue player color. */
        ROLAY_BLUE( "Royal Blue"     , new Color(  65, 105, 225 ) ),
        /** The Teal player color. */
        TEAL      ( "Teal"           , new Color(   0, 128, 128 ) ),
        /** The Cyan/Aqua player color. */
        CYAN      ( "Cyan/Aqua"      , new Color(   0, 255, 255 ) ),
        /** The Lime player color. */
        LIME      ( "Lime"           , new Color(   0, 255,   0 ) ),
        /** The Green player color. */
        GREEN     ( "Green"          , new Color(   0, 128,   0 ) ),
        /** The Red player color. */
        RED       ( "Red"            , new Color( 255,   0,   0 ) ),
        /** The Tomato player color. */
        TOMATO    ( "Tomato"         , new Color( 255,  99,  71 ) ),
        /** The Maroon player color. */
        MAROON    ( "Maroon"         , new Color( 128,   0,   0 ) ),
        /** The Brown player color. */
        BROWN     ( "Brown"          , new Color( 165,  42,  42 ) ),
        /** The Purple player color. */
        PURPLE    ( "Purple"         , new Color( 128,   0, 128 ) ),
        /** The Fuchsia player color. */
        MAGENTA   ( "Magenta/Fuchsia", new Color( 255,   0, 255 ) ),
        /** The Violet player color. */
        VIOLET    ( "Violet"         , new Color( 233, 130, 233 ) ),
        /** The Pink player color. */
        PINK      ( "Pink"           , new Color( 255, 192, 203 ) ),
        /** The Olive player color. */
        OLIVE     ( "Olive"          , new Color( 128, 128,   0 ) ),
        /** The Orange player color. */
        ORANGE    ( "Orange"         , new Color( 255, 165,   0 ) ),
        /** The Gold player color. */
        GOLD      ( "Gold"           , new Color( 255, 215,   0 ) ),
        /** The Yellow player color. */
        YELLOW    ( "Yellow"         , new Color( 255, 255,   0 ) ),
        /** The White player color. */
        WHITE     ( "White"          , new Color( 255, 255, 255 ) ),
        /** The Silver player color. */
        SILVER    ( "Silver"         , new Color( 192, 192, 192 ) ),
        /** The Gray player color. */
        GRAY      ( "Gray"           , new Color( 128, 128, 128 ) );
        
        /** Name of the player color.  */
        public final String name;
        /** Value of the player color. */
        public final Color  value;
        
        /**
         * Creates a new PlayerColors.
         * @param name  name of the player color
         * @param value value of the player color
         */
        private PlayerColors( final String name, final Color value ) {
        	this.name  = name;
        	this.value = value;
        }
        
        /**
         * Overrides toString() method for displaying the names of the colors
         * in the color choosing list in client options dialog.
         * @return the name of the color
         */
        public String toString() {
        	return name;
        }
        
    }

    /**
     * Keys which can be used to control a bomberman.
     * @author Andras Belicza
     */
    public enum PlayerControlKeys {
        /** Control key to move bomberman up.                       */
        UP,
        /** Control key to move bomberman up.                       */
        DOWN,
        /** Control key to move bomberman right.                    */
        RIGHT,
        /** Control key to move bomberman left.                     */
        LEFT,
        /** Control key to function 1 (place bombs for example).    */
        FUNCTION1,
        /** Control key to function 2 (detonate bombs for example). */
        FUNCTION2
    }
    
    /**
     * The available items in the game.
     * @author Andras Belicza
     */
    public enum Items {
        /** The Fire item.                                         */
        FIRE          { public String toString() { return "Fire"         ; } },
        /** The Super fire item.                                   */
        SUPER_FIRE    { public String toString() { return "Super fire"   ; } },
        /** The Heart item.                                        */
        HEART         { public String toString() { return "Heart"        ; } },
        /** The Bomb item.                                         */
        BOMB          { public String toString() { return "Bomb"         ; } },
        /** The Roller-skates item.                                */
        ROLLER_SKATES { public String toString() { return "Roller skates"; } },
        /** The Jelly item.                                        */
        JELLY         { public String toString() { return "Jelly"        ; } },
        /** The Boots item.                                        */
        BOOTS         { public String toString() { return "Boots"        ; } },
        /** The Blue Gloves item.                                  */
        BLUE_GLOVES   { public String toString() { return "Blue gloves"  ; } },
        /** The Boxing Gloves item.                                */
        BOXING_GLOVES { public String toString() { return "Boxing gloves"; } },
        /** The Bomb sprinkle item.                                */
        BOMB_SPRINKLE { public String toString() { return "Bomb sprinkle"; } },
        /** The Trigger item.                                      */
        TRIGGER       { public String toString() { return "Trigger"      ; } },
        /** The Wall climber item.                                 */
        WALL_CLIMBING { public String toString() { return "Wall climbing"; } },
        /** The Spider bomb item.                                  */
        SPIDER_BOMB   { public String toString() { return "Spider bomb"  ; } },
        /** The Disease item.                                      */
        DISEASE       { public String toString() { return "Disease"      ; } },
        /** The Super disease item.                                */
        SUPER_DISEASE { public String toString() { return "Super disease"; } },
        /** The Wall building item.                                */
        WALL_BUILDING { public String toString() { return "Wall building"; } }
    }
    
    /**
     * The walls of the level.
     * @author Andras Belicza
     */
    public enum Walls {
        /** Empty wall.    */
        EMPTY,
        /** Concrete wall. */
        CONCRETE,
        /** Brick wall.    */
        BRICK
    }
    
    /** Items from which we can pick up more than one, we can accumulate them. */
    public static final EnumSet< Items > ACCUMULATEABLE_ITEMS = EnumSet.of( Items.BOMB, Items.FIRE, Items.ROLLER_SKATES );

    /** 
     * The Map of items which neutralize others.
     * The key object is the neutralizer item, 
     * the value object is the set of the neutralized items.
     */
    public static final EnumMap< Items, EnumSet< Items > > NEUTRALIZER_ITEMS_MAP =
        new EnumMap< Items, EnumSet< Items > >( Items.class );
        
    /**
     * We initialize the NEUTRALIZER_ITEMS_MAP constant.
     */
    static {
        // Blue Gloves neutralizes Bomb sprinkle
        NEUTRALIZER_ITEMS_MAP.put( Items.BLUE_GLOVES  , EnumSet.of( Items.BOMB_SPRINKLE ) );
        // Bomb sprinkle neutralizes Blue Gloves
        NEUTRALIZER_ITEMS_MAP.put( Items.BOMB_SPRINKLE, EnumSet.of( Items.BLUE_GLOVES ) );
        // Trigger neutralizes Boxing Gloves, Wall building and Jelly
        NEUTRALIZER_ITEMS_MAP.put( Items.TRIGGER      , EnumSet.of( Items.BOXING_GLOVES, Items.WALL_BUILDING, Items.JELLY ) );
        // Boxing Gloves neutralizes Trigger and Wall building
        NEUTRALIZER_ITEMS_MAP.put( Items.BOXING_GLOVES, EnumSet.of( Items.TRIGGER, Items.WALL_BUILDING ) );
        // Wall building neutralizes Trigger and Boxing Gloves
        NEUTRALIZER_ITEMS_MAP.put( Items.WALL_BUILDING, EnumSet.of( Items.TRIGGER, Items.BOXING_GLOVES ) );
        // Jelly neutralizes Trigger
        NEUTRALIZER_ITEMS_MAP.put( Items.JELLY        , EnumSet.of( Items.TRIGGER ) );
    }

    /**
     * The available diseases in the game.
     * @author Andras Belicza
     */
    public enum Diseases {
        /** The Ceasefire disease.                                 */
        CEASEFIRE         { public String toString() { return "Ceasefire"        ; } },
        /** The Bomb-shitting disease.                             */
        BOMB_SHITTING     { public String toString() { return "Bomb shitting"    ; } },
        /** The Toddling disease.                                  */
        TODDLING          { public String toString() { return "Toddling"         ; } },
        /** The Fast detonation disease.                           */
        FAST_DETONATION   { public String toString() { return "Fast detonation"  ; } },
        /** The Scuding disease.                                   */
        SCUDING           { public String toString() { return "Scuding"          ; } },
        /** The Short range disease.                               */
        SHORT_RANGE       { public String toString() { return "Short range"      ; } },
        /** The Position changing disease.                         */
        POSITION_CHANGING { public String toString() { return "Position changing"; } }
    }
    
    /** Level name for indicating random level (not the one that is displayed in the level list combo box). */
    public static final String RANDOM_LEVEL_NAME = "";
    
    /**
     * The available game types in the game.
     * @author Andras Belicza
     */
    public enum GameTypes {
        /** Free for all game type.                                */
        FREE_FOR_ALL { public String toString() { return "free for all"; } },
        /** Team play game type.                                   */
        TEAM_PLAY    { public String toString() { return "team play"   ; } }
    }
    
    /**
     * Possibilities for distributing the killing points when bombermen die.
     * @author Andras Belicza
     */
    public enum KillsBelongTos {
        /** Kills belong to the owner of killer bomb.              */
        OWNER_OF_KILLER_BOMB    { public String toString() { return "the owner of killer bomb"   ; } },
        /** Kills belong to the owner of triggerer bomb.           */
        OWNER_OF_TRIGGERER_BOMB { public String toString() { return "the owner of triggerer bomb"; } }
    }
    
    /**
     * Possible network latencies.
     * @author Andras Belicza
     */
    public enum NetworkLatencies {
        /** Low network latency.        */
        LOW        { public String toString() { return "low"       ; } },
        /** High network latency.       */
        HIGH       { public String toString() { return "high"      ; } },
        /** Extra high network latency. */
        EXTRA_HIGH { public String toString() { return "extra high"; } }
    }
    
    /**
     * Game scene refresh modes.
     * @author Andras Belicza
     */
    public enum SceneRefreshModes {
        /** Low network latency.        */
        NORMAL     { public String toString() { return "normal"    ; } },
        /** High network latency.       */
        SLOW       { public String toString() { return "slow"      ; } },
        /** Extra high network latency. */
        EXTRA_SLOW { public String toString() { return "extra slow"; } }
    }
    
    /**
     * Image scaling algorithms.
     * @author Andras Belicza
     */
    public enum ImageScalingAlgorithms {
        /** Fast image scaling algorithm.   */
        FAST   { public String toString() { return "fast"  ; } },
        /** Smooth image scaling algorithm. */
        SMOOTH { public String toString() { return "smooth"; } }
    }
    
}
