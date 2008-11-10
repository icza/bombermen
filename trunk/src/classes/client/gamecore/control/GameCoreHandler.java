
/*
 * Created on October 9, 2004
 */

package classes.client.gamecore.control;

import classes.GameManager;
import classes.MainFrame;
import classes.MainComponentHandler;
import classes.AbstractAnimationMainComponentHandler;
import classes.client.graphics.AnimationDatas;
import classes.client.graphics.GraphicsManager;
import java.util.Random;
import java.util.StringTokenizer;

import static classes.client.gamecore.Consts.FIRE_ITERATIONS;
import static classes.client.gamecore.Consts.LEVEL_COMPONENT_GRANULARITY;
import static classes.client.gamecore.Consts.MAX_PLAYER_VITALITY;
import static classes.options.ServerComponentOptions.RANDOMLY_GENERATED_LEVEL_NAME;
import static classes.options.Consts.PlayerControlKeys;
import classes.options.Consts.Items;
import classes.options.Consts.Walls;
import classes.options.model.LevelOptions;
import classes.options.model.PublicClientOptions;
import classes.options.model.ServerOptions;
import classes.utils.GeneralStringTokenizer;
import classes.client.gamecore.Consts;
import classes.client.gamecore.Consts.Activities;
import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.Consts.Directions;
import classes.client.gamecore.Consts.FireShapes;
import classes.client.gamecore.control.Player;
import classes.client.gamecore.control.Bomb;
import classes.client.gamecore.model.BombModel;
import classes.client.gamecore.model.FireModel;
import classes.client.gamecore.model.ModelProvider;
import classes.client.gamecore.model.PlayerModel;
import classes.client.gamecore.control.Level;
import classes.client.gamecore.model.level.LevelComponent;
import classes.client.gamecore.model.level.LevelModel;
import static classes.options.Consts.Walls;
import java.util.Vector;

/**
 * The class which handles the core of the game: manages rounds and the running of a game.
 *
 * @author Andras Belicza
 */
public class GameCoreHandler implements ModelProvider, ModelController {

    /** Reference to the game manager.                                                            */
    private final GameManager                   gameManager;
    /** Reference to the main frame.                                                              */
    private final MainFrame                     mainFrame;
    /** The global server options.                                                                */
    private final ServerOptions                 globalServerOptions;
    /** Received level model from the server (if level is set to randomly generated, this can be null). */
    private final LevelModel                    receivedLevelModel;
    /** Random object to be used when we generate random datas.                                   */
    private final Random                        random;
    /** Vector of public client options of the clients.                                           */
    private final Vector< PublicClientOptions > clientsPublicClientOptions;
    /** Our client index, our place among the clients.                                            */
    private  int                                ourClientIndex;
    
    
    /** Level of the game.                                                                        */
    private Level                               level;
    /** The Player objects of the game.
     * Every client has a player array, and this is the vector of these arrays.                   */
    private Vector< Player[] >                  clientsPlayers;
    /** This is a shortcut for the models of the players of all clients.                          */
    private Vector< PlayerModel[] >             clientsPlayerModels;
    /** The bombs of the game.                                                                    */
    private Vector< Bomb >                      bombs;
    /** Shortcut for the models of the bombs.                                                     */
    private Vector< BombModel >                 bombModels;
    
    
    /** Handler of the main component being the draw game animation component.                    */
    private final MainComponentHandler drawGameAnimationMainComponentHandler;
    /** Handler of the main component being the losing animation component.                       */
    private final MainComponentHandler losingAnimationMainComponentHandler;
    /** Handler of the main component being the winning animation component.                      */
    private final MainComponentHandler winningAnimationMainComponentHandler;

    
    /**
     * Creates a new GameCoreHandler. A new GameCoreHandler is created for every new game
     * (but used only one for the rounds of a game).<br>
     * 
     * @param gameManager                reference to the game manager
     * @param mainFrame                  reference to the main frame
     * @param globalServerOptions        the global server options
     * @param levelModel                 level model where game should be played (if level is set to randomly generated, this can be null)
     * @param random                     a random object to be used when we generate random data (these are seeded to the same value at all clients)
     * @param clientsPublicClientOptions vector of public client options of the clients
     * @param ourClientIndex             our client index, our place among the clients
     */
    public GameCoreHandler( final GameManager gameManager, final MainFrame mainFrame, final ServerOptions globalServerOptions, final LevelModel levelModel, final Random random, final Vector< PublicClientOptions > clientsPublicClientOptions, final int ourClientIndex ) {
        this.gameManager                = gameManager;
        this.mainFrame                  = mainFrame;
        this.globalServerOptions        = globalServerOptions;
        this.receivedLevelModel         = levelModel;
        this.random                     = random;
        this.clientsPublicClientOptions = clientsPublicClientOptions;
        this.ourClientIndex             = ourClientIndex;
        
        clientsPlayers      = new Vector< Player     [] >( this.clientsPublicClientOptions.size() );
        clientsPlayerModels = new Vector< PlayerModel[] >( this.clientsPublicClientOptions.size() );
        for ( int i = 0; i < this.clientsPublicClientOptions.size(); i++ ) {
        	final PublicClientOptions publicClientOptions =  this.clientsPublicClientOptions.get( i );
        	final Player[]      players      = new Player     [ publicClientOptions.playerNames.length ];
        	final PlayerModel[] playerModels = new PlayerModel[ publicClientOptions.playerNames.length ];
        	
        	for ( int j = 0; j < players.length; j++ ) {
        		players     [ j ] = new Player( i, j, this, this );
        		playerModels[ j ] = players[ j ].getModel();
        	}
        	
        	clientsPlayers     .add( players      );
        	clientsPlayerModels.add( playerModels );
        }
        
        drawGameAnimationMainComponentHandler = new AbstractAnimationMainComponentHandler( this.mainFrame ) {
            protected AnimationDatas getNewAnimationDatas() {
                return GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
            }
        };
        losingAnimationMainComponentHandler   = new AbstractAnimationMainComponentHandler( this.mainFrame ) {
            protected AnimationDatas getNewAnimationDatas() {
                return GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
            }
        };
        winningAnimationMainComponentHandler  = new AbstractAnimationMainComponentHandler( this.mainFrame ) {
            protected AnimationDatas getNewAnimationDatas() {
                return GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
            }
        };
    }

