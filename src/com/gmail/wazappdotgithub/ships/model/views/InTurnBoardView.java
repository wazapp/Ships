package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * Implementation of BoardView which is used while the client is inturn state 
 * @author tor
 *
 */
public final class InTurnBoardView extends BoardView {
	
	private String tag = "Ships_InTurnBoardView";
	
	public InTurnBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * don't have to override onDraw() since it calls this method... 
	 */
	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		int obytwo = offset / 2;
		/*TODO only for debugging, remove at some point
		for (IShip s : Game.getConfiguredInstance().getOpponentClient().getBoard().arrayOfShips()) {
			int x = s.getXposition() * offset;
			int y = s.getYposition() * offset;
			int si = s.getSize() * offset;

			if ( s.isHorizontal() ) {
				canvas.drawRect(x, y, x + si, y + offset, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + si, y - 2 + offset, backgroundPaint);
				//canvas.drawRect(x + 3, y + 3, x - 3 + si, y - 3 + offset, shipsPaint);
			} else {
				canvas.drawRect(x, y, x + offset, y + si, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + offset, y - 2 + si, backgroundPaint);
				//canvas.drawRect(x + 3, y + 3, x - 3 + offset, y - 3 + si, shipsPaint);
			}
		}
		end only for debugging
		*/
		
		for (Bomb b : LocalClient.getInstance().getBombsBoard()) { // TODO change graphics of these
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, missPaint);
		}
		for (Bomb b : LocalClient.getInstance().getInTurnBombs()) {
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, missPaint);
		}
	}
	
	@Override 
	protected void onTouchSpecial(MotionEvent event) {
		if ( event.getAction() == MotionEvent.ACTION_UP) {
			
			LocalClient.getInstance().placeBomb(translateX(event), translateY(event));
		}
	}
}
