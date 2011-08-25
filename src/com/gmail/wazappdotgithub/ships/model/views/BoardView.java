package com.gmail.wazappdotgithub.ships.model.views;

import com.gmail.wazappdotgithub.ships.R;
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
 * The Class is an abstract class that deals with the general look and feel of the board 
 * 
 * @author tor hammar 
 */
public abstract class BoardView extends View implements OnTouchListener{

	private String tag = "Ships_BoardView";
	protected static Paint backgroundPaint = new Paint();
	protected static Paint foregroundPaint = new Paint();
	protected static Paint shipsPaint = new Paint();
	protected static Paint selectPaint = new Paint();
	protected static Paint waterPaint = new Paint();
	protected static Paint hitPaint = new Paint();
	protected static Paint missPaint = new Paint();

	protected Rect selectRow, selectCol;
	
	protected int selectedShip, currentTouchCol, currentTouchRow;
	
	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		selectRow = new Rect();
		selectCol = new Rect();
		
		selectedShip = -1;
		currentTouchRow = 0;
		currentTouchCol = 0;
		
		BoardView.backgroundPaint.setColor(Color.WHITE);
		BoardView.foregroundPaint.setColor(Color.BLACK);
		BoardView.shipsPaint.setColor(Color.DKGRAY);
		BoardView.waterPaint.setColor(getResources().getColor(R.color.waterColor));
		BoardView.selectPaint.setColor(getResources().getColor(R.color.selectColor));
		BoardView.hitPaint.setColor(getResources().getColor(R.color.hitColor));
		BoardView.missPaint.setColor(getResources().getColor(R.color.missColor));
		
		this.setOnTouchListener(this);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(tag,tag + " drawing");
		int min = Math.min(this.getHeight(), this.getWidth());
		int offs = min / Constants.DEFAULT_BOARD_SIZE;
		
		drawWater(canvas,offs);

		drawSpecial(canvas,offs);
		
		drawGrid(canvas,offs, min);
		drawSelect(canvas);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(tag,tag + " touched");
		updateLocationAndSelect(event);
		
		//code to perform some action on the selected or pressed ship
		onTouchSpecial(event);
		
		invalidate(); // TODO make smaller
		return true;
	}

	
	protected abstract void drawSpecial(Canvas canvas, int offset);
	protected abstract void onTouchSpecial(MotionEvent event);
	
	private final void drawWater(Canvas canvas, int offset ) {
		//the 'water'
		canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offset,
				Constants.DEFAULT_BOARD_SIZE * offset, backgroundPaint);
		canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offset,
				Constants.DEFAULT_BOARD_SIZE * offset, waterPaint);

	}
	private final void drawGrid(Canvas canvas, int offset, int min) {
		// The grid
		for (int i = 0 ; i < Constants.DEFAULT_BOARD_SIZE + 1;i++) {
			int itimeso = i*offset;
			canvas.drawLine(0, itimeso, min, itimeso, foregroundPaint);
			canvas.drawLine(itimeso,0 , itimeso, min, foregroundPaint);
		}
	}
	
	 
	private final void drawSelect(Canvas canvas) {
		//the select
		canvas.drawRect(selectCol, selectPaint);
		canvas.drawRect(selectRow, selectPaint);

		canvas.drawText(currentTouchCol + ", "+currentTouchRow, selectCol.left, selectRow.top + 15, backgroundPaint);
	}
	
	public final int translateX(MotionEvent event) {
		//TODO find a better way to access the screen size .. static possibility?
		return ( (int) event.getX() ) / (Math.min(this.getHeight(), this.getWidth()) / Constants.DEFAULT_BOARD_SIZE);
	}
	
	public final int translateY(MotionEvent event) {
		//TODO find a better way to access the screen size .. static possibility?
		return ( (int) event.getY() ) / (Math.min(this.getHeight(), this.getWidth()) / Constants.DEFAULT_BOARD_SIZE);
	}
	
	private final void updateLocationAndSelect(MotionEvent event) {
		int min = Math.min(this.getHeight(), this.getWidth());
		// TODO figure out how to restrict the sensitive area, 
		// right now you can move your finger outside the view and it will still
		
		//listen to the touch
		int offs = min / Constants.DEFAULT_BOARD_SIZE;

		currentTouchCol = ( (int) event.getX() ) / offs;
		currentTouchRow = ( (int) event.getY() ) / offs;

		//code to update the selection rectangles
		if (currentTouchCol >= 0 && currentTouchRow >= 0)
			updateSelect(currentTouchCol, currentTouchRow, offs, min);	
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
