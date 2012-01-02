package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IShip;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * implementation of BoardView that is used while the player receive bombs from the opponent
 * @author tor
 * TODO this is more than is required, should extend inturnview and only override onDraw()
 */
public class WaitBoardView extends BoardView {

	private String tag = "Ships_WaitBoardView";
		
	public WaitBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		//Paint my own ships
		for (IShip s : RemoteClient.getInstance().getBoard().arrayOfShips()) {
			drawShip(canvas, offset, s);
		}
		
		//Paint the old bombs
		for (Bomb b : RemoteClient.getInstance().requestInTurnClientHistoricalBombs() ) {
			drawBomb(canvas, b, offset);
		}
		
		//Paint the new bombs
		for (Bomb b : delayedBombs) {
			drawNewBomb(canvas, b, offset);
		}
	}
	
	@Override
	protected void onTouchSpecial(MotionEvent event) {
		return;// do nothing
	}
}
