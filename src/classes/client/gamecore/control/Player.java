
/*
 * Created on November 24, 2005
 */

package classes.client.gamecore.control;

import static classes.client.gamecore.Consts.BOBMERMAN_MAX_SPEED;
import static classes.client.gamecore.Consts.BOBMERMAN_ROLLER_SKATES_SPEED_INCREMENT;
import static classes.client.gamecore.Consts.BOMBERMAN_BASIC_SPEED;
import static classes.client.gamecore.Consts.BOMB_FLYING_DISTANCE;
import static classes.client.gamecore.Consts.DEAD_ITERATIONS_BEFORE_REPLACING_ITEMS;
import static classes.client.gamecore.Consts.HEART_VITALITY;
import static classes.client.gamecore.Consts.LEVEL_COMPONENT_GRANULARITY;
import static classes.client.gamecore.Consts.MAX_PLAYER_VITALITY;
import static classes.client.gamecore.Consts.SUPER_FIRE_RANGE;
import static classes.options.Consts.ACCUMULATEABLE_ITEMS;
import static classes.options.Consts.NEUTRALIZER_ITEMS_MAP;

import java.util.EnumSet;

import classes.client.gamecore.Consts;
import classes.client.gamecore.Consts.Activities;
import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.Consts.BombTypes;
import classes.client.gamecore.Consts.Directions;
import classes.client.gamecore.model.BombModel;
import classes.client.gamecore.model.ModelProvider;
import classes.client.gamecore.model.PlayerModel;
import classes.client.gamecore.model.level.LevelComponent;
import classes.options.Consts.Items;
import classes.options.Consts.PlayerControlKeys;
import classes.options.Consts.Walls;

/**
 * The class implements the control of a player of the GAME (NOT the the application):
 * its calculation, simulation during working and playing.
 * 
 * @author Andras Belicza
 */
public class Player {

	/** The client index where this player belogns to. */
	private int                   clientIndex;
	/** The player index inside of its client.         */
	private final int             playerIndex;
	/** The model of the player.                       */
	private final PlayerModel     model = new PlayerModel();
	/** Reference to a model provider.                 */
	private final ModelProvider   modelProvider;
	/** Reference to a model controller.               */
	private final ModelController modelController;
	
	/**
	 * Creates a new Player.
	 * @param clientIndex     the client index where this player belongs to
	 * @param playerIndex     the player index inside of its client
	 * @param modelProvider   reference to a model provider
	 * @param modelController reference to a model controller
	 */
	public Player( final int clientIndex, final int playerIndex, final ModelProvider modelProvider, final ModelController modelController ) {
		this.clientIndex     = clientIndex;
		this.playerIndex     = playerIndex;
		this.modelProvider   = modelProvider;
		this.modelController = modelController;
	}

	/**
	 * Sets the client index where this player belongs to.
	 * @param clientIndex client index to be set
	 */
	public void setClientIndex( final int clientIndex ) {
		this.clientIndex = clientIndex;
	}
	
	/**
	 * Returns the model of the player.
	 * @return the model of the player
	 */
	public PlayerModel getModel() {
		return model;
	}
	
	/**
	 * Makes basic initializations for starting a new round.
	 * @param posX the x coordinate of the initial position
	 * @param posY the y coordinate of the initial position
	 */
	public void initForNextRound( final int posX, final int posY ) {
		model.setPosX     ( posX                );
		model.setPosY     ( posY                );
		
		model.setDirection( Directions.DOWN     );
		model.setActivity ( Activities.STANDING );
		model.setVitality ( MAX_PLAYER_VITALITY );
		model.setPickedUpBombModel( null );
		
		
        model.accumulateableItemQuantitiesMap.putAll( modelProvider.getLevelModel().getLevelOptions().accumulateableItemQuantitiesMap );
        model.hasNonAccumulateableItemsMap   .putAll( modelProvider.getLevelModel().getLevelOptions().hasNonAccumulateableItemsMap    );
        model.pickedUpAccumulateableItems    .clear();
        model.pickedUpNonAccumulateableItems .clear();

		model.setPlacableTriggeredBombs( model.hasNonAccumulateableItemsMap.get( Items.TRIGGER ) ? model.accumulateableItemQuantitiesMap.get( Items.BOMB ) : 0 );

		for ( final PlayerControlKeys playerControlKey : PlayerControlKeys.values() ) {
			// Twice, so we delete the last key state also
        	model.setControlKeyState( playerControlKey, false );
			model.setControlKeyState( playerControlKey, false );
        }
	}
	
