package com.gmail.wazappdotgithub.ships;

import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity implements Observer {

	private String tag = "Ships PreGame ";
	private IShipsClient model;
	
	private ProgressDialog waiting;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ALog.d(tag,"configuring pregame contentview");
		setContentView(R.layout.pregame);
		
		model = RemoteClient.getInstance();
		model.addAsObserver(this);
		
		
		View randomizeButton = findViewById(R.id.pregame_button_randomize);
		randomizeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ALog.d(tag,"randomising ships");
				model.getBoard().randomiseShipsLocations();
				findViewById(R.id.pregame_BoardView).invalidate();
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
				model.playerCompletedPreGame();
			}
		});
	}

	@Override
	public void update(Observable observable, Object data) {
		updateActivity((Statename) data);
	}	
	
	private void updateActivity(Statename newstate) {
		
		ALog.d(tag,"updateActivity " + newstate );
		
		switch ( newstate ) {
		case PREGAME_EXIT : disableInteraction(); launchProgressDialog(); break;
		case WAITGAME : model.playerCompletedWaitGame(); break;
		case WAITGAME_EXIT : dismissProgressDialog(); break;
		case TURN : progress(); break;
		case WAIT : progress(); break;
		default : throw new RuntimeException("Illegal state during pregame activity");
		}
	}
	
	private void launchProgressDialog() {
		waiting = ProgressDialog.show(this, "", "Waiting for opponent");
	}
	
	private void dismissProgressDialog() {
		waiting.setMessage("Connected " + model.getOpponentName());
		waiting.dismiss();
	}
	
	private void progress() {
		model.removeAsObserver(this);
		startActivity(new Intent(this,InGame.class));
		finish();
	}
	
	private void disableInteraction() {
		findViewById(R.id.pregame_BoardView).setEnabled(false);
		findViewById(R.id.pregame_button_randomize).setEnabled(false);
		findViewById(R.id.pregame_button_start).setEnabled(false);
	}
}
