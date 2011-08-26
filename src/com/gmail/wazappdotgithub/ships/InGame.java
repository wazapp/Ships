package com.gmail.wazappdotgithub.ships;

import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;
import com.gmail.wazappdotgithub.ships.model.views.BoardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * A view which will be used while the game turns are carried out
 * will switch between two views 
 * @author tor
 *
 */
public class InGame extends Activity implements OnClickListener, Observer {

	private String tag = "Ships_InGame";
	ViewFlipper vf;
	View waitokbutton,inturnokbutton;
	BoardView waitView,inturnView;
	
	Animation out = new RotateAnimation(0f, 90f);
	Animation in = new RotateAnimation(90f, 0f); // TODO just testing the pivots(f,f,f,f)
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(tag,tag + " initiating");
		super.onCreate(savedInstanceState);
		
		LocalClient.getInstance().getClientAsObservable().addObserver(this);
		
		setContentView(R.layout.ingame);
		in.setStartTime(Animation.START_ON_FIRST_FRAME);
		in.setDuration(1000);
		
		out.setStartTime(Animation.START_ON_FIRST_FRAME);
		out.setDuration(1000);
		
		//Log.d(tag,tag + " animations completed");
		vf = (ViewFlipper) findViewById(R.id.ingame_flipper);
		//Log.d(tag,tag + " viewcompleted");
		waitokbutton = findViewById(R.id.ingame_wait_okbutton);
		inturnokbutton = findViewById(R.id.ingame_inturn_okbutton);
		//Log.d(tag,tag + " buttons completed");
		
		inturnView = (BoardView) findViewById(R.id.ingame_inturn_boardview);
		waitView = (BoardView) findViewById(R.id.ingame_wait_boardview);
		//Log.d(tag,tag + " views completed");
		
		waitokbutton.setOnClickListener(this);
		inturnokbutton.setOnClickListener(this);
		
		//Log.d(tag,tag + " onclick listeners completed");
		vf.setInAnimation(in);
		vf.setOutAnimation(out);
		
		updateFireButtonText();
	}

	@Override
	public void onClick(View v) {
		if ( v == waitokbutton )
			vf.showNext();
		else if ( v == inturnokbutton ) {
			LocalClient.getInstance().reportAcceptBombs(); 
			
			vf.invalidate();
			vf.showNext();
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		//Log.d(tag, tag + "received update information");
		updateActivity((Game.ClientState) data);
	}	
	
	private void updateActivity(Game.ClientState newstate) {
		//Log.d(tag,"new state is " + newstate );
		switch ( newstate ) {
		case RECOUNTBOMBS 	: Log.d(tag, tag + "RECOUNT"); updateFireButtonText(); break;
		case INTURN 		: Log.d(tag, tag + "INTURN"); updateFireButtonText(); break;			
		case WAIT 			: Log.d(tag, tag + "WAIT");break;
		case POSTGAMELOOSER : Log.d(tag, tag + "LOOSE"); progress(); break;
		case POSTGAMEWINNER : Log.d(tag, tag + "WIN"); progress(); break;
		default : break; 
		}
	}
	
	private void updateFireButtonText() {
		IShipsClient c = LocalClient.getInstance();
		int remain = c.getRemainingBombs();
		int max = c.numLiveShips();
		Button b = ((Button)inturnokbutton);
		
		if ( remain < max ) {
			b.setEnabled(false);
			b.setText(remain+"/"+max);
		} else {
			b.setEnabled(true);
			b.setText(getString(R.string.ingameFireButton));
		}
	}
	
	private void progress() {
		LocalClient.getInstance().getClientAsObservable().deleteObserver(this);
		startActivity(new Intent(this,PostGame.class));
		finish();
	}
}