	/**
	 * Performs operations which are requried by passing the time.
	 * Increases the number of iterations during the current activity,
	 * and switches to a new activity if it is needed to.
	 */
	public void nextIteration() {
		if ( model.getActivity() == Activities.DYING ) {
			model.nextIteration(); // We're just counting, we will drop out the picked up items.
			
			if ( model.getIterationCounter() == DEAD_ITERATIONS_BEFORE_REPLACING_ITEMS ) {
				for ( final Items item : model.pickedUpNonAccumulateableItems )
					modelController.replaceItemOnLevel( item );
				for ( final Items item : model.pickedUpAccumulateableItems )
					modelController.replaceItemOnLevel( item );
			}
		}
		else {
			processActionsAndHandleActivityTransitions();
			
			stepPlayer( 0 );
	
			if ( model.getIterationCounter() + 1 < model.getActivity().activityIterations )
				model.nextIteration();
			else
				if ( model.getActivity().repeatable )
					model.setIterationCounter( 0 );
	
			// These keys can be interpreted as transitions also, we have to care about last states by "stepping" them (implemented by resetting them)
			model.setControlKeyState( PlayerControlKeys.FUNCTION1, model.getControlKeyState( PlayerControlKeys.FUNCTION1 ) );
			model.setControlKeyState( PlayerControlKeys.FUNCTION2, model.getControlKeyState( PlayerControlKeys.FUNCTION2 ) );
		}
	}
	
	/**
	 * Proccesses the player actions and handles the activitry transitions.
	 */
	private void processActionsAndHandleActivityTransitions() {
		switch ( model.getActivity() ) {
			
			case STANDING :
				if ( model.isDirectionKeyPressed() )
					model.setActivity( Activities.WALKING );
				if ( model.getControlKeyState( PlayerControlKeys.FUNCTION1 ) && !model.getLastControlKeyState( PlayerControlKeys.FUNCTION1 ) )
					handleFunction1WithoutBomb();
				else if ( model.getControlKeyState( PlayerControlKeys.FUNCTION2 ) && !model.getLastControlKeyState( PlayerControlKeys.FUNCTION2 ) )
					handleFunction2();
				break;
			
			case STANDING_WITH_BOMB :
				if ( model.isDirectionKeyPressed() )
					model.setActivity( Activities.WALKING_WITH_BOMB );
				if ( !model.getControlKeyState( PlayerControlKeys.FUNCTION1 ) )
					throwBombAway();
				break;
			
			case WALKING :
				if ( !model.isDirectionKeyPressed() )
					model.setActivity( Activities.STANDING );
				if ( model.getControlKeyState( PlayerControlKeys.FUNCTION1 ) && !model.getLastControlKeyState( PlayerControlKeys.FUNCTION1 ) )
					handleFunction1WithoutBomb();
				else if ( model.getControlKeyState( PlayerControlKeys.FUNCTION2 ) && !model.getLastControlKeyState( PlayerControlKeys.FUNCTION2 ) )
					handleFunction2();
				break;
			
			case WALKING_WITH_BOMB :
				if ( !model.isDirectionKeyPressed() )
					model.setActivity( Activities.STANDING_WITH_BOMB );
				if ( !model.getControlKeyState( PlayerControlKeys.FUNCTION1 ) )
					throwBombAway();
				break;
			
			case KICKING :
				if ( model.getIterationCounter() == model.getActivity().activityIterations - 1 )
					model.setActivity( model.isDirectionKeyPressed() ? Activities.WALKING : Activities.STANDING );
				break;
			
			case KICKING_WITH_BOMB :
				if ( model.getIterationCounter() == model.getActivity().activityIterations - 1 )
					model.setActivity( model.isDirectionKeyPressed() ? Activities.WALKING_WITH_BOMB : Activities.STANDING_WITH_BOMB );
				break;
			
			case PUNCHING :
				if ( model.getIterationCounter() == model.getActivity().activityIterations - 1 )
					model.setActivity( model.isDirectionKeyPressed() ? Activities.WALKING : Activities.STANDING );
				break;
			
			case PICKING_UP :
				if ( model.getIterationCounter() == model.getActivity().activityIterations - 1 ) {
					model.setActivity( model.isDirectionKeyPressed() ? Activities.WALKING_WITH_BOMB : Activities.STANDING_WITH_BOMB );
				}
				break;
			
			case DYING :
				break;

		}
	}
	
