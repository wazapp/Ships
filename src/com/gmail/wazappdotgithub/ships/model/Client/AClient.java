package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.Game.ClientState;
/**
 * An abstract that is a working client. Instantiate using subclass. 
 * For example ComputerClient, that is an AI powered client, or as LocalClient 
 * which expects input from the user.
 * 
 * @author tor
 *
 */
public abstract class AClient extends Observable implements IShipsClient {
	
	protected Game.ClientState currentState = null;
	protected IBoard board = null;
	protected List<Bomb> shootingRange = null;
	protected List<Bomb> latestTurnBombs = null;
	protected List<Bomb> inturnBombs = null;
	protected int bombstoplace;
	protected EndGameData endgamedata = null;
	
	public class EndGameData {
		public boolean winner = false;
		public int bombsShot = 0;
		public int liveShips = 0;
	}
	
	public AClient() {

	}
	
	@Override 
	public Observable getClientAsObservable() {
		return this;
	}
	
	@Override
	public IBoard getBoard() {
		return board;
	}

	@Override
	public List<Bomb> getBombsBoard() {
		return shootingRange;
	}

	@Override
	public List<Bomb> getInTurnBombs() {
		return inturnBombs;
	}
	
	@Override
	public List<Bomb> getLatestTurnBombs() {
		return latestTurnBombs;
	}
	
	@Override
	public List<Bomb> requestOpponentBombsBoard() {
		return Game.getConfiguredInstance().getOpponentsBombsBoard();
	}
	
	@Override
	public List<Bomb> requestOpponentLatestTurnBombs() {
		return Game.getConfiguredInstance().getOpponentsLatestTurnBombs();
	}
	
	@Override
	public ClientState getState() {
		return currentState;
	}


	@Override
	public boolean placeBomb(int xcoord, int ycoord) {
		//TODO sanity check on input!
		//TODO clean this method, it looks lousy
		if ( bombstoplace > 0 ) {
			Bomb b = new Bomb(xcoord, ycoord);

			if ( shootingRange.contains(b) )
				return false;	//do nothing

			if ( inturnBombs.contains(b) ) { // compares coords
				inturnBombs.remove(b);
				bombstoplace++;

			} else {
				inturnBombs.add(b);
				bombstoplace--;
			}
			
			setChanged();
			notifyObservers(Game.ClientState.RECOUNTBOMBS);
			return true;
			
		} else if ( bombstoplace == 0 ) { // only removal is allowed
			Bomb b = new Bomb(xcoord, ycoord);
			
			if ( shootingRange.contains(b) )
				return false;	//do nothing
			
			if ( inturnBombs.contains(b) ) {
					inturnBombs.remove(b);
					bombstoplace++;
					setChanged();
					notifyObservers(Game.ClientState.RECOUNTBOMBS);
					return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	@Override
	public int getRemainingBombs() {
		return numLiveShips() - bombstoplace;
	}


	@Override
	public void reportReady() {
		board.finalise();
		Game.getConfiguredInstance().clientReportReadyForGame(this, board);
	}


	@Override
	public void setToStateInTurn() {
		currentState = Game.ClientState.INTURN;
		bombstoplace = numLiveShips(); //TODO could have different rules
		
		setChanged();
		notifyObservers(currentState);
	}


	@Override
	public void setToStatePostGameAsWinner() {
		currentState = Game.ClientState.POSTGAMEWINNER;
		gatherDataAfterGame(true);
		
		setChanged();
		notifyObservers(currentState);
	}
	@Override
	public void setToStatePostGameAsLooser() {
		currentState = Game.ClientState.POSTGAMELOOSER;
		gatherDataAfterGame(false);
		
		setChanged();
		notifyObservers(currentState);
	}
	
	private void gatherDataAfterGame(boolean isTheWinner) {
		endgamedata = new EndGameData();
		endgamedata.liveShips = numLiveShips();
		endgamedata.bombsShot = shootingRange.size();
		endgamedata.winner = isTheWinner;
		
		shootingRange.clear();
		latestTurnBombs.clear();
		inturnBombs.clear();
		
		bombstoplace = 0;
	}

	public EndGameData retrieveEndGameData() {
		return endgamedata;
	}
	
	protected void initialize() {
		board = Game.getNewBoard();
		shootingRange = new LinkedList<Bomb>();
		latestTurnBombs = new LinkedList<Bomb>();
		inturnBombs = new LinkedList<Bomb>();
	}
	
	@Override
	public void setToStatePreGame() {
		currentState = Game.ClientState.PREGAME;
		initialize();
		
		setChanged();
		notifyObservers(currentState);
	}

	@Override
	public void setToStateWait() {
		currentState = Game.ClientState.WAIT;
		setChanged();
		notifyObservers(currentState);
	}

	@Override
	public void setToStateWaitGame() {
		currentState = Game.ClientState.WAITGAME;
		setChanged();
		notifyObservers(currentState);
	}
	
	@Override
	public void reportAcceptBombs() {
		for (Bomb b : inturnBombs ) {
			Game.getConfiguredInstance().dropBomb(this, b);
			shootingRange.add(b);
		}
		//moving to store to be accessed later
		latestTurnBombs = inturnBombs;
		inturnBombs = new LinkedList<Bomb>();
		
		Log.d("Ships_AClient", "Ships_AClient" + " Accepting bombs and updating them with hits");
		
		//NOTE: Remember to call reportBombingCompleted manually to let Game change the turn!
	}
	
	@Override
	public void reportBombingCompleted() {
		Game.getConfiguredInstance().clientReportFinishedBombing(this);
	}
	
	@Override 
	public int numLiveShips() {
		return board.numLiveShips();
	}
}
