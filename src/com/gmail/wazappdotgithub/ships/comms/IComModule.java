package com.gmail.wazappdotgithub.ships.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IComModule {

	public static enum opponentType {
		SERVE_COMPUTER,
		SERVE_TCPIP,
		CONNECT_TCPIP,
		SERVE_BLUE,
		CONNECT_BLUE
	}
	
	public DataInputStream getIn();
	public DataOutputStream getOut();
	public void stop() throws IOException;
	
}
