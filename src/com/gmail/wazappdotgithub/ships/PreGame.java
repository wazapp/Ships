package com.gmail.wazappdotgithub.ships;



import com.gmail.wazappdotgithub.ships.model.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity {

	public static final IBoard board = new BoardUsingMatrix(); 
	
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
				Log.d("Ships PreGame","rotatingShip");
				board.toggleOrientation(0);
				findViewById(R.id.pregame_BoardView).invalidate();
			}
		});
	}	
}
