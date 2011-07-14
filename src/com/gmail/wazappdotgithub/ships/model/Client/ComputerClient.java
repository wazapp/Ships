package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;
import java.util.Random;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;

/*
 * quite unintelligent ai-powered client that will act as the default opponent
 */

public final class ComputerClient extends AClient {
	
	private static ComputerClient instance = null;
	// Some AI
	private Random rand;
	
	//just a random bomb
	private Bomb getBomb() {
		Bomb boom = null;
		while ( boom == null || shootingRange.contains(boom) ) {
			boom = new Bomb(rand.nextInt(Constants.DEFAULT_BOARD_SIZE),
					rand.nextInt(Constants.DEFAULT_BOARD_SIZE));
		}
		
		return boom;
	}
	
	
	public static IShipsClient newInstance() {
		instance = new ComputerClient();
		
		return instance;
	}
	public static IShipsClient getInstance() {
		if ( instance == null )
			ComputerClient.newInstance();
		
		return instance;
	}
	
	private ComputerClient() {
		rand = new Random(System.currentTimeMillis());
	}

	@Override
	public void setToStatePreGame() {
		currentState = Game.ClientState.PREGAME;
		
		board = Game.getNewBoard();
		shootingRange = new LinkedList<Bomb>();
		board.randomiseShipsLocations();
		board.finalise();
		setChanged();
		reportReady();
	}
	
	@Override
	public void setToStateInTurn() {
		currentState = Game.ClientState.INTURN;
		setChanged();
		notifyObservers(currentState);
		
		inturnBombs = new LinkedList<Bomb>();
		
		inturnBombs.add(Game.getConfiguredInstance().dropBomb(this, getBomb()));
		reportAcceptBombs();
	}
}