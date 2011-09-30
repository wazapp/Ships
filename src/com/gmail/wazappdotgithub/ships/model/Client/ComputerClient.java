package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.Random;
import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;

/*
 * quite unintelligent ai-powered client that will act as the default opponent
 */

public final class ComputerClient extends AClient {
	
	private static ComputerClient instance = null;
	protected String tag = "Ships_ComputerClient ";
	// Some AI
	private Random rand;
	
	//just a random bomb
	private Bomb getBomb() {
		Bomb boom = null;
		while ( boom == null || historicalBombs.contains(boom) || inturnBombs.contains(boom)) {
			boom = new Bomb(rand.nextInt(Constants.DEFAULT_BOARD_SIZE),
					rand.nextInt(Constants.DEFAULT_BOARD_SIZE));
			/*Log.d(tag, tag + "bomb is " 
					+ String.valueOf(boom == null) 
					+ String.valueOf(shootingRange.contains(boom) + " (" +shootingRange.size() +") "
					+ String.valueOf(inturnBombs.contains(boom)) +  " (" +inturnBombs.size() +") "));
					*/
		}
		
		return boom;
	}
	
	@Override
	protected String tag() {
		return tag;
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
		initialize();
		
		board.randomiseShipsLocations();
		board.finalise();
		setChanged();
		notifyObservers(currentState);
		
		reportReady();
	}

	private void generateBombs() {
		for (int i = 0; i < bombstoplace; i++ )
			inturnBombs.add(getBomb());
	}
	
	@Override 
	protected void enterStateActions(Game.ClientState newstate) {
		currentState = newstate;
		
		switch ( currentState ) {
		case INTURN : restockBombs(); generateBombs(); requestNextState(); break;
		case I_EVALUATE : requestNextState(); break; // not interested in the result at the moment
		case I_COMPLETEDEVALUATION : requestNextState(); break;
		case READYCHANGETURNS : break; // wait for game (other player)
		case WAIT : manageBombsForStateSwitch() ; break; // automatic
		case W_EVALUATE : requestNextState(); break;
		case W_COMPLETEDEVALUATION : requestNextState(); break;
		default : break;
		}
		
		setChanged();
		notifyObservers(currentState);
	}	
}