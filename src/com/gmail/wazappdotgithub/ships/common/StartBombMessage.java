package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StartBombMessage extends Message {

	public static MessageType type = MessageType.START_BOMBMESSAGE;
	public int number;
	
	@Override
	public MessageType getType() { 
		return type;
	}
	
	protected void writeSpecial(DataOutputStream out) throws IOException {
		out.writeInt(number);
	}
	
	protected void readSpecial(DataInputStream in) throws IOException {
		number = in.readInt();
	}

}
