
/*
 * Created on November 24, 2005
 */

package classes.client.gamecore.model;

import java.util.Vector;

import classes.client.gamecore.model.level.LevelModel;
import classes.options.model.PublicClientOptions;

/**
 * Interface that defines methods to access the components of the game core model.
 * 
 * @author Andras Belicza
 */
public interface ModelProvider {

	/**
	 * Returns the level model of the game.
	 * @return the level model of the game
	 */
	LevelModel getLevelModel();

	/**
	 * Returns the player models of all clients.
	 * @return the player models of all clients
	 */
	Vector< PlayerModel[] > getClientsPlayerModels();

	/**
	 * Returns the bomb models of the game.
	 * @return the bomb models of the game
	 */
	Vector< BombModel > getBombModels();

	/**
	 * Returns our client index.
	 * @return our client index
	 */
	int getOurClientIndex();
	
	/**
	 * Returns the vector of public client options of the clients.
	 * @return the vector of public client options of the clients
	 */
	Vector< PublicClientOptions > getClientsPublicClientOptions();
	
	/**
	 * Returns the bomb being at a component position or the one hanging down into the component. 
	 * @param componentPosX x coordinate of the component
	 * @param componentPosY y coordinate of the component
	 * @return the bomb being at a component position or the one hanging down into the component; or null if there is no bomb there
	 */
	Integer getBombIndexAtComponentPosition( final int componentPosX, final int componentPosY );

	/**
	 * Tells whether there is a bomb at a component position or whether there is one that hangs down into that component. 
	 * @param componentPosX x coordinate of the component
	 * @param componentPosY y coordinate of the component
	 * @return true if there is a bomb at the specified position or there is one that hangs down into it; false otherwise
	 */
	boolean isBombAtComponentPosition( final int componentPosX, final int componentPosY );

	/**
	 * Tells whether there is a player at a component position or whether there is one that hangs down into that component. 
	 * @param componentPosX x      coordinate of the component
	 * @param componentPosY y      coordinate of the component
	 * @param playerModelToExclude model of player to be excluded 
	 * @return true if there is a player at the specified position or there is one that hangs down into it; false otherwise
	 */
	boolean isPlayerAtComponentPositionExcludePlayer( final int componentPosX, final int componentPosY, final PlayerModel playerModelToExclude );

}
