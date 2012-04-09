/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.receivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.telephony.SmsManager;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.utils.Log;



public class SMSHandleReceiver extends BroadcastReceiver{
	
	private String message;
	private String number;
	private long smsId;
	private long recipientId;
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts;
	int msgSize;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		message = intent.getStringExtra("MESSAGE");
		number = intent.getStringExtra("NUMBER");
		smsId = intent.getLongExtra("SMS_ID", 0);
		recipientId = intent.getLongExtra("RECIPIENT_ID", 0);
		DBAdapter mdba = new DBAdapter(context);
		
		
		
		mdba.open();
		mdba.makeOperated(recipientId);
		mdba.setStatus(smsId, 3);
		
		parts = smsManager.divideMessage(message);
		msgSize = parts.size();
		
		FlurryAgent.onStartSession(context, context.getResources().getString(R.string.flurry_key));
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Message Size", msgSize+"");
		FlurryAgent.logEvent("Sending Message", params);
		FlurryAgent.onEndSession(context);
		
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
			isent.putExtra("SMS_ID", smsId);
			isent.putExtra("RECIPIENT_ID", recipientId);
			isent.setAction(Constants.PRIVATE_SMS_ACTION + recipientId);
			pisent = PendingIntent.getBroadcast(context, 0, isent, PendingIntent.FLAG_UPDATE_CURRENT);
			sentIntents.add(pisent);
			
			Intent ideliver = new Intent(context, DeliveryReceiver.class);
			ideliver.putExtra("PART", i);
			ideliver.putExtra("SIZE", msgSize);
			ideliver.putExtra("MESSAGE", message);
			ideliver.putExtra("NUMBER", number);
			ideliver.putExtra("SMS_ID", smsId);
			ideliver.putExtra("RECIPIENT_ID", recipientId);
			ideliver.setAction(Constants.PRIVATE_SMS_ACTION + recipientId);
			pideliver = PendingIntent.getBroadcast(context, 0, ideliver, PendingIntent.FLAG_UPDATE_CURRENT);
			deliverIntents.add(pideliver);
		}
		try{
			smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, deliverIntents);
		}catch(IllegalArgumentException iae){}
		
		mdba.open();
		Cursor cur = mdba.fetchNextScheduled();
		mdba.close();
		if(cur.moveToFirst()){
			Log.d("more records");
			intent = new Intent(context, SMSHandleReceiver.class);
			intent.setAction(Constants.PRIVATE_SMS_ACTION);
			intent.putExtra("SMS_ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)));
			intent.putExtra("RECIPIENT_ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)));
			intent.putExtra("NUMBER", cur.getString(cur.getColumnIndex(DBAdapter.KEY_NUMBER)));
			intent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(DBAdapter.KEY_MESSAGE)));
			
			Random rand = new Random();
			int piNumber = rand.nextInt();
			PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mdba.open();
			mdba.updatePi(piNumber, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)), cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)));
			mdba.close();
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.set(AlarmManager.RTC_WAKEUP, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)), pi);
		}else{
			Log.d("no more records retrieved");
			mdba.open();
			mdba.updatePi(0, -1, -1);
			try{
				mdba.close();
			}catch (SQLiteException e) {
				// TODO: handle exception
			}
		}
	}
}