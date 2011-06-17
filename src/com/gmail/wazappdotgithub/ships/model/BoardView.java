package com.gmail.wazappdotgithub.ships.model;

import com.gmail.wazappdotgithub.ships.PreGame;
import com.gmail.wazappdotgithub.ships.common.Constants;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;

/**
 * Anything that is named here is relative to a portrait oriented screen
 * This means any reference to x, or width or column are friends and 
 * any reference to y, height or row belongs together.
 * any coordinate must be given in (x,y) orientation
 * 
 * @author tor hammar 
 */
public final class BoardView extends View implements OnTouchListener{

	private static Paint backgroundPaint = new Paint();
	private static Paint foregroundPaint = new Paint();
	private static Paint shipsPaint = new Paint();
	private static Paint selectPaint = new Paint();
	
	private Rect selectRow = new Rect();
	private Rect selectCol = new Rect();
	private IBoard board = PreGame.board;
	
	private int selectedShip = -1;
	private int currentTouchRow = 0;
	private int currentTouchCol = 0;

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		BoardView.backgroundPaint.setColor(Color.WHITE);
		BoardView.foregroundPaint.setColor(Color.BLACK);
		BoardView.shipsPaint.setColor(Color.DKGRAY);
		BoardView.selectPaint.setColor(Color.argb(125, 0, 0, 255)); //TODO move to XML
		
		this.setOnTouchListener(this);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		int min = Math.min(this.getHeight(), this.getWidth());
		int offs = min / Constants.DEFAULT_BOARD_SIZE;
		
		canvas.drawRect(new Rect(0, 0, Constants.DEFAULT_BOARD_SIZE * offs,
				Constants.DEFAULT_BOARD_SIZE * offs), backgroundPaint);
		//canvas.drawColor(Color.WHITE); // maybe too large?
		
		/* TODO because of rounding to int, there will be some pixels leftover at i * offset
		 * figure out if it can be avoided using some other solution than the rect
		 * 
		 * why not let offs be a float?
		 */
		
		//Since I want the grid placed on top of the ships I need to do this for now
		// later i may have to know the size of the ship or have a ship draw method instead.
		for (int i = 0 ; i < 11;i++) {
			int itimeso = i*offs;
			for (int j = 0; j < 11; j++) {
				if ( board.hasShip(i, j) ) {
					canvas.drawRect(new Rect(itimeso,j * offs, itimeso + offs, j * offs + offs), shipsPaint);
				}
			}
		}
		
		/*for (int i = 0 ; i < 11;i++) {
			int itimeso = i*offs;			
			canvas.drawLine(0, itimeso, min, itimeso, foregroundPaint);
			canvas.drawLine(itimeso,0 , itimeso, min, foregroundPaint);
		}*/
		
		canvas.drawRect(selectCol, selectPaint);
		canvas.drawRect(selectRow, selectPaint);

		canvas.drawText(currentTouchCol + ", "+currentTouchRow, selectCol.left, selectRow.top + 15, backgroundPaint);
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int min = Math.min(this.getHeight(), this.getWidth());
		// TODO figure out how to restrict the sensitive area, 
		// right now you can move your finger outside the view and it will still
		// listen to the touch
		int offs = min / Constants.DEFAULT_BOARD_SIZE;

		currentTouchCol = ( (int) event.getX() ) / offs;
		currentTouchRow = ( (int) event.getY() ) / offs;

		//code to update the selection rectangles
		if (currentTouchCol >= 0 && currentTouchRow >= 0)
			if (currentTouchCol < Constants.DEFAULT_BOARD_SIZE && currentTouchRow < Constants.DEFAULT_BOARD_SIZE) {
				selectCol.offsetTo(currentTouchCol * offs,0);
				selectCol.bottom = selectCol.top + min;
				selectCol.right = selectCol.left + offs;

				selectRow.offsetTo(0, currentTouchRow * offs);
				selectRow.bottom = selectRow.top + offs;
				selectRow.right = selectRow.left + min;
			}
		
		//code to perform some action on the selected or pressed ship
		
		switch ( event.getAction() ) {
		case MotionEvent.ACTION_DOWN : selectedShip = board.getShipId(currentTouchCol, currentTouchRow); break;
		case MotionEvent.ACTION_MOVE : board.moveShip(selectedShip, currentTouchCol, currentTouchRow); break;
		case MotionEvent.ACTION_UP : selectedShip = -1; break;
		default : Log.d("BoardView", "MotionEvent " + event.getAction() + " not caught");
		}
		
		Log.d("BoardView touchEvent: ","boardview ship:"+selectedShip+" \n" + currentTouchCol +","+ currentTouchRow);
			
		
		invalidate(); // TODO make smaller
		return true;
	}

}
