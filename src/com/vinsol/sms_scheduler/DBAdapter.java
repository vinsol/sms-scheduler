package com.vinsol.sms_scheduler;

import java.util.ArrayList;
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

import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;

public class DBAdapter {
	
	private final String DATABASE_NAME = "smsDatabase";
	private final String DATABASE_SMS_TABLE = "smsTable";
	private final String DATABASE_RECIPIENT_TABLE = "recipientTable";
	private final String DATABASE_PI_TABLE = "piTable";
	private final String DATABASE_TEMPLATE_TABLE = "templateTable";
	private final String DATABASE_GROUP_TABLE = "groupTable";
	private final String DATABASE_GROUP_CONTACT_RELATION = "groupContactRelation";
	private final String DATABASE_RECIPIENT_GROUP_REL_TABLE = "span_grp_rel_table";
	private final String DATABASE_RECENTS_TABLE = "recents_table";
	private final int 	DATABASE_VERSION = 1;
	
	Cursor cur;
	
	//---------------------------static keys for columns---------------------------------------
	
	//----------------keys for SMS table--------------------------
	public static final String KEY_ID = "_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATE = "date";
	public static final String KEY_TIME_MILLIS = "time_millis";
	public static final String KEY_MSG_PARTS = "msg_parts";
	public static final String KEY_STATUS = "status";
	
	//------------------keys for Recipients table-----------------------
	public static final String KEY_RECIPIENT_ID = "recipient_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_SENT = "sent";
	public static final String KEY_DELIVER = "deliver";
	public static final String KEY_OPERATED = "operated";
	public static final String KEY_S_MILLIS = "sent_milis";
	public static final String KEY_D_MILLIS = "deliver_milis";
	public static final String KEY_DISPLAY_NAME = "display_name";
	public static final String KEY_CONTACT_ID = "contact_id";
	public static final String KEY_RECIPIENT_TYPE = "recipient_type";
	
	//--------------keys for PI table---------------------------
	public static final String KEY_PI_ID = "_id";
	public static final String KEY_PI_NUMBER = "pi_number";
	public static final String KEY_SMS_ID = "sms_id";
	public static final String KEY_TIME = "time";
	
	//----------------keys for template table---------------------
	public static final String KEY_TEMP_ID = "_id";
	public static final String KEY_TEMP_CONTENT = "content";
	
	//-----------------keys for group table-----------------------
	public static final String KEY_GROUP_ID = "_id";
	public static final String KEY_GROUP_NAME = "group_name";
	
	//----------------keys for group contacts relation----------------
	public static final String KEY_RELATION_ID = "_id";
	public static final String KEY_GROUP_REL_ID = "group_rel_id";
	public static final String KEY_CONTACTS_ID = "contacts_id";
	
	//-------------------keys for recipient-groupId relation--------------------
	public static final String KEY_RECIPIENT_GRP_REL_ID = "_id";
	public static final String KEY_RECIPIENT_GRP_REL_RECIPIENT_ID = "recipient_grp_rel_recipient_id";
	public static final String KEY_RECIPIENT_GRP_REL_GRP_ID = "recipient_grp_rel_grp_id";
	public static final String KEY_RECIPIENT_GRP_REL_GRP_TYPE = "recipient_grp_rel_grp_type";
	
	//------------------keys for recents table-------------------------
	public static final String KEY_RECENT_CONTACT_ID = "_id";
	public static final String KEY_RECENT_CONTACT_CONTACT_ID = "contact_id";
	public static final String KEY_RECENT_CONTACT_NUMBER = "contact_number";
	
	//------------------------------------------------------------------end of static keys defs-------
	
	
	
	//SQL to open or create a database

