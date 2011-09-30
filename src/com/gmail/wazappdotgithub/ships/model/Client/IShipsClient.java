package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;
import java.util.Observable;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.Client.AClient.EndGameData;

public interface IShipsClient {

	// Game uses these to move in states before and after the game is running
	public void setToStatePreGame();
	public void setToStateWaitGame();
	public void setToStatePostGameAsWinner();
	public void setToStatePostGameAsLooser();
	
	/** 
	 * Call this to request to move away from current state
	 * The client requests a new state from the Game
	 */
	public void requestNextState();
	
	/**
	 * Game to call this to enforce the move to the appropriate state
	 */
	public void putToNextState(Game.ClientState newstate);
	
	
	public EndGameData retrieveEndGameData();
	
	//possibly sit in another interface!
	public List<Bomb> getHistoricalBombs();
	public List<Bomb> getLatestTurnBombs();
	
	public List<Bomb> requestInturnClientHistoricalBombs();
	public List<Bomb> requestInTurnClientAcceptedBombs();
	
	public List<Bomb> getInTurnBombs();
	
	public Game.ClientState getState();
	public IBoard getBoard();
	
	public Observable getClientAsObservable();
	
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

