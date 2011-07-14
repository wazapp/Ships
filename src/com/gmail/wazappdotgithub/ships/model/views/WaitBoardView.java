package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.IShip;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * implementation of BoardView that is used while the player receive bombs from the opponent
 * @author tor
 *
 */
public class WaitBoardView extends BoardView {

	public WaitBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		for (IShip s : Game.getConfiguredInstance().getLocalClient().getBoard().arrayOfShips()) {

			int x = s.getXposition() * offset;
			int y = s.getYPosition() * offset;
			int si = s.getSize() * offset;

			if ( s.isHorizontal() ) {
				canvas.drawRect(x, y, x + si, y + offset, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + si, y - 2 + offset, backgroundPaint);
				canvas.drawRect(x + 3, y + 3, x - 3 + si, y - 3 + offset, shipsPaint);
			} else {
				canvas.drawRect(x, y, x + offset, y + si, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + offset, y - 2 + si, backgroundPaint);
				canvas.drawRect(x + 3, y + 3, x - 3 + offset, y - 3 + si, shipsPaint);
			}
		}
		for (Bomb b : Game.getConfiguredInstance().getOpponentsShots() ) { //TODO only draws the latest, bug here or ComputerClient
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2, hitPaint);
			else
				canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2, missPaint);			
		}
	}

	@Override
	protected void onTouchSpecial(MotionEvent event) {
		// do nothing

	}

}
