package com.gmail.wazappdotgithub.ships.model;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;

public interface IGame {

	void clientReportReadyForGame(IShipsClient client, IBoard board);
	void clientReportFinishedBombing(IShipsClient client);
	Bomb dropBomb(IShipsClient Shootingclient, Bomb b);
	
	List<Bomb> getOpponentsShots();
	List<Bomb> getOpponentsLatestShots();
}
