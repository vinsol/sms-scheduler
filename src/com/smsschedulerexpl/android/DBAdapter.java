package com.smsschedulerexpl.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;



public class DBAdapter{
	private static final String DATABASE_NAME = "smsDatabase";
	private static final String DATABASE_SMS_TABLE = "smsTable";
	private static final String DATABASE_PI_TABLE = "piTable";
	private static final String DATABASE_TEMPLATE_TABLE = "templateTable";
	private static final int DATABASE_VERSION = 1;
	
	Cursor cur;
	public static final String PRIVATE_SMS_ACTION = "com.smsschedulerexpl.android.private_sms_action";
	public static final String PRIVATE_INTENT_ACTION = "com.smsschedulerexpl.android.private_intent_action";
	
	
	//---------------------------static keys for columns---------------------------------------
	
	//----------------keys for SMS table--------------------------
	public static final String KEY_ID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_GRPID = "group_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATE = "date";
	public static final String KEY_TIME_MILLIS = "time_millis";
	public static final String KEY_SENT = "sent";
	public static final String KEY_DELIVER = "deliver";
	public static final String KEY_MSG_PARTS = "msg_parts";
	public static final String KEY_S_MILLIS = "sent_milis";
	public static final String KEY_D_MILLIS = "deliver_milis";
	public static final String KEY_OPERATED = "operation_done";
	
	//--------------keys for PI table---------------------------
	public static final String KEY_PI_ID = "_id";
	public static final String KEY_PI_NUMBER = "pi_number";
	public static final String KEY_SMS_ID = "sms_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_ACTION = "action";
	
	//----------------keys for template table---------------------
	public static final String KEY_TEMP_ID = "_id";
	public static final String KEY_TEMP_CONTENT = "content";
	
	//------------------------------------------------------------------end of static keys defs-------
	
	
	
	//SQL to open or create a database
	private static final String DATABASE_CREATE_SMS_TABLE = "create table " + 
		DATABASE_SMS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " + KEY_GRPID + " integer, " + 
		KEY_NUMBER + " text not null, " + KEY_MESSAGE + " text, " + KEY_DATE + " text, " + KEY_TIME_MILLIS + " long, " 
		+ KEY_SENT + " integer default 0, "+ KEY_DELIVER + " integer default 0, " + KEY_MSG_PARTS + " integer default 0, " 
		+ KEY_S_MILLIS + " integer, " + KEY_D_MILLIS + " integer, " + KEY_OPERATED + " integer default 0);";
	
	
	private static final String DATABASE_CREATE_PI_TABLE = "create table " +
		DATABASE_PI_TABLE + " (" + KEY_PI_ID + " integer primary key, " + KEY_PI_NUMBER + " integer, " +
		KEY_SMS_ID + " integer, " + KEY_TIME + " integer, " + KEY_ACTION + " text);";
	
	
	private static final String DATABASE_CREATE_TEMPLATE_TABLE = "create table " +
		DATABASE_TEMPLATE_TABLE + " (" + KEY_TEMP_ID + " integer primary key autoincrement, " +
		KEY_TEMP_CONTENT + " text);";
	
	
	
	private SQLiteDatabase db;
	private final Context context;
	private MyOpenHelper myDbHelper;
	
	public DBAdapter(Context _context){
		context = _context;
		myDbHelper = new MyOpenHelper(context);
	}
	
	public DBAdapter open() throws SQLException{
		db = myDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		db.close();
	}

	
	// functions--------------------------------------------------------------------
	
