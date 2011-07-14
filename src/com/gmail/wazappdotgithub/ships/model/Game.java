package com.gmail.wazappdotgithub.ships.model;

import java.util.List;
import java.util.Observable;

import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;
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

	public static enum ClientState {
		WAITGAME,
		PREGAME,
		WAIT,
		INTURN,
		POSTGAME,
	}
	
	private Game() {
		
	}
	
	public static  IGame getConfiguredInstance() {
		if ( instance == null )
			throw new IllegalStateException("calling getConfiguredInstance without instance");
		
		return instance;
	}
	
	public static IGame startLocalOpponentInstance() {
		instance = new Game();
		//maybe null all parameters here...
		player = LocalClient.newInstance();
		opponent = ComputerClient.newInstance();
		
		changeStateOfClient(player, Game.ClientState.PREGAME);
		changeStateOfClient(opponent, Game.ClientState.PREGAME);
		
		return instance;
	}
	
	private static void changeStateOfClient(IShipsClient client, Game.ClientState state) {
		switch (state) {
		case INTURN : client.setToStateInTurn(); break;
		case WAIT : client.setToStateWait(); break;
		case WAITGAME : client.setToStateWaitGame(); break;
		case PREGAME : client.setToStatePreGame(); break;
		case POSTGAME : client.setToStatePostGame(); break;
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
		
		if (shootingclient == player)
			b.setHit(opponentBoard.hasShip(b.x, b.y));
		else
			b.setHit(playerBoard.hasShip(b.x, b.y));
		
		return b;
	}

	@Override
	public void clientReportFinishedBombing(IShipsClient client) {
		if ( client != currentPlayer )
			throw new IllegalStateException("client finished bombing is not currentplayer");
			//assess states
		
		IShipsClient temp = currentOpponent;
		currentOpponent = currentPlayer;
		currentPlayer = temp;
		
		changeStateOfClient(currentOpponent, Game.ClientState.WAIT);
		changeStateOfClient(currentPlayer, Game.ClientState.INTURN);
	}

	@Override
	public IShipsClient getLocalClient() {
		return player;
	}

	@Override
	public Observable getLocalClientObservable() {
		return (Observable) player;
	}

	@Override
	public List<Bomb> getOpponentsLatestShots() {
		return currentOpponent.getInTurnBombs();
	}
	@Override
	public List<Bomb> getOpponentsShots() {
		return currentOpponent.getBombsBoard();
	}
	
}
