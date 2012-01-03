package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.Message.MessageType;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;

public class Protocol implements Runnable {
	
	/* Static fields */
	public static final String tag = "Ships Protocol ";
	public static Runnable instance;
	public static final int listen_port = 48152;
	public static ServerSocket listenTo;
	public static Socket connect;
	
    /* instance fields */
    private boolean asServer = true;
    private boolean launchComputer = false;
    private boolean gameon = true;
    
    /*
     * These need to be fair because we are retrieving messages in a specific order
     * The longest expected stream is Start, 10 bombs, End
     */
	private final BlockingQueue<Message> outgoingQueue = new ArrayBlockingQueue<Message>(12, true);
	private final BlockingQueue<Message> incomingQueue = new ArrayBlockingQueue<Message>(12, true);
	
	
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
		listenTo = new ServerSocket(listen_port);
		
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
	public void send(Message m) throws InterruptedException {
		outgoingQueue.put(m);
	}
	
	/*
	 * Schedule a retrieval of a message from the remote client
	 */
	public Message retrieve() throws InterruptedException {
		return incomingQueue.take();
	}
	
	@Override
	public void run() {
		
		Thread.currentThread().setName(tag);
		DataOutputStream out = null;
		DataInputStream in = null;
		Socket remote = null;
		
		try {
			if (asServer) {
				//Start the computerclient if required
				if ( launchComputer ) {
					Log.d(tag, tag + "Launching Computer Opponent");
					Thread computer = new Thread(ComputerClient.newInstance());
					computer.start();
				}
				
				remote = listenTo.accept();
				Log.d(tag, tag + "accepted Connection");
			} else {
				remote = connect;
			}

			out = new DataOutputStream(remote.getOutputStream());
			in = new DataInputStream(remote.getInputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Have accepted remote connection, now starting the protocol */
		/* *********
		 *  Ensure both clients are ready
		 * *********/
		try {
			
			//Write the ready message to the remote player
			ReadyMessage rm = (ReadyMessage) outgoingQueue.take();
			rm.writeTo(out);
			out.flush();
			
			// expect a ready message from the remote player
			ReadyMessage response = new ReadyMessage();
			response.readFrom(in);
			incomingQueue.put(response);
			
			// both clients have now reported ready
			
			if (asServer)
				gameAsServer(out, in);
			else
				gameAsClient(out, in);
			
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
		while (gameon) {
			actTurn(out, in);
			if (! gameon ) // need a check here in the middle as well 
				break;
			actWait(out, in);
		}
	}
	/*
	 * Protocol loop for anyone who starts in WAIT
	 */
	private void gameAsClient(DataOutputStream out , DataInputStream in) throws InterruptedException, IOException {
		while (gameon) {
			actWait(out, in);
			if (! gameon ) // need a check here in the middle as well
				break;
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
		Message m = outgoingQueue.take();
		
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
	}
	
	/*
	 * Attempt to write a gamestatemessage to the remote player
	 */
	private void writeGameState(DataOutputStream out) throws InterruptedException, IOException {
		Message end;

		end = outgoingQueue.take();
		if (end.getType() != MessageType.END_MESSAGE)
			throw new RuntimeException("Expected END_MESSAGE got " + end.getType());

		end.writeTo(out);
		out.flush();
	}
}
