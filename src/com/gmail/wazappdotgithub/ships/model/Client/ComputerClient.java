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
import com.gmail.wazappdotgithub.ships.common.StartBombMessage;
import com.gmail.wazappdotgithub.ships.model.BoardUsingSimpleShip;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;

/*
 * quite unintelligent ai-powered client that will act as the default opponent
 */

public final class ComputerClient implements Runnable {

	private static ComputerClient instance = null;
	protected String tag = "Ships ComputerClient ";

	private Socket sock = null;

	protected Statename state;
	protected String opponentName;
	protected boolean starter = false;
	protected boolean is_game_ove = false;
	protected boolean is_winner = false;

	/* Local data */
	protected IBoard board = null;
	// List of all bombs placed by this client
	protected List<Bomb> historicalBombs = null;
	// List of bombs placed during this turn
	protected List<Bomb> inturnBombs = null;
	// List of bombs that hit a target during the latest turn 
	private List<Bomb> hits = null;
	private Queue<Bomb> prio = null;

	protected int bombstoplace;

	// Some AI
	private Random rand;

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
		boolean gameOver = false;

		try {
			out = new DataOutputStream(sock.getOutputStream());
			in = new DataInputStream(sock.getInputStream());

			//report ready as soon as the player has done so
			ReadyMessage rm = new ReadyMessage("Phone", false);

			new ReadyMessage().readFrom(in); // don't care about the name etc.
			rm.writeTo(out);
			out.flush();

			gameOver = false;

			while ( ! gameOver ) {
				//read start of transmission
				StartBombMessage sbm = new StartBombMessage();
				sbm.readFrom(in);
				int count = sbm.number;

				//read bombs
				List<Bomb> opp = new LinkedList<Bomb>();
				for ( int i = 0; i < count; i++) {
					Bomb b = new Bomb(-1, -1);
					b.readFrom(in);
					//check if hit
					b = board.bombCoordinate(b);
					opp.add(b);
				}

				//Write start of transmission
				sbm.writeTo(out);

				//write bombs
				for ( Bomb b : opp) {
					b.writeTo(out);
				}
				
				//write end result
				EndMessage end = new EndMessage(board.numLiveShips() <= 0);
				end.writeTo(out);
				out.flush();

				if ( end.isGameOver )
					break;

				//create bombs
				
				Log.d(tag,tag+"executing Turn");
				recountBombs();
				//generate a new set of bombs to send next time
				generatePriorityBombs();
				hits.clear();

				//write start transmission
				sbm = new StartBombMessage();
				sbm.number = bombstoplace;
				sbm.writeTo(out);

				//write bombs
				for (int j = 0; j < bombstoplace; j++) {
					Bomb b = getBomb();
					inturnBombs.add(b);

					b.writeTo(out);
				}

				out.flush();
				inturnBombs.clear();

				//read start transmission
				Log.d(tag,tag+"waiting for response");
				sbm.readFrom(in);

				// read bombs
				for (int j = 0; j < bombstoplace; j++) {
					Bomb b = new Bomb(-1,-1);
					b.readFrom(in);
					
					//store hits for future reference
					if ( b.hit ) {
						hits.add(b);
					}

					historicalBombs.add(b);
				}

				//read end result
				end = new EndMessage(false);
				end.readFrom(in);
				gameOver = end.isGameOver;
				Log.d(tag,tag+"gameOver? " + gameOver);

			}

			Log.w(tag,tag+"shutting down");
			in.close();
			out.close();
			sock.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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