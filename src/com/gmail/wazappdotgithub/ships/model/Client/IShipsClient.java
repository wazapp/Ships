package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.List;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;

public interface IShipsClient {
	
	public static enum Statename {
		PREGAME,	// preparing the board, placing ships
		PREGAME_EXIT,
		WAITGAME,	// waiting for the other player to complete pregame
		WAITGAME_EXIT,
		TURN, 		// place bombs
		TURN_EXIT,
		RECOUNTBOMBS, // tell UI the number of bombs have changed
		TURN_EVAL,	// look at the evaluation
		TURN_EVAL_EXIT,
		WAIT,		// wait for the other player to place bombs
		WAIT_EXIT,
		WAIT_EVAL,	// look at the evaluation
		WAIT_EVAL_EXIT,
		GAMEOVER
	}
	
	// TODO move this?
	public class EndGameData {
		public boolean winner = false;
		public int bombsShot = 0;
		public int liveShips = 0;
	}
	
	// Observer related
	public void addAsObserver(Observer obs);
	public void removeAsObserver(Observer obs);

	// Protocol related methods
	public void playerCompletedPreGame();
	public void playerCompletedWaitGame();
	public void playerCompletedTurn();
	public void playerCompletedWait();
	public void playerCompletedEvaluation();

	// General access methods 
	public IBoard getBoard();
	public Statename getState();
	public String getOpponentName();
	public boolean placeBomb(int xcoord, int ycoord);
	public int getRemainingBombs();
	public List<Bomb> requestInTurnClientAcceptedBombs();
	public List<Bomb> requestInTurnClientHistoricalBombs();
	public EndGameData retrieveEndGameData();
}

