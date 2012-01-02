package com.gmail.wazappdotgithub.ships.model.Client;

import java.util.LinkedList;

import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.StartBombMessage;
import com.gmail.wazappdotgithub.ships.common.Protocol;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.model.BoardUsingSimpleShip;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

public abstract class CState {
	private static RemoteClient cl;
	
	public static void setClient(RemoteClient c) {
		cl = c;
		enterState(Statename.PREGAME);
	}
	
	/*
	 * The purpose of running an enterState() method is to 
	 * prepare the client for the coming UI reading methods.
	 * 
	 * When entering the UI will be notified at the end of
	 * the process
	 */
	private static void enterState(Statename s) {
		switch (s) {
		case PREGAME : enterPreGame();
		case WAITGAME : enterWaitGame();
		case TURN : enterTurn();
		case WAIT : enterWait();
		case TURN_EVAL : enterTurnEval();
		case WAIT_EVAL : enterWaitEval();
		case GAMEOVER : enterGameOver();
		default : throw new IllegalStateException("No such state " + s);
		}
		
	}
	
	/*
	 * The purpose of running an exitState() method is to push the model from
	 * the old state to the new state. This should NOT affect the UI. 
	 * 
	 * When exiting the UI will be notified at the start of the exit process
	 * 
	 */
	protected static void exitState(Statename s) {
		switch (s) {
		case PREGAME : exitPreGame();
		case WAITGAME : exitWaitGame();
		case TURN : exitTurn();
		case WAIT : exitWait();
		case TURN_EVAL : exitTurnEval();
		case WAIT_EVAL : exitWaitEval();
		default : throw new IllegalStateException("No such state " + s);
		}
		
	}
	
	/* Convenience, used throughout the class */ 
	private static void stateUpdate(Statename newstate) {
		cl.state = newstate;
		cl.setToChanged();
		cl.notifyObservers(newstate);
	}
	
	/*
	 * Called to prepare the client model, before starting any game
	 */
	private static void enterPreGame() {
		//prepare local data
		cl.board = new BoardUsingSimpleShip();
		cl.historicalBombs = new LinkedList<Bomb>();
		cl.latestTurnBombs = new LinkedList<Bomb>();
		cl.inturnBombs = new LinkedList<Bomb>();
		
		//preparing remote data
		cl.r_historicalBombs = new LinkedList<Bomb>();
		cl.r_inturnBombs = new LinkedList<Bomb>();
		
		//let UI know it can modify the board etc.
		stateUpdate(Statename.PREGAME);
	};
	
	/*
	 * The User has chosen their configuration and is 
	 * prepared to progress
	 */
	private static void exitPreGame() {
		stateUpdate(Statename.PREGAME_EXIT);
		
		try {
			cl.board.finalise();
			Protocol.getInstance().send(new ReadyMessage("Player", cl.starter));
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Main thread waited for blocking queue");
		}
		
		enterState(Statename.WAIT);
	};
	/*
	 * Wait until the ready message arrives from the opponent
	 * When it does, progress
	 */
	private static void enterWaitGame() {
		stateUpdate(Statename.WAITGAME);
	};
	
