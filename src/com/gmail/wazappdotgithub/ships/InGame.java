package com.gmail.wazappdotgithub.ships;

import com.gmail.wazappdotgithub.ships.model.Game;
import com.gmail.wazappdotgithub.ships.model.views.BoardView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ViewFlipper;

/**
 * A view which will be used while the game turns are carried out
 * will switch between two views 
 * @author tor
 *
 */
public class InGame extends Activity implements OnClickListener {

	private String tag = "InGame";
	ViewFlipper vf;
	View waitokbutton,inturnokbutton;
	BoardView waitView,inturnView;
	
	Animation out = new RotateAnimation(0f, 90f);
	Animation in = new RotateAnimation(90f, 0f); // TODO just testing the pivots(f,f,f,f)
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag,tag + " initiating");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ingame);
		in.setStartTime(Animation.START_ON_FIRST_FRAME);
		in.setDuration(1000);
		
		out.setStartTime(Animation.START_ON_FIRST_FRAME);
		out.setDuration(1000);
		
		Log.d(tag,tag + " animations completed");
		vf = (ViewFlipper) findViewById(R.id.ingame_flipper);
		Log.d(tag,tag + " viewcompleted");
		waitokbutton = findViewById(R.id.ingame_wait_okbutton);
		inturnokbutton = findViewById(R.id.ingame_inturn_okbutton);
		Log.d(tag,tag + " buttons completed");
		
		inturnView = (BoardView) findViewById(R.id.ingame_inturn_boardview);
		waitView = (BoardView) findViewById(R.id.ingame_wait_boardview);
		Log.d(tag,tag + " views completed");
		
		waitokbutton.setOnClickListener(this);
		inturnokbutton.setOnClickListener(this);
		
		Log.d(tag,tag + " onclick listeners completed");
		vf.setInAnimation(in);
		vf.setOutAnimation(out);
	}

	@Override
	public void onClick(View v) {
		if ( v == waitokbutton )
			vf.showNext();
		else if ( v == inturnokbutton ) {
			Game.getConfiguredInstance().getLocalClient().reportAcceptBombs();
			vf.invalidate();
			vf.showNext();
		}
	}

}
