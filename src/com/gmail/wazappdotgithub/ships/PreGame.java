package com.gmail.wazappdotgithub.ships;



import com.gmail.wazappdotgithub.ships.model.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity {

	public static final IBoard board = new BoardUsingSimpleShip(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pregame);
		
		View randomizeButton = findViewById(R.id.pregame_button_randomize);
		randomizeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("Ships PreGame","randomisingShips");
				board.randomiseShipsLocations();
				findViewById(R.id.pregame_BoardView).invalidate();
			}
		});
		View rotateButton = findViewById(R.id.pregame_button_rotate);
		rotateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				/* TODO some method that will read the latest position, and return the ship id, if any
				 * BoardView could use the latest position of the selectRectangle and return the shipid 
				 */
				
				int id = ((BoardView) findViewById(R.id.pregame_BoardView)).shipIdUnderCursor();
				Log.d("Ships PreGame","rotatingShip id " + id);
				if ( id >= 0 ) {
					board.toggleOrientation(id);
					findViewById(R.id.pregame_BoardView).invalidate();
				}
			}
		});
	}	
}
