
/*
 * Created on December 13, 2005
 */

package classes.client.gamecore.model;

import classes.client.gamecore.Consts.FireShapes;

/**
 * The class represents the model of the fire.<br>
 * 
 * @author Andras Belicza
 */
public class FireModel extends IterableObject {

	/** Shape of the fire.                */
	private FireShapes  shape;
	/** The owner player of the fire.     */
	private PlayerModel ownerPlayer;
	/** The triggerer player of the fire. */
	private PlayerModel triggererPlayer;
	
	/**
	 * Returns the shape of the fire.
	 * @return the shape of the fire
	 */
	public FireShapes getShape() {
		return shape;
	}

	/**
	 * Sets the shape of the fire.
	 * @param shape shape of fire to be set
	 */
	public void setShape( final FireShapes shape ) {
		this.shape = shape;
	}

	/**
	 * Returns the owner player of the fire.
	 * @return the owner player of the fire
	 */
	public PlayerModel getOwnerPlayer() {
		return ownerPlayer;
	}

	/**
	 * Sets the owner player of the fire.
	 * @param ownerPlayer owner player of fire to be set
	 */
	public void setOwnerPlayer( final PlayerModel ownerPlayer ) {
		this.ownerPlayer = ownerPlayer;
	}

	/**
	 * Returns the triggerer player of the fire.
	 * @return the triggerer player of the fire
	 */
	public PlayerModel getTriggererPlayer() {
		return triggererPlayer;
	}

	/**
	 * Sets the triggerer player of the fire.
	 * @param triggererPlayer triggerer player of fire to be set
	 */
	public void setTriggererPlayer( final PlayerModel triggererPlayer ) {
		this.triggererPlayer = triggererPlayer;
	}

}
