package com.gmail.wazappdotgithub.ships.model;

public final class ShipSimple implements IShip {

	protected int xcolpos, yrowpos, size, hits;
	protected boolean ishorizontal;
	
	public ShipSimple(int xcoord, int ycoord, int size, boolean ishorizontal) {
		this.xcolpos = xcoord;
		this.yrowpos = ycoord;
		this.size = size;
		this.ishorizontal = ishorizontal;
		this.hits = 0;
	}
	
	@Override
	public int getSize() {
		return size;
	}
	@Override
	public int getHits() {
		return hits;
	}

	@Override
	public int getXposition() {
		return xcolpos;
	}

	@Override
	public int getYposition() {
		return yrowpos;
	}

	@Override
	public boolean isHorizontal() {
		return ishorizontal;
	}

	@Override
	public boolean makeDamage() {
		hits++;
		return isAlive();
	}
	
	@Override
	public boolean isAlive() {
		return hits < size;
	}

}
