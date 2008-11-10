
/*
 * Created on October 26, 2004
 */

package classes.client.gamecore.control;

import java.lang.reflect.Array;
import java.util.Vector;

import classes.client.gamecore.model.ModelProvider;
import classes.client.gamecore.model.level.LevelComponent;
import classes.client.gamecore.model.level.LevelModel;
import classes.options.Consts.Items;
import classes.options.Consts.Walls;
import classes.options.model.LevelOptions;
import classes.utils.GeneralUtilities;

/**
 * The control layer of the level.
 * 
 * @author Andras Belicza
 */
public class Level {

	/** The model of the level.                             */
	private final LevelModel         model;
	/** References to the fires taking places on the level.
	 *  There is a vector for each component.               */
	private final Vector< Fire >[][] fireVectorss;
	/** Reference to a model provider.                      */
	private final ModelProvider      modelProvider;
	/** Reference to a model controller.                    */
	private final ModelController    modelController;
	
    /**
     * Creates a new Level.<br>
     * Implementation simply calls the other constructor with a new level model.
     * @param levelOptions    options of this level
	 * @param modelProvider   reference to a model provider
	 * @param modelController reference to a model controller
     */
    public Level( final LevelOptions levelOptions, final ModelProvider modelProvider, final ModelController modelController ) {
    	this( new LevelModel( levelOptions ), modelProvider, modelController );
    }
	
    /**
     * Creates a new Level from the specified level model.
     * @param levelModel      level model to be used
	 * @param modelProvider   reference to a model provider
	 * @param modelController reference to a model controller
     */
    public Level( final LevelModel levelModel, final ModelProvider modelProvider, final ModelController modelController ) {
    	model                = levelModel;
    	this.modelProvider   = modelProvider;
    	this.modelController = modelController;
    	
    	fireVectorss = (Vector< Fire >[][]) Array.newInstance( new Vector< Fire >().getClass(), new int[] { model.getComponents().length, model.getComponents()[ 0 ].length } );
    	for ( final Vector< Fire >[] fireVectors : fireVectorss )
    		for ( int i = 0; i < fireVectors.length; i++ )
    			fireVectors[ i ] = new Vector< Fire >();
    }
	
	/**
	 * Returns the model of the level.
	 * @return the model of the level
	 */
	public LevelModel getModel() {
		return model;
	}
	
	/**
	 * Adds fire to a component position.
	 * @param fire          fire to be set
	 * @param componentPosX x coordinate of the component to set the fire on
	 * @param componentPosY y coordinate of the component to set the fire on
	 */
	public void addFireToComponentPos( final Fire fire, final int componentPosX, final int componentPosY ) {
		fireVectorss[ componentPosY ][ componentPosX ].add( fire );

		final LevelComponent levelComponent = modelProvider.getLevelModel().getComponents()[ componentPosY ][ componentPosX ];
		levelComponent.fireModelVector.add( fire.getModel() );

		// We decide what item and if there will be an item after the fire, cause it has to be appeared from the middle of the fire.
		if ( levelComponent.getWall() == Walls.BRICK && levelComponent.getItem() == null )  // Item has to be generated once (see: time delayed multiple fire).
			if ( modelController.getGlobalServerOptions().gettingItemProbability > modelController.getRandom().nextInt( 100 ) )
				levelComponent.setItem( Items.values()[ GeneralUtilities.pickWeightedRandom( modelController.getGlobalServerOptions().levelOptions.itemWeights, modelController.getRandom() ) ] );
	}
	
	/**
	 * Removes a fire from a specified component position.
	 * @param fire          fire to be removed
	 * @param componentPosX x coordinate of the component to remove the fire from
	 * @param componentPosY y coordinate of the component to remove the fire from
	 */
	public void removeFireFromComponentPos( final Fire fire, final int componentPosX, final int componentPosY ) {
		fireVectorss[ componentPosY ][ componentPosX ].remove( fire );
		model.getComponents()[ componentPosY ][ componentPosX ].fireModelVector.remove( fire.getModel() );
	}

	/**
	 * Performs operations which are requried by passing the time.
	 */
	public void nextIteration() {
		for ( final Vector< Fire >[] fireVectors : fireVectorss )
			for ( final Vector< Fire > fireVector : fireVectors )
				for ( int i = fireVector.size() - 1; i >= 0; i-- )  // Cannot be enhanced: fire can remove itself. And because it can remove itself, cycle must be downward...
					fireVector.get( i ).nextIteration();
	}

}