	/*
	 * Go to either Turn or Wait state
	 */
	private static void exitWaitGame() {
		try {
			ReadyMessage ready;
			ready = (ReadyMessage) Protocol.getInstance().retrieve();
			//read some data from the Message
			cl.opponentName = ready.nickname;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stateUpdate(Statename.WAITGAME_EXIT);
		
		// Start whoever was designated as starter
		if ( cl.starter )
			enterState(Statename.TURN);
		else 
			enterState(Statename.WAIT);
	};
	
	/*
	 * Let the client enter TURN state where the user can place bombs to be sent
	 */
	private static void enterTurn() {
		//Manage bombs ( both remote and local )
		cl.manageBombsForStateSwitch();
		
		// use rules to determine the number of bombs to restock.
		cl.bombstoplace = cl.getBoard().numLiveShips();
		
		stateUpdate(Statename.TURN);
	};
	
	/*
	 * The User have accepted all the bombs and has asked to get hem evaluated
	 * and to view the result
	 */
	private static void exitTurn() {
		stateUpdate(Statename.TURN_EXIT);
		enterState(Statename.TURN_EVAL);
	};
	
	/*
	 * Send / Recieve the bombs, also evaluate the game state (game over or not)
	 */
	private static void enterTurnEval() {
		try {
			//Let the protocol know how many bombs to expect
			StartBombMessage sbm = new StartBombMessage();
			sbm.number = cl.inturnBombs.size();
			Protocol.getInstance().send(sbm);
			
			//TODO, compress this s little you can send / retreive in a single loop
			//send them
			for (Bomb b : cl.inturnBombs) {
				Protocol.getInstance().send(b);
			}
			
			// replace the sent bombs with the received 
			// bombs that contain hit information
			StartBombMessage s = (StartBombMessage) Protocol.getInstance().retrieve();
			int count = s.number;
			cl.inturnBombs.clear();
			
			for (int i = 0; i < count; i++) {
				Bomb b = (Bomb) Protocol.getInstance().retrieve();
				cl.inturnBombs.add(b);
			}
			
			//read if the game is over
			EndMessage end = (EndMessage) Protocol.getInstance().retrieve();
			cl.is_game_over = end.isGameOver;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stateUpdate(Statename.TURN_EVAL);
	};
	
	/*
	 * Bombs have been evaluated
	 */
	private static void exitTurnEval() {
		stateUpdate(Statename.TURN_EVAL_EXIT);
		if ( cl.is_game_over )
			enterState(Statename.GAMEOVER);
		
		enterState(Statename.WAIT);
	};
	
	/*
	 * method is called when the player has exited the TURNeval state
	 */
	private static void enterWait() {
		// manage local bombs
		cl.manageBombsForStateSwitch();
		
		stateUpdate(Statename.WAIT);
	};	
	
	/*
	 * Exit the wait state
	 */
	private static void exitWait() {
		stateUpdate(Statename.WAIT_EXIT);
		enterState(Statename.WAIT_EVAL);
	};
	
	/*
	 * Enter wait evaluation, where we attempt to retrieve the remote bombs
	 * and evaluate them
	 */
	private static void enterWaitEval() {
		// read in remote data
		try {
			//find out how many bombs the opponent sends
			StartBombMessage s = (StartBombMessage) Protocol.getInstance().retrieve();
			int count = s.number;
			
			//expect to send the same number back
			Protocol.getInstance().send(s);

			//retrieve the remote bombs
			for (int i = 0; i < count; i++) {
				Bomb b = (Bomb) Protocol.getInstance().retrieve();

				// Bomb the coordinate locally and place the bomb in
				// the local cache
				b = cl.getBoard().bombCoordinate(b);
				cl.r_inturnBombs.add(b);
				
				//send response back
				Protocol.getInstance().send(b);
			}
			
			//send the state of the game to the opponent
			cl.is_game_over = cl.getBoard().numLiveShips() == 0;
			Protocol.getInstance().send(new EndMessage(cl.is_game_over));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stateUpdate(Statename.WAIT_EVAL);
	};

	/*
	 * Called whenever the player is finished viewing the 
	 * remote bombs
	 */
	private static void exitWaitEval() {
		stateUpdate(Statename.WAIT_EVAL_EXIT);
		if ( cl.is_game_over )
			enterState(Statename.GAMEOVER);
		
		enterState(Statename.TURN);
	};
	
	/*
	 * Evaluate if the client is the winner or not and let the UI know
	 */
	private static void enterGameOver() {
		cl.is_winner = cl.board.numLiveShips() > 0;
		stateUpdate(Statename.GAMEOVER);
	}
}
