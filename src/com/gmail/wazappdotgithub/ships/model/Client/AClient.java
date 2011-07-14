package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

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
	protected List<Bomb> inturnBombs = null;
	
	public AClient() {
		board = Game.getNewBoard();
		shootingRange = new LinkedList<Bomb>(); // possibly better with an ArrayList
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
	public ClientState getState() {
		return currentState;
	}


	@Override
	public void placeBomb(int xcoord, int ycoord) {
		//TODO sanity check on input!
		Bomb b = new Bomb(xcoord, ycoord);
		
		if ( shootingRange.contains(b) )
			return;
		
		if ( inturnBombs.contains(b) ) // compares coords
			inturnBombs.remove(b);
		else
			inturnBombs.add(b);
	}


	@Override
	public void reportReady() {
		board.finalise();
		Game.getConfiguredInstance().clientReportReadyForGame(this, board);
	}


	@Override
	public void setToStateInTurn() {
		currentState = Game.ClientState.INTURN;
		setChanged();
		notifyObservers(currentState);
		
		inturnBombs = new LinkedList<Bomb>();
	}


	@Override
	public void setToStatePostGame() {
		currentState = Game.ClientState.POSTGAME;
		setChanged();
		notifyObservers(currentState);
	}


	@Override
	public void setToStatePreGame() {
		currentState = Game.ClientState.PREGAME;
		board = Game.getNewBoard();
		shootingRange = new LinkedList<Bomb>();
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
		Game.getConfiguredInstance().clientReportFinishedBombing(this);
	}

}
