
/*
 * Created on November 29, 2005
 */

package classes.client.gamecore.control;

import java.util.Random;

import classes.client.gamecore.model.BombModel;
import classes.options.Consts.Items;
import classes.options.model.ServerOptions;

/**
 * Interface that defines methods to control and operate on the game model.
 * 
 * @author Andras Belicza
 */
interface ModelController {

	/**
	 * Returns the global server options.
	 * @return the global server options
	 */
	ServerOptions getGlobalServerOptions();

	/**
	 * Adds a new bomb to the model.
	 * @param bomb bomb to be added
	 */
	void addNewBomb( final Bomb bomb );
	
	/**
	 * Removes a bomb specified by its index.
	 * @param bombIndex index of bomb to be removed
	 */
	void removeBombAtIndex( final int bombIndex );

	/**
	 * Returns the Random object to be used for generating random datas.
	 * @return the Random object to be used for generating random datas
	 */
	Random getRandom();

	/**
	 * Checks and sets the x coordiante of the target position in case of a flying bomb.
	 * @param bombModel        bomb model whos target position to be validated and set
	 * @param flyingTargetPosX the whished x coordiante of the target position in case of a flying bomb 
	 */
	void validateAndSetFlyingTargetPosX( final BombModel bombModel, final int flyingTargetPosX );

	/**
	 * Checks and sets the y coordiante of the target position in case of a flying bomb.
	 * @param bombModel        bomb model whos target position to be validated and set
	 * @param flyingTargetPosY the whished y coordiante of the target position in case of a flying bomb 
	 */
	void validateAndSetFlyingTargetPosY( final BombModel bombModel, final int flyingTargetPosY );
	
	/**
	 * Tells whether a specified bomb can roll to a component position.
	 * @param bombModel     model of bomb to be checked
	 * @param componentPosX x coordinate of the position of the desired component
	 * @param componentPosY y coordinate of the position of the desired component
	 * @return true, if the specified bomb can roll to the component; false otherwise
	 */
	boolean canBombRollToComponentPosition( final BombModel bombModel, final int componentPosX, final int componentPosY );

	/**
	 * Replaces an item to a random position in the level. 
	 * @param item item to be replaced
	 */
	void replaceItemOnLevel( final Items item );

	/**
	 * Removes a fire from a specified component position.
	 * @param fire          fire to be removed
	 * @param componentPosX x coordinate of the component to remove the fire from
	 * @param componentPosY y coordinate of the component to remove the fire from
	 */
	void removeFireFromComponentPos( final Fire fire, final int componentPosX, final int componentPosY );
	
}
