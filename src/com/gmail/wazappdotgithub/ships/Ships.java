package com.gmail.wazappdotgithub.ships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Ships extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent intent = new Intent(this, PreGame.class);
        startActivity(intent);
    }
}