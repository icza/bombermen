
/*
 * Created on October 10, 2005
 */

package classes.client.gamecore;

/**
 * Holds constants for defining and calculating the game core.
 * 
 * @author Andras Belicza
 */
public class Consts {

	/** The granularity of a level component. This is the logical size of the level components.<br>
	 *  This SHOULD (MUST) be odd, so level components can have a perfect center point.
	 *  I determined a value which can be divided by many numbers for practical reasons.
	 *  (For example I say bomberman should step through a level component by 9 steps...)
	 *  All other space measurement, speed, dimension and lengths are given based on this.             */
	public static final int LEVEL_COMPONENT_GRANULARITY             = 1155;         // = 3*5*7*11
	
	
	/** Basic speed of a bomberman (no ROLLER_SKATES, no DISEASE).                                     */
	public static final int BOMBERMAN_BASIC_SPEED                   = LEVEL_COMPONENT_GRANULARITY / 6;
	/** Bomberman speed increment for a roller skates item.                                            */
	public static final int BOBMERMAN_ROLLER_SKATES_SPEED_INCREMENT = BOMBERMAN_BASIC_SPEED * 15 / 100;
	/** Maximum speed of a bomberman (no ROLLER_SKATES, no DISEASE).                                   */
	public static final int BOBMERMAN_MAX_SPEED                     = BOMBERMAN_BASIC_SPEED * 3;
	/** Maximum value of player vitality, the completely healthy state.                                */
	public static final int MAX_PLAYER_VITALITY                     = 1000;
	/** Sensitivity of movement correction. Movement correction will affect if the bomberman
	 *  is at least as near to the end of a component as this.
	 *  Usable value is between 0 and LEVEL_COMPONENT_GRANULARITY/2 (there are 2 ends of a component). */
	public static final int MOVEMENT_CORRECTION_SENSITIVITY1        = LEVEL_COMPONENT_GRANULARITY * 5 / 12;
	/** Number of iteratinos before replacing the picked up itmes of a player after he dies.           */
	public static final int DEAD_ITERATIONS_BEFORE_REPLACING_ITEMS  = 80;
	
	/** Vitality of a heart item.                                                                      */
	public static final int HEART_VITALITY                          = MAX_PLAYER_VITALITY / 3; 
	
	
	/** Number of game iterations of a one-time play of bomb phases.                                   */
	public static final int BOMB_ITERATIONS                         = 30;
	/** Flying speed of a bomb.                                                                        */
	public static final int BOMB_FLYING_SPEED                       = LEVEL_COMPONENT_GRANULARITY / 5;
	/** Flying distance of bombs, the minimal distance where they can fall down.                       */
	public static final int BOMB_FLYING_DISTANCE                    = LEVEL_COMPONENT_GRANULARITY * 3;   // Bombs fly 3 components
	/** Primary flying ascendence of a bomb.                                                           */
	public static final int BOMB_FLYING_ASCENDENCE_PRIMARY          = LEVEL_COMPONENT_GRANULARITY * 3 / 2;
	/** Secondary flying ascendence of a bomb.                                                         */
	public static final int BOMB_FLYING_ASCENDENCE_SECONDARY        = LEVEL_COMPONENT_GRANULARITY / 2;
	/** Flying speed of a bomb.                                                                        */
	public static final int BOMB_ROLLING_SPEED                      = LEVEL_COMPONENT_GRANULARITY / 5;
	/** Number of iterations of a bomb detonation (time until a bomb detonates).                       */
	public static final int BOMB_DETONATION_ITERATIONS              = 60;

	/** Number of game iterations of a fire (detonation duration of a bomb).                           */
	public static final int FIRE_ITERATIONS                         = 18;
	/** Range of the super fire (substituting infinite).                                               */
	public static final int SUPER_FIRE_RANGE                        = Integer.MAX_VALUE;
	