	public Cursor fetchAllScheduled(){
		
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_TIME_MILLIS, KEY_DATE, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS}, KEY_SENT + "= 0", null, null, null, KEY_TIME_MILLIS);
		Log.i("MESSAGE", "No of schedules from DBAdapter : " + cur.getCount());
		return cur;
	}
	
	public Cursor fetchAllSent(){
		Cursor cur  = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_DATE, KEY_TIME_MILLIS, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS, KEY_S_MILLIS, KEY_D_MILLIS}, KEY_SENT + ">0", null, null, null, KEY_TIME_MILLIS);
		Log.i("MESSAGE", "No of sents from DBAdapter : " + cur.getCount());
		return cur;
	}
	
	public Cursor fetchRemainingScheduled(){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_TIME_MILLIS, KEY_DATE, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS}, KEY_SENT + "= 0 AND " + KEY_OPERATED + "<> 1 AND " + KEY_MESSAGE + " NOT LIKE ' '" , null, null, null, KEY_TIME_MILLIS);
		Log.i("MESSAGE", "No of other schedules from DBAdapter : " + cur.getCount());
		return cur;
	}
	
	public Cursor fetchSmsDetails(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_DATE, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
		return cur;
	}
	
	public long scheduleSms(String number, String content, String date, int parts, long group_id, long timeInMilis){
		
		ContentValues addValues = new ContentValues();
		
		addValues.put(KEY_NUMBER, number);
		addValues.put(KEY_MESSAGE, content);
		addValues.put(KEY_DATE, date);
		addValues.put(KEY_TIME_MILLIS, timeInMilis);
		addValues.put(KEY_SENT, 0);
		addValues.put(KEY_DELIVER, 0);
		addValues.put(KEY_MSG_PARTS, parts);
		addValues.put(KEY_GRPID, group_id);
		addValues.put(KEY_S_MILLIS, -1);
		addValues.put(KEY_D_MILLIS, -1);
		
		return	db.insert(DATABASE_SMS_TABLE, null, addValues);
	}
	
	
	
	
	
	
	
	public int getNextGroupId(){
		cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_GRPID}, null, null, null, null, null);
		if(cur.moveToFirst()){
			cur.moveToLast();
			return cur.getInt(cur.getColumnIndex(KEY_GRPID)) + 1;
		}else{
			return 0;
		}
	}
	
	
	
	
	
	
	
	
