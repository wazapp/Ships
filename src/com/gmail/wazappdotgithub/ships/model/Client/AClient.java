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
	protected String tag = "Ships_AClient ";
	
	protected IBoard board = null;
	protected List<Bomb> historicalBombs = null;
	protected List<Bomb> latestTurnBombs = null;
	protected List<Bomb> inturnBombs = null;
	protected int bombstoplace;
	protected EndGameData endgamedata = null;
	
	public class EndGameData {
		public boolean winner = false;
		public int bombsShot = 0;
		public int liveShips = 0;
	}

	protected String tag() {
		return tag;
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
	public List<Bomb> getHistoricalBombs() {
		return historicalBombs;
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
	public List<Bomb> requestInturnClientHistoricalBombs() {
		return Game.getConfiguredInstance().getInTurnClientHistoricalBombs();
	}

	@Override
	public List<Bomb> requestInTurnClientAcceptedBombs() {
		return Game.getConfiguredInstance().getInTurnClientAcceptedBombs();
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

			if ( historicalBombs.contains(b) )
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

			if ( historicalBombs.contains(b) )
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
	public void setToStatePostGameAsWinner() {
		Log.d(tag(),tag() + "from " +currentState+" to " + Game.ClientState.POSTGAMEWINNER);
		currentState = Game.ClientState.POSTGAMEWINNER;
		gatherDataAfterGame(true);

		setChanged();
		notifyObservers(currentState);
	}
	@Override
	public void setToStatePostGameAsLooser() {
		Log.d(tag(),tag() + "from " +currentState+" to " + Game.ClientState.POSTGAMELOOSER);
		currentState = Game.ClientState.POSTGAMELOOSER;
		gatherDataAfterGame(false);

		setChanged();
		notifyObservers(currentState);
	}

	private void gatherDataAfterGame(boolean isTheWinner) {
		endgamedata = new EndGameData();
		endgamedata.liveShips = numLiveShips();
		endgamedata.bombsShot = historicalBombs.size();
		endgamedata.winner = isTheWinner;

		historicalBombs.clear();
		latestTurnBombs.clear();
		inturnBombs.clear();

		bombstoplace = 0;
	}
	
	@Override
	public EndGameData retrieveEndGameData() {
		return endgamedata;
	}

	protected void initialize() {
		board = Game.getNewBoard();
		historicalBombs = new LinkedList<Bomb>();
		latestTurnBombs = new LinkedList<Bomb>();
		inturnBombs = new LinkedList<Bomb>();
	}
	
	@Override
	public void setToStatePreGame() {
		Log.d(tag(),tag() + "from " +currentState+" to " + Game.ClientState.PREGAME);
		currentState = Game.ClientState.PREGAME;
		
		initialize();

		setChanged();
		notifyObservers(currentState);
	}
	
	@Override
	public void setToStateWaitGame() {
		Log.d(tag(),tag() + "from " +currentState+" to " + Game.ClientState.WAITGAME);
		currentState = Game.ClientState.WAITGAME;
		setChanged();
		notifyObservers(currentState);
	}
	
	@Override
	public void putToNextState(Game.ClientState newstate) {
		leaveStateActions();
		Log.d(tag(),tag() + "from " +currentState+" to " + newstate);
		enterStateActions(newstate);
	}
	
	protected void leaveStateActions() {
		Log.d(tag(), tag() + " preparing for next state");
		switch ( currentState ) {
		case INTURN : reportAcceptBombs(); manageBombsForStateSwitch(); break;
		case I_EVALUATE : break;
		case I_COMPLETEDEVALUATION : break;
		case READYCHANGETURNS : break;
		case WAIT : break;
		case W_EVALUATE : break;
		case W_COMPLETEDEVALUATION : break;
		default : break;
		}
	}
	
	protected void enterStateActions( Game.ClientState newstate ) {
		currentState = newstate;
		
		switch ( currentState ) {
		case INTURN : restockBombs(); break;
		case WAIT	: manageBombsForStateSwitch() ; break;
		default : break;
		}
		
		setChanged();
		notifyObservers(currentState);
	}
	
	protected void restockBombs() {
		bombstoplace = numLiveShips(); //TODO make a flexible rule
	}
	
	protected void reportAcceptBombs() {
		Log.d(tag(), tag() + " Starting acceptBombs received " + inturnBombs.size() );
		for (Bomb b : inturnBombs )
			Game.getConfiguredInstance().dropBomb(this, b);
	}

	@Override 
	public int numLiveShips() {
		return board.numLiveShips();
	}
	
	@Override
	public void requestNextState() {
		Game.getConfiguredInstance().progressState(this);
	}
	

	protected void manageBombsForStateSwitch() {
		switch ( currentState ) {
		case WAIT : 
			int latest = latestTurnBombs.size();
			historicalBombs.addAll( latestTurnBombs );
			latestTurnBombs.clear();
			Log.d(tag(),tag() + "moved "+latest+" bombs to historicalBombs store, now " + historicalBombs.size());
			break;
		case INTURN :
			latestTurnBombs = inturnBombs;
			inturnBombs = new LinkedList<Bomb>();
			Log.d(tag(),tag() + "moved " + latestTurnBombs.size() +" bombs to latestTurn store, cleared inturn store");
			break;
		default : break;
		}
	}
	
	public String toString() {
		return tag() + " (" + getState()+")";
	}
}
