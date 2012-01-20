package com.vinsol.sms_scheduler.receivers;

import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.utils.Log;



public class BootCompleteReceiver extends BroadcastReceiver{

	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("boot receiver triggered");
		mdba = new DBAdapter(context);
		mdba.open();
		Cursor cur = mdba.fetchNextScheduled();
		if(cur.moveToFirst()){
			intent = new Intent(context, SMSHandleReceiver.class);
			intent.setAction(Constants.PRIVATE_SMS_ACTION);
			intent.putExtra("SMS_ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)));
			intent.putExtra("RECIPIENT_ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)));
			intent.putExtra("NUMBER", cur.getString(cur.getColumnIndex(DBAdapter.KEY_NUMBER)));
			intent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(DBAdapter.KEY_MESSAGE)));
			
			Random rand = new Random();
			int piNumber = rand.nextInt();
			PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			mdba.updatePi(piNumber, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)), cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)));
			
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)) > System.currentTimeMillis()){
				alarmManager.set(AlarmManager.RTC_WAKEUP, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)), pi);
			}else{
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 180000, pi);
			}
		}else{
			mdba.updatePi(0, -1, -1);
		}
		mdba.close();
	}
}