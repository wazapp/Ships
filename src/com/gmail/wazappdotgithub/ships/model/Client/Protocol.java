package com.gmail.wazappdotgithub.ships.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.gmail.wazappdotgithub.ships.common.AbstractMessage;
import com.gmail.wazappdotgithub.ships.common.EndMessage;
import com.gmail.wazappdotgithub.ships.common.ReadyMessage;
import com.gmail.wazappdotgithub.ships.common.StartBombMessage;
import com.gmail.wazappdotgithub.ships.model.Bomb;

/*
 * The protocol's purpose is to manage the network communication
 * on behalf of the Clients.
 * 
 * Not sure about the structure of this yet.
 */
public final class Protocol {
	
	/*
	 * Attempt to write a ReadyMessage to the remote player
	 */
	protected static void writeReady(String name, boolean willstart, DataOutputStream out) throws IOException {
		ReadyMessage rm = new ReadyMessage(name, willstart);
		rm.writeTo(out);
		out.flush();
	}
	
	/*
	 * Attempt to read a ReadyMessage from a remote player
	 */
	protected static ReadyMessage readReady(DataInputStream in) throws IOException {
		ReadyMessage rm = new ReadyMessage();
		rm.readFrom(in);
		return rm;
	}
	
	/*
	 * Attempts to send the number of bombs provided by the client 
	 */
	protected static void writeBombs(List<Bomb> outgoing, DataOutputStream out) throws IOException {
		//Read how many bombs to send
		StartBombMessage sbm = new StartBombMessage();
		sbm.number = outgoing.size();
		// write start transmission
		sbm.writeTo(out);

		// write bombs	 
		for (Bomb b : outgoing)
			b.writeTo(out);

		out.flush();
	}

	/*
	 * Read a number of bombs from remote
	 */
	protected static List<Bomb> readBombs(DataInputStream in) throws IOException {
		StartBombMessage sbm = new StartBombMessage();
		//Read start transmission
		sbm.readFrom(in);

		//read bombs
		List<Bomb> read = new LinkedList<Bomb>();
		for (int i = 0; i < sbm.number; i++) {
			Bomb b = new Bomb(-1, -1);
			b.readFrom(in);
			read.add(b);
		}
		
		return read;
	}
	
	/*
	 * Attempt to read a game state message from the remote player
	 */
	protected static EndMessage readGameState(DataInputStream in) throws IOException {
		EndMessage end = new EndMessage(false);
		end.readFrom(in);		
		return end;
	}

	/*
	 * Attempt to write a gamestatemessage to the remote player
	 */
	protected static void writeGameState(boolean gameover, DataOutputStream out) throws IOException {
		AbstractMessage end = new EndMessage(gameover);
		end.writeTo(out);
		out.flush();
	}
}
