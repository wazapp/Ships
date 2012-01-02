package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.Protocol;
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
	protected String tag = "Ships_ComputerClient ";

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
		sock = new Socket("localhost",Protocol.listen_port);
		rand = new Random(System.currentTimeMillis());
		board = new BoardUsingSimpleShip();
		board.randomiseShipsLocations();
		
		historicalBombs = new LinkedList<Bomb>();
		inturnBombs = new LinkedList<Bomb>();
	}

	@Override
	public void run() {
		DataOutputStream out = null;
		DataInputStream in = null;
		boolean gameon = true;

		try {
			out = new DataOutputStream(sock.getOutputStream());
			in = new DataInputStream(sock.getInputStream());

			//report ready as soon as the player has done so
			ReadyMessage rm = new ReadyMessage("Phone", false);
			new ReadyMessage().readFrom(in);
			rm.writeTo(out);

			gameon = true;
			StartBombMessage sbm;
			EndMessage end;

			while ( gameon ) {
				//read and reply to the player
				sbm = new StartBombMessage();
				sbm.readFrom(in);
				int count = sbm.number;
				sbm.writeTo(out);

				for ( int i = 0; i < count; i++) {
					Bomb incoming = new Bomb(-1, -1);
					incoming.readFrom(in);
					incoming = board.bombCoordinate(incoming);
					incoming.writeTo(out);
				}

				end = new EndMessage(board.numLiveShips() == 0);
				end.writeTo(out);

				if ( end.isGameOver )
					break;


				//create bombs
				bombstoplace = board.numLiveShips();
				sbm.number = bombstoplace;
				sbm.writeTo(out);
				sbm.readFrom(in);

				for (int j = 0; j < bombstoplace; j++) {
					Bomb outgoing = getBomb();
					outgoing.writeTo(out);
					outgoing.readFrom(in);

				}
				end.readFrom(in);
				gameon = end.isGameOver;
			}
		
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
}