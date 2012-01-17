package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.common.Score;
import com.gmail.wazappdotgithub.ships.comms.ComModule;
import com.gmail.wazappdotgithub.ships.comms.IComModule;
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
		//Log.d(tag,tag+"changing state to " + newstate);
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
			//Protocol.getInstance().send(new ReadyMessage("Player", cl.starter));
			Protocol.writeReady("Player", cl.starter, ComModule.getInstance().getOut());
			/*} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Main thread waited for blocking queue");
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					ReadyMessage ready;
					ready = Protocol.readReady(ComModule.getInstance().getIn());
					//read some data from the Message
					cl.opponentName = ready.nickname;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}
			protected void onPostExecute(Void result) {
				// Start whoever was designated as starter
				if ( cl.starter )
					enterState(Statename.TURN);
				else 
					enterState(Statename.WAIT);
				
			}
		}.execute();

	};

	/*
	 * Let the client enter TURN state where the user can place bombs to be sent
	 */
	private static void enterTurn() {
		cl.manageBombsForStateSwitch();
		cl.recountBombs();

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
				Thread.currentThread().setName("Turn Evaluation networking ");
				IComModule network = ComModule.getInstance();
				try {
					Protocol.writeBombs(cl.inturnBombs, network.getOut());
					cl.inturnBombs = Protocol.readBombs(network.getIn());
					EndMessage end = Protocol.readGameState(network.getIn());
					cl.is_game_over = end.isGameOver;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}
			protected void onPostExecute(Void result) {
				Score.scoreme(cl.myscore, cl.inturnBombs);
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
				Thread.currentThread().setName("Wait Evaluation networking");
				IComModule network = ComModule.getInstance();
				
				try {
					ALog.d(tag,"Awaiting incomging bombs");
					cl.r_inturnBombs = Protocol.readBombs(network.getIn());
					List<Bomb> bombsToRemote = new LinkedList<Bomb>();
					
					for(Bomb b : cl.r_inturnBombs) {
						b = cl.board.bombCoordinate(b);
						bombsToRemote.add(b);
					}
					
					ALog.d(tag,"Writing response");
					Protocol.writeBombs(bombsToRemote, network.getOut());
					
					cl.is_game_over = cl.board.numLiveShips() == 0;
					
					ALog.d(tag,"Writing gamestate");
					Protocol.writeGameState(cl.is_game_over, network.getOut());
					
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(Void result) {
				// TODO odd to evaluate here, and send score back and forth?
				// this way both phones have to spend time evaluating
				Score.scoreme(cl.r_score, cl.r_inturnBombs);
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
