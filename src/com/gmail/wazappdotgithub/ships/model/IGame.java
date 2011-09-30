package com.gmail.wazappdotgithub.ships.model;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;

public interface IGame {

	/**
	 * Clients use this method to request to start the game.
	 * 
	 * @param client the client who called
	 * @param board the board of the client
	 */
	void clientReportReadyForGame(IShipsClient client, IBoard board);
	
	/**
	 * Clients use this to evaluate their bombs
	 * @param client the client who send the bomb
	 * @param b the bomb to be evaluated
	 * @return a modified Bomb object with it's flag set to hit or not
	 */
	Bomb dropBomb(IShipsClient client, Bomb b);
	
	/**
	 * Clients use this method to request to move to the next state
	 * @param client the client which request the state change
	 */
	void progressState(IShipsClient client);
	
	/**
	 * Call this method to receive the list of all previous Bombs from
	 * the client which is currently INTURN
	 * @return a list of historical Bombs 
	 */
	List<Bomb> getInTurnClientHistoricalBombs();
	
	/**
	 * Call this method to receive a list of bombs that have been evaluated
	 * during this turn (may be empty unless the client has evaluated it's 
	 * bombs)
	 * @return a list of the latest Bombs
	 */
	List<Bomb> getInTurnClientAcceptedBombs();
}
