
/*
 * Created on Dcember 13, 2005
 */

package classes.client.gamecore.control;

import static classes.client.gamecore.Consts.FIRE_ITERATIONS;
import classes.client.gamecore.model.FireModel;
import classes.client.gamecore.model.ModelProvider;

/**
 * The control layer of the fire.<br>
 * Stores the x and y coordinates of the component this fire takes place on for fast accessing the level component.
 * 
 * @author Andras Belicza
 */
public class Fire {

	/** The model of the fire.                                        */
	private final FireModel       model;
	/** X coordinate of the component where this fire takes place on. */
	private final int             componentPosX;
	/** Y coordinate of the component where this fire takes place on. */
	private final int             componentPosY;
	/** Reference to a model provider.                                */
	private final ModelProvider   modelProvider;
	/** Reference to a model controller.                              */
	private final ModelController modelController;
	
	/**
	 * Creates a new Fire.<br>
	 * @param componentPosX   x coordinate of the component where this fire takes place on
	 * @param componentPosY   y coordinate of the component where this fire takes place on
	 * @param modelProvider   reference to a model provider
	 * @param modelController reference to a model controller
	 */
	public Fire( final int componentPosX, final int componentPosY, final ModelProvider modelProvider, final ModelController modelController ) {
		this.componentPosX   = componentPosX;
		this.componentPosY   = componentPosY;
		this.modelProvider   = modelProvider;
		this.modelController = modelController;
		
		model = new FireModel();
	}
	
	/**
	 * Returns the model of the bomb.
	 * @return the model of the bomb
	 */
	public FireModel getModel() {
		return model;
	}

	/**
	 * Performs operations which are requried by passing the time.
	 */
	public void nextIteration() {
		if ( model.getIterationCounter() + 1 < FIRE_ITERATIONS )
			model.nextIteration();
		else
			modelController.removeFireFromComponentPos( this, componentPosX, componentPosY );
	}
	
}
