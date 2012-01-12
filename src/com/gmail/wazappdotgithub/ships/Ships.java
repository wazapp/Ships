package com.gmail.wazappdotgithub.ships;

import com.gmail.wazappdotgithub.ships.common.ALog;
import com.gmail.wazappdotgithub.ships.common.AndroidLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



public class Ships extends Activity {
	
	private static final String tag ="Ships ";
	protected boolean _active = true;
	protected int _splashTime = 2000;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activate non Android dependent logging to use Android logger
        ALog.setLogger(new AndroidLog());
        ALog.d(tag, "Activated logging");
        
        setContentView(R.layout.main);
        
        /* Will make the splashscreen show for a few sec */
        
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(1200);
                        if(_active) {
                            waited += 1200;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    Intent intent = new Intent(Ships.this, UserInput.class);
                    startActivity(intent);
                    finish();
                    
                }
            }
        };
        splashTread.start();
    }
}