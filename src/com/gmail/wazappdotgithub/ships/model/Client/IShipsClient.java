package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.Client.CState.Statename;

public interface IShipsClient {
	//Observer related
	public void addAsObserver(Observer obs);
	public void removeAsObserver(Observer obs);

	// General access methods 
	public IBoard getBoard();
	public Statename getState();
	
	// Protocol related methods
	public void playerCompletedPreGame();
	public boolean placeBomb(int xcoord, int ycoord);
	public int getRemainingBombs();
	public void playerCompletedTurn();
	public void playerCompletedWait();
	public List<Bomb> requestInTurnClientAcceptedBombs();
	public List<Bomb> requestInTurnClientHistoricalBombs();
	public void playerCompletedEvaluation();
}

