package com.gmail.wazappdotgithub.ships.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Client.ComputerClient;

public final class TCPComm {

	/* Static fields */
	private static final String tag = "TCPComm ";
	
	/* instance fields */
	protected DataOutputStream out = null;
	protected DataInputStream in = null;
	protected ServerSocket listen = null;
	protected Socket remote = null;
	
	/**
	 * Use to start a ServerSocket and listen for a connection.
	 * @param computerOpponent if to start a Computer opponent
	 * @param port which port to establish the ServerSocket
	 * @throws IOException
	 */
	protected TCPComm(boolean computerOpponent, int port) throws IOException, InterruptedIOException {
		//Create socket
		listen = new ServerSocket(port);
		remote = null;
		
		//Set a timeout
		//listen.setSoTimeout(Constants.DEFAULT_SOCKET_TIMEOUT_MS);
		
		if ( computerOpponent ) {
			Log.d(tag, tag + "Launching Computer Opponent");
			Thread computer = new Thread( ComputerClient.newInstance(InetAddress.getLocalHost(),Constants.DEFAULT_PORT) );
			computer.start();
		}

		Log.d(tag, tag + "initiating.. listening");
		remote = listen.accept();
		Log.d(tag, tag + "initiating.. connection accepted");

		out = new DataOutputStream(remote.getOutputStream());
		in = new DataInputStream(remote.getInputStream());
		Log.d(tag, tag + "initiating.. streams established");
		
	}
	/**
	 * Use to connect to a remote host 
	 * @param addr the address of the remote host
	 * @param port the port to connect to
	 * @throws IOException
	 */
	protected TCPComm(InetAddress addr, int port) throws IOException {
		remote = new Socket();
		remote.setSoTimeout(Constants.DEFAULT_SOCKET_TIMEOUT_MS);
		
		Log.d(tag, tag + "initiating.. connecting");
		remote = new Socket(addr, port);
		Log.d(tag, tag + "initiating.. connect ok");
		
		out = new DataOutputStream(remote.getOutputStream());
		in = new DataInputStream(remote.getInputStream());
		
		Log.d(tag, tag + "initiating.. streams established");
	}
	
	/**
	 * Attempt to close all connections and nullify the instance fields
	 * @throws IOException
	 */
	protected void stop() throws IOException {
		Log.d(tag, tag + "stopping services");
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
