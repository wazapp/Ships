package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;

public interface IShipsClient {
	/*
	 * Used to specify if we want to read Local or Cached (Remote) data
	 * from the model
	 */
	public static enum DataAccess {
		LOCAL,
		REMOTE
	}
	
	/*
	 * Specifies the states of the Client
	 */
	public static enum Statename {
		INIT, 			// class is set up
		PREGAME,		// preparing the board, placing ships
		PREGAME_EXIT,
		WAITGAME,		// waiting for the other player to complete pregame
		WAITGAME_EXIT,
		TURN, 			// place bombs
		TURN_EXIT,
		RECOUNTBOMBS, 	// tell UI the number of bombs have changed
		TURN_EVAL,		// look at the evaluation
		TURN_EVAL_EXIT,
		WAIT,			// wait for the other player to place bombs
		WAIT_EXIT,
		WAIT_EVAL,		// look at the evaluation
		WAIT_EVAL_EXIT,
		GAMEOVER		// game over
	}
	
	/*
	 * just a wrapper for some data used when the game is over
	 */
	public class EndGameData {
		public boolean winner = false;
		public int bombsShot = 0;
		public int liveShips = 0;
	}
	
	// Observer related
	public void addAsObserver(Observer obs);
	public void removeAsObserver(Observer obs);

	// Protocol related methods
	public void playerCompletedUserInput();
	public void playerCompletedPreGame();
	public void playerCompletedWaitGame();
	public void playerCompletedTurn();
	public void playerCompletedWait();
	public void playerCompletedTurnEvaluation();
	public void playerCompletedWaitEvaluation();

	// General access methods 
	public IBoard getBoard();
	public Statename getState();
	public String getOpponentName();
	public boolean placeBomb(int xcoord, int ycoord);
	public int getRemainingBombs();
	public List<Bomb> requestInTurnClientAcceptedBombs(DataAccess get);
	public List<Bomb> requestInTurnClientHistoricalBombs(DataAccess get);
	public EndGameData retrieveEndGameData();
}

