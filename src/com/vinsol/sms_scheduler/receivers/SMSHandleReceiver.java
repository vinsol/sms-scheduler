package com.vinsol.sms_scheduler.receivers;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;
import com.vinsol.sms_scheduler.activities.ScheduleNewSms;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.utils.Log;



public class SMSHandleReceiver extends BroadcastReceiver{
	
	private String message;
	private String number;
	private long id;
	private Context mContext;

	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts;
	int msgSize;
	
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		message = intent.getStringExtra("MESSAGE");
		number = intent.getStringExtra("NUMBER");
		id = intent.getLongExtra("ID", 0);
		DBAdapter mdba = new DBAdapter(context);
		
		mdba.open();
		mdba.makeOperated(id);
		
		parts = smsManager.divideMessage(message);
		msgSize = parts.size();
		
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliverIntents = new ArrayList<PendingIntent>();
		
		PendingIntent pisent;
		PendingIntent pideliver;
		
		for(int i = 1; i<= msgSize; i++){
			Intent isent = new Intent(context, SentReceiver.class);
			isent.putExtra("PART", i);
			isent.putExtra("SIZE", msgSize);
			isent.putExtra("MESSAGE", message);
			isent.putExtra("NUMBER", number);
			isent.putExtra("ID", id);
			isent.setAction(Constants.PRIVATE_SMS_ACTION + id);
			pisent = PendingIntent.getBroadcast(context, 0, isent, PendingIntent.FLAG_UPDATE_CURRENT);
			sentIntents.add(pisent);
			
			Intent ideliver = new Intent(context, DeliveryReceiver.class);
			ideliver.putExtra("PART", i);
			ideliver.putExtra("SIZE", msgSize);
			ideliver.putExtra("MESSAGE", message);
			ideliver.putExtra("NUMBER", number);
			ideliver.putExtra("ID", id);
			ideliver.setAction(Constants.PRIVATE_SMS_ACTION + id);
			pideliver = PendingIntent.getBroadcast(context, 0, ideliver, PendingIntent.FLAG_UPDATE_CURRENT);
			deliverIntents.add(pideliver);
		}
		try{
			smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, deliverIntents);
		}catch(IllegalArgumentException iae){
			
		}
		mdba.open();
		mdba.makeOperated(id);
		Cursor cur = mdba.fetchRemainingScheduled();
		mdba.close();
		
		if(cur.moveToFirst()){
			Intent nextIntent = new Intent(context, SMSHandleReceiver.class);
			
			nextIntent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)));
			nextIntent.putExtra("NUMBER", cur.getString(cur.getColumnIndex(DBAdapter.KEY_NUMBER)));
			nextIntent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(DBAdapter.KEY_MESSAGE)));

			Random rand = new Random();
			int piNumber = rand.nextInt();
			PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mdba.open();
			mdba.updatePi(piNumber, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)), cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)));
			mdba.close();
			
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(context.ALARM_SERVICE);
			if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)) > System.currentTimeMillis()){
				alarmManager.set(AlarmManager.RTC_WAKEUP, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)), pi);
			}else{
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pi);
			}
		}else{
			mdba.open();
			mdba.updatePi(0, -1, -1);
			mdba.close();
		}
	}
}