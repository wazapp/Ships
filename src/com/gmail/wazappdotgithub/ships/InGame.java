package com.gmail.wazappdotgithub.ships;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import com.gmail.wazappdotgithub.ships.common.Constants;
import com.gmail.wazappdotgithub.ships.model.Bomb;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.model.views.BoardView;

import android.app.Activity;
import android.app.ProgressDialog;
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
	
	private IShipsClient model = RemoteClient.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(tag,tag + " initiating");
		super.onCreate(savedInstanceState);
		model.addAsObserver(this);
		
		/*
		 * Note that the player will enter this Activity / state 
		 * either being in Turn or in Wait
		 * This Activity covers several client states (the whole game loop)
		 */
		
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
		
		// If the player is in wait state
		if (model.getState() == Statename.WAIT ) {
			enterWait();
			viewflipper.showNext();
		}
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
			
			Log.d(tag,tag + " Evaluate Thread got " + params[0].size() + " bombs during " + model.getState());
			if ( model.getState() == Statename.TURN_EVAL )
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
			if ( model.getState() == Statename.TURN_EVAL )
				exitTurnEval();
			
			if ( model.getState() == Statename.WAIT_EVAL )
				exitWaitEval();
	    }
	}

	@Override
	public void onClick(View v) {
		Statename current_state = model.getState();
		// for when the bombs have been placed
		if ( current_state == Statename.TURN )
			model.playerCompletedTurn();
		
		// for when the player has looked at his bombs
		if (current_state == Statename.TURN_EVAL)
			model.playerCompletedEvaluation();
		
		// for when the player has looked at the opponents bombs
		if (current_state == Statename.WAIT_EVAL)
			model.playerCompletedEvaluation();
	}
	
	@Override
	public void update(Observable observable, Object data) {
		//Log.d(tag, tag + "received update information");
		updateActivity((Statename) data);
	}	
	
	/*
	 * Let the UI react to an update from the Client
	 */
	private void updateActivity(Statename newstate) {
		//Log.d(tag,"new state is " + newstate );

		switch ( newstate ) {
		case RECOUNTBOMBS : updateFireButtonText();	break;
		case TURN : enterTurn(); break;
		case TURN_EXIT : exitTurn(); break;
		case TURN_EVAL : enterTurnEval(); break;
		//case TURN_EVAL_EXIT :	exitTurnEval();	break;
		case WAIT : enterWait(); break;
		case WAIT_EXIT : exitWait(); break;
		case WAIT_EVAL	: enterWaitEval(); break; 
		//case WAIT_EVAL_EXIT :  exitWaitEval();	break;
		case GAMEOVER : progress();	break;		
		default : break; 
		}
	}
	
	private void enterTurn() {
		updateFireButtonText();
	}
	
	private void exitTurn() {
		ProgressDialog.show(this, "", "Bombing "+ model.getOpponentName()+"'s"+" ships");
	}
	
	private void enterTurnEval() {
		//close progress dialog
		updateButton(inturnokbutton,false, getString(R.string.ingameFireButton_Evaluating)); 
		//Thread will ask for progress
		spawnIncrementalBombThread();
	}
	
	private void exitTurnEval() {
		inturnView.clearDelayedBombs();
		updateButton(inturnokbutton, true, getString(R.string.ingameFireButton_Continue));
	}
	
	private void enterWait() {
		model.playerCompletedWait();
	}
	
	private void exitWait() {
		ProgressDialog.show(this, "", "Waiting for " + model.getOpponentName());
	}
	
	private void enterWaitEval() {
		updateButton(waitokbutton,false, getString(R.string.ingameFireButton_Evaluating));
		spawnIncrementalBombThread();
		//thread will call next state
	}
	
	private void exitWaitEval() {
		waitView.clearDelayedBombs();
		updateButton(waitokbutton,true, getString(R.string.ingameFireButton_Continue));
	}
	
	private void spawnIncrementalBombThread() {
		if (mp != null)
			mp.release();
		
		mp = MediaPlayer.create(this, R.raw.plopp);
		new EvaluateBombsTask().execute(model.requestInTurnClientAcceptedBombs());
	}
	
	// a helper method
	private void updateButton(Button b, boolean enabled, String newtext) {
		b.setEnabled(enabled);
		b.setText(newtext);
	}
	
	// a helper method which updates the text on the firebutton when the player is placing bombs
	private void updateFireButtonText() {
		int remain = model.getRemainingBombs();
		int max = model.getBoard().numLiveShips();
		
		if ( remain < max ) {
			updateButton(inturnokbutton, false, remain+"/"+max);			
		} else {
			updateButton(inturnokbutton, true, getString(R.string.ingameFireButton_Fire));
		}
	}
	
	// move from this activity to the next
	private void progress() {
		model.removeAsObserver(this);
		startActivity(new Intent(this,PostGame.class));
		finish();
	}

}
