package com.smsschedulerexpl.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
	private static final int DATABASE_VERSION = 1;
	
	Cursor cur;
	
	
	//static keys for columns---------------------------------------
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
	
	public static final String KEY_PI_ID = "_id";
	public static final String KEY_PI_NUMBER = "pi_number";
	public static final String KEY_GS_ID = "gs_id";
	public static final String KEY_TIME = "time";
	
	//SQL to open or create a database
	private static final String DATABASE_CREATE_SMS_TABLE = "create table " + 
		DATABASE_SMS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " + KEY_GRPID + " integer, " + 
		KEY_NUMBER + " text not null, " + KEY_MESSAGE + " text, " + KEY_DATE + " text, " + KEY_TIME_MILLIS + " long, " 
		+ KEY_SENT + " integer default 0, "+ KEY_DELIVER + " integer default 0, " + KEY_MSG_PARTS + " integer default 0, " 
		+ KEY_S_MILLIS + " int, " + KEY_D_MILLIS + " int);";
	
	
	private static final String DATABASE_CREATE_PI_TABLE = "create table " +
		DATABASE_PI_TABLE + " (" + KEY_PI_ID + " integer primary key, " + KEY_PI_NUMBER + " integer, " +
		KEY_GS_ID + " integer, " + KEY_TIME + " integer);";
	
	
	
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
		return db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_TIME_MILLIS, KEY_DATE, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS}, KEY_SENT + "=0", null, null, null, KEY_TIME_MILLIS);
	}
	
	public Cursor fetchAllSent(){
		return db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_DATE, KEY_TIME_MILLIS, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS, KEY_S_MILLIS, KEY_D_MILLIS}, KEY_SENT + "=" + KEY_MSG_PARTS, null, null, null, KEY_TIME_MILLIS);
	}
	
	public Cursor fetchSmsDetails(long id){
		return db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_GRPID, KEY_NUMBER, KEY_MESSAGE, KEY_DATE, KEY_SENT, KEY_DELIVER, KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
	}
	
	public long scheduleSms(String title, String content, String date, int parts, int group_id){
		
		ContentValues addValues = new ContentValues();
		
		addValues.put(KEY_NUMBER, title);
		addValues.put(KEY_MESSAGE, content);
		addValues.put(KEY_DATE, date);
		addValues.put(KEY_SENT, 0);
		addValues.put(KEY_DELIVER, 0);
		addValues.put(KEY_MSG_PARTS, parts);
		addValues.put(KEY_GRPID, group_id);
		
		return	db.insert(DATABASE_SMS_TABLE, null, addValues);
	}
	
	
	
	
	
	
	
	public int getNextGroupId(){
		cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_GRPID}, null, null, null, null, null);
		if(cur.moveToFirst()){
			cur.moveToLast();
			return cur.getInt(cur.getColumnIndex("group_id")) + 1;
		}else{
			return 0;
		}
	}
	
	
	
	public int removeSms(long id){
		return db.delete(DATABASE_SMS_TABLE, KEY_ID + "=" + id, null);
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
	
	
	
	
//	public boolean removeGroup(int grp, Context _context){
//		try{
//			Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_ID, KEY_NUMBER}, KEY_GRPID + "=" + grp, null, null, null, null);
//			if(cur.moveToFirst()){
//				do{
//					Intent cancelIntent = new Intent(_context, MyBroadcastReceiver.class);
//					cancelIntent.setAction(cur.getString(cur.getColumnIndex(KEY_NUMBER)));
//					PendingIntent cancelPi = PendingIntent.getBroadcast(_context, (int)cur.getInt(cur.getColumnIndex(KEY_ID)), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//					cancelPi.cancel();
//				}while(cur.moveToNext());
//			}
//			db.delete(DATABASE_SMS_TABLE, KEY_GRPID + "=" + grp, null);
//			return true;
//		}catch(SQLiteException ex){
//			return false;
//		}
//	}
//	
	
	
	
	private int getDeliver(long id){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_DELIVER}, KEY_ID + "=" + id, null, null, null, null);
		if(cur.moveToFirst())
			return cur.getInt(cur.getColumnIndex(KEY_DELIVER));
		else 
			return 0;
	}
	
	
	
	public boolean increaseDeliver(long id){
		int deliver = getDeliver(id);
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
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_DELIVER, KEY_MSG_PARTS}, KEY_ID + "=" + id, null, null, null, null);
		return (cur.getInt(cur.getColumnIndex(KEY_DELIVER)) == cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS)));
	}
	
	
	
	
	public boolean checkDelivery(long id){
		Log.i("MESSAGE", "sent 		: " + String.valueOf(getSent(id)));
		Log.i("MESSAGE", "deliver 	: " + String.valueOf(getDeliver(id)));
		return (getSent(id) == getDeliver(id));
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
		return db.query(DATABASE_CREATE_PI_TABLE, null, KEY_PI_ID + "= 1", null, null, null, null);
	}
	
	
	
	
	public void updatePi(long pi_number, long gsid, long time){
		ContentValues cv = new ContentValues();
		cv.put(KEY_PI_NUMBER, pi_number);
		cv.put(KEY_GS_ID, gsid);
		cv.put(KEY_TIME, time);
		
		db.update(DATABASE_PI_TABLE, cv, KEY_PI_ID + "= 1", null);
	}
	
	
	
	
	public long getCurrentPiFiretime(){
		Cursor cur = db.query(DATABASE_CREATE_PI_TABLE, new String[] {KEY_TIME}, KEY_PI_ID + "= 1", null, null, null, null);
		return cur.getInt(cur.getColumnIndex(KEY_TIME));
	}
	
	//--------------------------------------------------------------------------------
	
	
	public class MyOpenHelper extends SQLiteOpenHelper{
		
		MyOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(DATABASE_CREATE_SMS_TABLE);
	        db.execSQL(DATABASE_CREATE_PI_TABLE);
	        
	        ContentValues initialPi = new ContentValues();
	        initialPi.put(KEY_PI_ID, 1);
	        initialPi.put(KEY_PI_NUMBER, 0);
	        initialPi.put(KEY_GS_ID, -1);
	        initialPi.put(KEY_TIME, 0);
	        
	        db.insert(DATABASE_PI_TABLE, null, initialPi);
	        
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}