package com.vinsol.SMSScheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SMSSchedulerDBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sms_scheduler.db";
    private static final String SMS_SCHEDULER_TABLE_NAME = "SMSScheduler";
    
    private static final String SMS_SCHEDULER_TABLE_COLUMN_ID = "_id";
    private static final String SMS_SCHEDULER_TABLE_COLUMN_CONTACT_NUMBER = "contact_number";
    private static final String SMS_SCHEDULER_TABLE_COLUMN_CONTACT_NAME = "name";
    private static final String SMS_SCHEDULER_TABLE_COLUMN_SCHEDULED_TIME = "scheduled_time";
    private static final String SMS_SCHEDULER_TABLE_COLUMN_SMS_BODY = "sms_body";
    private static final String SMS_SCHEDULER_TABLE_COLUMN_STATUS = "status";
    
    private static final String CREATE_SMS_SCHEDULER_TABLE =
                "CREATE TABLE " + SMS_SCHEDULER_TABLE_NAME + " (" 
                + SMS_SCHEDULER_TABLE_COLUMN_ID + " TEXT, "
                + SMS_SCHEDULER_TABLE_COLUMN_CONTACT_NUMBER + " TEXT, " 
                + SMS_SCHEDULER_TABLE_COLUMN_CONTACT_NAME + " TEXT," 
                + SMS_SCHEDULER_TABLE_COLUMN_SCHEDULED_TIME + " TEXT,"
                + SMS_SCHEDULER_TABLE_COLUMN_SMS_BODY + " TEXT,"
                + SMS_SCHEDULER_TABLE_COLUMN_STATUS + " TEXT"
                + ");";
    
    SQLiteDatabase SMSSchedulerDBObject;

    /**=====================================================================
     * constructor
     * @param context
     *======================================================================*/
    SMSSchedulerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**=====================================================================
     * method onCreate
     *======================================================================*/
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.v("in SMSSchedulerDBHelper -> in OnCreate", " before execSQL");
        db.execSQL(CREATE_SMS_SCHEDULER_TABLE);
        Log.v("in SMSSchedulerDBHelper -> in OnCreate", " after execSQL");
    }//end method onCreate
    
    /**=====================================================================
     * method onUpgrade
     *======================================================================*/
    @Override
    public void onUpgrade(SQLiteDatabase  db, int oldVersion, int newVersion){
    }//end method onUpgrade
    
    /**=====================================================================
     * method checkOrCreateDB
     *======================================================================*/
    public boolean checkOrCreateDB(){
    
    	try{
    		SMSSchedulerDBObject = getReadableDatabase();
    		SMSSchedulerDBObject.close();
    		return true;
    	}catch(SQLiteException sqle){
    		return false;
    	}
    }//end method checkOrCreateDB
}//end class SMSSchedulerDBHelper

