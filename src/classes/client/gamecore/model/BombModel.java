
/*
 * Created on November 28, 2005
 */

package classes.client.gamecore.model;

import java.util.Vector;

import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.Consts.BombTypes;
import classes.client.gamecore.Consts.Directions;

/**
 * The class represents the model of a bomb.<br>
 * Defines a new iteration counter which tells the number of iterations during the current phase.
 * This is neccessary, 'cause flying bombs have non-changing picture, but we still need to know
 * how long the bomb is flying.
 * The iteration counter inherited from PositionedIterableObject determines the picture phase of the bomb.
 * 
 * @author Andras Belicza
 */
public class BombModel extends PositionedIterableObject {

	/** Type of the bomb.                                                             */
	private BombTypes         type;
	/** The owner player of the bomb.                                                 */
	private final PlayerModel ownerPlayer;
	/** The phase of the bomb.                                                        */
	private BombPhases        phase;
	/** Number of iterations during the current phase.                                */
	private int               iterationsDuringPhase;
	/** Number of iterations when the bomb was ticking.                               */
	private int               tickingIterations;
	/** In case of a flying bomb, this is the x coordinate of its target position.    */
	private int               flyingTargetPosX;
	/** In case of a flying bomb, this is the y coordinate of its target position.    */
	private int               flyingTargetPosY;
	/** Tells wheter this bomb is dead. 
	 * A bomb is dead when it is thrown/punched away from the level,
	 * and it cannot come back (game rule).                                           */
	private boolean           dead;
	
	
	// The following attributes aid to detonate the bomb
	
	/** Tells whether this bomb is about to detonate (in the current game iteration). */
	private boolean           aboutToDetonate;
	/** Tells whether this bomb has been detonated.                                   */
	private boolean           detonated;
	/** The range of the bomb.                                                        */
	private int               range;
	/** Excluded detonation direction: when this bomb goes off,
	 * detonation cannot spread in these directions.                                  */
	public final Vector< Directions > excludedDetonationDirections = new Vector< Directions >( 2 );
	/** The triggerer player of the bomb.                                             */
	private PlayerModel       triggererPlayer;
	
	
	/**
	 * Creates a new Bomb.
	 * @param ownerPlayer the owner player
	 */
	public BombModel( final PlayerModel ownerPlayer ) {
		this.ownerPlayer = ownerPlayer;
		setPhase( BombPhases.STANDING );
	}
	
	/**
	 * Returns the type of the bomb.
	 * @return the type of the bomb
	 */
	public BombTypes getType() {
		return type;
	}
	
	/**
	 * Sets the type of the bomb.
	 * @param type type to be set
	 */
	public void setType( final BombTypes type ) {
		this.type = type;
	}
	
	/**
	 * Returns the owner player.
	 * @return the owner player
	 */
	public PlayerModel getOwnerPlayer() {
		return ownerPlayer;
	}

	/**
	 * Returns the phase of the bomb.
	 * @return the phase of the bomb
	 */
	public BombPhases getPhase() {
		return phase;
	}

	/**
	 * Sets the phase of the bomb.
	 * @param phase phase of the bomb to be set
	 */
	public void setPhase( final BombPhases phase ) {
		this.phase           = phase;
		iterationsDuringPhase = 0;
		
		if ( this.phase == BombPhases.FLYING )
			setIterationCounter( 0 );  // Flying bombs has the first picture of the phase pictures.
	}
	
	/**
	 * Returns the number of iterations during the current phase.
	 * @return the number of iterations during the current phase
	 */
	public int getIterationsDuringPhase() {
		return iterationsDuringPhase;
	}
	
	/**
	 * Increments the number of iterations during the current phase.
	 */
	public void incrementIterationsDuringPhase() {
		iterationsDuringPhase++;
	}
	
	/**
	 * Returns the number of iterations when the bomb was ticking.
	 * @return the number of iterations when the bomb was ticking
	 */
	public int getTickingIterations() {
		return tickingIterations;
	}
	
	/**
	 * Sets the number of iterations when the bomb was ticking.
	 * @param tickingIterations number of iterations to be set when the bomb was ticking
	 */
	public void setTickingIterations( final int tickingIterations ) {
		this.tickingIterations = tickingIterations;
	}

	/**
	 * Returns the x coordinate of the target position in case of a flying bomb.
	 * @return the x coordinate of the target position in case of a flying bomb
	 */
	public int getFlyingTargetPosX() {
		return flyingTargetPosX;
	}

	/**
	 * Sets the x coordiante of the target position in case of a flying bomb.
	 * @param flyingTargetPosX x coordinate of the target position to be set in case of a flying bomb
	 */
	public void setFlyingTargetPosX( final int flyingTargetPosX ) {
		this.flyingTargetPosX = flyingTargetPosX;
	}
	
	/**
	 * Returns the y coordinate of the target position in case of a flying bomb.
	 * @return the y coordinate of the target position in case of a flying bomb
	 */
	public int getFlyingTargetPosY() {
		return flyingTargetPosY;
	}

	/**
	 * Sets the y coordiante of the target position in case of a flying bomb.
	 * @param flyingTargetPosY y coordinate of the target position to be set in case of a flying bomb
	 */
	public void setFlyingTargetPosY( final int flyingTargetPosY) {
		this.flyingTargetPosY = flyingTargetPosY;
	}

	/**
	 * Tells whether this bomb is dead.
	 * @return true if this bomb is dead, false otherwise
	 */
	public boolean isDead() {
		return dead;
	}

	/**
	 * Sets the dead property of the bomb
	 * @param dead dead property of the bomb to be set
	 */
	public void setDead( final boolean dead ) {
		this.dead = dead;
	}

	/**
	 * Tells whether this bomb is about to detonate.
	 * @return true if this bomb is about to detonate; false otherwise
	 */
	public boolean isAboutToDetonate() {
		return aboutToDetonate;
	}

	/**
	 * Sets the aboutToDetonate attribute.
	 * @param aboutToDetonate value of aboutToDetonate to be set
	 */
	public void setAboutToDetonate( final boolean aboutToDetonate ) {
		this.aboutToDetonate = aboutToDetonate;
	}

	/**
	 * Tells whether this bomb has been detonated.
	 * @return true if this bomb has been detonated; false otherwise
	 */
	public boolean isDetonated() {
		return detonated;
	}

	/**
	 * Sets the detonated attribute of the bomb.
	 * @param detonated detonated state to be set
	 */
	public void setDetonated( final boolean detonated ) {
		this.detonated = detonated;
	}

	/**
	 * Returns the range of the bomb.
	 * @return the range of the bomb
	 */
	public int getRange() {
		return range;
	}

	/**
	 * Sets the range of the bomb.
	 * @param range range of bomb to be set
	 */
	public void setRange( final int range ) {
		this.range = range;
	}

	/**
	 * Returns the triggerer player of the bomb.
	 * @return the triggerer player of the bomb
	 */
	public PlayerModel getTriggererPlayer() {
		return triggererPlayer;
	}

	/**
	 * Sets the triggerer player of the bomb.
	 * @param triggererPlayer triggerer player of bomb to be set
	 */
	public void setTriggererPlayer( final PlayerModel triggererPlayer ) {
		this.triggererPlayer = triggererPlayer;
	}
	
}
