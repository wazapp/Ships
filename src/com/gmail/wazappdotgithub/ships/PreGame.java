package com.gmail.wazappdotgithub.ships;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public final class PreGame extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pregame);
		
		
		View rotateButton = findViewById(R.id.pregame_button_rotate);
		rotateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				;
			}
		});
	}

	
	
}
