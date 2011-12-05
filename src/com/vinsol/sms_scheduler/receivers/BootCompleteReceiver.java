package com.vinsol.sms_scheduler.receivers;

import java.util.Random;

import com.vinsol.sms_scheduler.DBAdapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;



public class BootCompleteReceiver extends BroadcastReceiver{

	
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("MSG", "boot receiver triggered");
		mdba = new DBAdapter(context);
		mdba.open();
		Cursor cur = mdba.fetchRemainingScheduled();
		mdba.close();
		
		if(cur.moveToFirst()){
			Log.i("MESSAGE", "there are other records too");
			Intent nextIntent = new Intent(context, SMSHandleReceiver.class);
			
			nextIntent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)));
			nextIntent.putExtra("NUMBER", cur.getString(cur.getColumnIndex(DBAdapter.KEY_NUMBER)));
			nextIntent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(DBAdapter.KEY_MESSAGE)));
//			int piNumber = (int)Math.random()*100;
			Random rand = new Random();
			int piNumber = rand.nextInt();
			Log.i("MESSAGE", "Pi Number : " + piNumber);
			PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mdba.open();
			mdba.updatePi(piNumber, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)), cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)));
			mdba.close();
			
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
			if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)) > System.currentTimeMillis()){
				alarmManager.set(AlarmManager.RTC_WAKEUP, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)), pi);
			}else{
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 180000, pi);
			}
	    	//Toast.makeText(context, "Message Scheduled", Toast.LENGTH_SHORT).show();
		}else{
			Log.i("MESSAGE", "there are no records, pi to be set to default");
			mdba.open();
			mdba.updatePi(0, -1, -1);
			mdba.close();
		}
	}

	
	
}
