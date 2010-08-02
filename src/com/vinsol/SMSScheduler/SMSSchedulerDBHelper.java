package com.vinsol.SMSScheduler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

public class SMSSchedulerDBHelper extends SQLiteOpenHelper {

	Context context;
	
	boolean isNewInstallation = false;
	
	//database details
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sms_scheduler.db";
    
	//table name
	private static final String MESSAGE_TABLE_NAME = "message";
    private static final String RECEIVER_TABLE_NAME = "receiver";
    private static final String TEMPLATE_TABLE_NAME = "template";
 
    //MESSAGE_TABLE columns name
    private static final String MESSAGE_TABLE_COLUMN_ID = "_id";
    private static final String MESSAGE_TABLE_COLUMN_SCHEDULED_TIME = "scheduled_time";
    private static final String MESSAGE_TABLE_COLUMN_MESSAGE_BODY = "message_body";
    private static final String MESSAGE_TABLE_COLUMN_STATUS = "status";
    
    //RECEIVER_TABLE columns name
    private static final String RECEIVER_TABLE_COLUMN_ID = "_id";
    private static final String RECEIVER_TABLE_MESSAGE_ID = "message_id";
    private static final String RECEIVER_TABLE_COLUMN_CONTACT_NUMBER = "contact_number";
    private static final String RECEIVER_TABLE_COLUMN_RECEIVER_NAME = "name";
    private static final String RECEIVER_TABLE_COLUMN_RECEIVER_IMAGE = "image";
    
    //TEMPLATE_TABLE columns name
    private static final String TEMPLATE_TABLE_COLUMN_ID = "_id";
    private static final String TEMPLATE_TABLE_COLUMN_TEMPLATE_BODY = "template_body";
    
    
    //create SMS table String
    private static final String CREATE_SMS_TABLE =
                "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" 
                + MESSAGE_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + " TEXT, "
                + MESSAGE_TABLE_COLUMN_MESSAGE_BODY + " TEXT, "
                + MESSAGE_TABLE_COLUMN_STATUS + " TEXT"
                + ");";
    
    //create receiver table string
    private static final String CREATE_RECEIVER_TABLE =
		        "CREATE TABLE " + RECEIVER_TABLE_NAME + " (" 
		        + RECEIVER_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		        + RECEIVER_TABLE_MESSAGE_ID + " TEXT, "
		        + RECEIVER_TABLE_COLUMN_CONTACT_NUMBER + " TEXT, " 
		        + RECEIVER_TABLE_COLUMN_RECEIVER_NAME + " TEXT, "
		        + RECEIVER_TABLE_COLUMN_RECEIVER_IMAGE + " BLOB"
		        + ");";
    
    //create template table string
    private static final String CREATE_TEMPLATE_TABLE =
		        "CREATE TABLE " + TEMPLATE_TABLE_NAME + " (" 
		        + TEMPLATE_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		        + TEMPLATE_TABLE_COLUMN_TEMPLATE_BODY + " TEXT UNIQUE" 
		        + ");";
    
    SQLiteDatabase SMSSchedulerDBObject;

