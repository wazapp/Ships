package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * Implementation of BoardView which is used while the client is inturn state 
 * @author tor
 *
 */
public final class InTurnBoardView extends BoardView {
	
	private String tag = "Ships_InTurnBoardView";
	
	private static Paint target_one = new Paint();
	private static Paint target_two = new Paint();
	private static Paint target_center = new Paint();
	
	public InTurnBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		target_one.setColor(Color.WHITE);
		target_two.setColor(Color.GREEN);
		target_center.setColor(Color.RED);
	}

	/*
	 * don't have to override onDraw() since it calls this method... 
	 */
	@Override
	protected void drawSpecial(Canvas canvas, int offset) {
		int obytwo = offset / 2;
		
		for (Bomb b : LocalClient.getInstance().getBombsBoard()) {
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, missPaint);
		}
		
		for (Bomb b : LocalClient.getInstance().getInTurnBombs()) {
			if ( b.hit ) 
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo , obytwo, hitPaint);
			else {
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, target_one);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-2, target_two);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-4, target_one);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-6, target_two);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-8, target_center);
			}
		}
		
		for (Bomb b : LocalClient.getInstance().getLatestTurnBombs()) {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2, backgroundPaint);
			if ( b.hit ) {
				canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2 - 2 , hitPaint);
			} else {
				canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2 - 2, missPaint);
			}
		}
	}
	
	@Override 
	protected void onTouchSpecial(MotionEvent event) {
		if ( event.getAction() == MotionEvent.ACTION_UP) {
			
			LocalClient.getInstance().placeBomb(translateX(event), translateY(event));
		}
	}
}
