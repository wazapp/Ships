package com.gmail.wazappdotgithub.ships.common;

import android.util.Log;

/**
 * Used in android applications instead of immediate android.util.log dependency 
 */
public class AndroidLog extends ALog {

	
	@Override
	public void info(String tag, String msg) { Log.i(tag, tag+msg); }

	@Override
	public void debug(String tag, String msg) { Log.d(tag, tag+msg); }

	@Override
	public void warn(String tag, String msg) { Log.w(tag, tag+msg); }

	@Override
	public void err(String tag, String msg) { Log.e(tag, tag+msg); }
}