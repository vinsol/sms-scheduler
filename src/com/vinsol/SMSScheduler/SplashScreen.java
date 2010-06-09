package com.vinsol.SMSScheduler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends Activity {
    /**================================================================
     *  method onCreate
     *  Called when the activity is first created. 
     * =============================================================== */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        
        Intent intent = new Intent(this, ScheduleSMS.class);
        finish();
        startActivity(intent);
        
    }//end method onCreate
}