	private final String DATABASE_CREATE_SMS_TABLE = "create table " + DATABASE_SMS_TABLE 
			+ " (" 
			+ KEY_ID          + " integer primary key autoincrement, "  
			+ KEY_MESSAGE     + " text, " 
			+ KEY_DATE        + " text, " 
			+ KEY_TIME_MILLIS + " long, " 
			+ KEY_MSG_PARTS   + " integer default 0, " 
			+ KEY_STATUS      + " integer default 1);";
	
	
	private final String DATABASE_CREATE_RECIPIENT_TABLE = "create table " + DATABASE_RECIPIENT_TABLE 
			+ " (" 
			+ KEY_RECIPIENT_ID  	+ " integer primary key autoincrement, "
			+ KEY_SMS_ID			+ " integer, "
			+ KEY_NUMBER      		+ " text not null, "
			+ KEY_CONTACT_ID		+ " interger, "
			+ KEY_SENT        		+ " integer default 0, "
			+ KEY_DELIVER     		+ " integer default 0, " 
			+ KEY_S_MILLIS    		+ " integer, " 
			+ KEY_D_MILLIS    		+ " integer, " 
			+ KEY_OPERATED    		+ " integer default 0, "
			+ KEY_DISPLAY_NAME		+ " text, "
			+ KEY_RECIPIENT_TYPE 	+ " integer);";
	
	
	private final String DATABASE_CREATE_PI_TABLE = "create table " + DATABASE_PI_TABLE 
			+ " (" 
			+ KEY_PI_ID      + " integer primary key, " 
			+ KEY_PI_NUMBER  + " integer, " 
			+ KEY_SMS_ID     + " integer, " 
			+ KEY_TIME       + " integer);"; 
	
	
	private final String DATABASE_CREATE_TEMPLATE_TABLE = "create table " + DATABASE_TEMPLATE_TABLE 
			+ " (" 
			+ KEY_TEMP_ID      + " integer primary key autoincrement, " 
			+ KEY_TEMP_CONTENT + " text);";
	
	
	private final String DATABASE_CREATE_GROUP_TABLE = "create table " + DATABASE_GROUP_TABLE 
			+ " (" 
			+ KEY_GROUP_ID   + " integer primary key autoincrement, " 
			+ KEY_GROUP_NAME + " text);";
	
	
	private final String DATABASE_CREATE_GROUP_CONTACT_RELATION = "create table " +DATABASE_GROUP_CONTACT_RELATION 
			+ " (" 
			+ KEY_RELATION_ID  + " integer primary key autoincrement, " 
			+ KEY_GROUP_REL_ID + " integer, " 
			+ KEY_CONTACTS_ID  + " integer);";
	
	
	private final String DATABASE_CREATE_RECIPIENT_GROUP_REL_TABLE = "create table " + DATABASE_RECIPIENT_GROUP_REL_TABLE 
			+ " (" 
			+ KEY_RECIPIENT_GRP_REL_ID + " integer primary key autoincrement, " 
			+ KEY_RECIPIENT_GRP_REL_RECIPIENT_ID + " integer, " 
			+ KEY_RECIPIENT_GRP_REL_GRP_ID + " integer, " 
			+ KEY_RECIPIENT_GRP_REL_GRP_TYPE + " integer);";
	
	
	private final String DATABASE_CREATE_RECENTS_TABLE = "create table " + DATABASE_RECENTS_TABLE 
			+ " (" 
			+ KEY_RECENT_CONTACT_ID         + " integer primary key autoincrement, " 
			+ KEY_RECENT_CONTACT_CONTACT_ID + " integer, " 
			+ KEY_RECENT_CONTACT_NUMBER     + " text);";
	
	
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

	
	// functions-----------------------------------------------------------------------------------------
	
	//------------------------functions for SMS and Recipient tables------------------------------
	
