package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.IShip;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class PreGameBoardView extends BoardView {
	IBoard board;	

	public PreGameBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if ( ! isInEditMode() )
			board = LocalClient.getInstance().getBoard();
	}

	public int shipIdUnderCursor() {
		return board.getShipId(currentTouchCol, currentTouchRow);
	}

	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		for (IShip s : board.arrayOfShips()) {
			drawShip(canvas, offset, s);
			}
	}

	@Override
	protected void onTouchSpecial(MotionEvent event) {
		if ( event.getEventTime() - event.getDownTime() > 300 ) { //TODO make this time a setting

			//TODO moving is jerky because it considers a mid-ship press a head press!
			switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
			case MotionEvent.ACTION_MOVE : board.moveShip(selectedShip, currentTouchCol, currentTouchRow); break;
			case MotionEvent.ACTION_UP : selectedShip = -1; break;
			default : Log.d("BoardView", "MotionEvent " + event.getAction() + " not caught");
			}
		} else {

			switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
			case MotionEvent.ACTION_UP : board.toggleOrientation(selectedShip); selectedShip = -1; break;
			default : Log.d("BoardView", "MotionEvent " + event.getAction() + " not caught");
			}
		}

		Log.d("BoardView touchEvent: ","boardview ship:"+selectedShip+ " " + currentTouchCol +", "+ currentTouchRow +  " time: "+ (event.getEventTime() - event.getDownTime()));

	}
}
