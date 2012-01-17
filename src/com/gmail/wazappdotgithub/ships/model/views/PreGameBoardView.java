package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.common.Constants;
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
	private int oldcol, oldrow;

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
			
			if (event.getAction() == MotionEvent.ACTION_DOWN ) {
				selectedShip = board.getShipId(currentTouchCol, currentTouchRow);
				oldcol = currentTouchCol;
				oldrow = currentTouchRow; 
			}
			
			boolean test = event.getEventTime() - event.getDownTime() > Constants.DEFAULT_SHIP_TOUCH_DELAY_MS;
			if ( test ) {
				switch ( event.getAction() ) {
				case MotionEvent.ACTION_MOVE : 
					if (oldcol == currentTouchCol && oldrow == currentTouchRow)
						break;
					
					board.moveShipRelative(selectedShip, (currentTouchCol - oldcol), (currentTouchRow - oldrow) ); 
					oldcol = currentTouchCol;
					oldrow = currentTouchRow; 
					
					break;
				case MotionEvent.ACTION_UP : selectedShip = -1; break;
				default : ALog.d("Ships PregameBoardView ", "MotionEvent " + event.getAction() + " ignored");
				}
				
			} else {

				switch ( event.getAction() ) {
				//case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
				case MotionEvent.ACTION_UP : board.toggleOrientation(selectedShip); selectedShip = -1; break;
				default : ALog.d("Ships PregameBoardView ", "MotionEvent " + event.getAction() + " ignored");
				}
			}

			//ALog.d("MOVE KALAS", "MOVE KALAS " + oldcol +", " + oldrow + " "+ event.getAction());
			//ALog.d("MOVE KALAS","MOVE KALAS" + "from " + event.getHistoricalX(0)+","+ event.getHistoricalY(0)+ "to"+ (currentTouchCol - event.getHistoricalX(0))+","+(currentTouchRow - event.getHistoricalY(0)));
		}
	}
}
