package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;


public class RemoteClient extends Observable implements IShipsClient {

	private static RemoteClient instance = null;
	private static String tag = "Ships_RemoteClient ";
	
	protected Statename state;
	protected String opponentName;
	protected boolean starter;
	protected boolean is_game_over;
	protected boolean is_winner;
	
	/* Local data */
	protected IBoard board = null;
	// List of all bombs placed by this client
	protected List<Bomb> historicalBombs = null;
	// List of bombs placed last turn (by player)
	protected List<Bomb> latestTurnBombs = null;
	// List of bombs placed during this turn
	protected List<Bomb> inturnBombs = null;
	
	protected int bombstoplace;

	/* Remote data */
	protected List<Bomb> r_historicalBombs = null;
	protected List<Bomb> r_inturnBombs = null;
	
	/* ********************
	* Constructors etc .
	* *********************/
	
	public static IShipsClient newInstance(Observer obs) {
		instance = new RemoteClient(obs);
		
		return instance;
	}
	
	public static IShipsClient getInstance() {
		if ( instance == null )
			throw new RuntimeException("Calling getInstance on RemoteClient, but there is no instance");
		
		return instance;
	}
	
	private RemoteClient(Observer obs) {
		//initiate the client, ready for board interactions
		addAsObserver(obs);
		CState.setClient(this);
	}
	
	/* ********************
	* Observer Management, should be used by Activities to register/de-register
	* *********************/
	@Override
	public void addAsObserver(Observer obs) {
		this.addObserver(obs);
	}
	@Override
	public void removeAsObserver(Observer obs) {
		this.deleteObserver(obs);
	}
	//used from CState to update the changed variable
	protected void setToChanged() {
		setChanged();
	}
	
	/* ********************
	* Board Interaction
	* *********************/
	@Override
	public IBoard getBoard() {
		/* theese methods are used by activities directly
		 * randomiseships()
		 * toggleOrientation()
		 * numLiveShips()
		 * 
		 */
		return board;
	}

	/* ********************
	* General access methods 
	* *********************/
	@Override
	public Statename getState() {
		return state;
	}
	
	@Override 
	public String getOpponentName() {
		return opponentName;
	}
	
	protected String tag() {
		return tag;
	}
	
	/* ********************
	* Rule / Protocol Interaction
	* *********************/
	
	/* ********************
	* Called by Activity to report the user has completed 
	* the pregame state and has completed moving/ placing
	* their ships
	* *********************/
	@Override
	public void playerCompletedPreGame() {
		CState.exitState(Statename.PREGAME);
	}
	
	@Override
	public void playerCompletedWaitGame() {
		CState.exitState(Statename.WAITGAME);
		//will enter either Turn or Wait
	}
	
	/* ********************
	* Methods called during TURN state to place bombs etc.
	* *********************/
	@Override
	public boolean placeBomb(int xcoord, int ycoord) {
		// TODO sanity check on input!
		
		// Determine if it is a valid coordinate
		Bomb b = new Bomb(xcoord, ycoord);
		if ( historicalBombs.contains(b) )
			return false;
		
		// Check if there is already a new bomb on the coordinate
		// an remove it if there is
		if ( inturnBombs.contains(b) ) {
			inturnBombs.remove(b);
			bombstoplace++;
			
			setChanged();
			notifyObservers(Statename.RECOUNTBOMBS);
			return true;
		}
		
		// if allowed to place a bomb, do so
		if ( bombstoplace > 0 ) {
				inturnBombs.add(b);
				bombstoplace--;
			return true;

		} else { // only removal is allowed
			return false;
		}		
	}
	
	@Override
	public int getRemainingBombs() { 
		return bombstoplace;
	}
	
	@Override
	public void playerCompletedTurn() {
		CState.exitState(Statename.TURN);
	}
	
	
	/* ********************
	* Methods called during WAIT state
	* *********************/
	@Override
	public void playerCompletedWait() {
		CState.exitState(Statename.WAIT);
	}
	
	/* ********************
	* Methods called during EVALUATION state to read current
	* bombs, historical bombs etc.
	* *********************/
	
	@Override
	public List<Bomb> requestInTurnClientAcceptedBombs() {
		switch(state) {
		case TURN_EVAL : return inturnBombs;
		case WAIT_EVAL : return r_inturnBombs;
		default : throw new RuntimeException("requested accepted bombs outside relevant state");
		}
	}
	
	@Override
	public List<Bomb> requestInTurnClientHistoricalBombs() {
		switch(state) {
		case TURN_EVAL : return historicalBombs;
		case WAIT_EVAL : return r_historicalBombs;
		default : throw new RuntimeException("requested accepted bombs outside relevant state");
		}
	}
	
	@Override
	public void playerCompletedEvaluation() {
		switch(state) {
		case TURN_EVAL : 
			CState.exitState(Statename.TURN_EVAL);
		case WAIT_EVAL :
			CState.exitState(Statename.WAIT_EVAL);
		}
	}
	
	//used by CState to manage the lists of bombs
	protected void manageBombsForStateSwitch() {
		switch ( state ) {
		case WAIT : 
			int latest = latestTurnBombs.size();
			historicalBombs.addAll( latestTurnBombs );
			latestTurnBombs.clear();
			//Log.d(tag(),tag() + "moved "+latest+" bombs to historicalBombs store, now " + historicalBombs.size());
			break;
		case TURN :
			//local
			latestTurnBombs = inturnBombs;
			inturnBombs = new LinkedList<Bomb>();
			
			//remote
			r_historicalBombs.addAll( r_inturnBombs );
			r_inturnBombs.clear();
			//Log.d(tag(),tag() + "moved " + latestTurnBombs.size() +" bombs to latestTurn store, cleared inturn store");
			break;
		default : break;
		}
	}

	
	/* ********************
	* Methods called during GAMEOVER state
	* *********************/
	@Override
	public EndGameData retrieveEndGameData() {
		EndGameData endgamedata = new EndGameData();
		
		endgamedata.liveShips = board.numLiveShips();
		endgamedata.bombsShot = historicalBombs.size();
		endgamedata.winner = is_winner;

		return endgamedata;
	}
	
}