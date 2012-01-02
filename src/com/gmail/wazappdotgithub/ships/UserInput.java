package com.gmail.wazappdotgithub.ships;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com.gmail.wazappdotgithub.ships.common.Protocol;
import com.gmail.wazappdotgithub.ships.model.Client.IShipsClient.Statename;
import com.gmail.wazappdotgithub.ships.common.Protocol.opponentType;
import com.gmail.wazappdotgithub.ships.model.Client.RemoteClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserInput extends Activity implements Observer{
	
	Intent nextActivity;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.userinput);
        
        final Button letsContinueButton = (Button) findViewById(R.id.button1);
        letsContinueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	try {
					Protocol.newInstance(opponentType.REMOTECOMPUTER);
					RemoteClient.newInstance(UserInput.this);
					
				} catch (IOException e) {
					e.printStackTrace();
					AlertDialog.Builder builder = new AlertDialog.Builder(UserInput.this);
					
					builder.setMessage("An error occurred establishing the connection")
					       .setCancelable(false)
					       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       })
					       .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   UserInput.this.finish();
					           }
					       });
					
					AlertDialog alert = builder.create();
					//alert.show() ??
				}
            }
        });

        final Button fleeHome = (Button) findViewById(R.id.button2);
        fleeHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                
            }
        });
             
	}

	@Override
	public void update(Observable observable, Object data) {
		Statename s = ((Statename) data);
		if (s == Statename.PREGAME) {
			RemoteClient.getInstance().removeAsObserver(this);
			startActivity(new Intent(UserInput.this, PreGame.class));
			finish();
		}
	}

}
