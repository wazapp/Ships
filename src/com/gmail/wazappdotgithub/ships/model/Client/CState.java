package com.gmail.wazappdotgithub.ships.model.Client;

import android.os.AsyncTask;
import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.StartBombMessage;
import com.gmail.wazappdotgithub.ships.common.Protocol;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

/*
 * RULES and DESCRIPTION
 * enterTurnEval() and enterWaitEval() spawns an Aynchronous Task
 * to deal with the network communication.
 * These threads will notify Observers (UI) when completed.
 * 
 * INIT states does not notify at all
 * 
 * an EXITstate will notify the UI immediately and automatically
 * move to the next state
 * 
 * an ENTERstate will notify when completed and stay there
 * 
 * The Client (UI) can only request EXITstate methods.
 */

public final class CState {
	private static RemoteClient cl;
	private static String tag = "Ships CState ";

	public static void initClient(RemoteClient c) {
		cl = c;
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
		//case INIT : enterInit();
		case PREGAME : enterPreGame(); break;
		case WAITGAME : enterWaitGame(); break;
		case TURN : enterTurn(); break;
		case WAIT : enterWait(); break;
		case TURN_EVAL : enterTurnEval(); break;
		case WAIT_EVAL : enterWaitEval(); break;
		case GAMEOVER : enterGameOver(); break;
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
		case INIT : exitInit();  break;
		case PREGAME : exitPreGame(); break;
		case WAITGAME : exitWaitGame(); break;
		case TURN : exitTurn(); break;
		case WAIT : exitWait(); break;
		case TURN_EVAL : exitTurnEval(); break;
		case WAIT_EVAL : exitWaitEval(); break;
		default : throw new IllegalStateException("No such state " + s);
		}

	}

	/* Convenience, used throughout the class */ 
	private static void stateUpdate(Statename newstate) {
		Log.d(tag,tag+"changing state to " + newstate);
		cl.state = newstate;
		cl.setToChanged();
		cl.notifyObservers(newstate);
	}

	/*
	 * NOTE the INIT states does not notify the UI
	 */
	private static void exitInit() {
		enterState(Statename.PREGAME);
		//nothing to be done here
	}

	/*
	 * Called to prepare the client model, before starting any game
	 */
	private static void enterPreGame() {
		//nothing to be done here
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

		enterState(Statename.WAITGAME);
	};
	/*
	 * Wait until the ready message arrives from the opponent
	 * When it does, progress
	 */
	private static void enterWaitGame() {
		//nothing to be done here
		stateUpdate(Statename.WAITGAME);
	};

	/*
	 * Go to either Turn or Wait state
	 */
	private static void exitWaitGame() {
		stateUpdate(Statename.WAITGAME_EXIT);
		
		try {
			ReadyMessage ready;
			ready = (ReadyMessage) Protocol.getInstance().retrieve();
			//read some data from the Message
			cl.opponentName = ready.nickname;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setName("Turn Evaluation networking");
				try {
					Protocol network = Protocol.getInstance();
					
					//Let the protocol know how many bombs to expect
					StartBombMessage sbm = new StartBombMessage();
					sbm.number = cl.inturnBombs.size();
					network.send(sbm);

					//send them
					for (Bomb b : cl.inturnBombs) {
						network.send(b);
					}

					// replace the sent bombs with the received 
					// bombs that contain hit information
					cl.inturnBombs.clear();
					
					StartBombMessage s = (StartBombMessage) network.retrieve();
					int count = s.number;
					
					for (int i = 0; i < count; i++) {
						Bomb b = (Bomb) network.retrieve();
						cl.inturnBombs.add(b);
					}

					//read if the game is over
					EndMessage end = (EndMessage) network.retrieve();
					cl.is_game_over = end.isGameOver;

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
			protected void onPostExecute(Void result) {
				stateUpdate(Statename.TURN_EVAL);
			}
			
		}.execute();
	};

	/*
	 * Bombs have been evaluated
	 */
	private static void exitTurnEval() {
		stateUpdate(Statename.TURN_EVAL_EXIT);
		if ( cl.is_game_over )
			enterState(Statename.GAMEOVER);
		else
			enterState(Statename.WAIT);
	};

	/*
	 * method is called when the player has exited the TURNeval state
	 */
	private static void enterWait() {
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
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.currentThread().setName("Wait Evaluation networking");
					Protocol network = Protocol.getInstance();
					
					//find out how many bombs the opponent sends
					StartBombMessage s = (StartBombMessage) network.retrieve();
					int count = s.number;

					if ( count == 0 )
						throw new RuntimeException(tag + "Scheduled to recieve 0 bombs");
					Log.d(tag, tag+"recieving and evaluating"+count+" bombs");
										
					//retrieve the remote bombs
					for (int i = 0; i < count; i++) {
						Bomb b = (Bomb) network.retrieve();
						// Bomb the coordinate locally and place the bomb in
						// the local cache
						Log.d(tag, tag+"evaluating"+i);
						b = cl.getBoard().bombCoordinate(b);
						cl.r_inturnBombs.add(b);
						
					}
					
					if ( cl.r_inturnBombs.size() != count )
						throw new RuntimeException(tag + "local cache mismatch, possibly enter TURN before completion");
					
					//expect to send the same number back
					network.send(s);
					
					//send response back
					int x = 0;
					for ( Bomb b : cl.r_inturnBombs) {
						network.send(b);
						x++;
					}
					
					Log.d(tag, tag+"schedule for send "+ x + " bombs");
					//send the state of the game to the opponent
					cl.is_game_over = cl.getBoard().numLiveShips() == 0;
					network.send(new EndMessage(cl.is_game_over));

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(Void result) {
				stateUpdate(Statename.WAIT_EVAL);
			}	

		}.execute();
	};

	/*
	 * Called whenever the player is finished viewing the 
	 * remote bombs
	 */
	private static void exitWaitEval() {
		stateUpdate(Statename.WAIT_EVAL_EXIT);
		if ( cl.is_game_over )
			enterState(Statename.GAMEOVER);
		else
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
