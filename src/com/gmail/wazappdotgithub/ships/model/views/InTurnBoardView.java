package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;

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
	
	public InTurnBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/*
	 * don't have to override onDraw() since it calls this method... 
	 */
	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		int obytwo = offset / 2;
		
		for (Bomb b : Game.getConfiguredInstance().getLocalClient().getBombsBoard()) { // TODO change graphics of these
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, missPaint);
		}
		for (Bomb b : Game.getConfiguredInstance().getLocalClient().getInTurnBombs()) {
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, missPaint);
		}
	}
	
	@Override 
	protected void onTouchSpecial(MotionEvent event) {
		if ( event.getAction() == MotionEvent.ACTION_UP) {
			Game.getConfiguredInstance().getLocalClient().placeBomb(translateX(event), translateY(event));
		}
	}
}
