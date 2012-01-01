package com.gmail.wazappdotgithub.ships;



import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;
import com.gmail.wazappdotgithub.ships.model.views.PreGameBoardView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity implements Observer {

	private String tag = "Ships PreGame";
	private IShipsClient model = RemoteClient.getInstance();
	
	ProgressDialog waiting;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		model.addAsObserver(this);
		
		//Log.d(tag,tag + "configuring pregame contentview");
		setContentView(R.layout.pregame);
		
		View randomizeButton = findViewById(R.id.pregame_button_randomize);
		randomizeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(tag,"randomisingShips");
				model.getBoard().randomiseShipsLocations();
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
					model.getBoard().toggleOrientation(id);
					findViewById(R.id.pregame_BoardView).invalidate();
				}
			}
		});
		
		View startButtonView = findViewById(R.id.pregame_button_start);
		startButtonView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * The player has completed the pregame phase and will 
				 * now wait for the other player to complete the phase too
				 * 
				 *  This will send a message to the other player
				 */
				Log.d(tag,"start button pressed");
				model.playerCompletedPreGame();
			}
		});
		
	}

	@Override
	public void update(Observable observable, Object data) {
		updateActivity((Statename) data);
	}	
	
	private void updateActivity(Statename newstate) {
		//Log.d(tag,"new state is " + newstate );
		switch ( newstate ) {
		case PREGAME : disableInteraction(); break;
		case WAITGAME : progress(); break;
		//default : disableInteraction(); break; 
		}
	}
	
	private void progress() {
		waiting.setMessage("Connected " + RemoteClient.getInstance().getOpponentName());
		waiting.dismiss();
		
		model.removeAsObserver(this);
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
		
		waiting = ProgressDialog.show(this, "", "Waiting for opponent");
	}
}
