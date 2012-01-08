package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.AbstractMessage;
import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.common.StartBombMessage;
import com.gmail.wazappdotgithub.ships.common.AbstractMessage.MessageType;
import com.gmail.wazappdotgithub.ships.model.Bomb;

/*
 * The protocol's purpose is to manage the network communication
 * on behalf of the RemoteClient.
 * 
 * There are two ways to create a Protocol, either as a "Server"
 * by using the default newInstance()
 * or as a "client" by using the newInstance(addr, port)
 * 
 * Uses DEFAULT_PORT from Constants
 */
public class Protocol implements Runnable {

	/* Static fields */
	public static final String tag = "Ships Protocol ";
	private static Runnable instance;
	private static ServerSocket listenTo;
	private static Socket connect;

	/* instance fields */
	private boolean asServer = true;
	private boolean launchComputer = false;
	private boolean gameover = false;

	/*
	 * These need to be fair because we are retrieving messages in a specific order
	 * The longest expected stream is Start, 10 bombs, End
	 */
	private final BlockingQueue<AbstractMessage> outgoingQueue = new ArrayBlockingQueue<AbstractMessage>(12, true);
	private final BlockingQueue<AbstractMessage> incomingQueue = new ArrayBlockingQueue<AbstractMessage>(12, true);


	public static enum opponentType {
		REMOTECOMPUTER, // start computer client to communicate with
		REMOTEPERSON
	}

	public static Runnable newInstance(opponentType type) throws IOException {
		if ( instance == null )
			instance = new Protocol(type);

		return instance;
	}

	public static Runnable newInstance(opponentType type, Inet4Address addr, int port) throws IOException {
		if ( instance == null )
			instance = new Protocol(addr, port);		

		return instance;
	}

	public static Protocol getInstance() {
		if (instance == null)
			throw new RuntimeException("There is no instance of Protocol");

		return (Protocol) instance;
	}

	private Protocol(opponentType type) throws IOException {
		this.asServer = true;
		Log.d(tag, tag + "Initiating Protocol.. opening socket");
		listenTo = new ServerSocket(Constants.DEFAULT_PORT);

		if ( type == opponentType.REMOTECOMPUTER ) {
			launchComputer = true;
		}
	}

	private Protocol(Inet4Address addr, int port) throws IOException {
		Log.d(tag, tag + "Initiating Protocol.. connecting");
		this.asServer = false;
		connect = new Socket(addr,port);
		Log.d(tag, tag + "Initiating Protocol.. connect ok");
	}

	/*
	 * Schedule a message to the remote client
	 */
	public void send(AbstractMessage m) throws InterruptedException {
		outgoingQueue.put(m);
	}

	/*
	 * Schedule a retrieval of a message from the remote client
	 */
	public AbstractMessage retrieve() throws InterruptedException {
		return incomingQueue.take();
	}

	@Override
	public void run() {

		Thread.currentThread().setName(tag);
		DataOutputStream out = null;
		DataInputStream in = null;
		Socket remote = null;

		try {
			
			/* *********
			 *  Establish in and out streams
			 * *********/

			if (asServer) {
				//Start the computerclient if required
				if ( launchComputer ) {
					Log.d(tag, tag + "Launching Computer Opponent");
					Thread computer = new Thread(ComputerClient.newInstance());
					computer.start();
				}
				
				//listen to connections
				remote = listenTo.accept();
				Log.d(tag, tag + "accepted Connection");
				
			} else {
				remote = connect;
			}

			out = new DataOutputStream(remote.getOutputStream());
			in = new DataInputStream(remote.getInputStream());

			/* *********
			 *  Ensure both clients are ready
			 * *********/

			//Write the ready message to the remote player
			ReadyMessage rm = (ReadyMessage) outgoingQueue.take();
			rm.writeTo(out);
			out.flush();

			// expect a ready message from the remote player
			ReadyMessage response = new ReadyMessage();
			response.readFrom(in);
			incomingQueue.put(response);

			/* *********
			 *  Start the communication loop, shall end when game is over
			 * *********/
			if (asServer)
				gameAsServer(out, in);
			else
				gameAsClient(out, in);

			Log.w(tag, tag+"exiting");
			in.close();
			out.close();
			remote.close();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Protocol loop for anyone who starts in TURN
	 */
	private void gameAsServer(DataOutputStream out ,DataInputStream in) throws InterruptedException, IOException {
		while ( ! gameover ) {
			actTurn(out, in);
			if ( gameover ) // need a check here in the middle as well 
				return;
			actWait(out, in);
		}
	}
	/*
	 * Protocol loop for anyone who starts in WAIT
	 */
	private void gameAsClient(DataOutputStream out , DataInputStream in) throws InterruptedException, IOException {
		while (! gameover ) {
			actWait(out, in);
			if ( gameover ) // need a check here in the middle as well
				return;
			actTurn(out, in);
		}
	}

	private void actTurn(DataOutputStream out ,	DataInputStream in) throws InterruptedException, IOException {
		sendBombs(out);
		receiveBombs(in);
		readGameState(in);
	}

	private void actWait(DataOutputStream out ,	DataInputStream in) throws InterruptedException, IOException {
		receiveBombs(in);
		sendBombs(out);
		writeGameState(out);
	}

	/*
	 * Attempts to send the number of bombs provided by the client 
	 */
	private void sendBombs(DataOutputStream out) throws InterruptedException, IOException {
		//Read how many bombs to send
		int count;
		AbstractMessage m = outgoingQueue.take();

		if (m.getType() != MessageType.START_BOMBMESSAGE)
			throw new RuntimeException("Expected START_BOMBMESSAGE, got " + m.getType());
		count = ((StartBombMessage) m).number;

		// write start transmission
		m.writeTo(out);

		// write bombs
		for (int i = 0; i < count ; i++) {
			m = outgoingQueue.take();
			if (m.getType() != MessageType.BOMB_MESSAGE)
				throw new RuntimeException("Expected BOMB_MESSAGE " +i+"/"+count+", got " + m.getType());
			m.writeTo(out);			
		}

		out.flush();
	}

	/*
	 * Read a number of bombs from remote
	 */
	private void receiveBombs(DataInputStream in) throws InterruptedException, IOException {
		int count;
		StartBombMessage s = new StartBombMessage();

		//Read start transmission
		s.readFrom(in);
		count = s.number;

		//forward to client
		incomingQueue.put(s);

		//read bombs
		for (int i = 0; i < count; i++) {
			Bomb b = new Bomb(-1, -1);
			b.readFrom(in);
			incomingQueue.put(b);
		}
	}

	/*
	 * Attempt to read a game state message from the remote player
	 */
	private void readGameState(DataInputStream in) throws InterruptedException, IOException{
		EndMessage end = new EndMessage(false);
		end.readFrom(in);		
		incomingQueue.put(end);
		gameover = end.isGameOver;
	}

	/*
	 * Attempt to write a gamestatemessage to the remote player
	 */
	private void writeGameState(DataOutputStream out) throws InterruptedException, IOException {
		AbstractMessage end;
		
		end =  outgoingQueue.take();
		if (end.getType() != MessageType.END_MESSAGE)
			throw new RuntimeException("Expected END_MESSAGE got " + end.getType());

		end.writeTo(out);
		out.flush();
		
		gameover = ((EndMessage) end).isGameOver;
	}
}
