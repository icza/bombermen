
/*
 * Created on July 5, 2004
 */

package classes.options.model;

import java.util.EnumMap;
import classes.options.Consts.Items;
import classes.options.Consts.Diseases;
import static classes.options.Consts.ACCUMULATEABLE_ITEMS;
import classes.utils.GeneralStringTokenizer;
import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;

/**
 * This class holds the options of a complete level options
 * (not included options needed only for generating random levels).
 *
 * @author Andras Belicza
 */
public class LevelOptions extends Options< LevelOptions > {

    /** Width of level in level component.                                   */
    public int                       levelWidth;
    /** Height of level in level component.                                  */
    public int                       levelHeight;

    /** Quantities of the accumulateable items at the beginning.             */
    public EnumMap< Items, Integer > accumulateableItemQuantitiesMap = new EnumMap< Items, Integer >( Items.class );
    /** Tells whether we have the non-accumulateable items at the beginning. */
    public EnumMap< Items, Boolean > hasNonAccumulateableItemsMap    = new EnumMap< Items, Boolean >( Items.class );

    /** The weights of the items.                                            */
    public int[]                     itemWeights    = new int[ Items   .values().length ];
    /** The weights of the diseases.                                         */
    public int[]                     diseaseWeights = new int[ Diseases.values().length ];

    /**
     * Creates a new LevelOptions.
     * Adds entries to accumulateableItemQuantitiesMap and hasNonAccumulateableItemsMap.
     */
    public LevelOptions() {
        for ( final Items accumulateableItem : ACCUMULATEABLE_ITEMS )
            accumulateableItemQuantitiesMap.put( accumulateableItem, new Integer( 0 ) );

        for ( final Items item : Items.values() )
            if ( !ACCUMULATEABLE_ITEMS.contains( item ) )
                hasNonAccumulateableItemsMap.put( item, new Boolean( false ) );
    }
    
    /**
     * Packs this object to a String so it can be transferred or stored.
     * @return a compact string representing this level options
     */
    public String packToString() {
        final StringBuilder buffer = new StringBuilder();
        
        buffer.append( levelWidth  ).append( GENERAL_SEPARATOR_CHAR );
        buffer.append( levelHeight ).append( GENERAL_SEPARATOR_CHAR );

        for ( final Integer accumulateableItemQuantity : accumulateableItemQuantitiesMap.values() ) {
            buffer.append( accumulateableItemQuantity.intValue()   ).append( GENERAL_SEPARATOR_CHAR );
        }
        
        for ( final Boolean hasNonAccumulateableItem : hasNonAccumulateableItemsMap.values() ) {
            buffer.append( hasNonAccumulateableItem.booleanValue() ).append( GENERAL_SEPARATOR_CHAR );
        }

        for ( final int itemWeight : itemWeights ) {
            buffer.append( itemWeight    ).append( GENERAL_SEPARATOR_CHAR );
        }

        for ( final int diseaseWeight : diseaseWeights ) {
            buffer.append( diseaseWeight ).append( GENERAL_SEPARATOR_CHAR );
        }

        return buffer.toString();
    }
    
    /**
     * Parses a level options object from a string.
     * @param source the String representing the parsable level options
     * @return a new LevelOptions created from the source string
     */
    public static LevelOptions parseFromString( final String source ) {
        final LevelOptions           levelOptions     = new LevelOptions();
        final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer( source );

        levelOptions.levelWidth  = optionsTokenizer.nextIntToken();
        levelOptions.levelHeight = optionsTokenizer.nextIntToken();

        for ( final Items accumulateableItem : ACCUMULATEABLE_ITEMS )
            levelOptions.accumulateableItemQuantitiesMap.put( accumulateableItem, optionsTokenizer.nextIntToken() );

        for ( final Items item : Items.values() )
            if ( !ACCUMULATEABLE_ITEMS.contains( item ) )
                levelOptions.hasNonAccumulateableItemsMap.put( item, optionsTokenizer.nextBooleanToken() );

        for ( int i = 0; i < levelOptions.itemWeights.length; i++ )
            levelOptions.itemWeights[ i ] = optionsTokenizer.nextIntToken();

        for ( int i = 0; i < levelOptions.diseaseWeights.length; i++ )
            levelOptions.diseaseWeights[ i ] = optionsTokenizer.nextIntToken();
        
        return levelOptions;
    }

    /**
     * Parses a level options object from a string.<br>
     * Simply returns the object created by parseFromString().
     * @param source the String representing the parsable level options
     * @return a new LevelOptions created from the source string
     */
    public LevelOptions dynamicParseFromString( final String source ) {
        return parseFromString( source );
    }

}
