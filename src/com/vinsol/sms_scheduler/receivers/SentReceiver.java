/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.utils.Log;

public class SentReceiver extends BroadcastReceiver{
	
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		FlurryAgent.onStartSession(context, context.getApplicationContext().getResources().getString(R.string.flurry_key_test));
		long smsId = (long)intent.getLongExtra("SMS_ID", 0);
		long recipientId = (long)intent.getLongExtra("RECIPIENT_ID", 0);
		Intent mIntent;
		Log.d("Recipient ID in SentReceiver : " + recipientId);
		Log.d("Sms ID in SentReceiver : " + smsId);
		mdba = new DBAdapter(context);
		switch (getResultCode())
         { 	
             case Activity.RESULT_OK:
            	 mdba.open();
                 mdba.increaseSent(smsId, recipientId);
                 mdba.close();
                 break;
                 
             case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                 break;
                 
             case SmsManager.RESULT_ERROR_NO_SERVICE:
         		 break;
                 
             case SmsManager.RESULT_ERROR_NULL_PDU:
                 break;
                 
             case SmsManager.RESULT_ERROR_RADIO_OFF:
                 break;
         }
		mIntent = new Intent();
        mIntent.putExtra("RECIPIENT_ID", recipientId);
        mIntent.setAction(context.getResources().getString(R.string.update_action));
        context.sendBroadcast(mIntent);
       
		mdba.open();
		Cursor cur = mdba.fetchRecipientsForSms(smsId);
   	 	boolean allSent = true;
   	 	if(cur.moveToFirst()){
   	 		do{
   	 			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_OPERATED))==0){
   	 				allSent = false;
   	 				break;
   	 			}
   	 		}while(cur.moveToNext());
   	 	}
   	 	if(allSent){
   		 mdba.setStatus(smsId, 2); 
   	 	}
   	 	mdba.close();
	}
}