/**
 * @author Tor Hammar, wazapp dot github at gmail dot com
 */

package com.gmail.wazappdotgithub.ships.model;


public interface IShip {
	int getXposition();
	int getYposition();
	int getSize();
	int getHits();
	boolean isHorizontal();
	/**
	 * check if the ship is still alive
	 * @return true if still alive
	 */
	boolean isAlive();
	/**
	 * Make damage to the ship and return if the ship
	 * is still alive
	 * @return if the ship is still alive
	 */
	boolean makeDamage();
	
	
}
