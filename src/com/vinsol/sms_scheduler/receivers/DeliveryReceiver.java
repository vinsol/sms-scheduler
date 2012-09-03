/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;

public class DeliveryReceiver extends BroadcastReceiver{

	boolean successdeliver = true;
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int msgSize = intent.getIntExtra("SIZE", 0);
		int part = (int)intent.getIntExtra("PART", 0);
//		long smsId = intent.getLongExtra("SMS_ID", 0);
		long recipientId = intent.getLongExtra("RECIPIENT_ID", 0);
		mdba = new DBAdapter(context);
		
		switch (getResultCode())
        {
            case Activity.RESULT_OK:
            	mdba.open();
            	mdba.increaseDeliver(recipientId);
            	mdba.close();
            	
            	if(part==msgSize){
            		mdba.open();        		
            		Intent mIntent = new Intent();
                    mIntent.setAction(context.getResources().getString(R.string.update_action));
                    context.sendBroadcast(mIntent);
            		mdba.close();
            	}
                break;
                
            case Activity.RESULT_CANCELED:
                break;                        
        }
	}
}