package com.gmail.wazappdotgithub.ships;

import com.gmail.wazappdotgithub.ships.R.id;
import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.EndGameData;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;


public class PostGame extends Activity {
	private String tag = "Ships PostGame ";
	EndGameData clientData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ALog.d(tag, "initiating");
		setContentView(R.layout.postgame);
		
		clientData = RemoteClient.getInstance().retrieveEndGameData();
		
		//TODO Dialog says Congratulations even if loosing
		TextView tv = (TextView) findViewById(id.postgameTextView);
		if ( clientData != null ) {
			StringBuffer sb = new StringBuffer();
			sb.append(getString(R.string.postgameWinnerGreet) + "\n");
			sb.append(getString(R.string.postgameDataShots) + " ");
			sb.append(clientData.bombsShot +"\n");
			sb.append(getString(R.string.postgameDataShips) + " ");
			sb.append(clientData.liveShips + "\n");
			sb.append("Your Score: " + clientData.score + "\n");
			sb.append(clientData.r_name + "'s Score: " + clientData.r_score);
			tv.setText(sb.toString());
		} else {
			tv.setText("clientData was null, it aint good");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//startActivity(new Intent(this,UserInput.class));
		// Want to go to either pregame or finish depending on
		// if we shall spawn a new game or not

		finish();
		return true;
	}
}
