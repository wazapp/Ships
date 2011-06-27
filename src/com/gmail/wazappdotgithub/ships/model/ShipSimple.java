package com.gmail.wazappdotgithub.ships.model;

public final class ShipSimple implements IShip {

	protected int xcolpos, yrowpos, size;
	protected boolean ishorizontal;
	
	public ShipSimple(int xcoord, int ycoord, int size, boolean ishorizontal) {
		this.xcolpos = xcoord;
		this.yrowpos = ycoord;
		this.size = size;
		this.ishorizontal = ishorizontal;
	}
	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getXposition() {
		return xcolpos;
	}

	@Override
	public int getYPosition() {
		return yrowpos;
	}

	@Override
	public boolean isHorizontal() {
		return ishorizontal;
	}

}
