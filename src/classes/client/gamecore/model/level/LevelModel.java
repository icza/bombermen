
/*
 * Created on October 16, 2004
 */

package classes.client.gamecore.model.level;

import classes.options.model.LevelOptions;

/**
 * The model of the level where bombermen take their fight.
 *
 * @author Andras Belicza
 */
public class LevelModel {
    
    /** Options of this level.       */
    private final LevelOptions       levelOptions;
    /** The components of the level. */
    private final LevelComponent[][] components;

    /**
     * Creates a new Level.
     * @param levelOptions options of this level
     */
    public LevelModel( final LevelOptions levelOptions ) {
        this.levelOptions = levelOptions;
        components = new LevelComponent[ levelOptions.levelHeight ][ levelOptions.levelWidth ];
        for ( final LevelComponent[] componentRow : components )
            for ( int i = 0; i < componentRow.length; i++ )
                componentRow[ i ] = new LevelComponent();
    }

    /**
     * Returns the level options.
     * @return the level options
     */
    public LevelOptions getLevelOptions() {
		return levelOptions;
	}
    
    /**
     * Returns the components of the level.
     * @return the components of the level
     */
    public LevelComponent[][] getComponents() {
        return components;
    }
    
    /**
     * Packs this object to a String so it can be transferred or stored.
     * @return a compact string representing this level
     */
    public String packToString() {
        return null;
    }
    
    /**
     * Parses a level object from a string.
     * @param source the String representing the parsable level
     * @return a new Level created from the source string
     */
    public static LevelModel parseFromString( final String source ) {
        return null;
    }
    
    /**
     * Clones and returns a clone of this level.
     * @return a clone of this level
     */
    public LevelModel cloneLevel() {
        return parseFromString( packToString() );
    }

}
