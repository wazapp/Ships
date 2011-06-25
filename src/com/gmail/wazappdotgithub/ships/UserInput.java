package com.gmail.wazappdotgithub.ships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserInput extends Activity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.userinput);
        
        
        final Button letsContinueButton = (Button) findViewById(R.id.button1);
        letsContinueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
                // Perform action on click
            	Intent intent2= new Intent(UserInput.this, PreGame.class);
		        
		        startActivity(intent2);	
            }
        });

        final Button fleeHome = (Button) findViewById(R.id.button2);
        fleeHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
                finish();
                
            }
        });
             
	}

}