    /**
     * This method is called when a client leaves the game.
     * @param clientIndex index of the client who has just left the game
     */
    public void aClientHasLeftTheGame( final int clientIndex ) {
    	clientsPlayers     .removeElementAt( clientIndex );
    	clientsPlayerModels.removeElementAt( clientIndex );
        
    	if ( ourClientIndex > clientIndex )
    		ourClientIndex--;
    	
    	for ( int i = clientIndex; i < clientsPlayers.size(); i++ )
    		for ( final Player player : clientsPlayers.get( i ) )
    			player.setClientIndex( i );
    }
    
    /**
     * Inits the next round.
     */
    public void initNextRound() {
        level = globalServerOptions.levelName.equals( RANDOMLY_GENERATED_LEVEL_NAME ) ?
        		generateRandomLevel() :
        		new Level( receivedLevelModel.cloneLevel(), this, this );

   		final LevelComponent[][] levelComponents = level.getModel().getComponents();

   		// The level is surrounded with concrete walls, we dont even try there...
		final int maxComponentPosX = levelComponents[ 0 ].length - 2;
		final int maxComponentPosY = levelComponents     .length - 2;
        
		final Vector< int[] > generatedStartPositions = new Vector< int[] >();
		
		
        for ( final Player[] players : clientsPlayers )
        	for ( final Player player : players ) {

        		// This is the quality of how perfectly can the players be positioned on the level.
        		// 0 is the best, and if the algorithm increases, will demand less and less from the potential positions.
        		// the 4th quality is the worst: if we reach it, we stop immediately, we dont care if even there is concrete wall in that position
        		int positioningAlgoritmQuality = 0;

        		// Now we generate random position
        		int componentPosX = 1 + getRandom().nextInt( maxComponentPosX ); 
        		int componentPosY = 1 + getRandom().nextInt( maxComponentPosY );
        		
        		qualityCycle:
        		while ( true ) {
	        		trialCycle:
	        		for ( int trialsCount = ( maxComponentPosX - 1 ) * ( maxComponentPosY - 1 ); trialsCount >= 0; trialsCount-- ) {
	        			if ( --componentPosX < 1 ) {
	        				componentPosX = maxComponentPosX;
	        				if ( --componentPosY < 1 )
	        					componentPosY = maxComponentPosY;
	        			}
	        			
	        			if ( positioningAlgoritmQuality < 4 )
		        			if ( levelComponents[ componentPosY ][ componentPosX ].getWall() == Walls.CONCRETE )
		        				continue;                 // Obvious...
	
	        			if ( positioningAlgoritmQuality < 2 ) {
		        			if ( levelComponents[ componentPosY ][ componentPosX - 1 ].getWall() == Walls.CONCRETE && levelComponents[ componentPosY ][ componentPosX + 1 ].getWall() == Walls.CONCRETE )
		        				continue;             // Horizontally not open the component
		        			if ( levelComponents[ componentPosY - 1 ][ componentPosX ].getWall() == Walls.CONCRETE && levelComponents[ componentPosY + 1 ][ componentPosX ].getWall() == Walls.CONCRETE )
		        				continue;             // Vertically not open the component
	        			}
	        			
	        			for ( final int[] startPosition : generatedStartPositions )
	        				if ( Math.abs( componentPosX - startPosition[ 0 ] ) + Math.abs( componentPosY - startPosition[ 1 ] ) < 4 - positioningAlgoritmQuality )
	        					continue trialCycle;  // Too close to another player
	        			
	        			generatedStartPositions.add( new int[] { componentPosX, componentPosY } );
	        			break qualityCycle;
	        		}
        		
        			positioningAlgoritmQuality++; // We couldnt place the player, we will try to place to worse positions
        		}

        		
        		// We clear the level around the player.
        		final int[][] DELTA_COORDS = new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };  // Delta coordinates of the clearable components
        		for ( int i = 0; i < DELTA_COORDS.length; i++ ) {
        			final LevelComponent levelComponent = levelComponents[ componentPosY + DELTA_COORDS[ i ][ 0 ] ][ componentPosX + DELTA_COORDS[ i ][ 1 ] ];
        			if ( levelComponent.getWall() != Walls.CONCRETE ) {
        				levelComponent.setWall( Walls.EMPTY );
        				levelComponent.setItem( null );
        			}
        		}
        		
        		player.initForNextRound( LEVEL_COMPONENT_GRANULARITY * componentPosX + LEVEL_COMPONENT_GRANULARITY / 2, LEVEL_COMPONENT_GRANULARITY * componentPosY + LEVEL_COMPONENT_GRANULARITY / 2 );
        	}
        
