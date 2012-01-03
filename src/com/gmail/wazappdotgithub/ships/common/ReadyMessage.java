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
	protected void writeSpecial(DataOutputStream out) throws IOException {		
		out.writeUTF(nickname);
		out.writeBoolean(starting);
	}

	@Override
	protected void readSpecial(DataInputStream in) throws IOException {
		nickname = in.readUTF();
		starting = in.readBoolean();
	}
	
	@Override
	public String toString() {
		return nickname + " is " + ((starting) ? "starting" : "waiting");
	}
}
