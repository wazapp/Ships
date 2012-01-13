package com.gmail.wazappdotgithub.ships.model;

import java.util.Random;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.common.Constants;

/*
 * I figured we need access to the Ship's size etc. to enable a little more
 * agile paint method and so..  This version of the model is a little less 'mechanic'
 * 
 * We will still store a representation of the board in memory because it will speed up
 * some access methods, the largest difference being the class for the Ship itself.
 * 
 */
public final class Board implements IBoard {

	private int[][] board;
	private int empty = -1;
	private ShipSimple[] ships;
	private boolean isFinal = false;
	private int liveships;
	private String tag = "Ships_Board";

	public Board() {
		board = new int[Constants.DEFAULT_BOARD_SIZE][Constants.DEFAULT_BOARD_SIZE];
		ships = new ShipSimple[Constants.DEFAULT_SHIPS_NUM];
		liveships = Constants.DEFAULT_SHIPS_NUM;

		//add new ships
		clearboard();
		//THIS WILL BREAK if ever the number of ships != board size
		for (int i = 0; i < Constants.DEFAULT_SHIPS_NUM; i++) {
			ships[i] = new ShipSimple(i, 0, Constants.DEFAULT_SHIPS[i], false);
			add(i);
		}

		if ( ! validate() )
			throw new IllegalStateException("the board is not valid during construction()");
	}

	private void clearboard() {
		for (int i = 0; i < Constants.DEFAULT_SHIPS_NUM; i++) {
			for (int j = 0; j < Constants.DEFAULT_SHIPS_NUM; j++) {
				board[i][j] = empty;
			}
		}
	}

	@Override
	public IShip[] arrayOfShips() {
		return ships;
	}

	@Override
	public void finalise() {
		if ( ! validate() )
			throw new IllegalStateException("The board is not valid during finalise()");

		isFinal = true;
	}

	@Override
	public int getShipId(int xcord, int ycord) {
		if ( hasShip(xcord, ycord) ) {
			for (int id = 0; id < Constants.DEFAULT_SHIPS_NUM; id++) {
				if ( ships[id].ishorizontal && ships[id].yrowpos == ycord) {
					if ( xcord >= ships[id].xcolpos 
							&& xcord < ships[id].xcolpos + ships[id].size ) {
						return id;
					}

				} else {
					if ( ships[id].xcolpos == xcord ) {
						if ( ycord >= ships[id].yrowpos 
								&& ycord < ships[id].yrowpos + ships[id].size ) {
							return id;
						}
					}
				}
			}
		}

		return -1;
	}

	@Override
	public boolean hasShip(int xcoord, int ycoord) {
		if ( ! coordinatesOk(xcoord, ycoord))
			return false;

		return board[xcoord][ycoord] >= 0;
	}

	@Override 
	public Bomb bombCoordinate(Bomb b) throws IllegalArgumentException {
		if ( ! coordinatesOk(b.x, b.y) )
			throw new IllegalArgumentException("Invalid coordinates");

		int s;
		if ( (s = getShipId(b.x, b.y) ) > -1 ) { // if there is a ship
			b.setHit(true);

			if ( ! ships[s].makeDamage() ) { // make damage and check if alive
				liveships--;
				b.destrship = true;
			}
		}
		return b;
	}

	@Override
	public boolean isFinalised() {
		return isFinal;
	}

	@Override
	public boolean moveShip(int id, int xcoord, int ycoord, boolean horizontal) {
		if ( ! coordinatesOk(xcoord, ycoord))
			return false;
		if ( id < 0 ||  ! (id < Constants.DEFAULT_SHIPS_NUM))
			return false;


		remove(id);
		boolean update = moveOK(id, xcoord, ycoord, horizontal, false);	

		if ( update ) {		
			//update ship data
			ships[id].xcolpos = xcoord;
			ships[id].yrowpos = ycoord;
			ships[id].ishorizontal = horizontal;			
		}

		add(id);
		return update;
	}

	@Override
	public boolean moveShip(int id, int xcord, int ycord) {
		if ( id < 0 ||  ! (id < Constants.DEFAULT_SHIPS_NUM))
			return false;

		return moveShip(id, xcord, ycord, ships[id].ishorizontal);
	}

	@Override
	public void randomiseShipsLocations() {
		// randomize ship rotation and position 
		if ( ! isFinal ) {
			Random r = new Random(System.currentTimeMillis());
			int dsn = Constants.DEFAULT_SHIPS_NUM;
			int dbs = Constants.DEFAULT_BOARD_SIZE;

			board = new int[dbs][dbs];
			clearboard();
			ships = new ShipSimple[Constants.DEFAULT_SHIPS_NUM];

			if ( ! validate() )
				throw new IllegalStateException("the board is not valid in empty state");

			for (int i = dsn - 1; i > -1; i--) {
				// Start with the largest ship
				ships[i] = new ShipSimple(0, 0, Constants.DEFAULT_SHIPS[i], true);

				//the randomization code
				boolean horizontal = r.nextInt(dsn) > ( dsn / 2 );
				int xcoord, ycoord;


				// must not use moveShip() at this point since this ship only exists in ships[]
				// and moveShip interacts with board[][] 
				if ( horizontal ) {
					while( ! moveOK(i, xcoord = r.nextInt(dbs - ships[i].size), ycoord = r.nextInt(dbs), horizontal,false) )
						;

				} else {
					while( ! moveOK(i, xcoord = r.nextInt(dbs), ycoord = r.nextInt(dbs - ships[i].size), horizontal,false) )
						;
				}

				//update ship data
				ships[i].xcolpos = xcoord;
				ships[i].yrowpos = ycoord;
				ships[i].ishorizontal = horizontal;			
				//add to board
				add(i);

			}
		}
	}

