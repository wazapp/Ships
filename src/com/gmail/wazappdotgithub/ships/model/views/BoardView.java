package com.gmail.wazappdotgithub.ships.model.views;

import java.util.LinkedList;
import java.util.List;

import com.gmail.wazappdotgithub.ships.R;
import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.IShip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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
	
	protected List<Bomb> delayedBombs = new LinkedList<Bomb>();
	
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
		//Log.d(tag,tag + " drawing, measured ="+super.getMeasuredHeight()+" vs=" +getHeight() );
		int min = Math.min(super.getWidth(), super.getHeight());
		int offs = min / Constants.DEFAULT_BOARD_SIZE;
		
		drawWater(canvas,offs);

		if ( ! isInEditMode() )
			drawSpecial(canvas,offs);
		
		drawGrid(canvas,offs, min);
		//drawSelect(canvas);
	}
	
	/**
	 * Used to add bombs to be drawn during some delayed process.
	 * default implementation does nothing
	 * @param b
	 */
	public synchronized void addDelayedBomb(Bomb b) {
		if ( b != null ) 
			delayedBombs.add(b);
	}
	
	
	/**
	 * Used to clear bombs drawn during last round
	 * default implementation does nothing
	 */
	public synchronized void clearDelayedBombs() {
		delayedBombs.clear();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		/*
		 * The board is a square
		 */
		int min = Math.min(super.getMeasuredWidth(), super.getMeasuredHeight());
		setMeasuredDimension(min, min);
		//Log.d(tag, tag + " setting size to " + min +"x"+min);
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Log.d(tag,tag + " touched");
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
	
	/**
	 * Default implementation for drawing a ship
	 * @param canvas the canvas to draw to
	 * @param offset the current offset
	 * @param s the IShip to draw
	 */
	protected void drawShip(Canvas canvas, int offset, IShip s) {
		int x = s.getXposition() * offset;
		int y = s.getYposition() * offset;
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
	
	protected void drawBomb(Canvas canvas, Bomb b, int offset) {
		if ( b.hit )
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2, hitPaint);
		else
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2, missPaint);	
	}
	
	protected void drawNewBomb(Canvas canvas, Bomb b, int offset) {
		canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2, backgroundPaint);
		if ( b.hit ) {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2 - 2 , hitPaint);
		} else {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2 - 2, missPaint);
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
