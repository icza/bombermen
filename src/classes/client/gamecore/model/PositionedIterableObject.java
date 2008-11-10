
/*
 * Created on November 28, 2005
 */

package classes.client.gamecore.model;

import static classes.client.gamecore.Consts.LEVEL_COMPONENT_GRANULARITY;
import classes.client.gamecore.Consts.Directions;

/**
 * Represents an iterable object which has a position and a direction.
 * 
 * @author Andras Belicza
 */
public class PositionedIterableObject extends IterableObject {

	/** The horizontal position of the center point of the object. */
	private int        posX;
	/** The vertical position of the center point of the object.   */
	private int        posY;
	/** The direction of the object.                               */
	private Directions direction;
	
	/**
	 * Returns the x coordinate of the position of the object.
	 * @return the x coordinate of the position of the object
	 */
	public int getPosX() {
		return posX;
	}
	
	/**
	 * Returns the x coordinate of the level component where the position belongs to
	 * @return the x coordinate of the level component where the position belongs to
	 */
	public int getComponentPosX() {
		return posX / LEVEL_COMPONENT_GRANULARITY;
	}

	/**
	 * Sets the x coordinate of the position of the object.
	 * @param posX the x coordinate of the position to be set
	 */
	public void setPosX( final int posX ) {
		this.posX = posX;
	}

	/**
	 * Returns the y coordinate of the position of the object.
	 * @return the y coordinate of the position of the object
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * Returns the y coordinate of the level component where the position belongs to
	 * @return the y coordinate of the level component where the position belongs to
	 */
	public int getComponentPosY() {
		return posY / LEVEL_COMPONENT_GRANULARITY;
	}

	/**
	 * Sets the y coordinate of the position of the object.
	 * @param posY the x coordinate of the position to be set
	 */
	public void setPosY( final int posY ) {
		this.posY = posY;
	}

	/**
	 * Returns the direction of the player.
	 * @return the direction of the player
	 */
	public Directions getDirection() {
		return direction;
	}
	
	/**
	 * Returns an integer which can be used to identify the horizontal component of the direction,
	 * and can be used to calculate positions ahead of the object (in the current direction).
	 * @return an integer identifying the horizontal component of the direction:<br>
	 * 		       -1, if direction is Directions.LEFT,
	 *			    1, if direction is Directions.RIGHT
	 *              0 otherwise
	 */
	public int getDirectionXMultiplier() {
		return direction.getXMultiplier();
	}

	/**
	 * Returns an integer which can be used to identify the vertical component of the direction,
	 * and can be used to calculate positions ahead of the object (in the current direction).
	 * @return an integer identifying the vertical component of the direction:<br>
	 * 		       -1, if direction is Directions.UP,
	 *			    1, if direction is Directions.DOWN
	 *              0 otherwise
	 */
	public int getDirectionYMultiplier() {
		return direction.getYMultiplier();
	}

	/**
	 * Sets the direction of the player
	 * @param direction the direction to be set
	 */
	public void setDirection( final Directions direction ) {
		this.direction = direction;
	}
	
	/**
	 * Aligns the x coordinate of the position to be at the center of the component this position is on.
	 */
	public void alignPosXToComponentCenter() {
		posX += LEVEL_COMPONENT_GRANULARITY / 2 - posX % LEVEL_COMPONENT_GRANULARITY; 
	}

	/**
	 * Aligns the x coordinate of the position to be at the center of the component this position is on.
	 */
	public void alignPosYToComponentCenter() {
		posY += LEVEL_COMPONENT_GRANULARITY / 2 - posY % LEVEL_COMPONENT_GRANULARITY; 
	}

}
