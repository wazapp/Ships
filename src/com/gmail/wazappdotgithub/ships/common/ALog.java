package com.gmail.wazappdotgithub.ships.common;

/*
 * To use instead of android.util.log to remove dependencies of that package
 * from many classes, this implementation defaults to stdout
 */
public class ALog {
	
	private static ALog channel = new ALog();
	public static void setLogger(ALog logger) {
		channel = logger;
	}
	
	/**
	 * write to Information channel of logger
	 * @param tag
	 * @param message
	 */
	public static void i(String tag, String message) { channel.info(tag, message); }
	
	/**
	 * write to Debug channel of logger
	 * @param tag
	 * @param message
	 */
	public static void d(String tag, String message) { channel.debug(tag, message); }
	
	/**
	 * write to Warning channel of logger
	 * @param tag
	 * @param message
	 */
	public static void w(String tag, String message) { channel.warn(tag, message); }
	
	/**
	 * write to Error channel of logger
	 * @param tag
	 * @param message
	 */
	public static void e(String tag, String message) { channel.err(tag, message); }
	
	//Override in subclass to implement specific logging
	public void info(String tag, String msg) { System.out.println(tag + msg); }
	public void debug(String tag, String msg) { System.out.println(tag + msg); }
	public void warn(String tag, String msg) { System.out.println(tag + msg); }
	public void err(String tag, String msg) { System.err.println(tag + msg); }
}
