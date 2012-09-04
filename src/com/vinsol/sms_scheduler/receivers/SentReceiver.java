/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.receivers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.utils.MyGson;

public class SentReceiver extends BroadcastReceiver{
	
	DBAdapter mdba;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		long smsId = (long)intent.getLongExtra("SMS_ID", 0);
		long recipientId = (long)intent.getLongExtra("RECIPIENT_ID", 0);
		Intent mIntent;
		Log.d("Recipient ID in SentReceiver : " + recipientId);
		Log.d("Sms ID in SentReceiver : " + smsId);
		mdba = new DBAdapter(context);
		
		new HashMap<String, String>();
		
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
   	 			Log.d("Key Operated Value : " + cur.getInt(cur.getColumnIndex(DBAdapter.KEY_OPERATED)));
   	 			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_OPERATED))==0){
   	 				allSent = false;
   	 				break;
   	 			}
   	 		}while(cur.moveToNext());
   	 	}
   	 	Log.d("Are all sent ? " + allSent);
   	 	if(allSent){
   	 		mdba.setStatus(smsId, 2);
   	 		handleRepitition(smsId);
   	 	}
   	 	mdba.close();
	}
	

	
	@SuppressWarnings("unchecked")
	/**
	 * @details checks whether the message to which the current message part belongs has a repeat mode set or not. If so, it replicates the
	 * 			SMS and saves it in the database as a new one. 
	 * @param smsId
	 */
	private void handleRepitition(long smsId){
		mdba.open();
		Cursor smsDetailsCur = mdba.fetchSmsDetails(smsId);
		if(smsDetailsCur.moveToFirst()){
			int repeatMode = smsDetailsCur.getInt(smsDetailsCur.getColumnIndex(DBAdapter.KEY_REPEAT_MODE));
			if(repeatMode>0){
				long previousTimeInMillis = smsDetailsCur.getLong(smsDetailsCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS));
				String hashString = smsDetailsCur.getString(smsDetailsCur.getColumnIndex(DBAdapter.KEY_REPEAT_STRING));
				
				MyGson myGson = new MyGson();
				HashMap<String, Object> repeatHash = myGson.deserializeRepeatHash(hashString);
				long newTimeInMillis = calculateNextScheduleTime(repeatHash, repeatMode, previousTimeInMillis);
				
				
				//if more repeats possible, schedule new sms
				if(newTimeInMillis>0){
					HashMap<String, Object> newRepeatHash = new HashMap<String, Object>();
					try{
						if(repeatMode == 4){
							newRepeatHash.put(Constants.REPEAT_HASH_FREQ, 0);
						}else{
							newRepeatHash.put(Constants.REPEAT_HASH_FREQ, (Integer)repeatHash.get(Constants.REPEAT_HASH_FREQ));
						}
						newRepeatHash.put(Constants.REPEAT_HASH_WEEK_BOOL, (ArrayList<Boolean>)repeatHash.get(Constants.REPEAT_HASH_WEEK_BOOL));
						newRepeatHash.put(Constants.REPEAT_HASH_END_MODE, (Integer)repeatHash.get(Constants.REPEAT_HASH_END_MODE));
						if((Integer)repeatHash.get(Constants.REPEAT_HASH_END_MODE) > 0){
							newRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, (Integer)repeatHash.get(Constants.REPEAT_HASH_END_FREQ) - 1);
						}else{
							newRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, (Integer)repeatHash.get(Constants.REPEAT_HASH_END_FREQ));
						}
						newRepeatHash.put(Constants.REPEAT_HASH_END_DATE, (Date)repeatHash.get(Constants.REPEAT_HASH_END_DATE));
						newRepeatHash.put(Constants.REPEAT_HASH_LAST_SENT_TIME, newTimeInMillis);
					}catch (ClassCastException e) {
						if(repeatMode == 4){
							newRepeatHash.put(Constants.REPEAT_HASH_FREQ, 0);
						}else{
							newRepeatHash.put(Constants.REPEAT_HASH_FREQ, ((Double)repeatHash.get(Constants.REPEAT_HASH_FREQ)).intValue());
						}
						newRepeatHash.put(Constants.REPEAT_HASH_WEEK_BOOL, (ArrayList<Boolean>)repeatHash.get(Constants.REPEAT_HASH_WEEK_BOOL));
						newRepeatHash.put(Constants.REPEAT_HASH_END_MODE, ((Double)repeatHash.get(Constants.REPEAT_HASH_END_MODE)).intValue());
						if(((Double)repeatHash.get(Constants.REPEAT_HASH_END_MODE)).intValue() > 0){
							newRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, ((Double)repeatHash.get(Constants.REPEAT_HASH_END_FREQ)).intValue() - 1);
						}else{
							newRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, ((Double)repeatHash.get(Constants.REPEAT_HASH_END_FREQ)).intValue());
						}
						newRepeatHash.put(Constants.REPEAT_HASH_END_DATE, new Date((String)repeatHash.get(Constants.REPEAT_HASH_END_DATE)));
						newRepeatHash.put(Constants.REPEAT_HASH_LAST_SENT_TIME, newTimeInMillis);
					}
					
					
					String repeatString = myGson.serializeRepeatHash(newRepeatHash);
					
					String message = smsDetailsCur.getString(smsDetailsCur.getColumnIndex(DBAdapter.KEY_MESSAGE));
					SmsManager sm = SmsManager.getDefault();
					int parts = (sm.divideMessage(message)).size();
					
					Date newDate = new Date(newTimeInMillis);
					SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
					String date = sdf.format(newDate);
					
					long newSmsId = mdba.scheduleSms(message, date, parts, newTimeInMillis, repeatMode, repeatString);
					Log.d("new Sms Id : " + newSmsId);
					
					
					//adding recipients...
					Cursor pRecipientsCur = mdba.fetchRecipientsForSms(smsId);
					if(pRecipientsCur.moveToFirst()){
						do{
							String number = pRecipientsCur.getString(pRecipientsCur.getColumnIndex(DBAdapter.KEY_NUMBER));
							String displayName = pRecipientsCur.getString(pRecipientsCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME));
							int type = pRecipientsCur.getInt(pRecipientsCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_TYPE));
							long contactId = pRecipientsCur.getLong(pRecipientsCur.getColumnIndex(DBAdapter.KEY_CONTACT_ID));
							
							mdba.addRecipient(newSmsId, number, displayName, type, contactId);
						}while(pRecipientsCur.moveToNext());
					}
				}
			}
			mdba.removeRepitition(smsId);
			mdba.close();
		}
	}
	
	
	
	/**
	 * @details calculates the Next schedule time based upon the supplied repeatHash, repeadMode and timeInMillis of previous sent.
	 * @param repeatHash
	 * @param repeatMode
	 * @param previousTimeInMillis
	 * @return time in millis for the next scheduling. A '0' value means end of repetition scheme, no more repeats required.
	 */
	private long calculateNextScheduleTime(HashMap<String, Object> repeatHash, int repeatMode, long previousTimeInMillis){
		long time = 0;
		
		switch (repeatMode){
			case 1:
				int days;
				try{
					days = (Integer) repeatHash.get(Constants.REPEAT_HASH_FREQ);
				}catch (ClassCastException e) {
					days = ((Double)repeatHash.get(Constants.REPEAT_HASH_FREQ)).intValue();
				}
				
				time = previousTimeInMillis + (days * 24 * 60 * 60 * 1000);
				break;
			case 2:
				int weekGap;
				try{
					weekGap = (Integer) repeatHash.get(Constants.REPEAT_HASH_FREQ);
				}catch (ClassCastException e) {
					weekGap = ((Double)repeatHash.get(Constants.REPEAT_HASH_FREQ)).intValue();
				}
				@SuppressWarnings("unchecked")
				ArrayList<Boolean> weekBools = (ArrayList<Boolean>)repeatHash.get(Constants.REPEAT_HASH_WEEK_BOOL);
				Date previousDate = new Date(previousTimeInMillis);
				Calendar cal = Calendar.getInstance();
				cal.set(previousDate.getYear()+1900, previousDate.getMonth(), previousDate.getDate(), previousDate.getHours(), previousDate.getMinutes(), previousDate.getSeconds());
				
				
				if((weekBools.get(0) || weekBools.get(1) || weekBools.get(2) || weekBools.get(3) || weekBools.get(4) || weekBools.get(5) || weekBools.get(6))){
					boolean dayNotFound = true;
					while(dayNotFound){
						
						int dow = cal.get(Calendar.DAY_OF_WEEK);
						
						if(dow != Calendar.SATURDAY){
							cal.setTimeInMillis(cal.getTimeInMillis() + (24 * 60 * 60 * 1000));
							
						}else{
							cal.setTimeInMillis(cal.getTimeInMillis() + (((weekGap - 1) * 7) + 1) * 24 * 60 * 60 *1000);
						}
						
						dow = cal.get(Calendar.DAY_OF_WEEK);
						
						if((dow == Calendar.SUNDAY && weekBools.get(0)) || (dow == Calendar.MONDAY && weekBools.get(1))
								|| (dow == Calendar.TUESDAY && weekBools.get(2)) || (dow == Calendar.WEDNESDAY && weekBools.get(3))
								|| (dow == Calendar.THURSDAY && weekBools.get(4)) || (dow == Calendar.FRIDAY && weekBools.get(5))
								|| (dow == Calendar.SATURDAY && weekBools.get(6))){
							
							
							time = cal.getTimeInMillis();
							dayNotFound = false;
							break;
						}
					}
				}else{
					time = 0;
				}
				
				break;
			case 3:
				int monthsGap;
				try{
					monthsGap = (Integer) repeatHash.get(Constants.REPEAT_HASH_FREQ);
				}catch (ClassCastException e) {
					monthsGap = ((Double)repeatHash.get(Constants.REPEAT_HASH_FREQ)).intValue();
				}
				Date previousDateM = new Date(previousTimeInMillis);
				Calendar calM = Calendar.getInstance();
				calM.set(previousDateM.getYear() + 1900, previousDateM.getMonth(), previousDateM.getDate(), previousDateM.getHours(), previousDateM.getMinutes(), previousDateM.getSeconds());
				calM.add(Calendar.MONTH, monthsGap);
				time = calM.getTimeInMillis();
				break;
			case 4:
				Date previousDateY = new Date(previousTimeInMillis);
				Calendar calY = Calendar.getInstance();
				calY.set(previousDateY.getYear()+1901, previousDateY.getMonth(), previousDateY.getDate(), previousDateY.getHours(), previousDateY.getMinutes(), previousDateY.getSeconds());
				time = calY.getTimeInMillis();
				break;
			default:
				time = 0;
				break;
		}
		
		int endMode;
		try{
			endMode = (Integer)repeatHash.get(Constants.REPEAT_HASH_END_MODE);
		}catch (ClassCastException e) {
			endMode = ((Double)repeatHash.get(Constants.REPEAT_HASH_END_MODE)).intValue();
		}
		
		switch (endMode){
			case Constants.END_MODE_NEVER:
				break;
			case Constants.END_MODE_AFTER:
				int endFreq;
				try{
					endFreq = (Integer)repeatHash.get(Constants.REPEAT_HASH_END_FREQ);
				}catch (ClassCastException e) {
					endFreq = ((Double)repeatHash.get(Constants.REPEAT_HASH_END_FREQ)).intValue();
				}
				
				if(endFreq<2){
					time = 0;
				}
				break;
			case Constants.END_MODE_ON:
				Date d;
				try{
					d = (Date)repeatHash.get(Constants.REPEAT_HASH_END_DATE);
				}catch (ClassCastException e) {
					d = new Date((String)repeatHash.get(Constants.REPEAT_HASH_END_DATE));
				}
				
				if(time> d.getTime()){
					time = 0;
				}
				break;
			default:
				break;
		}
		
		Log.d("Time : " + time);
		return time;
	}
}