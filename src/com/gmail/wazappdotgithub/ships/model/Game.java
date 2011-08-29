package com.gmail.wazappdotgithub.ships.model;

import java.util.List;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
/**
 * implementation of IGame, contains game logic and manages the states of clients 
 * @author tor
 *
 */
public class Game implements IGame {
	
	private static IGame instance = null;
	private static IShipsClient player = null,opponent = null;
	private IShipsClient currentPlayer = null,currentOpponent = null;
	private IBoard playerBoard = null, opponentBoard = null;

	private String tag = "Ships_Game";
	public static enum ClientState {
		WAITGAME,
		PREGAME,
		WAIT,
		INTURN,
		POSTGAMEWINNER,
		POSTGAMELOOSER,
		RECOUNTBOMBS
	}
	
	private Game() {
		
	}
	
	public static  IGame getConfiguredInstance() {
		if ( instance == null )
			throw new IllegalStateException("calling getConfiguredInstance without instance");
		
		return instance;
	}
	
	public static IGame startLocalOpponentInstance(IShipsClient InitiatingClient) {
		instance = new Game();
		//maybe null all parameters here...
		player = InitiatingClient;
		opponent = ComputerClient.newInstance();
		
		changeStateOfClient(player, Game.ClientState.PREGAME);
		changeStateOfClient(opponent, Game.ClientState.PREGAME);
		
		return instance;
	}
	
	private static void changeStateOfClient(IShipsClient client, Game.ClientState state) {
		switch (state) {
		case INTURN 		: client.setToStateInTurn(); break;
		case WAIT 			: client.setToStateWait(); break;
		case WAITGAME 		: client.setToStateWaitGame(); break;
		case PREGAME 		: client.setToStatePreGame(); break;
		case POSTGAMEWINNER : client.setToStatePostGameAsWinner(); break;
		case POSTGAMELOOSER : client.setToStatePostGameAsLooser(); break;
		default : throw new IllegalStateException("state could not be changed");
		}
	}
	
	public static IBoard getNewBoard() {
		return new BoardUsingSimpleShip();
	}

	@Override
	public void clientReportReadyForGame(IShipsClient client, IBoard board) {
		if ( client == player )
			playerBoard = board;
		else
			opponentBoard = board;
		
		changeStateOfClient(client, Game.ClientState.WAITGAME);
		
		if ( player.getState() == Game.ClientState.WAITGAME 
				&& opponent.getState() == Game.ClientState.WAITGAME ) {
			startNewGame(player, opponent);
		}
	}
	
	private void selectStartPlayer() {
		//simple for now
		currentPlayer = player;
		currentOpponent = opponent;
	}
	
	private void startNewGame(IShipsClient player0, IShipsClient player1) {
		if ( currentPlayer == null )
			selectStartPlayer();
		
		changeStateOfClient(currentOpponent, Game.ClientState.WAIT);
		changeStateOfClient(currentPlayer, Game.ClientState.INTURN);
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

	@Override
	public void clientReportFinishedBombing(IShipsClient client) {
		if ( client != currentPlayer )
			throw new IllegalStateException("client finished bombing is not currentplayer");
			//assess states

		Log.d(tag, tag + " currentPlayer has " + currentPlayer.numLiveShips() + "ships");
		Log.d(tag, tag + " currentOpponent has " + currentOpponent.numLiveShips() + "ships");
		
		//Check if the game is finished else - continue	
		if ( currentOpponent.numLiveShips() == 0) {
			changeStateOfClient(currentOpponent, Game.ClientState.POSTGAMELOOSER);
			changeStateOfClient(currentPlayer, Game.ClientState.POSTGAMEWINNER);
		
		} else { 
			if ( currentPlayer.getBombsBoard().size() == Constants.DEFAULT_BOARD_SIZE*Constants.DEFAULT_BOARD_SIZE)
				throw new IllegalStateException("The player has filled the board with bombs, but opponent is not dead");
			
			IShipsClient temp = currentOpponent;
			currentOpponent = currentPlayer;
			currentPlayer = temp;

			changeStateOfClient(currentOpponent, Game.ClientState.WAIT);
			changeStateOfClient(currentPlayer, Game.ClientState.INTURN);
		}
	}

	@Override
	public List<Bomb> getOpponentsLatestTurnBombs() {
		return currentOpponent.getLatestTurnBombs();
	}
	
	@Override
	public List<Bomb> getOpponentsBombsBoard() {
		return currentOpponent.getBombsBoard();
	}
}
