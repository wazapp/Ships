package com.gmail.wazappdotgithub.ships.common;

import java.util.List;

import com.gmail.wazappdotgithub.ships.model.Bomb;

public class Score {

	/*
	 * Updates the individual Bombs with their score, returns the new player score.
	 * This must be done, when the bombs have been updated with hit information
	 * and returned to the player who sent them.
	 */
	public static int scoreme(int oldscore, List<Bomb> mybombs) {
		int worth = 0;
		int localtotal = Constants.SCORELEVEL[0];
		
		for (int i = 0; i < mybombs.size(); i++) {
			Bomb bomb = mybombs.get(i);
			
			if ( bomb.hit ) {
				worth++;
			} else {
				worth = 0;
			}
			
			bomb.score = Constants.SCORELEVEL[worth];
			localtotal = localtotal+bomb.score;
		}
		
		return oldscore + localtotal;
	}
}
