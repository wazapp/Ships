package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EndMessage extends Message {

	private static final MessageType type = MessageType.END_MESSAGE;
	public boolean isGameOver = false;
	
	public EndMessage(boolean isGameOver) {
		this.isGameOver = isGameOver;
	}
	
	@Override
	public MessageType getType() {
		return type;
	}

	@Override
	protected void writeSpecial(DataOutputStream out) throws IOException {
		out.writeBoolean(isGameOver);
	}

	@Override
	protected void readSpecial(DataInputStream in) throws IOException {
		isGameOver = in.readBoolean();
	}
	
	@Override
	public String toString() {
		return (isGameOver) ? "gameover" : "gameon";
	}
}