    /**=====================================================================
     * constructor
     * @param context
     *======================================================================*/
    SMSSchedulerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context =context; 
    }

    /**=====================================================================
     * method onCreate
     *======================================================================*/
    @Override
    public void onCreate(SQLiteDatabase db) {
    	isNewInstallation = true;
    	
    	db.execSQL(CREATE_SMS_TABLE);
        db.execSQL(CREATE_RECEIVER_TABLE);
        db.execSQL(CREATE_TEMPLATE_TABLE);
       
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
    		
    		if(isNewInstallation) {
    			prefillTemplateTable();
    		}
    		return true;
    	}catch(SQLiteException sqle){
    		return false;
    	}
    }//end method checkOrCreateDB
    
    /**=====================================================================
     * method addMessage
     *======================================================================*/
    public long addMessage(String message, String scheduledTime) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	ContentValues messageValues = new ContentValues();
        messageValues.put(MESSAGE_TABLE_COLUMN_SCHEDULED_TIME, scheduledTime);
        messageValues.put(MESSAGE_TABLE_COLUMN_MESSAGE_BODY, message);
        messageValues.put(MESSAGE_TABLE_COLUMN_STATUS, Constant.STATUS_SCHEDULED);
        
        long resultRow = -1;
        
        try {
        	resultRow = SMSSchedulerDBObject.insertOrThrow(MESSAGE_TABLE_NAME, null, messageValues);
        }catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addMessage -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
    	return resultRow;
    }//end method addMessage
    
    /**=====================================================================
     * method updateMessage
     *======================================================================*/
    public int updateMessage(long idOfMessage, String message, String scheduledTime, int statusOfMessage) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	ContentValues messageValues = new ContentValues();
    	messageValues.put(MESSAGE_TABLE_COLUMN_ID, idOfMessage);
    	messageValues.put(MESSAGE_TABLE_COLUMN_SCHEDULED_TIME, scheduledTime);
        messageValues.put(MESSAGE_TABLE_COLUMN_MESSAGE_BODY, message);
        messageValues.put(MESSAGE_TABLE_COLUMN_STATUS, statusOfMessage);
        
        int numberOfRowsAffected = 0;
        
        try {
        	numberOfRowsAffected = SMSSchedulerDBObject.update(MESSAGE_TABLE_NAME, messageValues, MESSAGE_TABLE_COLUMN_ID + "=" + idOfMessage, null);
        }catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> updateMessage -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
    	return numberOfRowsAffected;
    }//end method updateMessage
    
    /**=====================================================================
     * method retrieveMessages
     *======================================================================*/
    public ArrayList<Message> retrieveMessages(long time, int status) {
    	
    	String selection = null;
    	String orderBy = null;
    	
    	if(time == Constant.ALL_TIME && status == Constant.STATUS_ALL) {
    		selection = null;
    	} else if (time == Constant.ALL_TIME && status == Constant.STATUS_SCHEDULED) {
    		selection = MESSAGE_TABLE_COLUMN_STATUS + "=" + Constant.STATUS_SCHEDULED;
    		orderBy = "" + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + " ASC";
    	} else if (time == Constant.ALL_TIME && status == Constant.STATUS_SENT) {
    		selection = MESSAGE_TABLE_COLUMN_STATUS + "=" + Constant.STATUS_SENT;
    		orderBy = "" + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + " DESC";
    	//if here means time != ALL_TIME
    	} else if(status == Constant.STATUS_SCHEDULED) {
    		selection = MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + "<=" + time + " and " + MESSAGE_TABLE_COLUMN_STATUS + "=" + Constant.STATUS_SCHEDULED;
    	} else {
    		Toast.makeText(context, "in SMSSchedulerDBHelper -> in retrieve Message -> unhandled type", Toast.LENGTH_LONG).show();
    	}
    	  
    	SMSSchedulerDBObject = getReadableDatabase();

		Cursor messagesCursor = SMSSchedulerDBObject.query(MESSAGE_TABLE_NAME, null, selection, null, null, null, orderBy);
       	
		ArrayList<Message> messagesList = new ArrayList<Message>();
		
		if (messagesCursor.moveToFirst()) {
			do {
				Message messageObject = new Message();
				messageObject.id = messagesCursor.getInt(0);
				messageObject.scheduledTimeInMilliSecond = messagesCursor.getLong(1);
				messageObject.messageBody = messagesCursor.getString(2);
				messageObject.status = messagesCursor.getInt(3);
				
				messagesList.add(messageObject);
			} while (messagesCursor.moveToNext());
		}else {
			messagesList = null;
		}
		if (messagesCursor != null && !messagesCursor.isClosed()) {
			messagesCursor.close();
		}
		
		SMSSchedulerDBObject.close();
		
		return messagesList;

    }//end method retrieveMessages
    
        
    /**=====================================================================
     * method delete message
     *======================================================================*/
    public boolean deleteMessage(long idOfClickedMessage) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	int affectedRow = 0;
        
        try {
        	affectedRow = SMSSchedulerDBObject.delete(RECEIVER_TABLE_NAME, RECEIVER_TABLE_MESSAGE_ID + "=" + idOfClickedMessage , null);
        	affectedRow = SMSSchedulerDBObject.delete(MESSAGE_TABLE_NAME, MESSAGE_TABLE_COLUMN_ID + "=" + idOfClickedMessage , null);
        }catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> deleteMessage -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
        
        if(affectedRow == 0) {
        	return false;
        }else{
        	return true;
        }
    }//end method deleteMessage
    
    /**=====================================================================
     * method add Receivers
     *======================================================================*/
    public void addReceivers(long messageId, ArrayList<Receiver> receiversList) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    
    	for(int i=0; i<receiversList.size(); i++) {
    		
    		String contactNumber = receiversList.get(i).getPhoneNumber();
    		String displayName = receiversList.get(i).getDisplayName();
    		Bitmap image = receiversList.get(i).getContactImage();
    		byte[] imageByteArray = null;
    		if(image != null){
    			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        		image.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
        		imageByteArray = baos.toByteArray();  
    		}
    		
    		ContentValues contactValues = new ContentValues();
            
    		
    		contactValues.put(RECEIVER_TABLE_MESSAGE_ID, messageId);
            contactValues.put(RECEIVER_TABLE_COLUMN_CONTACT_NUMBER, contactNumber);
            contactValues.put(RECEIVER_TABLE_COLUMN_RECEIVER_NAME, displayName);
            contactValues.put(RECEIVER_TABLE_COLUMN_RECEIVER_IMAGE, imageByteArray);
            
            try {
            	SMSSchedulerDBObject.insertOrThrow(RECEIVER_TABLE_NAME, null, contactValues);
            }catch(SQLException sqle) {
            	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addContacts -> in catch", "SQLException has occurred" + sqle);
            }
    	}//end for
    	
    	SMSSchedulerDBObject.close();
    	
    }//end method addReceivers
    
    /**=====================================================================
     * method update Receivers
     *======================================================================*/
    public void updateReceivers(long messageId, ArrayList<Receiver> receiversList) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    
    	
        try {
        	SMSSchedulerDBObject.delete(RECEIVER_TABLE_NAME, RECEIVER_TABLE_MESSAGE_ID + "=" + messageId , null);
        
        	for(int i=0; i<receiversList.size(); i++) {
        		
        		String contactNumber = receiversList.get(i).getPhoneNumber();
        		String displayName = receiversList.get(i).getDisplayName();
        		Bitmap image = receiversList.get(i).getContactImage();
        		byte[] imageByteArray = null;
        		if(image != null){
        			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
            		image.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
            		imageByteArray = baos.toByteArray();  
        		}
        		ContentValues contactValues = new ContentValues();
                
        		
        		contactValues.put(RECEIVER_TABLE_MESSAGE_ID, messageId);
                contactValues.put(RECEIVER_TABLE_COLUMN_CONTACT_NUMBER, contactNumber);
                contactValues.put(RECEIVER_TABLE_COLUMN_RECEIVER_NAME, displayName);
                contactValues.put(RECEIVER_TABLE_COLUMN_RECEIVER_IMAGE, imageByteArray);
                
            	SMSSchedulerDBObject.insertOrThrow(RECEIVER_TABLE_NAME, null, contactValues);
                
        	}//end for
 
        } catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> updateReceivers -> in catch", "SQLException has occurred" + sqle);
        } finally {
        	SMSSchedulerDBObject.close();
        }
    }//end method updateReceivers
    
    
    /**=====================================================================
     * method retrieveReceivers
     *======================================================================*/
    public ArrayList<Receiver> retrieveReceivers(long messageId) {
    	  
    	SMSSchedulerDBObject = getReadableDatabase();

		Cursor receiversCursor = SMSSchedulerDBObject.query(RECEIVER_TABLE_NAME, null, RECEIVER_TABLE_MESSAGE_ID + "=" + messageId, null, null, null, null);
       	
		ArrayList<Receiver> receiversList = new ArrayList<Receiver>();
		
		if (receiversCursor.moveToFirst()) {
			do {
				Receiver receiverObject = new Receiver();
				receiverObject.setPhoneNumber(receiversCursor.getString(2));
				receiverObject.setDisplayName(receiversCursor.getString(3));
				byte[] imageByteArray = receiversCursor.getBlob(4);
				Bitmap image = null;
				
				if(imageByteArray != null) {
					image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length); 
				}
				receiverObject.setContactImage(image);
				
				receiversList.add(receiverObject);
			} while (receiversCursor.moveToNext());
		}else {
			receiversList = null;
		}
		if (receiversCursor != null && !receiversCursor.isClosed()) {
			receiversCursor.close();
		}
		SMSSchedulerDBObject.close();
		
		return receiversList;

    }//end method retrieveReceivers

    
    /**=====================================================================
     * method findNextSMSScheduledTime
     *======================================================================*/
    public long findNextSMSScheduledTime(long currentTime) {
    	  
    	SMSSchedulerDBObject = getReadableDatabase();
    	
    	String nextScheduledTimeQuery = "select MIN(" + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + ")"
    									+ " from " + MESSAGE_TABLE_NAME
    									+ " where " + MESSAGE_TABLE_COLUMN_STATUS + "!=" + Constant.STATUS_SENT
    									+ " and " + MESSAGE_TABLE_COLUMN_SCHEDULED_TIME + ">=" + currentTime;	
    		
		Cursor nextTimeCursor = SMSSchedulerDBObject.rawQuery(nextScheduledTimeQuery, null);
       	
		long nextScheduledTime;
		
		if (nextTimeCursor.moveToFirst()) {
			do {
				nextScheduledTime = nextTimeCursor.getLong(0);
			} while (nextTimeCursor.moveToNext());
		}else {
			nextScheduledTime = Constant.NO_NEXT_SCHEDULED_TIME;
		}
		if (nextTimeCursor != null && !nextTimeCursor.isClosed()) {
			nextTimeCursor.close();
		}
		SMSSchedulerDBObject.close();
		
		return nextScheduledTime;

    }//end method findNextSMSScheduledTime
    
    
    /**=====================================================================
     * method prefillTemplateTable
     *======================================================================*/
    public void prefillTemplateTable() {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    
    	String[] templateArray = context.getResources().getStringArray(R.array.template_array);
    	
    	for(int i=0; i<templateArray.length; i++) {
    		
    		String templateBody = templateArray[i];
    		
    		ContentValues contactValues = new ContentValues();
            
    		contactValues.put(TEMPLATE_TABLE_COLUMN_TEMPLATE_BODY, templateBody);
            
            try {
            	SMSSchedulerDBObject.insertOrThrow(TEMPLATE_TABLE_NAME, null, contactValues);
            }catch(SQLException sqle) {
            	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> prefillTemplateTable -> in catch", "SQLException has occurred" + sqle);
            }
    	}//end for
    	
    	SMSSchedulerDBObject.close();
    	
    }//end method prefillTemplateTable
    
    
    /**=====================================================================
     * method addTemplate
     *======================================================================*/
    public long addTemplate(String templateBody) {
    	  
    	SMSSchedulerDBObject = getWritableDatabase();
    	
    	ContentValues templateValues = new ContentValues();
        templateValues.put(TEMPLATE_TABLE_COLUMN_TEMPLATE_BODY, templateBody);
        
        long resultRow = -1;
        
        try {
        	resultRow = SMSSchedulerDBObject.insertOrThrow(TEMPLATE_TABLE_NAME, null, templateValues);
        } catch(SQLiteConstraintException sqlce) {
        	resultRow = Constant.TEMPLATE_ALREADY_EXIST;
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addTemplate -> in catch", "SQLiteConstraintException has occurred" + sqlce);
        } catch(SQLException sqle) {
        	Log.v("in SMSScheduler -> in SMSSchedulerDBHelper -> addTemplate -> in catch", "SQLException has occurred" + sqle);
        }finally {
        	SMSSchedulerDBObject.close();
        }
    	return resultRow;
    }//end method addTemplate
    
    
    /**=====================================================================
     * method retrieveTemplates
     *======================================================================*/
    public ArrayList<String> retrieveTemplates() {
    	
    	SMSSchedulerDBObject = getReadableDatabase();

		Cursor templatesCursor = SMSSchedulerDBObject.query(TEMPLATE_TABLE_NAME, null, null, null, null, null, null);
       	
		ArrayList<String> templatesList = new ArrayList<String>();
		
		if (templatesCursor.moveToFirst()) {
			do {
				String templateBody = templatesCursor.getString(1);
				
				templatesList.add(templateBody);
			} while (templatesCursor.moveToNext());
		}else {
			templatesList = null;
		}
		if (templatesCursor != null && !templatesCursor.isClosed()) {
			templatesCursor.close();
		}
		
		SMSSchedulerDBObject.close();
		
		return templatesList;

    }//end method retrieveTemplate
    
}//end class SMSSchedulerDBHelper

