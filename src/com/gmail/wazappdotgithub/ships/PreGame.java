package com.gmail.wazappdotgithub.ships;



import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.*;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;
import com.gmail.wazappdotgithub.ships.model.views.PreGameBoardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity implements Observer {

	private String tag = "Ships PreGame";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Game.getConfiguredInstance().getLocalClientObservable().addObserver(this);
		
		Log.d(tag,tag + "configuring pregame contentview");
		setContentView(R.layout.pregame);
		Log.d(tag,tag + "configured pregame contentview");
		
		View randomizeButton = findViewById(R.id.pregame_button_randomize);
		randomizeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(tag,"randomisingShips");
				Game.getConfiguredInstance().getLocalClient().getBoard().randomiseShipsLocations();
				findViewById(R.id.pregame_BoardView).invalidate();
			}
		});
		
		View rotateButton = findViewById(R.id.pregame_button_rotate);
		rotateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int id = ((PreGameBoardView) findViewById(R.id.pregame_BoardView)).shipIdUnderCursor();
				Log.d(tag,"rotatingShip id " + id);
				if ( id >= 0 ) {
					Game.getConfiguredInstance().getLocalClient().getBoard().toggleOrientation(id);
					findViewById(R.id.pregame_BoardView).invalidate();
				}
			}
		});
		
		View startButtonView = findViewById(R.id.pregame_button_start);
		startButtonView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(tag,"phone start button pressed");
				Game.getConfiguredInstance().getLocalClient().reportReady();
			}
		});
		
		updateActivity(LocalClient.getInstance().getState());
	}

	@Override
	public void update(Observable observable, Object data) {
		// the observable is the client, the data the new state
		updateActivity((Game.ClientState) data);
	}	
	
	private void updateActivity(Game.ClientState newstate) {
		Log.d(tag,"new state is " + newstate );
		switch ( newstate ) {
		case PREGAME : enableInteraction(); break;
		case WAITGAME : progress(); break;
		default : disableInteraction(); break; 
		}
	}
	private void progress() {
		startActivity(new Intent(this,InGame.class));
		finish();
	}
	
	private void enableInteraction() {
		findViewById(R.id.pregame_BoardView).setEnabled(true);
		findViewById(R.id.pregame_button_randomize).setEnabled(true);
		findViewById(R.id.pregame_button_rotate).setEnabled(true);
		findViewById(R.id.pregame_button_start).setEnabled(true);
	}
	private void disableInteraction() {
		findViewById(R.id.pregame_BoardView).setEnabled(false);
		findViewById(R.id.pregame_button_randomize).setEnabled(false);
		findViewById(R.id.pregame_button_rotate).setEnabled(false);
		findViewById(R.id.pregame_button_start).setEnabled(false);
	}
}
