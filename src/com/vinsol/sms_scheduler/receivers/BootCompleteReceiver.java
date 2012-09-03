/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

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


/**
 * @details Fires up when the Devices completes booting up. It sets the pending intent at that time.
 */
public class BootCompleteReceiver extends BroadcastReceiver{

	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
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
				//When a devices boots up and this broadcast receiver fires up, there's a delay for the device to get functional as in
				//network and services. So we set the Pending Intent with a delay of 3 minutes ~ 180000 milliseconds.
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 180000, pi);
			}
		}else{
			mdba.updatePiForNoSmsValue();
		}
		mdba.close();
	}
}