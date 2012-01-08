package com.gmail.wazappdotgithub.ships.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;

public final class TCPComm {

	/* Static fields */
	private static final String tag = "TCPComm ";
	
	/* instance fields */
	protected DataOutputStream out = null;
	protected DataInputStream in = null;
	protected ServerSocket listen = null;
	protected Socket remote = null;
	
	protected TCPComm(boolean computerOpponent, int port) throws IOException {
		listen = new ServerSocket(port);
		remote = null;
		
		if ( computerOpponent ) {
			Log.d(tag, tag + "Launching Computer Opponent");
			Thread computer = new Thread( ComputerClient.newInstance() );
			computer.start();
		}
		Log.d(tag, tag + "initiating.. listening");
		remote = listen.accept();
		Log.d(tag, tag + "initiating.. connection accepted");
		
		out = new DataOutputStream(remote.getOutputStream());
		in = new DataInputStream(remote.getInputStream());
	}

	protected TCPComm(Inet4Address addr, int port) throws IOException {
		Log.d(tag, tag + "initiating.. connecting");
		remote = new Socket(addr, port);
		Log.d(tag, tag + "initiating.. connect ok");
		
		out = new DataOutputStream(remote.getOutputStream());
		in = new DataInputStream(remote.getInputStream());
	}
	
	protected void stop() throws IOException {
		if ( out != null )
			out.close();
		if ( in != null )
			in.close();
		if (listen != null)
			listen.close();
		if ( remote != null)
			remote.close();
		
		out = null;
		in = null;
		listen = null;
		remote = null;
	}
}
