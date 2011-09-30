package com.gmail.wazappdotgithub.ships.model;

import java.util.List;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
/**
 * implementation of IGame, contains game logic and manages the states of clients 
 * @author tor
 */
public class Game implements IGame {

	private static IGame instance = null;
	private static IShipsClient player = null,opponent = null;
	private IShipsClient currentPlayer = null,currentOpponent = null;
	private IBoard playerBoard = null, opponentBoard = null;

	private static String tag = "Ships_Game";

	/*
	 *  A number of states in which a Client must be.
	 *  The RECOUNT and state is not really a state. More of a signal,
	 *  It can probably be remove/replaced by other functionality
	 */
	public static enum ClientState {
		PREGAME,
		WAITGAME,
		INTURN,
		I_EVALUATE,
		W_EVALUATE,
		I_COMPLETEDEVALUATION,
		W_COMPLETEDEVALUATION,
		READYCHANGETURNS,
		WAIT,
		POSTGAMEWINNER,
		POSTGAMELOOSER,
		RECOUNTBOMBS,  //TODO remove this, it is not a state
	}
	
	// an array which represents the order of states between which a client will move
	private ClientState[] inGameStateArray = {
			//currentPlayer states = 0
			ClientState.INTURN, 
			ClientState.I_EVALUATE,
			ClientState.I_COMPLETEDEVALUATION,
			ClientState.READYCHANGETURNS,
			//currentOpponent states = 4
			ClientState.WAIT,
			ClientState.W_EVALUATE,
			ClientState.W_COMPLETEDEVALUATION,
			ClientState.READYCHANGETURNS
	};
	
	private Game() {

	}

	/**
	 * Return The existing IGame. If no IGame exists it will throw an IllegalStateException
	 * @return The existing IGame, if it exists
	 */
	public static  IGame getConfiguredInstance() throws IllegalStateException {
		if ( instance == null )
			throw new IllegalStateException("calling getConfiguredInstance without instance");

		return instance;
	}

	/**
	 * Instantiate a new Game with a local Opponent (ComputerClient)
	 * returns the new Instance 
	 * @param InitiatingClient the Client which initiated the Game
	 * @return The new Instance of IGame
	 */
	public static IGame startLocalOpponentInstance(IShipsClient InitiatingClient) {
		instance = new Game();
		//maybe null all parameters here...
		player = InitiatingClient;
		opponent = ComputerClient.newInstance();

		Log.d(tag, tag + " initiating players to PREGAME");
		changeStateOfClient(player, ClientState.PREGAME);
		changeStateOfClient(opponent, ClientState.PREGAME);

		return getConfiguredInstance();
	}
	
	// will call the client and ask to update to the requested state 
	private static void changeStateOfClient(IShipsClient client, ClientState state) {
		switch (state) {
		case WAITGAME 		: client.setToStateWaitGame(); break;
		case PREGAME 		: client.setToStatePreGame(); break;
		case POSTGAMEWINNER : client.setToStatePostGameAsWinner(); break;
		case POSTGAMELOOSER : client.setToStatePostGameAsLooser(); break;
		default : client.putToNextState(state); break;
		}
	}

	/**
	 * Generate and return a new board
	 * @return a new IBoard
	 */
	public static IBoard getNewBoard() {
		return new BoardUsingSimpleShip();
	}

	@Override
	public void clientReportReadyForGame(IShipsClient client, IBoard board) {
		if ( client == player )
			playerBoard = board;
		else
			opponentBoard = board;

		changeStateOfClient(client, ClientState.WAITGAME);

		if ( player.getState() == ClientState.WAITGAME 
				&& opponent.getState() == ClientState.WAITGAME ) {
			Log.d(tag, tag + " both players awaiting game, starting...");
			startNewGame(player, opponent);
		}
	}

