
/*
 * Created on July 17, 2004
 */

package classes.options;

import javax.swing.*;

import classes.options.model.LevelOptions;
import static classes.options.Consts.*;
import java.util.EnumMap;
import java.awt.event.*;

/**
 * This class makes the level options available for viewing and changing
 * on the screen.<br>
 * The manageable level options is passed to the constructor.
 *
 * @author Andras Belicza
 */
public class LevelComponentOptions extends ComponentOptions< LevelOptions > implements ItemListener {

    /** Minimum value of level widht option.  */
    private static final int   MINIMUM_LEVEL_WIDTH  =   3;
    /** Default value of level widht option.  */
    private static final int   DEFAULT_LEVEL_WIDTH  =  17;
    /** Maximum value of level widht option.  */
    private static final int   MAXIMUM_LEVEL_WIDTH  = 150;
    /** Minimum value of level height option. */
    private static final int   MINIMUM_LEVEL_HEIGHT =   3;
    /** Default value of level height option. */
    private static final int   DEFAULT_LEVEL_HEIGHT =  13;
    /** Maximum value of level height option. */
    private static final int   MAXIMUM_LEVEL_HEIGHT = 100;

    /** Minimum value of accumulateable item quantity.               */
    private static final int   MINIMUM_ACCUMULATEABLE_ITEM_QUANTITY =  0;
    /** Default value of Bomb accumulateable item quantity.          */
    private static final int   DEFAULT_BOMB_QUANTITY                =  1;
    /** Default value of Fire accumulateable item quantity.          */
    private static final int   DEFAULT_FIRE_QUANTITY                =  2;
    /** Minimum value of accumulateable item quantity.               */
    private static final int   MAXIMUM_ACCUMULATEABLE_ITEM_QUANTITY =  500;
    /** Maximum value of Roller skates accumulateable item quantity. */
    private static final int   MAXIMUM_ROLLER_SKATES_QUANTITY       =  10;

    /** Weight unit to determine items and diseases weights.                   */
    private static final int   WEIGHT_UNIT                          =   1;
    /** Minimum value disease and item of weights.                             */
    private static final int   MINIMUM_WEIGHT                       =   0;
    /** Default value of disease weights and default for default item weights. */
    private static final int   DEFAULT_WEIGHT                       = WEIGHT_UNIT * 10;
    /** Maximum value of disease and item weights.                             */
    private static final int   MAXIMUM_WEIGHT                       = 100;

    /** The default item weights. */
    private static final int[] DEFAULT_ITEM_WEIGHTS                 = new int[ Items.values().length ];
        
    /**
     * We initialize the DEFAULT_ITEM_WEIGHTS constant.
     */
    static {

        for ( int i = 0; i < DEFAULT_ITEM_WEIGHTS.length; i++ )
            DEFAULT_ITEM_WEIGHTS[ i ] = DEFAULT_WEIGHT;
        
        DEFAULT_ITEM_WEIGHTS[ Items.BOXING_GLOVES.ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.BLUE_GLOVES  .ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.WALL_BUILDING.ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.BOOTS        .ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.JELLY        .ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.BOMB_SPRINKLE.ordinal() ] = WEIGHT_UNIT * 5;
        DEFAULT_ITEM_WEIGHTS[ Items.SUPER_DISEASE.ordinal() ] = WEIGHT_UNIT * 5;

        DEFAULT_ITEM_WEIGHTS[ Items.TRIGGER      .ordinal() ] = WEIGHT_UNIT * 2;
        DEFAULT_ITEM_WEIGHTS[ Items.SUPER_FIRE   .ordinal() ] = WEIGHT_UNIT * 2;
        DEFAULT_ITEM_WEIGHTS[ Items.SPIDER_BOMB  .ordinal() ] = WEIGHT_UNIT * 2;
        DEFAULT_ITEM_WEIGHTS[ Items.WALL_CLIMBING.ordinal() ] = WEIGHT_UNIT * 2;
    }



    /** Component for level width option.              */
    private final JSpinner   levelWidth_c      = new JSpinner( new SpinnerNumberModel( DEFAULT_LEVEL_WIDTH, MINIMUM_LEVEL_WIDTH, MAXIMUM_LEVEL_WIDTH, 1 ) );
    /** Component for level height option.             */
    private final JSpinner   levelHeight_c     = new JSpinner( new SpinnerNumberModel( DEFAULT_LEVEL_HEIGHT, MINIMUM_LEVEL_HEIGHT, MAXIMUM_LEVEL_HEIGHT, 1 ) );
    
    /** Components for accumulateable item quantities. */
    private final EnumMap< Items, JSpinner  > accumulateableItemQuantitiesMap_cs = new EnumMap< Items, JSpinner  >( Items.class );
    /** Components for has non-accumulateable items.   */
    private final EnumMap< Items, JCheckBox > hasNonAccumulateableItemsMap_cs    = new EnumMap< Items, JCheckBox >( Items.class );

