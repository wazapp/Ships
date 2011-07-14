package com.gmail.wazappdotgithub.ships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



public class Ships extends Activity {
	
	protected boolean _active = true;
	protected int _splashTime = 2000;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        /* Will make the splashscreen show for a few sec */
        
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    
                    Intent intent = new Intent(Ships.this, UserInput.class);
                
                    startActivity(intent);
                    stop();
                }
            }
        };
        splashTread.start();
        
    }
}