	/**
	 * The directions of an entity (can be a player or a bomb).
	 * @author Andras Belicza
	 */
	public enum Directions {
		/** The down direction.  */
		DOWN,
		/** The up direction.    */
		UP,
		/** The right direction. */
		RIGHT,
		/** The left direction.  */
		LEFT;
		
		/**
		 * Returns the opposite of this direction.
		 * @return the opposite of this direction
		 */
		public Directions getOpposite() {
			switch ( this ) {
				case DOWN  : return UP   ;
				case UP    : return DOWN ;
				case RIGHT : return LEFT ;
				case LEFT  : return RIGHT;
			}
			throw new RuntimeException( "WTF?!? Check added new directions!!!" );
		}

		/**
		 * Returns an integer which can be used to identify the horizontal component of the direction,
		 * and can be used to calculate positions ahead in the direction.
		 * @return an integer identifying the horizontal component of the direction:<br>
		 * 		       -1, if this is LEFT,
		 *			    1, if this is RIGHT
		 *              0 otherwise
		 */
		
		public int getXMultiplier() {
			return this == LEFT ? -1 : ( this == RIGHT ? 1 : 0 );
		}

		/**
		 * Returns an integer which can be used to identify the vertical component of the direction,
		 * and can be used to calculate positions ahead in the direction.
		 * @return an integer identifying the vertical component of the direction:<br>
		 * 		       -1, if this is UP,
		 *			    1, if this is DOWN
		 *              0 otherwise
		 */
		public int getYMultiplier() {
			return this == UP ? -1 : ( this == DOWN ? 1 : 0 );
		}

	}
	
	/**
	 * Activities of the bombermen.
	 * @author Andras Belicza
	 */
	public enum Activities {
		/** Standing activity.          */
		STANDING          ( 1 , true  ),
		/** Standing activity.          */
		STANDING_WITH_BOMB( 1 , true  ),
		/** Walking activity.           */
		WALKING           ( 10, true  ),
		/** Walking with bomb activity. */
		WALKING_WITH_BOMB ( 10, true  ),
		/** Kicking activity.           */
		KICKING           ( 6 , false ),
		/** Kicking activity.           */
		KICKING_WITH_BOMB ( 5 , false ),
		/** Punching activity.          */
		PUNCHING          ( 5 , false ),
		/** Picking up activity.        */
		PICKING_UP        ( 6 , false ),
		/** Dying activity.             */
		DYING             ( 30, false );
		
		/** The number of game iterations of the activity for a one-time play.
		 * After that, it may or may not be repeated based on the repeatable attribute.         */
		public final int     activityIterations;
		/** Tells whether this activity is repeatable by itself if player input doesn't change. */
		public final boolean repeatable;
		
		/**
		 * Creates a new Activities.
		 * @param activityIterations the number of game iterations of the activity for a one-time play
		 * @param repeatable         tells whether this activity is repeatable once it has been played over
		 */
		private Activities( final int activityIterations, final boolean repeatable ) {
			this.activityIterations = activityIterations;
			this.repeatable         = repeatable;
		}
	}
	
	/**
	 * Types of the bombs.
	 * @author Andras Belicza
	 */
	public enum BombTypes {
		/** Normal bomb.    */
		NORMAL,
		/** Jelly bomb.     */
		JELLY,
		/** Triggered bomb. */
		TRIGGERED
	}
	
	/**
	 * Phases of the bombs.
	 * @author Andras Belicza
	 */
	public enum BombPhases {
		/** The flying bomb phase, the bomb is punched or thrown away.   */
		FLYING,
		/** The rolling bomb phase, the bomb has been kicked.            */
		ROLLING,
		/** The standing bomb phase, the bomb is not bothered.           */
		STANDING
	}
	
	/**
	 * Shapes of the fire of an explosion.
	 * @author Andras Belicza
	 */
	public enum FireShapes {
		/** Horizontal fire shape. */
		HORIZONTAL,
		/** Vertical fire shape.   */
		VERTICAL,
		/** Crossing fire shape.   */
		CROSSING
	}
	
}
