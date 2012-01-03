package com.gmail.wazappdotgithub.ships.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.gmail.wazappdotgithub.ships.common.Message;

/*
 * Used to transfer information
 */
public final class Bomb extends Message {

	private static final MessageType type = MessageType.BOMB_MESSAGE;
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
	
	@Override
	public MessageType getType() {
		return type;
	}
	
	@Override
	protected void writeSpecial(DataOutputStream out) throws IOException {		
		out.writeInt(x);
		out.writeInt(y);
		out.writeBoolean(hit);
		out.writeBoolean(destrship);
	}

	@Override
	protected void readSpecial(DataInputStream in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		hit = in.readBoolean();
		destrship = in.readBoolean();
	}
	
	@Override
	public String toString() {
		return "["+x+","+y+","+hit+"]";
	}
}
