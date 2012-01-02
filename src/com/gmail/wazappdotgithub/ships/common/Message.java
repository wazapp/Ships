package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Message {
	
	public enum MessageType {
		READY_MESSAGE, 
		START_BOMBMESSAGE, BOMB_MESSAGE, 
		END_MESSAGE
	}

	public abstract MessageType getType();
	
	public void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(getType().ordinal());
	}
	
	public void readFrom(DataInputStream in) throws IOException {
		int ordinal = in.readInt();
		if ( ordinal != getType().ordinal() )
			throw new RuntimeException("Error in protocol, did not read expected ordinal");
	}
}
