package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;

public interface IShipsClient {
	
	public static enum Statename {
		PREGAME,	// preparing the board, placing ships
		WAITGAME,	// waiting for the other player to complete pregame
		TURN, 		// place bombs
		RECOUNTBOMBS, // tell UI the number of bombs have changed
		TURN_EVAL,	// look at the evaluation
		WAIT,		// wait for the other player to place bombs
		WAIT_EVAL	// look at the evaluation
	}
	
	//TODO move this?
	public class EndGameData {
		public boolean winner = false;
		public int bombsShot = 0;
		public int liveShips = 0;
	}
	
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
	public EndGameData retrieveEndGameData();
}