    /** Components for item weights.                   */
    private final JSpinner[] itemWeights_cs    = new JSpinner[ options.itemWeights.length ];
    /** Components for disease weights.                */
    private final JSpinner[] diseaseWeights_cs = new JSpinner[ options.diseaseWeights.length ];



    /**
     * Creates a new LevelComponentOptions.<br>
     * The new level component options will contain changable options.
     * @param levelOptions the level options object to be handled
     */
    public LevelComponentOptions( final LevelOptions levelOptions ) {
        this( levelOptions, false );
    }

    /**
     * Creates a new LevelComponentOptions.
     * @param levelOptions the level options object to be handled
     * @param viewOnly     tells whether we just want to view the options but not to modify
     */
    public LevelComponentOptions( final LevelOptions levelOptions, final boolean viewOnly ) {
        super( levelOptions );
        
        for ( final Items item : options.accumulateableItemQuantitiesMap.keySet() ) {
            int defaultQuantity = MINIMUM_ACCUMULATEABLE_ITEM_QUANTITY;
            int maximumQuantity = MAXIMUM_ACCUMULATEABLE_ITEM_QUANTITY;
            switch ( item ) {
                case BOMB          : defaultQuantity = DEFAULT_BOMB_QUANTITY;          break;
                case FIRE          : defaultQuantity = DEFAULT_FIRE_QUANTITY;          break;
                case ROLLER_SKATES : maximumQuantity = MAXIMUM_ROLLER_SKATES_QUANTITY; break;
            }
            accumulateableItemQuantitiesMap_cs.put( item, new JSpinner( new SpinnerNumberModel( defaultQuantity, MINIMUM_ACCUMULATEABLE_ITEM_QUANTITY, maximumQuantity, 1 ) ) );
        }
        
        for ( final Items item : options.hasNonAccumulateableItemsMap.keySet() ) {
            final JCheckBox checkBox = new JCheckBox( item.toString() );
            checkBox.addItemListener( this );
            hasNonAccumulateableItemsMap_cs.put( item, checkBox );
        }

        for ( int i = 0; i < itemWeights_cs.length; i++ )
            itemWeights_cs[ i ]    = new JSpinner( new SpinnerNumberModel( DEFAULT_ITEM_WEIGHTS[ i ], MINIMUM_WEIGHT, MAXIMUM_WEIGHT, 1 ) );
        for ( int i = 0; i < diseaseWeights_cs.length; i++ )
            diseaseWeights_cs[ i ] = new JSpinner( new SpinnerNumberModel( DEFAULT_WEIGHT, MINIMUM_WEIGHT, MAXIMUM_WEIGHT, 1 ) );
        
        buildOptionsTabbedPane( viewOnly );
    }

    /**
     * Builds the options tabbed pane.
     * @param viewOnly tells whether we just want to view the options but not to modify
     */
    private void buildOptionsTabbedPane( final boolean viewOnly ) {
        final boolean componentsEnabled = viewOnly ? false : true;
        JPanel        panel;
        Box           box;
        
        box = Box.createVerticalBox();
            panel = new JPanel();
            panel.add( createLabel( "Width of level:", componentsEnabled ) );
            levelWidth_c.setEnabled( componentsEnabled );
            panel.add( levelWidth_c );
        box.add( panel );
            panel = new JPanel();
            panel.add( createLabel( "Height of level:", componentsEnabled ) );
            levelHeight_c.setEnabled( componentsEnabled );
            panel.add( levelHeight_c );
        box.add( panel );
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Level sizes", panel );
        
        box = Box.createVerticalBox();
        for ( final Items item : accumulateableItemQuantitiesMap_cs.keySet() ) {
            panel = new JPanel();
            panel.add( createLabel( item + ":", componentsEnabled ) );
            accumulateableItemQuantitiesMap_cs.get( item ).setEnabled( componentsEnabled );
            panel.add( accumulateableItemQuantitiesMap_cs.get( item ) );
            box.add( panel );
        }
        for ( final JCheckBox hasNonAccumulateableItem_c : hasNonAccumulateableItemsMap_cs.values() ) {
            panel = new JPanel();
            hasNonAccumulateableItem_c.setEnabled( componentsEnabled );
            panel.add( hasNonAccumulateableItem_c );
            box.add( panel );
        }
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Start items", panel );
        
        box = Box.createVerticalBox();
        for ( int i = 0; i < itemWeights_cs.length; i++ ) {
            panel = new JPanel();
            panel.add( createLabel( Items.values()[ i ] + ":", componentsEnabled ) );
            itemWeights_cs[ i ].setEnabled( componentsEnabled );
            panel.add( itemWeights_cs[ i ] );
            box.add( panel );
        }
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Item weights", panel );

        box = Box.createVerticalBox();
        for ( int i = 0; i < diseaseWeights_cs.length; i++ ) {
            panel = new JPanel();
            panel.add( createLabel( Diseases.values()[ i ] + ":", componentsEnabled ) );
            diseaseWeights_cs[ i ].setEnabled( componentsEnabled );
            panel.add( diseaseWeights_cs[ i ] );
            box.add( panel );
        }
        panel = new JPanel();
        panel.add( box );
        optionsTabbedPane.addTab( "Disease weights", panel );
    }
    
