package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.model.IBoard;
import com.gmail.wazappdotgithub.ships.model.IShip;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PreGameBoardView extends BoardView {
	IBoard board;	

	public PreGameBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if ( ! isInEditMode() )
			board = RemoteClient.getInstance().getBoard();
	}

	public int shipIdUnderCursor() {
		return board.getShipId(currentTouchCol, currentTouchRow);
	}

	@Override
	protected void drawSpecial(Canvas canvas, float offset) {
		for (IShip s : board.arrayOfShips()) {
			drawShip(canvas, offset, s);
		}
	}

	@Override
	protected void onTouchSpecial(MotionEvent event) {
		if (RemoteClient.getInstance().getState() == Statename.PREGAME) {
			if ( event.getEventTime() - event.getDownTime() > 300 ) { //TODO make this time a setting

				//TODO moving is jerky because it considers a mid-ship press a head press!
				switch ( event.getAction() ) {
				case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
				case MotionEvent.ACTION_MOVE : board.moveShip(selectedShip, currentTouchCol, currentTouchRow); break;
				case MotionEvent.ACTION_UP : selectedShip = -1; break;
				default : ALog.d("Ships PregameBoardView ", "MotionEvent " + event.getAction() + " ignored");
				}
			} else {

				switch ( event.getAction() ) {
				case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
				case MotionEvent.ACTION_UP : board.toggleOrientation(selectedShip); selectedShip = -1; break;
				default : ALog.d("Ships PregameBoardView ", "MotionEvent " + event.getAction() + " ignored");
				}
			}

			//ALog.d("Ships BoardView ",touchEvent: ","boardview ship:"+selectedShip+ " " + currentTouchCol +", "+ currentTouchRow +  " time: "+ (event.getEventTime() - event.getDownTime()));
		}
	}
}
