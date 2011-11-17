package com.smsschedulerexpl.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SentReceiver extends BroadcastReceiver{
	
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int part = (int)intent.getIntExtra("PART", 0);
		long id = (long)intent.getLongExtra("ID", 0);
		int msgSize = (int)intent.getIntExtra("SIZE", 0);
		String number = intent.getStringExtra("NUMBER");
		Log.i("MESSAGE", "ID in SentReceiver : " + id);
		mdba = new DBAdapter(context);
		switch (getResultCode())
         { 	
			
             case Activity.RESULT_OK:
                 Toast.makeText(context, "Part " + part + "/" + msgSize + " sent to " + number, Toast.LENGTH_SHORT).show();
                 
                 mdba.open();
                 mdba.increaseSent(id);
                 mdba.close();
                 
                 
                 Intent mIntent = new Intent();
                 mIntent.putExtra("ID", id);
                 mIntent.setAction("My special action");
                 PendingIntent pi = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         		
         		 AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
         		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
         		 
//         		 mIntent.setAction("update action");
//         		 pi = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
        		 
                 
                 break;
             case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                 Toast.makeText(context, "Generic failure", 
                         Toast.LENGTH_SHORT).show();
                 break;
             case SmsManager.RESULT_ERROR_NO_SERVICE:
                 Toast.makeText(context, "No service", 
                         Toast.LENGTH_SHORT).show();
                 break;
             case SmsManager.RESULT_ERROR_NULL_PDU:
                 Toast.makeText(context, "Null PDU", 
                         Toast.LENGTH_SHORT).show();
                 break;
             case SmsManager.RESULT_ERROR_RADIO_OFF:
                 Toast.makeText(context, "Radio off", 
                         Toast.LENGTH_SHORT).show();
                 break;
         }
	}

	
}