//	public boolean editSms(int grpid, String number, String content, String date, int parts, Context _context){
//		
//		try{
//			removeGroup(grpid, _context);
//			String numbers[] = number.split(",");
//			for(int i = 0; i < number.length(); i++){
//				long id = scheduleSms(numbers[i], content, date, parts, grpid);
//				putOnBroadcast(id, number, _context, date, content);
//			}
//	}
//	
		
		
	public void makeOperated(long id){
		ContentValues cv = new ContentValues();
		cv.put(KEY_OPERATED, 1);
		db.update(DATABASE_SMS_TABLE, cv, KEY_ID + "=" + id, null);
	}
	
	
		
		
		
	public int getSent(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_SENT}, KEY_ID + "=" + id, null, null, null, null);
		if(cur.moveToFirst())
			return cur.getInt(cur.getColumnIndex(KEY_SENT));
		else 
			return 0;
	}
	
	public boolean increaseSent(long id){
		int sent = getSent(id);
		ContentValues sentValue = new ContentValues();
		sentValue.put(KEY_SENT, sent + 1);
		try{
			db.update(DATABASE_SMS_TABLE, sentValue, KEY_ID + "=" + id, null);
			Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_SENT}, KEY_ID + "=" + id, null, null, null, null);
			
			int sentval = 0;
			
			if(cur.moveToFirst()){
				sentval = cur.getInt(cur.getColumnIndex("sent"));
				Log.i("MESSAGE", "sent value : " + String.valueOf(sentval));
			}
			
			cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
			cur.moveToFirst();
			int parts = cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS));
			if (sentval == parts){
				ContentValues sentTimeSaver = new ContentValues();
				sentTimeSaver.put(KEY_S_MILLIS, System.currentTimeMillis());
				db.update(DATABASE_SMS_TABLE, sentTimeSaver, KEY_ID + "=" + id, null);
			}
			
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	
	
	
	public boolean checkSent(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_SENT, KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
		return ((cur.getInt(cur.getColumnIndex(KEY_SENT)) == cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS))));
	}
	
	
	
	
	
	
	public boolean removeAllDelivered(){
		try{
			db.delete(DATABASE_SMS_TABLE, KEY_DELIVER + "=" + KEY_MSG_PARTS, null);
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	
	
	
	public ArrayList<Long> getIds(Long grp){
		ArrayList<Long> ids = new ArrayList<Long>();
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID}, KEY_GRPID + "=" + grp, null, null, null, null);
		if(cur.moveToFirst()){
			do{
				ids.add(cur.getLong(cur.getColumnIndex(KEY_ID)));
			}while(cur.moveToNext());
		}
		return ids;
	}
	
	
	
	
	
	public void deleteSms(long id, Context context){
		Log.i("MESSAGE", "==================== From Deletesms().....Id :" + id);
		Log.i("MESSAGE", "==================== From Deletesms().....Id in PiTable :" + getCurrentPiId());
		if(getCurrentPiId()==id){
			Log.i("MESSAGE", "==================== From Deletesms()");
			Cursor cur = getPiDetails();
			cur.moveToFirst();
			
			Intent intent = new Intent(context, SMSHandleReceiver.class);
			intent.setAction(PRIVATE_SMS_ACTION);
			PendingIntent pi = PendingIntent.getBroadcast(context, cur.getInt(cur.getColumnIndex(KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
			pi.cancel();
			db.delete(DATABASE_SMS_TABLE, KEY_ID + "=" + id, null);
			Cursor cur2 = fetchRemainingScheduled();
			if(cur2.moveToFirst()){
				Log.i("MESSAGE", "==================== From Deletesms().....more msgs found");
				intent.setAction(PRIVATE_SMS_ACTION);
				intent.putExtra("ID", cur2.getString(cur2.getColumnIndex(KEY_ID)));
				intent.putExtra("NUMBER", cur2.getString(cur2.getColumnIndex(KEY_NUMBER)));
				intent.putExtra("MESSAGE", cur2.getString(cur2.getColumnIndex(KEY_MESSAGE)));
				
				Random rand = new Random();
				int piNumber = rand.nextInt();
				pi = PendingIntent.getBroadcast(context, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				updatePi(piNumber, cur2.getLong(cur2.getColumnIndex(KEY_GRPID)), cur2.getLong(cur2.getColumnIndex(KEY_TIME_MILLIS)));
				
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		    	alarmManager.set(AlarmManager.RTC_WAKEUP, cur2.getLong(cur2.getColumnIndex(KEY_TIME_MILLIS)), pi);
			}else{
				Log.i("MESSAGE", "==================== From Deletesms()..... no more msgs found");
				updatePi(0, -1, -1);
			}
			
			
		}else{
			db.delete(DATABASE_SMS_TABLE, KEY_ID + "=" + id, null);
		}
		
	}
	
	
	
	
	
	public boolean removeGroup(int grp, Context _context){
		try{
			Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_NUMBER}, KEY_GRPID + "=" + grp, null, null, null, null);
			Cursor cur2 = db.query(DATABASE_PI_TABLE, null, KEY_PI_ID + "= 1", null, null, null, null);
			cur2.moveToFirst();
			if(cur2.getLong(cur2.getColumnIndex(DBAdapter.KEY_TIME))>0){
				long setId = cur2.getLong(cur2.getColumnIndex(KEY_SMS_ID));
				if(cur.moveToFirst()){
					do{
						if(setId == cur.getLong(cur.getColumnIndex(KEY_ID))){
							Intent cancelIntent = new Intent(_context, SMSHandleReceiver.class);
							cancelIntent.setAction(PRIVATE_SMS_ACTION);
							PendingIntent cancelPi = PendingIntent.getBroadcast(_context, (int)cur.getLong(cur.getColumnIndex(KEY_ID)), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
							cancelPi.cancel();
						}
					}while(cur.moveToNext());
				}
				cur = fetchAllScheduled();
				if(cur.moveToFirst()){
					Intent nextIntent = new Intent(context, SMSHandleReceiver.class);
					
					nextIntent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)));
					nextIntent.putExtra("NUMBER", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_NUMBER)));
					nextIntent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(DBAdapter.KEY_MESSAGE)));
					int piNumber = (int)Math.random()*10000;
					PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					updatePi(piNumber, cur.getLong(cur.getColumnIndex(DBAdapter.KEY_ID)), cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)));
					
				}else{
					updatePi(0, -1, -1);
				}
			}
			
			db.delete(DATABASE_SMS_TABLE, KEY_GRPID + "=" + grp, null);
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	
	
	private int getDelivers(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_DELIVER}, KEY_ID + "=" + id, null, null, null, null);
		if(cur.moveToFirst())
			return cur.getInt(cur.getColumnIndex(KEY_DELIVER));
		else 
			return 0;
	}
	
	
	
	public boolean increaseDeliver(long id){
		int deliver = getDelivers(id);
		ContentValues deliverValue = new ContentValues();
		deliverValue.put(KEY_DELIVER, deliver + 1);
		try{
			db.update(DATABASE_SMS_TABLE, deliverValue, KEY_ID + "=" + id, null);
			if(checkDeliver(id)){
				ContentValues deliverTimeSaver = new ContentValues();
				deliverTimeSaver.put(KEY_D_MILLIS, System.currentTimeMillis());
				db.update(DATABASE_SMS_TABLE, deliverTimeSaver, KEY_ID + "=" + id, null);
			}
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	public boolean checkDeliver(long id){
		Log.i("MESSAGE", "ID in Check Delivery : " + id);
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_DELIVER, KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
		Log.i("MESSAGE", "Number of records in check Deliver : " + cur.getCount());
		cur.moveToFirst();
		boolean bool = ((cur.getInt(cur.getColumnIndex(KEY_DELIVER))) == (cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS))));
		Log.i("MESSAGE", "value of equality : " + bool);
		return bool;
	}
	
	
	
	
	public boolean checkDelivery(long id){
		Log.i("MESSAGE", "sent 		: " + String.valueOf(getSent(id)));
		Log.i("MESSAGE", "deliver 	: " + String.valueOf(getDelivers(id)));
		return (getSent(id) == getDelivers(id));
	}
	
	
	
	
	
	public long getSentDate(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_S_MILLIS}, KEY_ID + "=" + id, null, null, null, null);
		cur.moveToFirst();
		return cur.getLong(cur.getColumnIndex(KEY_S_MILLIS));
	}
	
	
	
	
	
	public long getDeliveryDate(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_D_MILLIS}, KEY_ID + "=" + id, null, null, null, null);
		cur.moveToFirst();
		return cur.getLong(cur.getColumnIndex(KEY_D_MILLIS));
	}
	
	
	//--------------------------------------functions on Pending Intent Table -----------------------------------------
	public Cursor getPiDetails(){
		return db.query(DATABASE_PI_TABLE, null, KEY_PI_ID + "= 1", null, null, null, null);
	}
	
	
	
	public long getCurrentPiId(){
		Cursor cur = db.query(DATABASE_PI_TABLE, new String[] {KEY_SMS_ID}, KEY_PI_ID + "=1", null, null, null, null);
		cur.moveToFirst();
		long currentSmsId = cur.getLong(cur.getColumnIndex(KEY_SMS_ID));
		return currentSmsId;
	}
	
	
	
	public void updatePi(long pi_number, long id, long time){
		
		ContentValues cv = new ContentValues();
		cv.put(KEY_OPERATED, 0);
		db.update(DATABASE_SMS_TABLE, cv, KEY_ID + "=" + getCurrentPiId(), null);
		cv.clear();
		
		cv.put(KEY_PI_NUMBER, pi_number);
		cv.put(KEY_SMS_ID, id);
		cv.put(KEY_TIME, time);
		
		db.update(DATABASE_PI_TABLE, cv, KEY_PI_ID + "= 1", null);
	}
	
	
	
	
	public long getCurrentPiFiretime(){
		Cursor cur = db.query(DATABASE_PI_TABLE, new String[] {KEY_TIME}, KEY_PI_ID + "= 1", null, null, null, null);
		cur.moveToFirst();
		long currentPiFireTime = cur.getLong(cur.getColumnIndex(KEY_TIME));
		return currentPiFireTime;
	}
	
	//--------------------------------------------------------------------------------
	
	
	
	//-------------------------functions for template table---------------------------
	public Cursor fetchAllTemplates(){
		Cursor cur = db.query(DATABASE_TEMPLATE_TABLE, new String[] {KEY_TEMP_CONTENT, KEY_TEMP_ID}, null, null, null, null, null);
		Log.v("as", cur.getCount()+" ui");
		return cur;
	}
	
	public long addTemplate(String template){
		ContentValues addTemplateValues = new ContentValues();
		addTemplateValues.put(KEY_TEMP_CONTENT, template);
		try{
			long newId = db.insert(DATABASE_TEMPLATE_TABLE, null, addTemplateValues);
			return newId;
		}catch(SQLException sqe){
			return 0;
		}
	}
	
	public boolean removeTemplate(long id){
		try{
			Log.i("MESSAGE", "id in DB : " + id);
			db.delete(DATABASE_TEMPLATE_TABLE, KEY_TEMP_ID + "=" + id, null);
			return true;
		}catch(SQLException sqe){
			return false;
		}
	}
	//-----------------------------------------------------end of function for template table-----
	
	
	
	
	public class MyOpenHelper extends SQLiteOpenHelper{
		
		MyOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(DATABASE_CREATE_SMS_TABLE);
	        db.execSQL(DATABASE_CREATE_TEMPLATE_TABLE);
	        db.execSQL(DATABASE_CREATE_PI_TABLE);
	        
	        ContentValues initialPi = new ContentValues();
	        initialPi.put(KEY_PI_ID, 1);
	        initialPi.put(KEY_PI_NUMBER, 0);
	        initialPi.put(KEY_SMS_ID, -1);
	        initialPi.put(KEY_TIME, -1);
	        initialPi.put(KEY_ACTION, "");
	        
	        db.insert(DATABASE_PI_TABLE, null, initialPi);
	        
	        ContentValues initialTemplates = new ContentValues();
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "Happy Birthday");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "Where are you?");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "I'm busy. Will call you back in a moment");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "My throat is itching, lets have some BEER!!!");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "Are you coming to the Dope Show???");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}