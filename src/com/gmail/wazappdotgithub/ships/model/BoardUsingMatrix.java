package com.gmail.wazappdotgithub.ships.model;

import java.util.Random;

import com.gmail.wazappdotgithub.ships.common.Constants;
/**
 * An IBoard implementation that use a boolean matrix as model.
 * @author Tor Hammar wazapp github at gmail com
 */
public final class BoardUsingMatrix implements IBoard {
	/* internal model where board[xcoordinate][ycoordinate] (or column, row) 
	 * (x,y) as seen on screen portrait mode, x is (horizontal,vertical)
	 */
	private boolean[][] board = new boolean[Constants.DEFAULT_BOARD_SIZE]
	                                        [Constants.DEFAULT_BOARD_SIZE];
	private ShipUsingMatrix[] ships = new ShipUsingMatrix[Constants.DEFAULT_SHIPS_NUM];
	private boolean isFinal = false;
	
	/**
	 * Creates a ShipBoard with the predefined number of ships and respective size in Constants
	 */
	public BoardUsingMatrix() {
		for (int i = 0; i < Constants.DEFAULT_SHIPS_NUM; i++)
			ships[i] = ShipUsingMatrix.newInstance(Constants.DEFAULT_SHIPS[i], i);
	}

	// just check the incoming coordinates
	private boolean coordinatesOk(int x, int y) {
		if ( x >= 0 && x < Constants.DEFAULT_BOARD_SIZE) {
			if ( y >= 0 && y < Constants.DEFAULT_BOARD_SIZE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void finalise() {
		for ( ShipUsingMatrix s : ships )
			s.writeShipTo(board);

		isFinal = true;
	}

	@Override
	public boolean moveShip(int id, int xcoordinate, int ycoordinate, boolean horizontal) {
		if ( id > -1 ) {
			if ( moveOK(id,xcoordinate,ycoordinate,horizontal) ) {
				ships[id].moveTo(xcoordinate, ycoordinate, horizontal);
				return true;
			}
		}
		return false;
	}
	
	@Override 
	public boolean moveShip(int id, int xcord, int ycord) {
		if ( id > -1  && id < Constants.DEFAULT_SHIPS_NUM)
			return moveShip(id,xcord,ycord,ships[id].horizontal);
		else
			return false;
	}

	@Override
	public int getShipId(int xcoordinate, int ycoordinate) {
		if ( ! coordinatesOk(xcoordinate, ycoordinate) )
			return -1;

		
		for (int i = 0; i < Constants.DEFAULT_SHIPS_NUM; i++) {
			if ( ships[i].ship[xcoordinate][ycoordinate] )
				return i;
		}

		return -1;
	}

	private boolean moveOK(int id, int xcoordinate, int ycoordinate, boolean horizontal) {
		if (isFinal)
			return false;
		if ( ! coordinatesOk(xcoordinate, ycoordinate))
			return false;
		if ( id < 0 ||  ! (id < Constants.DEFAULT_SHIPS_NUM))
			return false;
		if (xcoordinate == ships[id].xcolposition && ycoordinate == ships[id].yrowposition)
			return false;
		
		//deep check
		if ( horizontal ) {     
			if ( ! (xcoordinate + ships[id].size <= Constants.DEFAULT_BOARD_SIZE) )
				return false;
		} else { 
			if ( ! (ycoordinate + ships[id].size <= Constants.DEFAULT_BOARD_SIZE) )
				return false;
		}
		/*
		 * Need to check positions of all other ships against the suggested position.
		 */
		for( ShipUsingMatrix othership : ships ) {
			if ( othership != ships[id] ) { // exclude the ship itself
				if ( horizontal ) {
					if ( othership.yrowposition == ycoordinate ) // only if the ship is in the target row
						for (int i = 0; i < ships[id].size ; i++) {
							if ( othership.ship[xcoordinate + i][ycoordinate] == true )	// check horizontal positions
								return false;
						}
				}
				else {
					if ( othership.xcolposition == xcoordinate ) // only if the ship is in the target column
						for (int i = 0; i < ships[id].size ; i++) { // check vertical positions
							if ( othership.ship[xcoordinate][ycoordinate + i] == true )
								return false;
						}
				}
			}
		}

		return true;
	}

	@Override
	public boolean toggleOrientation(int id) {
		if ( id > -1 ) {
			ShipUsingMatrix s = ships[id];
			s.toggleOrientation();
			if ( moveOK(id,s.xcolposition,s.yrowposition,s.horizontal) ) {
				return true;
			} else
				s.toggleOrientation();
		}

		return false;
	}

	
	@Override
	public boolean hasShip(int xcord, int ycord) {
		if ( coordinatesOk(xcord, ycord) ) {
			if ( isFinal )
				return board[xcord][ycord];
			else
				return getShipId(xcord, ycord) > -1;
		} else
			return false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Board :\n");

		for (int row = 0; row < Constants.DEFAULT_BOARD_SIZE; row++) {
			for (int col = 0; col < Constants.DEFAULT_BOARD_SIZE; col++) {
				sb.append("[" + (hasShip(col, row) == true ? "X" : " ") +"]");
			}
			sb.append("\n");
		}
		return sb.toString();
	}



	@Override
	public boolean isFinalised() {
		return isFinal;
	}

	@Override
	public void randomiseShipsLocations() {
		// randomize ship rotation and position 
		if ( ! isFinal ) {
			Random r = new Random(System.currentTimeMillis());
			int dsn = Constants.DEFAULT_SHIPS_NUM;
			int dbs = Constants.DEFAULT_BOARD_SIZE;
			/*
			 * Cannot use moveOK, since it will access null in ships[]
			 * solutions ?
			 * modify this class to deal with null elements (some methods need to change)
			 * let all the elements be the same ship (!!)
			 * do not use moveOk, rewrite much of that code
			 * enable empty ships instead of nullvalue
			 * let ships grow dynamically, adding new ships to the last position in the array
			 *			
			 * start with the largest ship and fill the array with it, same position
			 */
			
			for (int i = dsn - 1; i > -1; i--) {
				// Start with the largest ship, and create a new instance
				ships[i] = ShipUsingMatrix.newInstance(Constants.DEFAULT_SHIPS[i], i);
				
				if ( i == dsn - 1 ) // only once, ensure no null's
					cloneLayerToAllPositions(i);
	
				//the random code
				boolean horizontal = r.nextInt(dsn) > ( dsn / 2 );
				if ( horizontal )
					while( ! moveShip(i, r.nextInt(dbs - ships[i].size), r.nextInt(dbs), horizontal) )
						;
				else
					while( ! moveShip(i, r.nextInt(dbs), r.nextInt(dbs - ships[i].size), horizontal) )
						;
				
				//need to ensure we write the first entry to all positions in the array after random accept
				if ( i == dsn - 1 ) // only once, ensure no null's
					cloneLayerToAllPositions(i);
			}
		}
	}
	
	private void cloneLayerToAllPositions(int layer) {
			for (int a = 0 ; a < Constants.DEFAULT_SHIPS_NUM ; a++ )
				ships[a] = ships[layer];
	}
	
}

