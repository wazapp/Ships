package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.Client.AClient.EndGameData;

public interface IShipsClient {
	
	//used by game logic to set the client to the appropriate state
	public void setToStatePreGame();
	public void setToStateWaitGame();
	public void setToStateWait();
	public void setToStateInTurn();
	public void setToStatePostGameAsWinner();
	public void setToStatePostGameAsLooser();
	
	public EndGameData retrieveEndGameData();
	
	//possibly sit in another interface!
	public Game.ClientState getState();
	public IBoard getBoard();
	public List<Bomb> getBombsBoard();
	public List<Bomb> getInTurnBombs();
	public void reportAcceptBombs();
	/**
	 * attempt to place or remove a bomb at the selected coordinates 
	 * @param xcoord
	 * @param ycoord
	 * @return true if the board was updated
	 */
	public boolean placeBomb(int xcoord, int ycoord);
	public void reportReady();
	
	/**
	 * @return the number of live ships on the board
	 */
	public int numLiveShips();
	public int getRemainingBombs();

}

