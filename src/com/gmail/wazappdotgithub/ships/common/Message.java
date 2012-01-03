package com.gmail.wazappdotgithub.ships.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

public abstract class Message {
	
	public enum MessageType {
		READY_MESSAGE, 
		START_BOMBMESSAGE, BOMB_MESSAGE, 
		END_MESSAGE
	}

	public abstract MessageType getType();
	protected abstract void writeSpecial(DataOutputStream out) throws IOException;
	protected abstract void readSpecial(DataInputStream in) throws IOException;
	
	public void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(getType().ordinal());
		
		writeSpecial(out);
		
		String tag = Thread.currentThread().getName();
		Log.d(tag,tag + " wrote " + getType() + " " + toString());
	}
	
	public void readFrom(DataInputStream in) throws IOException {
		int ordinal = in.readInt();
		if ( ordinal != getType().ordinal() ) {
			MessageType[] vals = MessageType.values();
			throw new RuntimeException("Error in protocol, did not read expected ordinal. Read " + vals[ordinal] + " expected " + getType());
		}
		
		readSpecial(in);
		
		String tag = Thread.currentThread().getName();
		Log.d(tag,tag + " read " + getType() +" "+ toString());
		
	}
}