        bombs      = new Vector< Bomb      >();
        bombModels = new Vector< BombModel >();
    }

    /**
     * Generates and returns a random level specified by the global server options.
     * @return a random level specified by the global server options
     */
    private Level generateRandomLevel() {
        final LevelOptions       levelOptions    = globalServerOptions.levelOptions;
        final Level              level           = new Level( levelOptions, this, this );
        final LevelComponent[][] levelComponents = level.getModel().getComponents();

        final int levelWidth  = levelComponents[ 0 ].length;
        final int levelHeight = levelComponents.length;
        
		for ( int y = 0; y < levelHeight; y++ )
            for ( int x = 0; x < levelWidth; x++ ) {
                Walls wall;
                
                if ( y == 0 || y == levelHeight - 1 || x == 0 || x == levelWidth - 1 )
                    wall = Walls.CONCRETE;       // Border
                else if ( ( x & 0x01 ) == 0 && ( y & 0x01 ) == 0 )
                    wall = Walls.CONCRETE;       // Inner concrete matrix
                else
                    wall = random.nextInt( 100 ) < globalServerOptions.amountOfBrickWalls ? Walls.BRICK : Walls.EMPTY;
                
                levelComponents[ y ][ x ].setWall( wall );
            }
		
        return level;
    }
	
	/**
	 * Called when next iteration starts.<br>
	 * Calculates the next iteration.
	 * @param newClientsActions new, unprocess actions of all clients (including us), null means there are no new unprocessed actions
	 */
	public void nextIteration( final String newClientsActions ) {
		if ( newClientsActions != null )
			processNewClientsActions( newClientsActions );
		
		for ( final Player[] players : clientsPlayers )
			for ( final Player player : players )
				player.nextIteration();
		
		level.nextIteration();
		
		for ( final Bomb bomb : bombs )
			bomb.nextIteration();

		// We remove the bombs just went dead. 
		for ( int i = bombs.size() - 1; i >= 0; i--  ) {
			final BombModel bombModel = bombModels.get( i );
			if ( bombModel.isDead() ) {
				bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.put( Items.BOMB, bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.get( Items.BOMB ) + 1 );
				bombs     .remove( i );
				bombModels.remove( i );
			}
		}
		
		checkAndHandleBombDetonations();
		
		// We remove the bombs that just had been detonated. 
		for ( int i = bombs.size() - 1; i >= 0; i--  ) {
			final BombModel bombModel = bombModels.get( i );
			if ( bombModels.get( i ).isDetonated() ) {
				bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.put( Items.BOMB, bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.get( Items.BOMB ) + 1 );
				bombs     .remove( i );
				bombModels.remove( i );
			}
		}
		
		// Now we damage players being in fire.
		for ( final PlayerModel[] playerModels : clientsPlayerModels )
			for ( final PlayerModel playerModel : playerModels )
				if ( playerModel.getActivity() != Activities.DYING ) {
					int firesCount = getLevelModel().getComponents()[ playerModel.getComponentPosY() ][ playerModel.getComponentPosX() ].fireModelVector.size();
					if ( !globalServerOptions.multipleFire && firesCount > 1 )
						firesCount = 1;
					if ( firesCount > 0 ) {
						final int damage = firesCount * (int) ( MAX_PLAYER_VITALITY * globalServerOptions.damageOfWholeBombFire / ( 100.0 * FIRE_ITERATIONS ) + 0.5 );  // +0.5 for ceiling (can't flooring, cause 100% damage might cause remainder, would let the player live!)
						playerModel.setVitality( Math.max( 0, playerModel.getVitality() - damage ) );
						if ( playerModel.getVitality() <= 0 )
							playerModel.setActivity( Activities.DYING );
					}
				}
	}
	
	/**
	 * Processes the new actions of all clients.
	 * @param newClientsActions new clients actions to be processed
	 */
	private void processNewClientsActions( final String newClientsActions ) {
		final GeneralStringTokenizer clientsActionsTokenizer = new GeneralStringTokenizer( newClientsActions );
		
		while ( clientsActionsTokenizer.hasRemainingString() ) {
			final StringTokenizer clientActionsTokenizer = new StringTokenizer( clientsActionsTokenizer.nextStringToken(), " " );
			
			final int clientIndex = Integer.parseInt( clientActionsTokenizer.nextToken() );
			
			do {
				final int               playerIndex             = Integer.parseInt( clientActionsTokenizer.nextToken() );
				final PlayerControlKeys playerControlKey        = PlayerControlKeys.values()[ Integer.parseInt( clientActionsTokenizer.nextToken() ) ];
				final boolean           playerControlKeyPressed = clientActionsTokenizer.nextToken().charAt( 0 ) == 'p' ? true : false;
				
				clientsPlayers.get( clientIndex )[ playerIndex ].getModel().setControlKeyState( playerControlKey, playerControlKeyPressed );
				
			} while ( clientActionsTokenizer.hasMoreTokens() );
		}
	}

	
	/**
	 * Checks and handles bomb detonations.<br>
	 * This includes setting and spreading the fire and detonating reached other bombs.
	 */
	private void checkAndHandleBombDetonations() {
		final LevelComponent[][]  levelComponents       = getLevelModel().getComponents();
		final Vector< BombModel > detonatableBombModels = new Vector< BombModel >();
		boolean                   checkedAllBombModels;
		
		// First we check the fire triggered bombs...
		for ( final BombModel bombModel : bombModels )
			if ( bombModel.getPhase() != BombPhases.FLYING && !bombModel.isAboutToDetonate() && !levelComponents[ bombModel.getComponentPosY() ][ bombModel.getComponentPosX() ].fireModelVector.isEmpty() ) {
				bombModel.setAboutToDetonate( true );
				bombModel.setTriggererPlayer( levelComponents[ bombModel.getComponentPosY() ][ bombModel.getComponentPosX() ].fireModelVector.lastElement().getTriggererPlayer() );
			}
		
		do {
			checkedAllBombModels = true;  // The fact that we didn't check all of 'em will be known if we find one that hasn't been checked out yet.
			
			for ( final BombModel bombModel : bombModels )
				if ( !bombModel.isDetonated() && bombModel.isAboutToDetonate() ) {
					bombModel.setTriggererPlayer( bombModel.getOwnerPlayer() );
					detonatableBombModels.add( bombModel );
					checkedAllBombModels = false;
					break;
				}
			
			for ( int i = 0; i < detonatableBombModels.size(); i++ ) {   // Not enhanced for: we may add new detonatable bombs inside the cycle (so it must be upward)!
				final BombModel detonatedBombModel         = detonatableBombModels.get( i );
				final int       detonatedBombComponentPosX = detonatedBombModel.getComponentPosX();
				final int       detonatedBombComponentPosY = detonatedBombModel.getComponentPosY();
				
				for ( final Directions direction : Directions.values() )
					for ( int range = direction == Directions.values()[ 0 ] ? 0 : 1; range < detonatedBombModel.getRange(); range++ ) {
						if ( range > 0 && detonatedBombModel.excludedDetonationDirections.contains( direction ) )
							break;
						
						final int            componentPosX  = detonatedBombComponentPosX + direction.getXMultiplier() * range;
						final int            componentPosY  = detonatedBombComponentPosY + direction.getYMultiplier() * range;
						final LevelComponent levelComponent = levelComponents[ componentPosY ][ componentPosX ];
						
						if ( levelComponent.getWall() == Walls.CONCRETE )
							break;
						
						final Integer bombIndexAtComponentPos = range == 0 ? null : getBombIndexAtComponentPosition( componentPosX, componentPosY );
						
						if ( bombIndexAtComponentPos != null ) {
							final BombModel bombModelAtComponentPos = bombModels.get( bombIndexAtComponentPos );
							bombModelAtComponentPos.excludedDetonationDirections.add( direction.getOpposite() );
							if ( !bombModelAtComponentPos.isDetonated() && !detonatableBombModels.contains( bombModelAtComponentPos ) ) {
								bombModelAtComponentPos.setTriggererPlayer( detonatedBombModel.getTriggererPlayer() );
								detonatableBombModels.add( bombModelAtComponentPos );
							}
							break;
						}
						else {
							// Now here we can set the fire...
							final Fire      fire      = new Fire( componentPosX, componentPosY, this, this );
							final FireModel fireModel = fire.getModel();
							
							fireModel.setShape          ( range == 0 ? FireShapes.CROSSING : ( direction.getXMultiplier() != 0 ? FireShapes.HORIZONTAL : FireShapes.VERTICAL ) );
							fireModel.setOwnerPlayer    ( detonatedBombModel.getOwnerPlayer    () );
							fireModel.setTriggererPlayer( detonatedBombModel.getTriggererPlayer() );
							
							level.addFireToComponentPos( fire, componentPosX, componentPosY );
							
							if ( levelComponent.getWall() == Walls.BRICK || levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null )
								break;
						}
					}
						
				
				detonatedBombModel.setDetonated( true );
			}
			
			detonatableBombModels.clear();
			
		} while ( !checkedAllBombModels );
	}
	

	
	/* ======================== MODEL PROVIDER =========================== */
	
	/**
	 * Returns the level model of the game.
	 * @return the level model of the game
	 */
	public LevelModel getLevelModel() {
		return level == null ? null : level.getModel();
	}
	
	/**
	 * Returns the players of all clients.
	 * @return the players of all clients
	 */
	public Vector< PlayerModel[] > getClientsPlayerModels() {
		return clientsPlayerModels;
	}
	
	/**
	 * Returns the bomb models of the game.
	 * @return the bomb models of the game
	 */
	public Vector< BombModel > getBombModels() {
		return bombModels;
	}

	/**
	 * Returns our client index.
	 * @return our client index
	 */
	public int getOurClientIndex() {
		return ourClientIndex;
	}

	/**
	 * Returns the vector of public client options of the clients.
	 * @return the vector of public client options of the clients
	 */
	public Vector< PublicClientOptions > getClientsPublicClientOptions() {
		return clientsPublicClientOptions;
	}

	/**
	 * Tells whether there is a bomb at a component position or whether there is one that hangs down into that component.<br>
	 * Implementation simply calls getBombIndexAtComponentPosition() and examines its return value.  
	 * @param componentPosX x coordinate of the component
	 * @param componentPosY y coordinate of the component
	 * @return true if there is a bomb at the specified position or there is one that hangs down into it; false otherwise
	 */
	public boolean isBombAtComponentPosition( final int componentPosX, final int componentPosY ) {
		return getBombIndexAtComponentPosition( componentPosX, componentPosY ) != null;
	}
	
	/**
	 * Returns the bomb being at a component position or the one hanging down into the component. 
	 * @param componentPosX x coordinate of the component
	 * @param componentPosY y coordinate of the component
	 * @return the bomb being at a component position or the one hanging down into the component; or null if there is no bomb there
	 */
	public Integer getBombIndexAtComponentPosition( final int componentPosX, final int componentPosY ) {
		for ( int i = bombModels.size() - 1; i >= 0; i-- ) {
			final BombModel bombModel = bombModels.get( i );
			if ( bombModel.getPhase() != BombPhases.FLYING )  // Flying bombs "aren't" in the level.
				if ( bombModel.getComponentPosX() == componentPosX && bombModel.getComponentPosY() == componentPosY )
					return i;
		}
		
		return null;
	}
	
	/**
	 * Tells whether there is a player at a component position or whether there is one that hangs down into that component. 
	 * @param componentPosX x      coordinate of the component
	 * @param componentPosY y      coordinate of the component
	 * @param playerModelToExclude model of player to be excluded, can be null
	 * @return true if there is a player at the specified position or there is one that hangs down into it; false otherwise
	 */
	public boolean isPlayerAtComponentPositionExcludePlayer( final int componentPosX, final int componentPosY, final PlayerModel playerModelToExclude ) {
		for ( final PlayerModel[] playerModels : clientsPlayerModels )
			for ( final PlayerModel playerModel : playerModels )
				if ( playerModel != playerModelToExclude )
					if ( playerModel.getActivity() != Activities.DYING )     // "Dead" players doesn't count...
						if ( playerModel.getComponentPosX() == componentPosX && playerModel.getComponentPosY() == componentPosY )
							return true;
		
		return false;
	}
	
	

	
	
	
	
	/* ======================== MODEL CONTROLLER ========================= */

	/**
	 * Returns the global server options.
	 * @return the global server options
	 */
	public ServerOptions getGlobalServerOptions() {
		return globalServerOptions;
	}
	
	/**
	 * Adds a new bomb to the game model.
	 * @param bomb bomb to be added
	 */
	public void addNewBomb( final Bomb bomb) {
		bombs     .add( bomb            );
		bombModels.add( bomb.getModel() );
	}
	
	/**
	 * Removes a bomb specified by its index.
	 * @param bombIndex index of bomb to be removed
	 */
	public void removeBombAtIndex( final int bombIndex ) {
		bombs     .remove( bombIndex );
		bombModels.remove( bombIndex );
	}
	
	/**
	 * Returns the Random object to be used for generating random datas.
	 * @return the Random object to be used for generating random datas
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * Checks and sets the x coordiante of the target position in case of a flying bomb.
	 * @param bombModel        bomb model whos target position to be validated and set
	 * @param flyingTargetPosX the whished x coordiante of the target position in case of a flying bomb 
	 */
	public void validateAndSetFlyingTargetPosX( final BombModel bombModel, final int flyingTargetPosX ) {
		final LevelComponent[][] levelComponents = getLevelModel().getComponents();
		
		if ( flyingTargetPosX < 0 )
			bombModel.setFlyingTargetPosX( ( levelComponents[ 0 ].length - 1 ) * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
		else if ( flyingTargetPosX > levelComponents[ 0 ].length * LEVEL_COMPONENT_GRANULARITY )
			bombModel.setFlyingTargetPosX( LEVEL_COMPONENT_GRANULARITY / 2 );
		else
			bombModel.setFlyingTargetPosX( flyingTargetPosX );
	}
	
	/**
	 * Checks and sets the y coordiante of the target position in case of a flying bomb.
	 * @param bombModel        bomb model whos target position to be validated and set
	 * @param flyingTargetPosY the whished y coordiante of the target position in case of a flying bomb 
	 */
	public void validateAndSetFlyingTargetPosY( final BombModel bombModel, final int flyingTargetPosY ) {
		final LevelComponent[][] levelComponents = getLevelModel().getComponents();
		
		if ( flyingTargetPosY < 0 )
			bombModel.setFlyingTargetPosY( ( levelComponents.length - 1 ) * LEVEL_COMPONENT_GRANULARITY + LEVEL_COMPONENT_GRANULARITY / 2 );
		else if ( flyingTargetPosY > levelComponents.length * LEVEL_COMPONENT_GRANULARITY )
			bombModel.setFlyingTargetPosY( LEVEL_COMPONENT_GRANULARITY / 2 );
		else
			bombModel.setFlyingTargetPosY( flyingTargetPosY );
	}
	
	/**
	 * Tells whether a specified bomb can roll to a component position.
	 * @param bombModel     model of bomb to be checked
	 * @param componentPosX x coordinate of the position of the desired component
	 * @param componentPosY y coordinate of the position of the desired component
	 * @return true, if the specified bomb can roll to the component; false otherwise
	 */
	public boolean canBombRollToComponentPosition( final BombModel bombModel, final int componentPosX, final int componentPosY ) {
		
		final LevelComponent levelComponentAheadAhead = getLevelModel().getComponents()[ componentPosY ][ componentPosX ];
		if ( levelComponentAheadAhead.getWall() != Walls.EMPTY )
			return false;
		if ( levelComponentAheadAhead.getWall() == Walls.EMPTY && levelComponentAheadAhead.getItem() != null && getGlobalServerOptions().itemsStopRollingBombs )
			return false;
		
		// Collision with players:
		for ( final PlayerModel[] playerModels : getClientsPlayerModels() )
			for ( final PlayerModel playerModel : playerModels )
				if ( playerModel.getActivity() != Activities.DYING )     // "Dead" players doesn't count...
					if ( playerModel.getComponentPosX() == componentPosX && playerModel.getComponentPosY() == componentPosY )
						if ( playerModel.getComponentPosX() != bombModel.getComponentPosX() || playerModel.getComponentPosY() != bombModel.getComponentPosY() )
							return false;


		final Integer bombIndexAhead = getBombIndexAtComponentPosition( componentPosX, componentPosY );
		if ( bombIndexAhead != null && getBombModels().get( bombIndexAhead ) != bombModel ) // There's another bomb
			return false;
		
		return true;
	}
	
	/**
	 * Replaces an item to a random position in the level. 
	 * @param item item to be replaced
	 */
	public void replaceItemOnLevel( final Items item ) {
		final LevelComponent[][] levelComponents = getLevelModel().getComponents();

		// The level is surrounded with concrete walls, we dont even try there...
		final int maxComponentPosX = levelComponents[ 0 ].length - 2;
		final int maxComponentPosY = levelComponents     .length - 2;
		
		int componentPosX = 1 + getRandom().nextInt( maxComponentPosX ); 
		int componentPosY = 1 + getRandom().nextInt( maxComponentPosY );
		
		for ( int trialsCount = ( maxComponentPosX - 1 ) * ( maxComponentPosY - 1 ); trialsCount >= 0; trialsCount-- ) {
			if ( --componentPosX < 1 ) {
				componentPosX = maxComponentPosX;
				if ( --componentPosY < 1 )
					componentPosY = maxComponentPosY;
			}
			
			final LevelComponent levelComponent = levelComponents[ componentPosY ][ componentPosX ];

			if ( levelComponent.getWall() != Walls.EMPTY || levelComponent.getItem() != null )
				continue;
			
			if ( isBombAtComponentPosition( componentPosX, componentPosY ) )
				continue;

			if ( isPlayerAtComponentPositionExcludePlayer( componentPosX, componentPosY, null ) )
				continue;
			
			levelComponent.setItem( item );
			break;
		}
	}
	
	/**
	 * Removes a fire from a specified component position.
	 * @param fire          fire to be removed
	 * @param componentPosX x coordinate of the component to remove the fire from
	 * @param componentPosY y coordinate of the component to remove the fire from
	 */
	public void removeFireFromComponentPos( final Fire fire, final int componentPosX, final int componentPosY ) {
		final LevelComponent levelComponent = getLevelModel().getComponents()[ componentPosY ][ componentPosX ];
		
		if ( levelComponent.getWall() == Walls.BRICK )
			levelComponent.setWall( Walls.EMPTY );
		else if ( levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null ) {
			final Items item = levelComponent.getItem();
			levelComponent.setItem( null );
			if ( item == Items.DISEASE && !getGlobalServerOptions().explosionAnnihilatesDiseases )
				replaceItemOnLevel( item );
		}
		
		level.removeFireFromComponentPos( fire, componentPosX, componentPosY );
	}
	
}
