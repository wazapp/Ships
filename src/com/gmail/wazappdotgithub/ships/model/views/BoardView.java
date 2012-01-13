package com.gmail.wazappdotgithub.ships.model.views;

import java.util.LinkedList;
import java.util.List;

import com.gmail.wazappdotgithub.ships.R;
import com.gmail.wazappdotgithub.ships.common.ALog;
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

	private String tag = "Ships BoardView ";
	protected static Paint backgroundPaint = new Paint();
	protected static Paint gridPaint = new Paint();
	protected static Paint shipsPaint = new Paint();
	protected static Paint selectPaint = new Paint();
	protected static Paint waterPaint = new Paint();
	protected static Paint hitPaint = new Paint();
	protected static Paint missPaint = new Paint();
	protected static Paint bombwhitePaint = new Paint();

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
		BoardView.gridPaint.setColor(getResources().getColor(R.color.gridColor));
		BoardView.shipsPaint.setColor(Color.DKGRAY);
		BoardView.waterPaint.setColor(getResources().getColor(R.color.waterColor));
		BoardView.selectPaint.setColor(getResources().getColor(R.color.selectColor));
		BoardView.hitPaint.setColor(getResources().getColor(R.color.hitColor));
		BoardView.hitPaint.setAntiAlias(true);
		BoardView.missPaint.setColor(getResources().getColor(R.color.missColor));
		BoardView.missPaint.setAntiAlias(true);
		BoardView.bombwhitePaint.setColor(Color.WHITE);
		BoardView.bombwhitePaint.setAntiAlias(true);
		
		this.setOnTouchListener(this);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		//Log.d(tag,tag + " drawing, measured ="+super.getMeasuredHeight()+" vs=" +getHeight() );
		int min = Math.min(this.getWidth(), this.getHeight());
		float offs = min / (float) Constants.DEFAULT_BOARD_SIZE;
		
		drawWater(canvas,offs);
		drawGrid(canvas,offs, min);
		
		if ( ! isInEditMode() )
			drawSpecial(canvas,offs);
		
		//drawSelect(canvas); TODO change to float precision
		
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

	
	protected abstract void drawSpecial(Canvas canvas, float offset);
	protected abstract void onTouchSpecial(MotionEvent event);
	
	private final void drawWater(Canvas canvas, float offset ) {
		//the 'water'
		//canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offset,
		//		Constants.DEFAULT_BOARD_SIZE * offset, backgroundPaint);
		canvas.drawRect(0, 0, Constants.DEFAULT_BOARD_SIZE * offset,
				Constants.DEFAULT_BOARD_SIZE * offset, waterPaint);

	}
	private final void drawGrid(Canvas canvas, float offset, int min) {
		// The grid
		for (int i = 0 ; i < Constants.DEFAULT_BOARD_SIZE; i++) {
			float itimeso = i*offset;
			canvas.drawLine(0, itimeso, min, itimeso, gridPaint);
			canvas.drawLine(itimeso,0 , itimeso, min, gridPaint);
		}
		//the last ones 
		canvas.drawLine(min - 1, 0, min - 1, min, gridPaint);
		canvas.drawLine(0, min - 1, min - 1, min - 1, gridPaint);
	}
	
	/**
	 * Default implementation for drawing a ship
	 * @param canvas the canvas to draw to
	 * @param offset the current offset
	 * @param s the IShip to draw
	 */
	protected void drawShip(Canvas canvas, float offset, IShip s) {
		float x = s.getXposition() * offset;
		float y = s.getYposition() * offset;
		float si = s.getSize() * offset;

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
	
	protected void drawBomb(Canvas canvas, Bomb b, float offset) {
		if ( b.hit ) {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2 - 2 , hitPaint);
		} else {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2 - 2, missPaint);
		}
	}
	
	protected void drawNewBomb(Canvas canvas, Bomb b, float offset) {
		canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2, bombwhitePaint);
		if ( b.hit ) {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2) , offset / 2 - 2 , hitPaint);
		} else {
			canvas.drawCircle(b.x * offset + (offset / 2), b.y * offset + (offset / 2), offset / 2 - 2, missPaint);
		}
		//canvas.drawText(String.valueOf(b.score), b.x * offset, b.y * offset, backgroundPaint);
	}
	
	private final void drawSelect(Canvas canvas) {
		//the select
		canvas.drawRect(selectCol, selectPaint);
		canvas.drawRect(selectRow, selectPaint);

		//canvas.drawText(currentTouchCol + ", "+currentTouchRow, selectCol.left, selectRow.top + 15, backgroundPaint);
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
		float eX = event.getX();
		float eY = event.getY();
		
		float touchmin = Math.min(eX,eY);
		float touchmax = Math.max(eX,eY);
		
		// early out
		if (touchmin <= 0 || touchmax >= min)
			return;
		
		float offs = min / (float) Constants.DEFAULT_BOARD_SIZE;

		currentTouchCol = (int) (eX / offs);
		currentTouchRow = (int) (eY / offs);
		
		//code to update the selection rectangles
		//if (currentTouchCol >= 0 && currentTouchRow >= 0)
		updateSelect(currentTouchCol, currentTouchRow, (int)offs, min);
		
		if (currentTouchCol < 0 || currentTouchCol >= Constants.DEFAULT_BOARD_SIZE || currentTouchRow < 0 || currentTouchRow >= Constants.DEFAULT_BOARD_SIZE) {
			ALog.e(tag, "Check failed" + 
					"["+touchmin+"("+(int) touchmin+"),"+touchmax +"("+(int) touchmax+") against" + 
					this.getWidth() + "," + this.getHeight() + " becoming ["+currentTouchCol+","+currentTouchRow+"]" 
					);
		}
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
