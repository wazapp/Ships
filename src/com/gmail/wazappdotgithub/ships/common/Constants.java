package com.gmail.wazappdotgithub.ships.common;

/*
 * Some general constants, will possibly sit somewhere else at some point in time
 */
public class Constants {
    public static final int DEFAULT_BOARD_SIZE = 10;
    //largest ship at the last position
    public static final int[] DEFAULT_SHIPS = new int[] {2,2,2,2,3,3,3,4,4,6};
    public static final int DEFAULT_SHIPS_NUM = DEFAULT_SHIPS.length;
    public static final int animated_bombdelay_ms = 500;
    public static final int animated_hitvibro_ms = 100;
    public static final int DEFAULT_PORT = 48152;
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10000; //10 seconds
    public static final int[] SCORELEVEL = new int[] {0,2,4,8,16,32,64,128,256,512};
}
