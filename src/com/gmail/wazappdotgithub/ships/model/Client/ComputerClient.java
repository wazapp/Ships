package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
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
				for ( Bomb b : opp)
					b.writeTo(out);
				
				//write end result
				EndMessage end = new EndMessage(board.numLiveShips() <= 0);
				end.writeTo(out);
				out.flush();
				
				if ( end.isGameOver )
					break;

				//create bombs
				Log.d(tag,tag+"executing Turn");
				recountBombs();

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
					Bomb b = getBomb();
					b.readFrom(in);
					
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

	//just a random bomb
	private Bomb getBomb() {
		Bomb boom = null;
		while ( boom == null || historicalBombs.contains(boom) || inturnBombs.contains(boom)) {
			boom = new Bomb(rand.nextInt(Constants.DEFAULT_BOARD_SIZE),
					rand.nextInt(Constants.DEFAULT_BOARD_SIZE));
		}

		return boom;
	}
	
	private void recountBombs() {
		int remaining_spaces = Constants.DEFAULT_BOARD_SIZE * Constants.DEFAULT_BOARD_SIZE - historicalBombs.size();
		bombstoplace = board.numLiveShips();
		if (bombstoplace > remaining_spaces)
			bombstoplace = remaining_spaces;
	}
}