	// starts the actual game and moves the Clients from waitgame into inturn and wait
	private void startNewGame(IShipsClient player0, IShipsClient player1) {
		if ( currentPlayer == null )
			selectStartPlayer();

		Log.d(tag, tag + " game started");
		changeStateOfClient(currentOpponent, ClientState.WAIT);
		changeStateOfClient(currentPlayer, ClientState.INTURN);
	}

	private void selectStartPlayer() {
		//simple for now
		currentPlayer = player;
		currentOpponent = opponent;
	}

	// calculate and return the next state for the given client
	private ClientState nextState(IShipsClient client) {
		ClientState state = client.getState();
		int i = 4;
		if ( client == currentPlayer )
			i = 0;
		
		while ( i < inGameStateArray.length - 1 ) {
			if ( state == inGameStateArray[i] ) {
				return inGameStateArray[i+1];
			}
			i++;
		}
		return inGameStateArray[0];
	}

	@Override
	public Bomb dropBomb(IShipsClient shootingclient, Bomb b) {

		if (shootingclient == player) {
			b = opponentBoard.bombCoordinate(b);
		}
		else {
			b = playerBoard.bombCoordinate(b);
		}

		Log.d(tag, tag + " Bomb properties " 
				+ "(" + b.x + "," + b.y +") " 
				+ "hit="+b.hit + ", "
				+ "destrship="+b.destrship);
		return b;
	}

	/* Evaluates if the games is over and sets winner/looser
	 * Otherwise 
	 * perform a change turn and updates the internal pointers
	 */
	private void changeTurns() {
		Log.d(tag, tag + " changing turns");
		Log.d(tag, tag + " currentPlayer has " + currentPlayer.numLiveShips() + "ships");
		Log.d(tag, tag + " currentOpponent has " + currentOpponent.numLiveShips() + "ships");

		//Check if the game is finished else - continue	
		if ( currentOpponent.numLiveShips() == 0) {
			changeStateOfClient(currentOpponent, ClientState.POSTGAMELOOSER);
			changeStateOfClient(currentPlayer, ClientState.POSTGAMEWINNER);
			return;
			
		} else { 
			if ( currentPlayer.getHistoricalBombs().size() == Constants.DEFAULT_BOARD_SIZE*Constants.DEFAULT_BOARD_SIZE)
				throw new IllegalStateException("The player has filled the board with bombs, but opponent is not dead");
			
			IShipsClient temp = currentOpponent;
			currentOpponent = currentPlayer;
			currentPlayer = temp;
			
			changeStateOfClient(currentOpponent,ClientState.WAIT);
			changeStateOfClient(currentPlayer,ClientState.INTURN);
		}
	}

	@Override
	public void progressState(IShipsClient client) {
		ClientState newstate = nextState(client);
		
		if ( client == currentPlayer ) { // need to ensure this because of how the computerclient works
			ClientState co = currentOpponent.getState();
			
			if ( newstate == ClientState.I_EVALUATE && co == ClientState.WAIT ) {
				// automatic move to evaluate also for the opponent
				changeStateOfClient(client, newstate);
				changeStateOfClient(currentOpponent, nextState(currentOpponent));
				return;
				
			} else if ( newstate == ClientState.READYCHANGETURNS && co == ClientState.READYCHANGETURNS) {
				changeStateOfClient(client, newstate);
				changeTurns();
				return;
			}
			
			changeStateOfClient(client, newstate);
			return;		
		} else { // currentOpponent
			ClientState cp = currentPlayer.getState();
			
			if ( newstate == ClientState.READYCHANGETURNS && cp == ClientState.READYCHANGETURNS) {
				changeStateOfClient(client, newstate);
				changeTurns();
				return;
			}
			
			changeStateOfClient(client, newstate);
		}
	}


	@Override
	public List<Bomb> getInTurnClientAcceptedBombs() {
		return currentPlayer.getLatestTurnBombs();
	}

	@Override
	public List<Bomb> getInTurnClientHistoricalBombs() {
		return currentPlayer.getHistoricalBombs();
	}
}
