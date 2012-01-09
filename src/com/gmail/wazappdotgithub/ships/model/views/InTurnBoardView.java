package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.DataAccess;

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
		//Log.d(tag, Thread.currentThread().getName() + " redrawing inturnview");
		int obytwo = offset / 2;
		
		//Draw all earlier bombs
		for (Bomb b : RemoteClient.getInstance().requestInTurnClientHistoricalBombs(DataAccess.LOCAL)) {
			drawBomb(canvas, b, offset);
		}
		
		//draw whatever I entered this turn
		for (Bomb b : RemoteClient.getInstance().requestInTurnClientAcceptedBombs(DataAccess.LOCAL)) {
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo, target_one);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-2, target_two);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-4, target_one);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-6, target_two);
				canvas.drawCircle(b.x * offset + obytwo, b.y * offset + obytwo, obytwo-8, target_center);
		}
		
		//draw my hits/misses during evaluation
		for (Bomb b : delayedBombs) {
			drawNewBomb(canvas, b, offset);
		}
	}
	
	@Override 
	protected void onTouchSpecial(MotionEvent event) {
		if ( RemoteClient.getInstance().getState() == Statename.TURN) {
			if ( event.getAction() == MotionEvent.ACTION_UP) {
				RemoteClient.getInstance().placeBomb(translateX(event), translateY(event));
			}
		}
	}
}