    /**
     * Restores the default values of the level options to the option components.
     */
    public void restoreDefaultValuesToComponents() {
        levelWidth_c .setValue( DEFAULT_LEVEL_WIDTH );
        levelHeight_c.setValue( DEFAULT_LEVEL_HEIGHT );

        for ( final Items item : accumulateableItemQuantitiesMap_cs.keySet() ) {
            int defaultQuantity = MINIMUM_ACCUMULATEABLE_ITEM_QUANTITY;
            switch ( item ) {
                case BOMB          : defaultQuantity = DEFAULT_BOMB_QUANTITY;          break;
                case FIRE          : defaultQuantity = DEFAULT_FIRE_QUANTITY;          break;
            }
            accumulateableItemQuantitiesMap_cs.get( item ).setValue( defaultQuantity );
        }
        for ( final Items item : hasNonAccumulateableItemsMap_cs.keySet() ) {
            hasNonAccumulateableItemsMap_cs.get( item ).setSelected( false );
        }

        for ( int i = 0; i< itemWeights_cs.length; i++ )
            itemWeights_cs[ i ].setValue( DEFAULT_ITEM_WEIGHTS[ i ] );
        for ( final JSpinner spinner : diseaseWeights_cs )
            spinner.setValue( DEFAULT_WEIGHT );
    }
    
    /**
     * Creates a new level options object, stores into that the actual states of the option components,
     * and returns it.
     * @return the LevelOptions object holding the values/states of the option components
     */
    public LevelOptions getOptionsFromComponents() {
        final LevelOptions levelOptions = new LevelOptions();

        levelOptions.levelWidth  = (Integer) levelWidth_c.getValue();
        levelOptions.levelHeight = (Integer) levelHeight_c.getValue();
        
        for ( final Items item : levelOptions.accumulateableItemQuantitiesMap.keySet() )
            levelOptions.accumulateableItemQuantitiesMap.put( item, (Integer) accumulateableItemQuantitiesMap_cs.get( item ).getValue() );
        for ( final Items item : levelOptions.hasNonAccumulateableItemsMap.keySet() )
            levelOptions.hasNonAccumulateableItemsMap.put( item, hasNonAccumulateableItemsMap_cs.get( item ).isSelected() );

        for ( int i = 0; i < levelOptions.itemWeights.length; i++ )
            levelOptions.itemWeights[ i ]    = (Integer) itemWeights_cs[ i ].getValue();
        for ( int i = 0; i < levelOptions.diseaseWeights.length; i++ )
            levelOptions.diseaseWeights[ i ] = (Integer) diseaseWeights_cs[ i ].getValue();

        return levelOptions;
    }
    
    /**
     * Stores the values of the level option attributes to the appropriate components.
     */
    public void synchronizeComponentsToOptions() {
        levelWidth_c .setValue( options.levelWidth );
        levelHeight_c.setValue( options.levelHeight );

        for ( final Items item : accumulateableItemQuantitiesMap_cs.keySet() )
            accumulateableItemQuantitiesMap_cs.get( item ).setValue( options.accumulateableItemQuantitiesMap.get( item ) );
        for ( final Items item : hasNonAccumulateableItemsMap_cs.keySet() )
            hasNonAccumulateableItemsMap_cs.get( item ).setSelected( options.hasNonAccumulateableItemsMap.get( item ) );

        for ( int i = 0; i < itemWeights_cs.length; i++ )
            itemWeights_cs[ i ].setValue( options.itemWeights[ i ] );
        for ( int i = 0; i < diseaseWeights_cs.length; i++ )
            diseaseWeights_cs[ i ].setValue( options.diseaseWeights[ i ] );
    }
    
    /**
     * Handles the changes of states of the has non-accumulateable items checkboxes
     * which means we have to uncheck items which are neutraliez by the item just have been checked.
     * @param ie details of the item state changed event
     */
    public void itemStateChanged( final ItemEvent ie ) {
        final JCheckBox checkBox = (JCheckBox) ie.getSource();
        if ( checkBox.isSelected() )
            for ( final Items item : hasNonAccumulateableItemsMap_cs.keySet() )
                if ( hasNonAccumulateableItemsMap_cs.get( item ) == checkBox ) {
                    if ( NEUTRALIZER_ITEMS_MAP.containsKey( item ) )
                        for ( final Items neutralizedItem : NEUTRALIZER_ITEMS_MAP.get( item ) )
                            hasNonAccumulateableItemsMap_cs.get( neutralizedItem ).setSelected( false );
                    break;
                }
    }
    
}
