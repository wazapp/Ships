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
	
	public void writeTo(DataOutputStream out) throws IOException {
		super.writeTo(out);
		out.writeInt(number);
	}
	
	public void readFrom(DataInputStream in) throws IOException {
		super.readFrom(in);
		number = in.readInt();
				
	}

}
