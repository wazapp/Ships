package com.gmail.wazappdotgithub.ships.model;

import com.gmail.wazappdotgithub.ships.common.Constants;

final class ShipUsingMatrix {
	// coordinate system ship[x][y] where x is column, or horizontal and y is row or vertical
	protected boolean ship[][];
	protected int xcolposition;
	protected int yrowposition;
	protected int size;
	protected boolean horizontal;

	private ShipUsingMatrix() {
		;
	}

	private ShipUsingMatrix(int size, int pos) {
		this.xcolposition = pos;
		this.yrowposition = 0;
		this.size = size;
		this.horizontal = false;
		
		ship = new boolean[Constants.DEFAULT_BOARD_SIZE][Constants.DEFAULT_BOARD_SIZE];
		fillRepresentation(pos, 0, this.horizontal);
	}

	protected static ShipUsingMatrix newInstance(int size, int pos) {
		ShipUsingMatrix s = new ShipUsingMatrix(size, pos);
		return s;
	}

	protected void writeShipTo(boolean[][] board) {
		if ( this.horizontal ) { // change in xcolpos
			for (int i = 0 ; i < size; i++)
				board[xcolposition + i][yrowposition] = true;
		} else {
			for (int i = 0 ; i < size; i++)
				board[xcolposition][yrowposition + i] = true;
		}
	}

	/**
	 * will place the ships head at the new position and rotation 
	 * @param xcoordinate
	 * @param ycoordinate
	 * @param horizontal
	 * @return
	 */
	protected boolean moveTo(int xcoordinate, int ycoordinate, boolean horizontal) {
		if ( xcoordinate < 0 || ycoordinate < 0 )
			throw new IllegalArgumentException("Argument below 0");

		if ( horizontal ) {     
			if ( !(ycoordinate  < Constants.DEFAULT_BOARD_SIZE && xcoordinate + size <= Constants.DEFAULT_BOARD_SIZE) )
				throw new IllegalArgumentException("Argument too large, horisontal " + ycoordinate + ", " + (xcoordinate + size));
		} else { 
			if ( !(ycoordinate + size <= Constants.DEFAULT_BOARD_SIZE && xcoordinate < Constants.DEFAULT_BOARD_SIZE) )
				throw new IllegalArgumentException("Argument too large, vertical " + (ycoordinate + size) + ", " + xcoordinate);
		}

		//removing the current representation
		clearRepresentation();

		//adding the new representation
		fillRepresentation(xcoordinate, ycoordinate, horizontal);

		this.horizontal = horizontal;
		this.yrowposition = ycoordinate;
		this.xcolposition = xcoordinate;

		return true;
	}

	protected void fillRepresentation(int xcoordinate, int ycoordinate, boolean horizontal) {
		if ( horizontal ) {
			for ( int i = 0; i < this.size; i++)
				ship[xcoordinate + i][ycoordinate] = true;
		} else {
			for ( int i = 0; i < this.size; i++)
				ship[xcoordinate][ycoordinate + i] = true;
		}
	}
	protected void clearRepresentation() {
		if ( this.horizontal ) {
			for ( int i = 0; i < this.size; i++)
				ship[this.xcolposition + i][this.yrowposition] = false;
		} else {
			for ( int i = 0; i < this.size; i++)
				ship[this.xcolposition][this.yrowposition + i] = false;
		}
		
		//TODO some checks to get any bugs out
		for (int i = 0; i < Constants.DEFAULT_BOARD_SIZE; i++)
			for (int j = 0; j < Constants.DEFAULT_BOARD_SIZE; j++)
				if ( ship[j][i] )
					throw new IllegalStateException("The ship did not clear itself");
		
	}

	/**
	 * Will toggle ship between horisontal and vertical, in its current position
	 * @param xcoordinate
	 * @param ycoordinate
	 */
	protected void toggleOrientation() {
		clearRepresentation();

		if ( horizontal ) {
			fillRepresentation(xcolposition,yrowposition , false);

		} else {
			fillRepresentation(xcolposition, yrowposition, true);
		}

		horizontal = !horizontal;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ShipUsingMatrix : " + size + " (" + xcolposition + ", " + yrowposition + ", " +horizontal+")\n");

		for (int row = 0; row < Constants.DEFAULT_BOARD_SIZE; row++) {
			for (int col = 0; col < Constants.DEFAULT_BOARD_SIZE; col++) {
				sb.append("[" + (ship[col][row] == true ? "X" : " ") +"]");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}