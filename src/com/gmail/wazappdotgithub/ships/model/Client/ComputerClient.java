package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.model.BoardUsingSimpleShip;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;

/*
 * quite unintelligent ai-powered client that will act as the default opponent
 */

public final class ComputerClient implements Runnable {

	private static ComputerClient instance = null;
	private static String name = "Phone";
	protected String tag = "Ships ComputerClient ";

	private Socket sock = null;

	protected boolean starter = false;
	protected boolean is_winner = false;
	protected boolean gameOver = false;
	protected int bombstoplace;
	
	protected IBoard board = null;
	// List of all bombs placed by this client
	protected List<Bomb> historicalBombs = null;
	// List of bombs placed during this turn
	protected List<Bomb> inturnBombs = null;
	
	// Some AI
	private Random rand;
	// List of bombs that hit a target during the latest turn 
	private List<Bomb> hits = null;
	private Queue<Bomb> prio = null;

	public static Runnable newInstance() throws UnknownHostException, IOException {
		instance = new ComputerClient();

		return instance;
	}
	public static ComputerClient getInstance() {
		if ( instance == null )
			throw new RuntimeException("calling getInstance, but there is no instance or Computerclient");

		return instance;
	}

	private ComputerClient() throws UnknownHostException, IOException {
		sock = new Socket("localhost",Constants.DEFAULT_PORT);
		rand = new Random(System.currentTimeMillis());
		board = new BoardUsingSimpleShip();
		board.randomiseShipsLocations();

		historicalBombs = new LinkedList<Bomb>();
		inturnBombs = new LinkedList<Bomb>();
		hits = new LinkedList<Bomb>();
		prio = new LinkedList<Bomb>();
	}

	@Override
	public void run() {
		Thread.currentThread().setName(tag);
		DataOutputStream out = null;
		DataInputStream in = null;
		
		try {
			out = new DataOutputStream(sock.getOutputStream());
			in = new DataInputStream(sock.getInputStream());
			
			//report ready as soon as the player has done so
			ReadyMessage rm = Protocol.readReady(in);
			starter = ! rm.starting;
			Protocol.writeReady(ComputerClient.name, starter, out);

			if ( starter )
				gameAsServer(out, in);
			else
				gameAsClient(out, in);

			Log.w(tag,tag+"shutting down");
			in.close();
			out.close();
			sock.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Protocol loop for anyone who starts in TURN
	 */
	private void gameAsServer(DataOutputStream out ,DataInputStream in) throws InterruptedException, IOException {
		while ( ! gameOver ) {
			actTurn(out, in);
			if ( gameOver ) // need a check here in the middle as well 
				return;
			actWait(out, in);
		}
	}
	/*
	 * Protocol loop for anyone who starts in WAIT
	 */
	private void gameAsClient(DataOutputStream out , DataInputStream in) throws InterruptedException, IOException {
		while (! gameOver ) {
			actWait(out, in);
			if ( gameOver ) // need a check here in the middle as well
				return;
			actTurn(out, in);
		}
	}

	protected void actTurn(DataOutputStream out ,	DataInputStream in) throws IOException {
		List<Bomb> bombsFromRemote = null;
		
		recountBombs();
		generatePriorityBombs();
		hits.clear();
		inturnBombs.clear();
		
		for (int j = 0; j < bombstoplace; j++) {
			Bomb b = getBomb();
			inturnBombs.add(b);
		}

		Log.d(tag,tag+"actTurn, Writing bombs");
		Protocol.writeBombs(inturnBombs, out);
		
		Log.d(tag,tag+"actTurn, Waiting for response");
		bombsFromRemote = Protocol.readBombs(in);
		for ( Bomb b : bombsFromRemote ) {
			//store hits for future reference
			if ( b.hit )
				hits.add(b);
			
			historicalBombs.add(b);
		}
		
		Log.d(tag,tag+"actTurn, Reading Gamestate");
		EndMessage end = Protocol.readGameState(in);
		gameOver = end.isGameOver;
		
	}

	protected void actWait(DataOutputStream out ,	DataInputStream in) throws IOException {
		Log.d(tag,tag+"actWait, Awaiting incomging bombs");
		List<Bomb> bombsFromRemote = Protocol.readBombs(in);
		List<Bomb> bombsToRemote = new LinkedList<Bomb>();
		
		for(Bomb b : bombsFromRemote) {
			b = board.bombCoordinate(b);
			bombsToRemote.add(b);
		}
		
		Log.d(tag,tag+"actWait, Writing response");
		Protocol.writeBombs(bombsToRemote, out);
		
		gameOver = board.numLiveShips() == 0;
		
		Log.d(tag,tag+"actWait, Writing gamestate");
		Protocol.writeGameState(gameOver, out);
	}
	
	/*
	 * Create a number of bombs per hit from latest round
	 * stores them all in priority Bombs list, 
	 * execute this method after evaluation of the opponents bombs
	 * and before calls to getBomb()
	 */
	private void generatePriorityBombs() {
		//generate bombs close to a hit from the latest round
		int i = prio.size();
		
		Log.d(tag, tag + "there were "+ hits.size() +" hits from last round");
		for (Bomb boom : hits) {
			//4 coordinates most likely to contain a ship
			Bomb[] closest = {
					new Bomb(boom.x + 1, boom.y),
					new Bomb(boom.x - 1, boom.y),
					new Bomb(boom.x, boom.y + 1),
					new Bomb(boom.x, boom.y - 1)
			};

			//ensure they are valid
			for (Bomb b : closest) {
				if ( b.x >= 0 && 
						b.x < Constants.DEFAULT_BOARD_SIZE &&
						b.y >= 0 &&
						b.y < Constants.DEFAULT_BOARD_SIZE
						) {

					//if it is, store in prio list
					prio.offer(b);
				}
			}
		}
		
		Log.d(tag, tag + "added "+ (prio.size() - i) +" potential locations");
	}

	private Bomb getBomb() {
		Bomb boom = null;
		
		//while the queue is not empty
		while ( (boom = prio.poll()) != null) {
			if ( historicalBombs.contains(boom) ) {
				Log.d(tag, tag + " poll in history " + boom);
			} else if ( inturnBombs.contains(boom) ) {
				Log.d(tag, tag + " poll inturn "+ boom);
			} else {
				Log.d(tag, tag + " poll ok "+ boom);
				return boom;
			}
		}
		
		Log.d(tag, tag + " queue empty, resorting to random");
		//if, at any point the queue was empty, resort to random bomb
		while ( boom == null || historicalBombs.contains(boom) || inturnBombs.contains(boom)) {
			boom = new Bomb(rand.nextInt(Constants.DEFAULT_BOARD_SIZE),
					rand.nextInt(Constants.DEFAULT_BOARD_SIZE));
		}
		
		Log.d(tag,tag + "boom "+ boom.x +", " +boom.y);
		return boom;
	}

	private void recountBombs() {
		int remaining_spaces = Constants.DEFAULT_BOARD_SIZE * Constants.DEFAULT_BOARD_SIZE - historicalBombs.size();
		bombstoplace = board.numLiveShips();
		if (bombstoplace > remaining_spaces)
			bombstoplace = remaining_spaces;
	}
}