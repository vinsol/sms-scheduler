package com.vinsol.sms_scheduler.receivers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.utils.Log;

public class DeliveryReceiver extends BroadcastReceiver{

	boolean successdeliver = true;
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int msgSize = intent.getIntExtra("SIZE", 0);
		int part = (int)intent.getIntExtra("PART", 0);
		String number = intent.getStringExtra("NUMBER");
		long smsId = intent.getLongExtra("SMS_ID", 0);
		long recipientId = intent.getLongExtra("RECIPIENT_ID", 0);
		Log.d("Recipient ID in DeliverReceiver : " + recipientId);
		Log.d("Sms ID in DeliverReceiver : " + smsId);
		mdba = new DBAdapter(context);
		
		switch (getResultCode())
        {
            case Activity.RESULT_OK:

            	mdba.open();
            	mdba.increaseDeliver(recipientId);
            	mdba.close();
            	
            	if(part==msgSize){
            		mdba.open();
//            		Cursor cur = mdba.fetchRecipientDetails(recipientId);
//               	 	cur.moveToFirst();
//               	 	String receiverName = cur.getString(cur.getColumnIndex(DBAdapter.KEY_SPAN_DN));          		
            		Intent mIntent = new Intent();
                    mIntent.setAction(context.getResources().getString(R.string.update_action));
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            		
            		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
            		
            		mdba.close();
            	}
            	
            	
            	
                break;
            case Activity.RESULT_CANCELED:
            	
                break;                        
        }
	}

	
}