	/**
	 * Handles Function 1 key when we don't hold a bomb.<br>
	 * This means we place a bomb, if there is none at the component where we stand at (and we have bomb of course),
	 * or optionally places a lot more in front of us if there is one bomb under us	and if we have Items.BOMB_SPRINKLE
	 * or if we dont have Items.BOMB_SPRINKLE but we have Items.BLUE_GLOVES, then we pick up the bomb
	 * being under us.  
	 */
	private void handleFunction1WithoutBomb() {
		final int playerComponentPosX = model.getComponentPosX();
		final int playerComponentPosY = model.getComponentPosY();
		int       componentPosX       = playerComponentPosX;
		int       componentPosY       = playerComponentPosY;
		int       maxPlacableBombs    = Math.min( 1, model.accumulateableItemQuantitiesMap.get( Items.BOMB ) );

		final Integer bombIndexAtComponentPosition = modelProvider.getBombIndexAtComponentPosition( componentPosX, componentPosY );
		if ( bombIndexAtComponentPosition != null ) {
			if ( model.hasNonAccumulateableItemsMap.get( Items.BLUE_GLOVES ) ) {
				if ( modelProvider.getBombModels().get( bombIndexAtComponentPosition ).getOwnerPlayer() != model )
					return;  // We can only pick up our own bombs
				model.setPickedUpBombModel( modelProvider.getBombModels().get( bombIndexAtComponentPosition ) );
				modelController.removeBombAtIndex( bombIndexAtComponentPosition );
				model.setActivity( Activities.PICKING_UP );
				return;
			}
			if ( model.hasNonAccumulateableItemsMap.get( Items.BOMB_SPRINKLE ) ) {
				maxPlacableBombs = model.accumulateableItemQuantitiesMap.get( Items.BOMB );
				// The position of the first bomb is ahead of us.
				componentPosX += model.getDirectionXMultiplier();
				componentPosY += model.getDirectionYMultiplier();
			}
		}
			
		final LevelComponent[][] levelComponents = modelProvider.getLevelModel().getComponents();
		
		int bombsCount = model.accumulateableItemQuantitiesMap.get( Items.BOMB );
		for ( int i = 0; i < maxPlacableBombs; i++ ) {
			final Walls wallInPosition = levelComponents[ componentPosY ][ componentPosX ].getWall();
			
			if ( modelProvider.isBombAtComponentPosition( componentPosX, componentPosY )
				 || wallInPosition != Walls.EMPTY || wallInPosition == Walls.EMPTY && levelComponents[ componentPosY ][ componentPosX ].getItem() != null )
				break;
			if ( componentPosX != playerComponentPosX || componentPosY != playerComponentPosY )
				if ( modelProvider.isPlayerAtComponentPositionExcludePlayer( componentPosX, componentPosY, model ) )
					break;
			
			model.accumulateableItemQuantitiesMap.put( Items.BOMB, --bombsCount );
			final Bomb      newBomb      = new Bomb( model, modelProvider, modelController );
			final BombModel newBombModel = newBomb.getModel();
			newBombModel.setRange( model.hasNonAccumulateableItemsMap.get( Items.SUPER_FIRE ) ? SUPER_FIRE_RANGE : model.accumulateableItemQuantitiesMap.get( Items.FIRE ) + 1 );
			newBombModel.setPosX( componentPosX * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
			newBombModel.setPosY( componentPosY * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );

			if ( model.hasNonAccumulateableItemsMap.get( Items.JELLY ) )
				newBombModel.setType( BombTypes.JELLY );
			else if ( model.hasNonAccumulateableItemsMap.get( Items.TRIGGER ) && model.getPlacableTriggeredBombs() > 0 ) {
				newBombModel.setType( BombTypes.TRIGGERED );
				model.setPlacableTriggeredBombs( model.getPlacableTriggeredBombs() - 1 );
			}
			else
				newBombModel.setType( BombTypes.NORMAL );
			
			modelController.addNewBomb( newBomb );

			componentPosX += model.getDirectionXMultiplier();
			componentPosY += model.getDirectionYMultiplier();
		}
	}
	
	/**
	 * Throws away the picked up bomb.
	 */
	private void throwBombAway() {
		final BombModel bombModel = model.getPickedUpBombModel();
		
		bombModel.setTickingIterations( 0 );                     // Thrown away bombs start ticking from the beginning again.
		bombModel.setDirection        ( model.getDirection() );  // We throw in our direction
		bombModel.setPosX             ( model.getComponentPosX() * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
		bombModel.setPosY             ( model.getComponentPosY() * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );

		bombModel.setPhase            ( BombPhases.FLYING );

		modelController.validateAndSetFlyingTargetPosX( bombModel, bombModel.getPosX() + bombModel.getDirectionXMultiplier() * BOMB_FLYING_DISTANCE );
		modelController.validateAndSetFlyingTargetPosY( bombModel, bombModel.getPosY() + bombModel.getDirectionYMultiplier() * BOMB_FLYING_DISTANCE );
		
		modelController.addNewBomb( new Bomb( bombModel, modelProvider, modelController ) );
		model.setPickedUpBombModel( null );

		model.setActivity( model.getActivity() == Activities.STANDING_WITH_BOMB ? Activities.STANDING : Activities.WALKING );
	}
	
	/**
	 * Handles Function 2.<br>
	 * This entirely depens on which of the items we have.<br>
	 * If we have Items.BOXING_GLOVES, we punch the bomb in front of us.
	 * If we have Items.TRIGGER, we detonate the earliest triggered bomb.
	 * If we have Items.WALL_BUILDING, we build a wall in front of us.
	 */
	private void handleFunction2() {
		// First of all, function 2 stops normal rolling bombs and triggers triggerable bombs.
		for ( final BombModel bombModel : modelProvider.getBombModels() )
			if ( bombModel.getOwnerPlayer() == model && bombModel.getType() == BombTypes.NORMAL && bombModel.getPhase() == BombPhases.ROLLING ) {
				bombModel.setPhase( BombPhases.STANDING );
				bombModel.alignPosXToComponentCenter();
				bombModel.alignPosYToComponentCenter();
			}
		
		
		if ( model.hasNonAccumulateableItemsMap.get( Items.BOXING_GLOVES ) ) {
			model.setActivity( Activities.PUNCHING );
			
			final Integer bombIndexAhead = modelProvider.getBombIndexAtComponentPosition( model.getComponentPosX() + model.getDirectionXMultiplier(), model.getComponentPosY() + model.getDirectionYMultiplier() );
			if ( bombIndexAhead != null ) {
				final BombModel bombModel = modelProvider.getBombModels().get( bombIndexAhead );
				bombModel.setDirection( model.getDirection() );  // We punch in our direction
				bombModel.setPosX     ( bombModel.getComponentPosX() * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
				bombModel.setPosY     ( bombModel.getComponentPosY() * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
				bombModel.setPhase    ( BombPhases.FLYING );

				modelController.validateAndSetFlyingTargetPosX( bombModel, bombModel.getPosX() + bombModel.getDirectionXMultiplier() * BOMB_FLYING_DISTANCE );
				modelController.validateAndSetFlyingTargetPosY( bombModel, bombModel.getPosY() + bombModel.getDirectionYMultiplier() * BOMB_FLYING_DISTANCE );
			}
		}
		
		else if ( model.hasNonAccumulateableItemsMap.get( Items.TRIGGER ) ) {
			for ( final BombModel bombModel : modelProvider.getBombModels() )
				if ( bombModel.getOwnerPlayer() == model && bombModel.getType() == BombTypes.TRIGGERED && bombModel.getPhase() != BombPhases.FLYING ) {
					bombModel.setAboutToDetonate( true );
					break; // Function2 only detonates 1 triggered bomb
				}
		}
		
		else if ( model.hasNonAccumulateableItemsMap.get( Items.WALL_BUILDING ) ) {
			final int componentPosX = model.getComponentPosX() + model.getDirectionXMultiplier();
			final int componentPosY = model.getComponentPosY() + model.getDirectionYMultiplier();
			if ( isComponentPositionFreeForWallBuilding( componentPosX, componentPosY ) )
				modelProvider.getLevelModel().getComponents()[ componentPosY ][ componentPosX ].setWall( Walls.BRICK );
		}
	}
	
	/**
	 * Tells whether a component is free for building a brick wall on it.<br>
	 * A brick wall can be built in a position if it's empty (no bombs, no players, no item can be found on that),
	 * and no object is hanging down into it (bombs and players can't even hang down into it).
	 * @param componentPosX x coordinate of the component to check
	 * @param componentPosY y coordinate of the component to check
	 * @return true, if a brick wall can be built in the specified position; false otherwise
	 */
	private boolean isComponentPositionFreeForWallBuilding( final int componentPosX, final int componentPosY ) {
		final LevelComponent levelComponent = modelProvider.getLevelModel().getComponents()[ componentPosY ][ componentPosX ];
        
		if ( levelComponent.getWall() != Walls.EMPTY || levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null )
        	return false;
        	
		if ( modelProvider.isBombAtComponentPosition( componentPosX, componentPosY ) )
			return false;
		
		if ( modelProvider.isPlayerAtComponentPositionExcludePlayer( componentPosX, componentPosY, null ) )
			return false;

		return true;
	}

	/**
	 * Determines the direction of the player, and makes a step.<br>
	 * This method can invoke itself if it realizes that bomberman step had to be cut
	 * in order of movement correction to be able to turn in a direction.
	 * By compensation of the cut, the player will be stepped again.
	 * 
	 * @param invocationDepth the invocation depth 'cause this method can invoce itself
	 */
	private void stepPlayer( final int invocationDepth ) {
		boolean wasStepCutInOrderToTurn = false;
		
		final Activities activity = model.getActivity();  // Shortcut to the players activity
		
		// if player can step based on the activity
		if ( activity == Activities.WALKING || activity == Activities.WALKING_WITH_BOMB || activity == Activities.PUNCHING && model.isDirectionKeyPressed() ) {
			// First of all we determine in what direction we will face and/or move to
			boolean movementCorrectionActivated = determineNewDirection();
			
			int speed = BOMBERMAN_BASIC_SPEED + model.accumulateableItemQuantitiesMap.get( Items.ROLLER_SKATES ) * BOBMERMAN_ROLLER_SKATES_SPEED_INCREMENT;
			if ( speed > BOBMERMAN_MAX_SPEED )
				speed = BOBMERMAN_MAX_SPEED; 
			
			boolean needsToBeContained = false;
			final int posXAhead = model.getPosX() + model.getDirectionXMultiplier() * LEVEL_COMPONENT_GRANULARITY;
			final int posYAhead = model.getPosY() + model.getDirectionYMultiplier() * LEVEL_COMPONENT_GRANULARITY;
			if ( movementCorrectionActivated  // If movement correction is activated, we cannot pass the center of the next column
					|| !movementCorrectionActivated && !canPlayerStepToPosition( posXAhead, posYAhead ) ) // We cannot also, if we have obstruction ahead
				needsToBeContained = true;
			
			if ( needsToBeContained ) {  
				int newSpeed = -1;
				final boolean bombAhead = modelProvider.isBombAtComponentPosition( posXAhead / LEVEL_COMPONENT_GRANULARITY, posYAhead / LEVEL_COMPONENT_GRANULARITY );
				
				switch ( model.getDirection() ) {
					case LEFT  :
						if ( bombAhead && model.getPosX() % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY/2 )
							newSpeed = 0;
						else
							newSpeed = speed - ( LEVEL_COMPONENT_GRANULARITY / 2 - ( model.getPosX() - speed ) % LEVEL_COMPONENT_GRANULARITY ); break;
					case RIGHT :
						if ( bombAhead && model.getPosX() % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY/2 )
							newSpeed = 0;
						else
							newSpeed = speed - ( ( model.getPosX() + speed ) % LEVEL_COMPONENT_GRANULARITY - LEVEL_COMPONENT_GRANULARITY / 2 ); break;
					case UP    :
						if ( bombAhead && model.getPosY() % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY/2 )
							newSpeed = 0;
						else
							newSpeed = speed - ( LEVEL_COMPONENT_GRANULARITY / 2 - ( model.getPosY() - speed ) % LEVEL_COMPONENT_GRANULARITY ); break;
					case DOWN  :
						if ( bombAhead && model.getPosY() % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY/2 )
							newSpeed = 0;
						else
							newSpeed = speed - ( ( model.getPosY() + speed ) % LEVEL_COMPONENT_GRANULARITY - LEVEL_COMPONENT_GRANULARITY / 2 ); break;
				}

				if ( newSpeed >= 0 && newSpeed < speed ) {   // If it's negative, it's beyond one level component, but that case we surly dont need to change speed
					if ( movementCorrectionActivated )
						wasStepCutInOrderToTurn = true;
					speed = newSpeed;
				}
			}
			
			if ( speed > 0 ) {   // If we can, we step...
				// And finally we make the step
				model.setPosX( model.getPosX() + model.getDirectionXMultiplier() * speed );
				model.setPosY( model.getPosY() + model.getDirectionYMultiplier() * speed );
				checkAndHandleItemPickingUp();
			}                    // ...else we check for kick
			else if ( model.hasNonAccumulateableItemsMap.get( Items.BOOTS ) )
				tryToKick();
		}
		
		if ( wasStepCutInOrderToTurn && invocationDepth == 0 )
			stepPlayer( invocationDepth + 1 );  // Without this it gives a feeling of stucking for a moment on turns!!
	}

	/**
	 * Check whether we stand on an item, and handles the picking up.
	 */
	private void checkAndHandleItemPickingUp() {
		final LevelComponent levelComponent = modelProvider.getLevelModel().getComponents()[ model.getComponentPosY() ][ model.getComponentPosX() ];
		
		if ( levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null && levelComponent.fireModelVector.isEmpty() ) {
			final Items item = levelComponent.getItem();
			
			if ( ACCUMULATEABLE_ITEMS.contains( item ) ) {
				if ( item != Items.HEART ) {  // We don't accumulate HEARTs.
					model.accumulateableItemQuantitiesMap.put( item, model.accumulateableItemQuantitiesMap.get( item ) + 1 );
					model.pickedUpAccumulateableItems    .add( item );
				}
			}
			else {
				model.hasNonAccumulateableItemsMap  .put( item, true );
				if ( !model.pickedUpNonAccumulateableItems.contains( item ) )
					model.pickedUpNonAccumulateableItems.add( item );
				
				final EnumSet< Items > neutralizedItems = NEUTRALIZER_ITEMS_MAP.get( item );
				if ( neutralizedItems != null ) // Can be null in case of diseases
					for ( final Items neutralizedItem : neutralizedItems )
						if ( model.hasNonAccumulateableItemsMap.get( neutralizedItem ) ) {
							
							model.hasNonAccumulateableItemsMap.put( neutralizedItem, false );
							if ( model.pickedUpNonAccumulateableItems.remove( neutralizedItem ) )
								modelController.replaceItemOnLevel( neutralizedItem );
							
							// If Trigger has been thrown out the window, we have to transform triggered bombs back to normal or jelly.
							if ( neutralizedItem == Items.TRIGGER )
								for ( final BombModel bombModel : modelProvider.getBombModels() )
									if ( bombModel.getOwnerPlayer() == model && bombModel.getType() == BombTypes.TRIGGERED ) {
										bombModel.setType( model.hasNonAccumulateableItemsMap.get( Items.JELLY ) ? BombTypes.JELLY : BombTypes.NORMAL );
										bombModel.setIterationCounter ( 0 );
										bombModel.setTickingIterations( 0 );
									}
						}
			}
			
			levelComponent.setItem( null );
			
			// Special things to do when an item is picked up
			switch ( item ) {
				case TRIGGER :
					model.setPlacableTriggeredBombs( model.accumulateableItemQuantitiesMap.get( Items.BOMB ) );
					for ( final BombModel bombModel : modelProvider.getBombModels() )
						if ( bombModel.getOwnerPlayer() == model )
							model.setPlacableTriggeredBombs( model.getPlacableTriggeredBombs() + 1 );
					break;
				case BOMB :
					if ( model.hasNonAccumulateableItemsMap.get( Items.TRIGGER ) )
						model.setPlacableTriggeredBombs( model.getPlacableTriggeredBombs() + 1 );
					break;
				case HEART : 
					model.setVitality( Math.min( model.getVitality() + HEART_VITALITY, MAX_PLAYER_VITALITY ) );
					break;
			}
		}
	}
	
	/**
	 * Determines and sets the new direction of the player.
	 * @return true if movement correction have to be used for the new direction; false otherwise
	 */
	private boolean determineNewDirection() {
		final int posX = model.getPosX();   // Shortcut to the posX of the player 
		final int posY = model.getPosY();   // Shortcut to the posY of the player

		// There are 2 kinds of movement correction:
		// The first is taking us away from the component where we stand on in order to turn on the next component.
		// The second is taking us toward the center of the component we're standing on in order to be able to turn on it.
		
		final int movementCorrectionSensitivity = LEVEL_COMPONENT_GRANULARITY * modelProvider.getClientsPublicClientOptions().get( clientIndex ).movementCorrectionSensitivities[ playerIndex ] / 200;
		
		if ( model.getControlKeyState( PlayerControlKeys.DOWN  ) ) {
			model.setDirection( Directions.DOWN );

			// The first kind of movement correction
			if ( !canPlayerStepToPosition( posX, posY + (LEVEL_COMPONENT_GRANULARITY/2+1) ) ) {    // The specified direction is unreachable for movement, try the movement correction function...
				if ( posX % LEVEL_COMPONENT_GRANULARITY <  movementCorrectionSensitivity ) // If movement correction can be activated in one of the directions
					if ( canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.LEFT );
						return true;  // Movement correction is ACTIVATED
					}

				if ( posX % LEVEL_COMPONENT_GRANULARITY >= LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity ) // If movement correction can be activated in the other direction
					if ( canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.RIGHT );
						return true;  // Movement correction is ACTIVATED
					}
			}
			// The second kind of movement correction
			else {                    // We could move the specified direction, but we have a side obstrucion and we're closer than LEVEL_COMPONENT_GRANULARITY/2 
				if ( posX % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.RIGHT );
						return true;  // Movement correction is ACTIVATED
					}
				if ( posX % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.LEFT );
						return true;  // Movement correction is ACTIVATED
					}
			}
		}
		else if ( model.getControlKeyState( PlayerControlKeys.UP    ) ) {
			model.setDirection( Directions.UP );

			if ( !canPlayerStepToPosition( posX, posY - (LEVEL_COMPONENT_GRANULARITY/2+1) ) ) {    // The specified direction is unreachable for movement, try the movement correction function...
				if ( posX % LEVEL_COMPONENT_GRANULARITY <  movementCorrectionSensitivity ) // If movement correction can be activated in one of the directions
					if ( canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.LEFT );
						return true;  // Movement correction is ACTIVATED
					}

				if ( posX % LEVEL_COMPONENT_GRANULARITY >= LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity ) // If movement correction can be activated in the other direction
					if ( canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.RIGHT );
						return true;  // Movement correction is ACTIVATED
					}
			}
			// The second kind of movement correction
			else {                    // We could move the specified direction, but we have a side obstrucion and we're closer than LEVEL_COMPONENT_GRANULARITY/2 
				if ( posX % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.RIGHT );
						return true;  // Movement correction is ACTIVATED
					}
				if ( posX % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.LEFT );
						return true;  // Movement correction is ACTIVATED
					}
			}
		}
		else if ( model.getControlKeyState( PlayerControlKeys.LEFT  ) ) {
			model.setDirection( Directions.LEFT );

			if ( !canPlayerStepToPosition( posX - (LEVEL_COMPONENT_GRANULARITY/2+1), posY ) ) {    // The specified direction is unreachable for movement, try the movement correction function...
				if ( posY % LEVEL_COMPONENT_GRANULARITY <  movementCorrectionSensitivity ) // If movement correction can be activated in one of the directions
					if ( canPlayerStepToPosition( posX, posY - LEVEL_COMPONENT_GRANULARITY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.UP );
						return true;  // Movement correction is ACTIVATED
					}

				if ( posY % LEVEL_COMPONENT_GRANULARITY >= LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity ) // If movement correction can be activated in the other direction
					if ( canPlayerStepToPosition( posX, posY + LEVEL_COMPONENT_GRANULARITY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.DOWN );
						return true;  // Movement correction is ACTIVATED
					}
			}
			// The second kind of movement correction
			else {                    // We could move the specified direction, but we have a side obstrucion and we're closer than LEVEL_COMPONENT_GRANULARITY/2 
				if ( posY % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.DOWN );
						return true;  // Movement correction is ACTIVATED
					}
				if ( posY % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX - LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.UP );
						return true;  // Movement correction is ACTIVATED
					}
			}
		}
		else if ( model.getControlKeyState( PlayerControlKeys.RIGHT ) ) {
			model.setDirection( Directions.RIGHT );

			if ( !canPlayerStepToPosition( posX + (LEVEL_COMPONENT_GRANULARITY/2+1), posY ) ) {    // The specified direction is unreachable for movement, try the movement correction function...
				if ( posY % LEVEL_COMPONENT_GRANULARITY <  movementCorrectionSensitivity ) // If movement correction can be activated in one of the directions
					if ( canPlayerStepToPosition( posX, posY - LEVEL_COMPONENT_GRANULARITY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.UP );
						return true;  // Movement correction is ACTIVATED
					}

				if ( posY % LEVEL_COMPONENT_GRANULARITY >= LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity ) // If movement correction can be activated in the other direction
					if ( canPlayerStepToPosition( posX, posY + LEVEL_COMPONENT_GRANULARITY )  // If the direction in which movement correction would take us is free..
						&& canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) { // ...and and the position where the movement correction wants to take us to is allowed
						model.setDirection( Directions.DOWN );
						return true;  // Movement correction is ACTIVATED
					}
			}
			// The second kind of movement correction
			else {                    // We could move the specified direction, but we have a side obstrucion and we're closer than LEVEL_COMPONENT_GRANULARITY/2 
				if ( posY % LEVEL_COMPONENT_GRANULARITY < LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY - LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.DOWN );
						return true;  // Movement correction is ACTIVATED
					}
				if ( posY % LEVEL_COMPONENT_GRANULARITY > LEVEL_COMPONENT_GRANULARITY / 2 )
					if ( !canPlayerStepToPosition( posX + LEVEL_COMPONENT_GRANULARITY, posY + LEVEL_COMPONENT_GRANULARITY ) ) {
						model.setDirection( Directions.UP );
						return true;  // Movement correction is ACTIVATED
					}
			}
		}


		return false;  // No movement correction have to be used
	}
	
	/**
	 * Checks whether the player can step onto a component of the level specified by position.<br>
	 * Implementation is simply calling the canPlayerStepOntoComponent() with the component coordiantes
	 * specified by the position.
	 * @param posX x coordinate of the position
	 * @param posY y coordinate of the position
	 * @return true, if the player can step onto the component specified by the position; false otherwise
	 */
	private boolean canPlayerStepToPosition( final int posX, final int posY ) {
		final int componentPosX = posX / LEVEL_COMPONENT_GRANULARITY;
		final int componentPosY = posY / LEVEL_COMPONENT_GRANULARITY;

		final Walls wall = modelProvider.getLevelModel().getComponents()[ componentPosY ][ componentPosX ].getWall();
		
		if ( model.hasNonAccumulateableItemsMap.get( Items.WALL_CLIMBING ) ) {
			if ( wall == Walls.CONCRETE )
				return false;
		}
		else {
			if ( wall != Walls.EMPTY )
				return false;
		}
		
		if ( modelProvider.isBombAtComponentPosition( componentPosX, componentPosY ) )
			return false;
		
		return true;
	}

	/**
	 * Tries to kick.
	 */
	private void tryToKick() {
		final int componentPosXAhead = model.getComponentPosX() + model.getDirectionXMultiplier();
		final int componentPosYAhead = model.getComponentPosY() + model.getDirectionYMultiplier();

		final Integer bombIndexAhead = modelProvider.getBombIndexAtComponentPosition( componentPosXAhead, componentPosYAhead );
		if ( bombIndexAhead == null ) // There isn't bomb to kick..
			return;
		
		final BombModel bombModel = modelProvider.getBombModels().get( bombIndexAhead );
		
		final int componentPosXAheadAhead = componentPosXAhead + model.getDirectionXMultiplier();
		final int componentPosYAheadAhead = componentPosYAhead + model.getDirectionYMultiplier();
		
		if ( !modelController.canBombRollToComponentPosition( bombModel, componentPosXAheadAhead, componentPosYAheadAhead ) )
			return;
		
		
		// Activity can be PUNCHING!!!!
		model.setActivity( model.getActivity() == Activities.WALKING_WITH_BOMB ? Activities.KICKING_WITH_BOMB : Activities.KICKING );
		bombModel.setPhase( BombPhases.ROLLING );
		bombModel.setDirection( model.getDirection() );  // We punch in our direction

		// We align the bomb to the center based on the kicking direction
		if ( bombModel.getDirectionXMultiplier() != 0 )
			bombModel.alignPosYToComponentCenter();
		if ( bombModel.getDirectionYMultiplier() != 0 )
			bombModel.alignPosXToComponentCenter();
	}
	
	
}
