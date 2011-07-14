package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.IBoard;

public interface IShipsClient {
	
	//used by game logic to set the client to the appropriate state
	public void setToStatePreGame();
	public void setToStateWaitGame();
	public void setToStateWait();
	public void setToStateInTurn();
	public void setToStatePostGame();
	
	//possibly sit in another interface!
	public Game.ClientState getState();
	public IBoard getBoard();
	public List<Bomb> getBombsBoard();
	public List<Bomb> getInTurnBombs();
	public void reportAcceptBombs();
	public void placeBomb(int xcoord, int ycoord);
	public void reportReady();

}

