package com.gmail.wazappdotgithub.ships.model;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;

public interface IGame {

	void clientReportReadyForGame(IShipsClient client, IBoard board);
	Bomb dropBomb(IShipsClient Shootingclient, Bomb b);
	
	void progressState(IShipsClient client);
	
	List<Bomb> getInTurnClientHistoricalBombs();
	List<Bomb> getInTurnClientAcceptedBombs();
}
