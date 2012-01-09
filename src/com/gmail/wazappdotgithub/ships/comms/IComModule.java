package com.gmail.wazappdotgithub.ships.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*
 * Simple interface for the Communication module.
 * The current module currently only supports TCP connections.
 */
public interface IComModule {

	/**
	 * Returns the DataInputStream for the established connection 
	 * @return null if there is no established connection
	 */
	public DataInputStream getIn();
	
	/**
	 * Returns the DataOutputStream for the established connection
	 * @return null if there is no established connection
	 */
	public DataOutputStream getOut();
	
	/**
	 * Attempt to gracefully close all connections and reset the
	 * connection 
	 * @throws IOException
	 */
	public void stop() throws IOException;
	
}
