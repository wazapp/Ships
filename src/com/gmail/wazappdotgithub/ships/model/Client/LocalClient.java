package com.gmail.wazappdotgithub.ships.model.Client;

/**
 * Default instance of AClient, currently works as the "normal" player
 * @author tor
 *
 */
public final class LocalClient extends AClient {
	private static LocalClient instance = null;
	
	public static IShipsClient newInstance() {
		instance = new LocalClient();
		
		return instance;
	}
	public static IShipsClient getInstance() {
		if ( instance == null )
			LocalClient.newInstance();
		
		return instance;
	}
	
	private LocalClient() {
	}	
}
