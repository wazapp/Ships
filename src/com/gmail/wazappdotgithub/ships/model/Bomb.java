package com.gmail.wazappdotgithub.ships.model;


/*
 * Used to transfer information
 */
public final class Bomb {

	public int x;
	public int y;
	public boolean hit;
	public boolean destrship;
	
	public Bomb(int xcoord, int ycoord) {
		x = xcoord; y = ycoord; hit = false; destrship = false;
	}
	public void setHit(boolean washit) {
		hit = washit;
	}
	public boolean getHit() {
		return hit;
	}
	public boolean getDestrShip() {
		return destrship;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o.getClass() == this.getClass() ) {
			Bomb b = (Bomb) o;
			return b.x == this.x && b.y == this.y;
		}
		return false;	
	}
	
	
}
