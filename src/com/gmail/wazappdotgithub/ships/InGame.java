package com.gmail.wazappdotgithub.ships;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.LocalClient;
import com.gmail.wazappdotgithub.ships.model.Game.ClientState;
import com.gmail.wazappdotgithub.ships.model.views.BoardView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * An activity which will be used while the game turns are carried out
 * will switch between two views. It Uses an AsyncTask to present the 
 * latest Bombs on the view in an incremental fashion.
 * @author tor
 */
public class InGame extends Activity implements OnClickListener, Observer {

	private String tag = "Ships_InGame";
	private ViewFlipper viewflipper;
	private Button waitokbutton,inturnokbutton;
	private BoardView waitView,inturnView;
	
	private Animation out = new RotateAnimation(0f, 90f);
	private Animation in = new RotateAnimation(90f, 0f); // TODO just testing the pivots(f,f,f,f)
	
	private MediaPlayer mp;
	private Vibrator vibro;
	
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
		viewflipper = (ViewFlipper) findViewById(R.id.ingame_flipper);
		//Log.d(tag,tag + " viewcompleted");
		waitokbutton = (Button) findViewById(R.id.ingame_wait_okbutton);
		inturnokbutton = (Button) findViewById(R.id.ingame_inturn_okbutton);
		//Log.d(tag,tag + " buttons completed");
		
		inturnView = (BoardView) findViewById(R.id.ingame_inturn_boardview);
		waitView = (BoardView) findViewById(R.id.ingame_wait_boardview);
		//Log.d(tag,tag + " views completed");
		
		waitokbutton.setOnClickListener(this);
		inturnokbutton.setOnClickListener(this);
		
		//Log.d(tag,tag + " onclick listeners completed");
		viewflipper.setInAnimation(in);
		viewflipper.setOutAnimation(out);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		updateFireButtonText();
	}
	
	/*
	 * Using this as is recommended in http://developer.android.com/guide/topics/fundamentals/processes-and-threads.html
	 * to enable incremental presentation of the bombs 
	 */
	private class EvaluateBombsTask extends AsyncTask<List<Bomb>,Boolean,Void> {
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() 
	     * @return */
		private BoardView v = waitView;
		
		@Override
		protected Void doInBackground(List<Bomb>... params) {
			/*
			Log.d(tag, tag + "Animation Status ");
			Log.d(tag, tag + "Animation Status in.hasStarted " +  in.hasStarted() + " hasEnded() " + in.hasEnded());
			Log.d(tag, tag + "Animation Status out.hasStarted " +  out.hasStarted() + " hasEnded() " + out.hasEnded());
			Log.d(tag, tag + "Starting incremental bombing ");
			*/ // TODO wait for the animation to complete before starting to drop bombs, not sure how thoese flags work
			
			Log.d(tag,tag + " Evaluate Thread got " + params[0].size() + " bombs during " + LocalClient.getInstance().getState());
			if ( LocalClient.getInstance().getState() == ClientState.I_EVALUATE )
				v = inturnView;
			
			try {
				for (Bomb b : params[0]) {
					v.addDelayedBomb(b);	//possibly have a currentView pointer in InGame
					publishProgress(b.hit); // play sound if miss
					if (b.hit == false)
			    		mp.start();	
					else
						vibro.vibrate(Constants.animated_hitvibro_ms);
						
					Thread.sleep(Constants.animated_bombdelay_ms);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	    
	    @Override
		protected void onProgressUpdate(Boolean... values) {
	    	v.invalidate();
	    }

		/** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
		@Override
	    protected void onPostExecute(Void v) {
			LocalClient.getInstance().requestNextState();
	    }
	}

	@Override
	public void onClick(View v) {
		Game.ClientState current_state = LocalClient.getInstance().getState();
		if ( current_state == Game.ClientState.W_COMPLETEDEVALUATION 
				|| current_state == Game.ClientState.I_COMPLETEDEVALUATION )
			viewflipper.showNext();
		
		
		//always
		LocalClient.getInstance().requestNextState();
	}
	
	@Override
	public void update(Observable observable, Object data) {
		//Log.d(tag, tag + "received update information");
		updateActivity((Game.ClientState) data);
	}	
	
	/*
	 * Let the UI react to an update from the Client
	 */
	private void updateActivity(Game.ClientState newstate) {
		//Log.d(tag,"new state is " + newstate );

		switch ( newstate ) {
		case RECOUNTBOMBS : 
			updateFireButtonText(); 
			break;
			
		case INTURN : 
			updateFireButtonText(); 
			break;
			
		case I_EVALUATE : 
			updateButton(inturnokbutton,false, getString(R.string.ingameFireButton_Evaluating)); 
			spawnIncrementalBombThread(newstate); 
			break;
			
		case I_COMPLETEDEVALUATION : 
			updateButton(inturnokbutton, true, getString(R.string.ingameFireButton_Continue)); 
			break;
			
		case W_EVALUATE	:
			updateButton(waitokbutton,false, getString(R.string.ingameFireButton_Evaluating)); 
			spawnIncrementalBombThread(newstate); 
			break;  
			
		case W_COMPLETEDEVALUATION : 
			updateButton(waitokbutton,true, getString(R.string.ingameFireButton_Continue)); 
			break;
			
		case READYCHANGETURNS : 
			updateButton(waitokbutton,false, getString(R.string.ingameWaitButton_Waiting));
			waitView.clearDelayedBombs();
			updateButton(inturnokbutton,false, getString(R.string.ingameWaitButton_Waiting)); 
			inturnView.clearDelayedBombs();
			break;
			
		case WAIT : 
			break;
			
		case POSTGAMELOOSER : 
			progress(); 
			break;
			
		case POSTGAMEWINNER : 
			progress(); 
			break;
			
		default : break; 
		}
	}
	
	private void spawnIncrementalBombThread(Game.ClientState newstate) {
		if (mp != null)
			mp.release();
		
		mp = MediaPlayer.create(this, R.raw.plopp);
		new EvaluateBombsTask().execute(LocalClient.getInstance().requestInTurnClientAcceptedBombs());

	}
	
	// a helper method
	private void updateButton(Button b, boolean enabled, String newtext) {
		b.setEnabled(enabled);
		b.setText(newtext);
	}
	
	// a helper method which updates the text on the firebutton when the player is placing bombs
	private void updateFireButtonText() {
		IShipsClient c = LocalClient.getInstance();
		int remain = c.getRemainingBombs();
		int max = c.numLiveShips();
		
		if ( remain < max ) {
			updateButton(inturnokbutton, false, remain+"/"+max);			
		} else {
			updateButton(inturnokbutton, true, getString(R.string.ingameFireButton_Fire));
		}
	}
	
	// move from this activity to the next
	private void progress() {
		LocalClient.getInstance().getClientAsObservable().deleteObserver(this);
		startActivity(new Intent(this,PostGame.class));
		finish();
	}
	
	
	
	//Unused for the moment
	private long[] getBombPattern(List<Bomb> listofbombs) {
		int hit = 250;
		int miss = 0;
		int hit_gap = 100;
		int miss_gap = 350;
		
		long[] pattern = new long[listofbombs.size() * 2 + 1];
		pattern[0] = 0;
	    
		for (int a = 0; a < listofbombs.size(); a++) {
			
			if (listofbombs.get(a).hit) {
				pattern[a * 2 + 1] = hit;
				pattern[a * 2 + 2] = hit_gap;
			} else {
				pattern[a * 2 + 1] = miss;
				pattern[a * 2 + 2] = miss_gap;
			}
		}
		
		return pattern;
	}
}
