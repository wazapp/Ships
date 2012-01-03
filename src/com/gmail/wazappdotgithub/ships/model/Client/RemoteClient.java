package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.model.BoardUsingSimpleShip;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;

/*
 * The Remote client is the implementation for IShipsClient
 * It is a part of the Model ( RemoteClient, CState and Protocol )
 * with which the UI controls the state switches and reads any 
 * relevant information from the model.
 */

public final class RemoteClient extends Observable implements IShipsClient {

	private static IShipsClient instance = null;
	private static String tag = "Ships RemoteClient ";
	
	protected Statename state;
	protected String opponentName;
	protected boolean starter;
	protected boolean is_game_over;
	protected boolean is_winner;
	
	/* Local data */
	protected IBoard board = null;
	// List of all bombs placed by this client
	protected List<Bomb> historicalBombs = null;
	// List of bombs placed during this turn
	protected List<Bomb> inturnBombs = null;
	
	protected int bombstoplace;

	/* Remote data */
	protected List<Bomb> r_historicalBombs = null;
	protected List<Bomb> r_inturnBombs = null;
	
	/* ********************
	* Constructors etc .
	* *********************/
	public static IShipsClient newInstance(Observer obs, boolean starter) {
		instance = new RemoteClient(obs, starter);
		return instance;
	}
	
	public static IShipsClient getInstance() {
		if ( instance == null )
			throw new RuntimeException("Calling getInstance on RemoteClient, but there is no instance");
		
		return instance;
	}
	
	private RemoteClient(Observer obs, boolean starter) {
		//initiate the client, ready for board interactions
		Log.d(tag, tag + "Constructing");
		state = Statename.INIT;
		
		this.starter = starter;
		
		board = new BoardUsingSimpleShip();
		historicalBombs = new LinkedList<Bomb>();
		inturnBombs = new LinkedList<Bomb>();
		
		//preparing remote data
		r_historicalBombs = new LinkedList<Bomb>();
		r_inturnBombs = new LinkedList<Bomb>();
		
		addAsObserver(obs);
		CState.initClient(this);
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
	// used from CState to update the changed variable
	// since setChanged() is protected
	protected void setToChanged() {
		setChanged();
	}
	
	/* ********************
	* Board Interaction
	* *********************/
	@Override
	public IBoard getBoard() {
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
	
	/* ********************
	* Rule / Protocol Interaction
	* Used by UI to push the Client into the next state
	* *********************/
	@Override
	public void playerCompletedUserInput() {
		CState.exitState(Statename.INIT);
	}

	@Override
	public void playerCompletedPreGame() {
		CState.exitState(Statename.PREGAME);
	}
	
	@Override
	public void playerCompletedWaitGame() {
		CState.exitState(Statename.WAITGAME);
	}
	
	@Override
	public void playerCompletedTurn() {
		CState.exitState(Statename.TURN);
	}
	
	@Override
	public void playerCompletedTurnEvaluation() {
		CState.exitState(Statename.TURN_EVAL);
	}
	
	@Override
	public void playerCompletedWait() {
		CState.exitState(Statename.WAIT);
	}
	
	@Override
	public void playerCompletedWaitEvaluation() {
		CState.exitState(Statename.WAIT_EVAL);
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
			
			setChanged();
			notifyObservers(Statename.RECOUNTBOMBS);
			return true;

		} else { // only removal is allowed
			return false;
		}		
	}
	
	@Override
	public int getRemainingBombs() { 
		return bombstoplace;
	}
	
	/* ********************
	* Methods called during different states to read current
	* bombs, historical bombs etc.
	* *********************/
	
	@Override
	public List<Bomb> requestInTurnClientAcceptedBombs(DataAccess get) {
		switch( get ) {
		case LOCAL		: return inturnBombs;
		case REMOTE		: return r_inturnBombs;
		default : return null;
		}
	}
	
	@Override
	public List<Bomb> requestInTurnClientHistoricalBombs(DataAccess get) {
		switch(get) {
		case LOCAL : return historicalBombs;
		case REMOTE: return r_historicalBombs;
		default : return null;
		}
	}

	/*
	 * used by CState to manage the lists of bomb
	 * Following a bomb run by the bombs will be moved 
	 * into historical storage
	 */
	protected void manageBombsForStateSwitch() {
		switch ( state ) {
		case WAIT_EVAL_EXIT : 
			// Manage remote bombs
			Log.d(tag,tag+"Move remote bomb cache to history");
			r_historicalBombs.addAll( r_inturnBombs );
			r_inturnBombs.clear();
			break;
			
		case TURN_EVAL_EXIT :
			// manage local bombs
			Log.d(tag,tag+"Move local bomb cache to history");
			historicalBombs.addAll( inturnBombs );
			inturnBombs.clear();
			break;
			
		case WAITGAME_EXIT : /* ignore, there are no bombs to manage */ break;
		default : throw new RuntimeException(tag + "Manage bomb list in invalid state " + state);
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