	@Override
	public boolean toggleOrientation(int id) {
		if ( id < 0 ||  ! (id < Constants.DEFAULT_SHIPS_NUM))
			return false;

		remove(id);
		boolean update = moveOK(id, ships[id].xcolpos, ships[id].yrowpos, !ships[id].ishorizontal, true);

		if ( update )
			ships[id].ishorizontal = !ships[id].ishorizontal;

		add(id);
		return update;
	}

	/* 	return true if the suggested move is OK.
	 *	@pre x,y and id are valid and in range
	 *	will check for rotations and moves outside the board or into other ships
	 */
	private boolean moveOK(int id, int xcoordinate, int ycoordinate, boolean horizontal, boolean rotateOrder) {
		if ( isFinal ) {
			ALog.d(tag,"Move disallowed, in final state"); 
			return false;
		}
		
		ShipSimple theship = ships[id];

		if ( ! rotateOrder ) {// only bother to check this if we do not rotate
			if (xcoordinate == theship.xcolpos && ycoordinate == theship.yrowpos) {
				ALog.d(tag,"Move, irrelevant (same position), no rotation");
				return false;
			}
		}

		// deep check
		if ( horizontal ) {
			// note  0 + size 2 = 2 = [0,1,2] = wrong
			// note	 7 + size 2 = 9 = [7,8,9] = wrong
			// head of ship == x,y coordinate = 1
			// ensure it fits inside the board
			if ( ! (xcoordinate + theship.size - 1  < Constants.DEFAULT_BOARD_SIZE) ) {
				ALog.d(tag,"Move disallowed, will not fit (x coordinate)"); 
				return false; 
			}
			//ensure there is nothing at the intended positions
			// (unless it is the ship itself already)
			for (int i = 0; i < theship.size ; i++) {
				int coordinate = board[xcoordinate + i][ycoordinate];
				if (  coordinate != empty && coordinate != id ) {
					ALog.d(tag,"Move disallowed, space is occupied ("+(xcoordinate+1)+","+ycoordinate+"=" +coordinate+")"); 
					return false;
				}
			}
			
		} else { 
			// vertical position, from head coordinate and increasing y value
			// ensure it fits inside the board
			if ( ! (ycoordinate + theship.size - 1 < Constants.DEFAULT_BOARD_SIZE) ) {
				ALog.d(tag,"Move disallowed, will not fit (y coordinate)"); 
				return false; 
			}
			//ensure there is nothing at the intended positions
			// (unless it is the ship itself already)
			for (int i = 0; i < theship.size ; i++) {
				int coordinate = board[xcoordinate][ycoordinate + i];
				if (  coordinate != empty && coordinate != id ) {
					ALog.d(tag,"Move disallowed, space is occupied ("+xcoordinate+","+(ycoordinate + i)+"=" +coordinate+")"); 
					return false;
				}
			}
		}

		//Log.d(tag,tag + "Move allowed, destination ("+xcoordinate+","+(ycoordinate)+") h="+horizontal + " s= "+ ships[id].size);
		return true;
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

	/*
	 * Add a ship to the board
	 */
	private void add(int id) {
		if ( ships[id].ishorizontal ) {
			for ( int i = 0; i < ships[id].size; i++)
				board[ships[id].xcolpos + i][ships[id].yrowpos] = id;
		} else {
			for ( int i = 0; i < ships[id].size; i++)
				board[ships[id].xcolpos][ships[id].yrowpos + i] = id;
		}
	}

	/*
	 * Remove a ship from the board
	 */
	private void remove(int id) {
		if ( ships[id].ishorizontal ) {
			for ( int i = 0; i < ships[id].size; i++)
				board[ships[id].xcolpos + i][ships[id].yrowpos] = empty;
		} else {
			for ( int i = 0; i < ships[id].size; i++)
				board[ships[id].xcolpos][ships[id].yrowpos + i] = empty;
		}
	}

	@Override
	public int numLiveShips() {
		return liveships;
	}

	
	private boolean validate() {
		int count = 0; // existing "ships"
		for (int i = 0; i < Constants.DEFAULT_BOARD_SIZE; i++)
			for (int j = 0; j < Constants.DEFAULT_BOARD_SIZE; j++)
				if (board[i][j] >= 0 ) { count++; }

		
		for (int id = 0; id < Constants.DEFAULT_SHIPS_NUM; id++) {
			ShipSimple sh = ships[id]; 
			if (sh != null) { 
				count -= sh.size; //count this ship
				for ( int s = 0; s < sh.size ; s++ ) { // ensure all coords in it's path is it's own
					boolean ok = ( sh.ishorizontal ) ? (board[sh.xcolpos + s][sh.yrowpos] == id) : (board[sh.xcolpos][sh.yrowpos + s] == id);
					if (! ok ) return false;
				}
			}
		}

		return count == 0;
	}
}
