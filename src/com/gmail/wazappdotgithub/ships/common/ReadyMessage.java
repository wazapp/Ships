package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReadyMessage extends Message {
	
	private static final MessageType type = MessageType.READY_MESSAGE;

	public String nickname;
	public boolean starting;
	
	public ReadyMessage() {
		
	}
		
	public ReadyMessage(String nick, boolean start) {
		this.nickname = nick;
		this.starting = start;
	}

	@Override
	public MessageType getType() {
		return type;
	}

	@Override
	public void writeTo(DataOutputStream out) throws IOException {
		super.writeTo(out);
		out.writeUTF(nickname);
		out.writeBoolean(starting);
	}

	@Override
	public void readFrom(DataInputStream in) throws IOException {
		super.readFrom(in);
		nickname = in.readUTF();
		starting = in.readBoolean();
	}
}