	public boolean ifSmsExist(){
		Cursor cur = db.query(DATABASE_SMS_TABLE, null, null, null, null, null, null);
		return (cur.getCount()>0);
	}
	
	
	public boolean isDraft(long smsId){
		Cursor cur = db.query(DATABASE_SMS_TABLE, new String[]{KEY_STATUS}, KEY_ID + "=" + smsId, null, null, null, null);
		cur.moveToFirst();
		return (cur.getInt(cur.getColumnIndex(KEY_STATUS))==0);
	}
	
	
	public Cursor fetchAllRecipientDetails(){
		String sql = "SELECT * FROM smsTable, recipientTable "
			+ "WHERE smsTable._id=recipientTable.sms_id "
			+ "ORDER BY smsTable.time_millis";
		
		Cursor cur = db.rawQuery(sql, null);
		return cur;
	}
	
	
	public Cursor fetchRecipientDetails(long recipientId){
		String sql = "SELECT * FROM smsTable, recipientTable "
			+ "WHERE smsTable._id=recipientTable.sms_id AND recipientTable.recipient_id =" + recipientId;
		
		Cursor cur = db.rawQuery(sql, null);
		return cur;
	}
	
	
	public Cursor fetchRecipientsForSms(long smsId){
		String sql = "SELECT * FROM smsTable, recipientTable "
			+ "WHERE smsTable._id=recipientTable.sms_id AND smsTable._id=" + smsId;
		
		Cursor cur = db.rawQuery(sql, null);
		return cur;
	}
	
	
	public ArrayList<Long> fetchRecipientIdsForSms(long smsId){
		String sql = "SELECT * FROM smsTable, recipientTable "
			+ "WHERE smsTable._id=recipientTable.sms_id AND smsTable._id=" + smsId;
		
		ArrayList<Long> recipientIds = new ArrayList<Long>();
		
		Cursor cur = db.rawQuery(sql, null);
		if(cur.moveToFirst()){
			do{
				recipientIds.add(cur.getLong(cur.getColumnIndex(KEY_RECIPIENT_ID)));
			}while(cur.moveToNext());
		}
		
		return recipientIds;
	}
	
	
	public void deleteRecipient(long recipientId){
		db.delete(DATABASE_RECIPIENT_TABLE, KEY_RECIPIENT_ID + "=" + recipientId, null);
		deleteRecipientGroupRelsForRecipient(recipientId);
	}
	
	
	
//	public Cursor fetchAllScheduled(){
//		String sql = "SELECT * FROM smsTable, recipientTable "
//			+ "WHERE smsTable._id=recipientTable.sms_id "
//			+ "AND (smsTable.status=1 OR smsTable.status=3) "
//			+ "AND recipientTable.operated=0 "
//			+ "ORDER BY smsTable.time_millis";
//		
//		Cursor cur = db.rawQuery(sql, null);
//		Log.d("size of cur in db : " + cur.getCount());
//		return cur;
//	}
	
	
	
