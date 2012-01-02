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
	public void writeTo(DataOutputStream out) throws IOException {		
		out.writeInt(type.ordinal());
		out.writeInt(x);
		out.writeInt(y);
		out.writeBoolean(hit);
		out.writeBoolean(destrship);
	}

	@Override
	public void readFrom(DataInputStream in) throws IOException {
		int ordinal = in.readInt();
		if ( ordinal != type.ordinal() )
			throw new RuntimeException("Error in protocol, did not read expected ordinal");
		
		x = in.readInt();
		y = in.readInt();
		hit = in.readBoolean();
		destrship = in.readBoolean();
	}
}
