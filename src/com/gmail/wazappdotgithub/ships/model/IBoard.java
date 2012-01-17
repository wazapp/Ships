package com.gmail.wazappdotgithub.ships.model;

/**
 * An interface for a board implementation.
 *  Any xcord or ycord refers to board relative coordinates, 0 - 9.
 *  xcord refers to the horizontal axis (columns) and y the vertical axis (rows)
 *  
 *  Any class implementing the interface must quietly ignore any Illegal Arguments etc.
 *  
 * @author Tor Hammar < wazapp.github@gmail.com >
 *
 */

public interface IBoard {
	
	/**
	 * Returns a positive integer ( 0 inclusive ) that will 
	 * identify the Ship on this board. If no ship was found 
	 * on the specified location the method will return a 
	 * negative integer.
	 * @param xcord the row of the board
	 * @param ycord the column of the board
	 * @return an integer that represents the ship on this board, 
	 *         otherwise a negative integer
	 */
	int getShipId(int xcord, int ycord);

	/**
	 * Move a ship from it's current location to the provided
	 * location with the specified orientation. The location
	 * specifies the head of the Ship. Returns true if a move
	 * was performed. Will return false if the id was not correct
	 * or if the move was never performed.
	 * @param id the id of the ship on this board.
	 * @param xcord the row of the location
	 * @param ycord the column of the location
	 * @param horizontal true for horizontal orientation
	 * @return true if the move was performed, otherwise false
	 */
	boolean moveShip(int id, int xcord, int ycord, boolean horizontal);
	boolean moveShip(int id, int xcord, int ycord);
	boolean moveShipRelative(int id, int x, int y );
	
	/**
	 * Return true if any part of a ship is present at the 
	 * specified location
	 * @param xcord
	 * @param ycord
	 * @return true if any part of a ship is present at the 
	 * location
	 */
	boolean hasShip(int xcord, int ycord);
	
	/**
	 * Place a bomb on the specified location, make damage to any 
	 * ship on that location and returns a new Bomb with the updated information
	 * @param xcord
	 * @param ycord
	 * @return an updated Bomb
	 */
	Bomb bombCoordinate(Bomb bomb) throws IllegalArgumentException; 
	
	/**
	 * Randomise the ship's locations and orientations
	 */
	void randomiseShipsLocations();
	
	/**
	 * Returns an array of IShip
	 * @return an array of the ships in the model
	 */
	IShip[] arrayOfShips();
	
	/**
	 * prevents further changes to the board.
	 */
	void finalise();
	
	boolean isFinalised	();

	/**
	 * Toggle a ship's orientation between horizontal and vertical
	 * @param id the id of the ship to rotate
	 * @return true if the orientation change was valid and performed
	 */
	boolean toggleOrientation(int id);
	
	/**
	 * return the number of live ships on this board
	 * @return the number of live ships on this board
	 */
	int numLiveShips();
	
}