	public Cursor fetchNextScheduled(){
		String sql = "SELECT * FROM smsTable, recipientTable WHERE recipientTable.sms_id=smsTable._id AND recipientTable.recipient_id="
			 + "(SELECT recipientTable.recipient_id FROM recipientTable, smsTable " 
					+ "WHERE recipientTable.sms_id = smsTable._id AND recipientTable.operated=0 AND smsTable._id="
				           + "(SELECT smsTable._id FROM smsTable WHERE  smsTable.time_millis="
						          + "(SELECT MIN(smsTable.time_millis) FROM smsTable, recipientTable WHERE (smsTable.status=1 OR smsTable.status=3))))";
		
		Cursor cur = db.rawQuery(sql, null);
		Log.d("size of cur in db : " + cur.getCount());
		return cur;
	}
	

	
	public long scheduleSms(String message, String date, int parts, long timeInMilis){
		ContentValues addValues = new ContentValues();
		
		addValues.put(KEY_MESSAGE, message);
		addValues.put(KEY_DATE, date);
		addValues.put(KEY_TIME_MILLIS, timeInMilis);
		addValues.put(KEY_MSG_PARTS, parts);
		
		return	db.insert(DATABASE_SMS_TABLE, null, addValues);
	}
	
	
	public long addRecipient(long smsId, String number, String displayName, int type, long contactId){
		ContentValues addValues = new ContentValues();
		
		addValues.put(KEY_SMS_ID, smsId);
		addValues.put(KEY_NUMBER, number);
		addValues.put(KEY_DISPLAY_NAME, displayName);
		addValues.put(KEY_RECIPIENT_TYPE, type);
		addValues.put(KEY_SENT, 0);
		addValues.put(KEY_DELIVER, 0);
		addValues.put(KEY_S_MILLIS, -1);
		addValues.put(KEY_D_MILLIS, -1);
		addValues.put(KEY_CONTACT_ID, contactId);
		
		return db.insert(DATABASE_RECIPIENT_TABLE, null, addValues);
	}
	
	
	public void setAsDraft(long smsId){
		ContentValues cv = new ContentValues();
		cv.put(KEY_STATUS, 0);
		
		db.update(DATABASE_SMS_TABLE, cv, KEY_ID + "=" + smsId, null);
	}
	
	
	public void setStatus(long smsId, int status){
		ContentValues cv = new ContentValues();
		cv.put(KEY_STATUS, status);
		
		db.update(DATABASE_SMS_TABLE, cv, KEY_ID + "=" + smsId, null);
	}
	
		
	public void makeOperated(long recipientId){
		ContentValues cv = new ContentValues();
		cv.put(KEY_OPERATED, 1);
		db.update(DATABASE_RECIPIENT_TABLE, cv, KEY_RECIPIENT_ID + "=" + recipientId, null);
	}
	
	
	public int getSent(long recipientId){
		Cursor cur = db.query(DATABASE_RECIPIENT_TABLE, new String[] {KEY_SENT}, KEY_RECIPIENT_ID + "=" + recipientId, null, null, null, null);
		if(cur.moveToFirst())
			return cur.getInt(cur.getColumnIndex(KEY_SENT));
		else 
			return 0;
	}
	
	
	public boolean increaseSent(long smsId, long recipientId){
		int sent = getSent(recipientId);
		ContentValues sentValue = new ContentValues();
		sentValue.put(KEY_SENT, sent + 1);
		try{
			db.update(DATABASE_RECIPIENT_TABLE, sentValue, KEY_RECIPIENT_ID + "=" + recipientId, null);
			Cursor cur = db.query(DATABASE_RECIPIENT_TABLE, new String[] {KEY_SENT, KEY_SMS_ID}, KEY_RECIPIENT_ID + "=" + recipientId, null, null, null, null);
			
			sent  = sent + 1;
			
			cur = db.query(DATABASE_SMS_TABLE, new String[] {KEY_MSG_PARTS}, KEY_ID + "=" + smsId, null, null, null, null);
			cur.moveToFirst();
			int parts = cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS));
			if (sent == parts){
				ContentValues sentTimeSaver = new ContentValues();
				sentTimeSaver.put(KEY_S_MILLIS, System.currentTimeMillis());
				db.update(DATABASE_RECIPIENT_TABLE, sentTimeSaver, KEY_RECIPIENT_ID + "=" + recipientId, null);
			}
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	public int getDelivers(long recipientId){
		Cursor cur = db.query(DATABASE_RECIPIENT_TABLE, new String[] {KEY_DELIVER}, KEY_RECIPIENT_ID + "=" + recipientId, null, null, null, null);
		if(cur.moveToFirst())
			return cur.getInt(cur.getColumnIndex(KEY_DELIVER));
		else 
			return 0;
	}
	
	
	public boolean increaseDeliver(long recipientId){
		int deliver = getDelivers(recipientId);
		ContentValues deliverValue = new ContentValues();
		deliverValue.put(KEY_DELIVER, deliver + 1);
		try{
			db.update(DATABASE_RECIPIENT_TABLE, deliverValue, KEY_RECIPIENT_ID + "=" + recipientId, null);
			if(checkDelivery(recipientId)){
				ContentValues deliverTimeSaver = new ContentValues();
				deliverTimeSaver.put(KEY_D_MILLIS, System.currentTimeMillis());
				db.update(DATABASE_RECIPIENT_TABLE, deliverTimeSaver, KEY_RECIPIENT_ID + "=" + recipientId, null);
			}
			return true;
		}catch(SQLiteException ex){
			return false;
		}
	}
	
	
	public boolean checkDelivery(long recipientId){
		Cursor cur = fetchRecipientDetails(recipientId);
		cur.moveToFirst();
		boolean bool = ((cur.getInt(cur.getColumnIndex(KEY_DELIVER))) == (cur.getInt(cur.getColumnIndex(KEY_MSG_PARTS))));
		return bool;
	}
	
	
	public void deleteSms(long smsId, Context context){
		ArrayList<Long> recipientIds = fetchRecipientIdsForSms(smsId);
		for(int i = 0; i< recipientIds.size(); i++){
			if(getCurrentPiId()==recipientIds.get(i)){
				Cursor cur = getPiDetails();
				cur.moveToFirst();
				
				Intent intent = new Intent(context, SMSHandleReceiver.class);
				intent.setAction(Constants.PRIVATE_SMS_ACTION);
				PendingIntent pi = PendingIntent.getBroadcast(context, cur.getInt(cur.getColumnIndex(KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
				pi.cancel();
			}
			deleteRecipient(recipientIds.get(i));
		}
		db.delete(DATABASE_SMS_TABLE, KEY_ID + "=" + smsId, null);
		
		Cursor cur = fetchNextScheduled();
		if(cur.moveToFirst()){
			Intent intent = new Intent(context, SMSHandleReceiver.class);
			intent.setAction(Constants.PRIVATE_SMS_ACTION);
			intent.putExtra("SMS_ID", cur.getLong(cur.getColumnIndex(KEY_ID)));
			intent.putExtra("RECIPIENT_ID", cur.getLong(cur.getColumnIndex(KEY_RECIPIENT_ID)));
			intent.putExtra("NUMBER", cur.getString(cur.getColumnIndex(KEY_NUMBER)));
			intent.putExtra("MESSAGE", cur.getString(cur.getColumnIndex(KEY_MESSAGE)));
			
			Random rand = new Random();
			int piNumber = rand.nextInt();
			PendingIntent pi = PendingIntent.getBroadcast(context, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			updatePi(piNumber, cur.getLong(cur.getColumnIndex(KEY_RECIPIENT_ID)), cur.getLong(cur.getColumnIndex(KEY_TIME_MILLIS)));
			
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.set(AlarmManager.RTC_WAKEUP, cur.getLong(cur.getColumnIndex(KEY_TIME_MILLIS)), pi);
		}else{
			updatePi(0, -1, -1);
		}
	}
	//--------------------------------------------------------end of functions for SMS and Recipient tables---------------
	
	
	
	
	
	//--------------------------------------functions for Pending Intent Table -----------------------------------------
	public Cursor getPiDetails(){
		return db.query(DATABASE_PI_TABLE, null, KEY_PI_ID + "= 1", null, null, null, null);
	}
	
	
	public long getCurrentPiId(){
		Cursor cur = db.query(DATABASE_PI_TABLE, new String[] {KEY_SMS_ID}, KEY_PI_ID + "=1", null, null, null, null);
		cur.moveToFirst();
		long currentSmsId = cur.getLong(cur.getColumnIndex(KEY_SMS_ID));
		return currentSmsId;
	}
	
	
	public void updatePi(long pi_number, long recipientId, long time){
		
		ContentValues cv = new ContentValues();
		
		if(getCurrentPiId()!=-1){
			Cursor cur = fetchRecipientDetails(getCurrentPiId());
			if(cur.moveToFirst()){
				if(cur.getLong(cur.getColumnIndex(KEY_TIME_MILLIS))>System.currentTimeMillis()){
					cv.put(KEY_OPERATED, 0);
					db.update(DATABASE_RECIPIENT_TABLE, cv, KEY_RECIPIENT_ID + "=" + getCurrentPiId(), null);
				}
			}
		}
		
		cv.clear();
		
		cv.put(KEY_PI_NUMBER, pi_number);
		cv.put(KEY_SMS_ID, recipientId);
		cv.put(KEY_TIME, time);
		
		db.update(DATABASE_PI_TABLE, cv, KEY_PI_ID + "= 1", null);
	}
	
	
	public long getCurrentPiFiretime(){
		Cursor cur = db.query(DATABASE_PI_TABLE, new String[] {KEY_TIME}, KEY_PI_ID + "= 1", null, null, null, null);
		cur.moveToFirst();
		long currentPiFireTime = cur.getLong(cur.getColumnIndex(KEY_TIME));
		return currentPiFireTime;
	}
	//--------------------------------------------------------end of functions for Pending Intent table---------
	
	
	
	
	
	//-------------------------functions for template table---------------------------
	public Cursor fetchAllTemplates(){
		Cursor cur = db.query(DATABASE_TEMPLATE_TABLE, new String[] {KEY_TEMP_CONTENT, KEY_TEMP_ID}, null, null, null, null, null);
		Log.d(cur.getCount()+" ui");
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
	
	
	public void editTemplate(long rowId, String template){
		ContentValues cv = new ContentValues();
		cv.put(KEY_TEMP_CONTENT, template);
		db.update(DATABASE_TEMPLATE_TABLE, cv, KEY_TEMP_ID + "=" + rowId, null);
	}
	
	
	public boolean removeTemplate(long id){
		try{
			db.delete(DATABASE_TEMPLATE_TABLE, KEY_TEMP_ID + "=" + id, null);
			return true;
		}catch(SQLException sqe){
			return false;
		}
	}
	//-----------------------------------------------------end of functions for template table-----
	
	
	
	
	
	//-----------------------------------functions for group table--------------------------------------
	public Cursor fetchAllGroups(){
		Cursor cur = db.query(DATABASE_GROUP_TABLE, null, null, null, null, null, null);
		return cur;
	}
	
	
	public ArrayList<Long> fetchIdsForGroups(long groupId){
		ArrayList<Long> ids = new ArrayList<Long>();
		Cursor cur = db.query(DATABASE_GROUP_CONTACT_RELATION, new String[]{KEY_CONTACTS_ID}, KEY_GROUP_REL_ID + "=" + groupId, null, null, null, null);
		if(cur.moveToFirst()){
			do{
				ids.add(cur.getLong(cur.getColumnIndex(KEY_CONTACTS_ID)));
			}while(cur.moveToNext());
		}
		return ids;
	}
	
	
	public long createGroup(String name, ArrayList<Long> contactIds){
		ContentValues cv = new ContentValues();
		cv.put(KEY_GROUP_NAME, name);
		long grpid = db.insert(DATABASE_GROUP_TABLE, null, cv);
		for(int i = 0; i< contactIds.size(); i++){
			addContactToGroup(contactIds.get(i), grpid);
		}
		return grpid;
	}
	
	
	public void addContactToGroup(long contactId, long groupId){
		ContentValues cv = new ContentValues();
		cv.put(KEY_GROUP_REL_ID, groupId);
		cv.put(KEY_CONTACTS_ID, contactId);
		db.insert(DATABASE_GROUP_CONTACT_RELATION, null, cv);
	}
	
	
	public void removeContactFromGroup(long contactId, long groupId){
		db.delete(DATABASE_GROUP_CONTACT_RELATION, KEY_GROUP_REL_ID + "=" + groupId + " AND " + KEY_CONTACTS_ID + "=" + contactId, null);
	}
	
	
	public void removeGroup(long groupId){
		db.delete(DATABASE_GROUP_CONTACT_RELATION, KEY_GROUP_REL_ID + "=" + groupId, null);
		db.delete(DATABASE_GROUP_TABLE, KEY_GROUP_ID + "=" + groupId, null);
	}
	
	
	public void setGroupName(String name, long groupId){
		ContentValues cv = new ContentValues();
		cv.put(KEY_GROUP_NAME, name);
		db.update(DATABASE_GROUP_TABLE, cv, KEY_GROUP_ID + "=" + groupId, null);
	}
	//---------------------------------------------------------------end of functions for group table---------------------
	
	
	
	
	//--------------------------------functions for span-group-relation table--------------------------------
	//*** Table to log the relation between added spans and groups ***
	public ArrayList<Long> fetchGroupsForRecipient(long recipientId){
		Cursor cur = db.query(DATABASE_RECIPIENT_GROUP_REL_TABLE, new String[]{KEY_RECIPIENT_GRP_REL_GRP_ID}, KEY_RECIPIENT_GRP_REL_RECIPIENT_ID + "=" + recipientId, null, null, null, null);
		ArrayList<Long> groupIds = new ArrayList<Long>();
		if(cur.moveToFirst()){
			do{
				groupIds.add(cur.getLong(cur.getColumnIndex(KEY_RECIPIENT_GRP_REL_GRP_ID)));
			}while(cur.moveToNext());
		}
		return groupIds;
	}
	
	
	public ArrayList<Integer> fetchGroupTypesForSpan(long recipientId){
		Cursor cur = db.query(DATABASE_RECIPIENT_GROUP_REL_TABLE, new String[]{KEY_RECIPIENT_GRP_REL_GRP_TYPE}, KEY_RECIPIENT_GRP_REL_RECIPIENT_ID + "=" + recipientId, null, null, null, null);
		ArrayList<Integer> groupTypes = new ArrayList<Integer>();
		if(cur.moveToFirst()){
			do{
				groupTypes.add(cur.getInt(cur.getColumnIndex(KEY_RECIPIENT_GRP_REL_GRP_TYPE)));
			}while(cur.moveToNext());
		}
		return groupTypes;
	}
	
	
	public ArrayList<Long> fetchRecipientsForGroup(long groupId, int type){
		Cursor cur = db.query(DATABASE_RECIPIENT_GROUP_REL_TABLE, new String[]{KEY_RECIPIENT_GRP_REL_RECIPIENT_ID}, KEY_RECIPIENT_GRP_REL_GRP_ID + "=" + groupId + " AND " + KEY_RECIPIENT_GRP_REL_GRP_TYPE + "=" + type, null, null, null, null);
		ArrayList<Long> recipientIds = new ArrayList<Long>();
		if(cur.moveToFirst()){
			do{
				recipientIds.add(cur.getLong(cur.getColumnIndex(KEY_RECIPIENT_GRP_REL_RECIPIENT_ID)));
			}while(cur.moveToNext());
		}
		return recipientIds;
	}
	
	
	public void addRecipientGroupRel(long recipientId, long groupId, int type){
		ContentValues cv = new ContentValues();
		cv.put(KEY_RECIPIENT_GRP_REL_RECIPIENT_ID, recipientId);
		cv.put(KEY_RECIPIENT_GRP_REL_GRP_ID, groupId);
		cv.put(KEY_RECIPIENT_GRP_REL_GRP_TYPE, type);
		
		db.insert(DATABASE_RECIPIENT_GROUP_REL_TABLE, null, cv);
	}
	
	
	public void deleteRecipientGroupRelsForRecipient(long recipientId){
		db.delete(DATABASE_RECIPIENT_GROUP_REL_TABLE, KEY_RECIPIENT_GRP_REL_RECIPIENT_ID + "=" + recipientId, null);
	}
	
	
	public void deleteRecipientGroupRel(long recipientId, long groupId, int type){
		db.delete(DATABASE_RECIPIENT_GROUP_REL_TABLE, KEY_RECIPIENT_GRP_REL_RECIPIENT_ID + "=" + recipientId + " AND " + KEY_RECIPIENT_GRP_REL_GRP_ID + "=" + groupId + " AND " + KEY_RECIPIENT_GRP_REL_GRP_TYPE + "=" + type, null);
	}
	//----------------------------------------------end of functions for span-group-relation table----------------
	
	
	
	//----------------------------functions for recents table----------------------------------
	public void addRecentContact(long contactId, String contactNumber){
		Cursor cur = db.query(DATABASE_RECENTS_TABLE, new String[]{KEY_RECENT_CONTACT_ID, KEY_RECENT_CONTACT_CONTACT_ID, KEY_RECENT_CONTACT_NUMBER}, null, null, null, null, KEY_RECENT_CONTACT_ID);
		boolean contactExist = false;
		if(cur.moveToFirst()){
			do{
				if((cur.getLong(cur.getColumnIndex(KEY_RECENT_CONTACT_CONTACT_ID)) == contactId)){// || (cur.getString(cur.getColumnIndex(KEY_RECENT_CONTACT_NUMBER)).equals(contactNumber))){
					contactExist = true;
					break;
				}
				if(((cur.getLong(cur.getColumnIndex(KEY_RECENT_CONTACT_CONTACT_ID))) == -1) && (cur.getString(cur.getColumnIndex(KEY_RECENT_CONTACT_NUMBER)).equals(contactNumber))){
					contactExist = true;
					break;
				}
			}while(cur.moveToNext());
		}
		if(!contactExist){
			ContentValues cv = new ContentValues();
			cv.put(KEY_RECENT_CONTACT_CONTACT_ID, contactId);
			cv.put(KEY_RECENT_CONTACT_NUMBER, contactNumber);
			if(cur.getCount()<20){
				db.insert(DATABASE_RECENTS_TABLE, null, cv);
			}else{
				cur.moveToFirst();
				long idToDelete = cur.getLong(cur.getColumnIndex(KEY_RECENT_CONTACT_ID));
				db.delete(DATABASE_RECENTS_TABLE, KEY_RECENT_CONTACT_ID + "=" + idToDelete, null);
				db.insert(DATABASE_RECENTS_TABLE, null, cv);
			}
		}
	}
	
	
	public Cursor fetchAllRecents(){
		Cursor cur = db.query(DATABASE_RECENTS_TABLE, null, null, null, null, null, KEY_RECENT_CONTACT_ID + " DESC");
		return cur;
	}
	//----------------------------------------------end of functions for recents table-----------------
	
	//----------------------------------------------------------end of functions--------------------------------
	
	
	
	public class MyOpenHelper extends SQLiteOpenHelper{
		
		MyOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(DATABASE_CREATE_SMS_TABLE);
	        db.execSQL(DATABASE_CREATE_RECIPIENT_TABLE);
	        db.execSQL(DATABASE_CREATE_TEMPLATE_TABLE);
	        db.execSQL(DATABASE_CREATE_PI_TABLE);
	        db.execSQL(DATABASE_CREATE_GROUP_TABLE);
	        db.execSQL(DATABASE_CREATE_GROUP_CONTACT_RELATION);
	        db.execSQL(DATABASE_CREATE_RECIPIENT_GROUP_REL_TABLE);
	        db.execSQL(DATABASE_CREATE_RECENTS_TABLE);
	        
	        
	        //-------Setting initial content of Pending Intent-------
	        ContentValues initialPi = new ContentValues();
	        initialPi.put(KEY_PI_ID, 1);
	        initialPi.put(KEY_PI_NUMBER, 0);
	        initialPi.put(KEY_SMS_ID, -1);
	        initialPi.put(KEY_TIME, -1);
	        
	        db.insert(DATABASE_PI_TABLE, null, initialPi);
	        //-------------------------------------------------------
	        
	        
	        //-------Setting default templates for the app-----------
	        ContentValues initialTemplates = new ContentValues();
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "I'm in a meeting. I'll contact you later.");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "I'm driving now. I'll contact you later.");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "I'm busy. Will give you a call later.");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "Sorry, I'm going to be late.");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        
	        initialTemplates.put(KEY_TEMP_CONTENT, "Have a nice day!");
	        db.insert(DATABASE_TEMPLATE_TABLE, null, initialTemplates);
	        //---------------------------------------------------------
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	}
}