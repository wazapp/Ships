package com.gmail.wazappdotgithub.ships.model.Client;

import com.gmail.wazappdotgithub.ships.model.Game;

/**
 * Default instance of AClient, currently works as the "normal" player
 * @author tor
 *
 */
public final class LocalClient extends AClient {
	private static LocalClient instance = null;
	
	public static enum opponentType {
		LOCALCOMPUTER,
		LOCALPERSON,
		REMOTECOMPUTER,
		REMOTEPERSON
	}
	
	public static IShipsClient newInstance(opponentType opponent) {
		instance = new LocalClient();
		switch (opponent) {
			case LOCALCOMPUTER : Game.startLocalOpponentInstance(instance); break;
			default : throw new IllegalArgumentException("This opponent type is not supported by client");
		}
		
		return instance;
	}
	public static IShipsClient getInstance() throws IllegalStateException {
		if ( instance == null )
			throw new IllegalStateException("The Client is not initiated, create a new instance first");
		
		return instance;
	}
	
	private LocalClient() {
	}	
}
