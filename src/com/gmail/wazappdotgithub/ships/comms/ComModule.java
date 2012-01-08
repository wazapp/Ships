package com.gmail.wazappdotgithub.ships.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;

public class ComModule implements IComModule {

	/* static fields */
	private static final String tag = "Ships Comm ";
	private static ComModule instance;
	
	public static IComModule connect_to_tcp(Inet4Address addr, int port) throws IOException {
		nullCheck();
		
		instance = new ComModule();
		instance.network = new TCPComm(addr, port);

		return instance;
	}
	
	public static IComModule serve_from_tcp(int port) throws IOException {
		nullCheck();
		
		instance = new ComModule();
		instance.network = new TCPComm(false, port );
		
		return instance;
	}
	
	public static IComModule serve_computer(int port) throws IOException {
		nullCheck();
		
		instance = new ComModule();
		instance.network = new TCPComm(true, port );
		
		return instance;
	}
	
	public static IComModule getInstance() {
		if  ( instance == null )
			throw new RuntimeException("Calling getInstance, but instance is null");
		
		return instance;
	}
	
	private static void nullCheck() throws IOException {
		if ( instance != null)
			instance.stop();
	}
	
	/* Instance fields */
	//TODO add Bluetooth support
	private TCPComm network;
	
	private ComModule() {

	}

	@Override
	public DataInputStream getIn() {
		return network.in;
	}

	@Override
	public DataOutputStream getOut() {
		return network.out;
	}

	@Override
	public void stop() throws IOException {
		network.stop();
	}

}
