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
	public void writeTo(DataOutputStream out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(isGameOver);
	}

	@Override
	public void readFrom(DataInputStream in) throws IOException {
		super.readFrom(in);
		isGameOver = in.readBoolean();
	}
}
