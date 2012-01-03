package com.gmail.wazappdotgithub.ships;

import com.gmail.wazappdotgithub.ships.R.id;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.EndGameData;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;


public class PostGame extends Activity {
	private String tag = "Ships PostGame ";
	EndGameData clientData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(tag,tag + " initiating");
		setContentView(R.layout.postgame);
		
		clientData = RemoteClient.getInstance().retrieveEndGameData();
		
		//TODO Dialog says Congratualtions even if loosing
		TextView tv = (TextView) findViewById(id.postgameTextView);
		if ( clientData != null ) {
			StringBuffer sb = new StringBuffer();
			sb.append(getString(R.string.postgameWinnerGreet));
			sb.append("\n");
			sb.append(getString(R.string.postgameDataShots));
			sb.append(" ");
			sb.append(clientData.bombsShot);
			sb.append("\n");
			sb.append(getString(R.string.postgameDataShips));
			sb.append(" ");
			sb.append(clientData.liveShips);

			tv.setText(sb.toString());
		} else {
			tv.setText("clientData was null, it aint good");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//startActivity(new Intent(this,UserInput.class));
		finish();
		return true;
	}
}
