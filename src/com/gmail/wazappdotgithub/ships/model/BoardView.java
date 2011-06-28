package com.gmail.wazappdotgithub.ships.model;

import com.gmail.wazappdotgithub.ships.PreGame;
import com.gmail.wazappdotgithub.ships.R;
import com.gmail.wazappdotgithub.ships.common.Constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
	private static Paint waterPaint = new Paint();

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
		BoardView.waterPaint.setColor(getResources().getColor(R.color.waterColor));
		BoardView.selectPaint.setColor(getResources().getColor(R.color.selectColor));
		
		this.setOnTouchListener(this);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int min = Math.min(this.getHeight(), this.getWidth());
		int offs = min / Constants.DEFAULT_BOARD_SIZE;
		
		//the 'water'
		canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offs,
				Constants.DEFAULT_BOARD_SIZE * offs, backgroundPaint);
		canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offs,
				Constants.DEFAULT_BOARD_SIZE * offs, waterPaint);


		// the ships
		for (IShip s : board.arrayOfShips()) {
			
			int x = s.getXposition() * offs;
			int y = s.getYPosition() * offs;
			int si = s.getSize() * offs;
			
			if ( s.isHorizontal() ) {
				canvas.drawRect(x, y, x + si, y + offs, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + si, y - 2 + offs, backgroundPaint);
				canvas.drawRect(x + 3, y + 3, x - 3 + si, y - 3 + offs, shipsPaint);
			} else {
				canvas.drawRect(x, y, x + offs, y + si, waterPaint);
				canvas.drawRect(x + 2, y + 2, x - 2 + offs, y - 2 + si, backgroundPaint);
				canvas.drawRect(x + 3, y + 3, x - 3 + offs, y - 3 + si, shipsPaint);
			}
		}
		
		// The grid
		for (int i = 0 ; i < Constants.DEFAULT_BOARD_SIZE + 1;i++) {
			int itimeso = i*offs;
			canvas.drawLine(0, itimeso, min, itimeso, foregroundPaint);
			canvas.drawLine(itimeso,0 , itimeso, min, foregroundPaint);
		}
		
		//the select
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
			updateSelect(currentTouchCol, currentTouchRow, offs, min);
		
		//code to perform some action on the selected or pressed ship
		
		if ( event.getEventTime() - event.getDownTime() > 300 ) { //TODO make this time a setting
	
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
			
		
		invalidate(); // TODO make smaller
		return true;
	}

	public int shipIdUnderCursor() {
		return board.getShipId(currentTouchCol, currentTouchRow);
	}
	
	private void updateSelect(int column, int row, int offset, int min) {
		int intr = offset / 3;
		
		if (currentTouchCol >= 0 && currentTouchRow >= 0)
			if (currentTouchCol < Constants.DEFAULT_BOARD_SIZE && currentTouchRow < Constants.DEFAULT_BOARD_SIZE) {
				selectCol.offsetTo(currentTouchCol * offset + intr,0);
				selectCol.bottom = selectCol.top + min;
				selectCol.right = selectCol.left + offset - (intr << 1);

				selectRow.offsetTo(0, currentTouchRow * offset + intr);
				selectRow.bottom = selectRow.top + offset - (intr << 1);
				selectRow.right = selectRow.left + min;
			}
	}

}
