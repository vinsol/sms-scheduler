/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler;

public class Constants {

	public static final int GENERIC_DEFAULT_INT_VALUE = -1;
	
	//------------ Statics for HashMap to store detail of Contact Groups to show in Expandable list --------------
	public static final String GROUP_NAME = "group_name";
	public static final String GROUP_CHECK = "group_check";
	public static final String GROUP_IMAGE = "group_image";
	public static final String GROUP_TYPE = "group_type";
	public static final String GROUP_ID = "group_id";
	
	public static final String CHILD_NAME = "child_name";
	public static final String CHILD_CHECK = "child_check";
	public static final String CHILD_NUMBER = "child_number";
	public static final String CHILD_IMAGE = "child_contact_image";
	public static final String CHILD_CONTACT_ID = "child_contact_id";
	//---------------------------------------------------------------------------------------------
	
	
	public static final String PRIVATE_SMS_ACTION = "com.smsschedulerexpl.android.private_sms_action";
	public static final String PRIVATE_INTENT_ACTION = "com.smsschedulerexpl.android.private_intent_action";
	public static final String DIALOG_CONTROL_ACTION = "com.vinsol.sms_scheduler.DIALOG_CONTROL_ACTION";
	
	//---------------- Statics for HashMap to store detail of Repeat mode ------------------
	//-----------------  which is serialized and stored in Shared prefs. -------------------
	public static final String REPEAT_HASH_FREQ = "repeat_hash_frequency";
	public static final String REPEAT_HASH_WEEK_BOOL = "repeat_hash_week_boolean";
	public static final String REPEAT_HASH_END_MODE = "repeat_hash_end_mode";
	public static final String REPEAT_HASH_END_FREQ = "repeat_hash_end_frequency";
	public static final String REPEAT_HASH_END_DATE = "repeat_hash_end_date";
	public static final String REPEAT_HASH_LAST_SENT_TIME = "repeat_hash_last_sent_time";
	//--------------------------------------------------------------------------------------
	
	//--------------Repeat and End modes-------------------------------------------------
	public static final int REPEAT_MODE_NO_REPEAT = 0;
	public static final int REPEAT_MODE_DAILY = 1;
	public static final int REPEAT_MODE_WEEKLY = 2;
	public static final int REPEAT_MODE_MONTHLY = 3;
	public static final int REPEAT_MODE_YEARLY = 4;
	
	public static final int END_MODE_NEVER = 0;
	public static final int END_MODE_AFTER = 1;
	public static final int END_MODE_ON	   = 2;
	//-------------------------------------------------------------------------------------
	
	//-------------Statics for SMS status -----------------------
	public static final int SMS_STATUS_DRAFT = 0;
	public static final int SMS_STATUS_SCHEDULED = 1;
	public static final int SMS_STATUS_SENT = 2;
	public static final int SMS_STATUS_DELIVERED = 3;
	//-----------------------------------------------------------
	
	//------------Statics for Recipients Operated Flag-----------
	public static final int RECIPIENT_OPERATED_FLAG_SET = 1;
	public static final int RECIPIENT_OPERATED_FLAG_UNSET = 0;
	//-----------------------------------------------------------
	
	//-----------------Statics for Recipient Defaults -----------------------------
	public static final int DEFAULT_RECIPIENT_ID = -1;
	public static final int RECIPIENT_TYPE_NUMBER = 1;
	public static final int RECIPIENT_TYPE_CONTACT = 2;
	public static final int RECIPIENT_CONTACT_ID_FOR_NUMBER = -1;
	//-----------------------------------------------------------------------------
	
	//-----------------Statics for group types --------------------------
	public static final int GROUP_TYPE_NATIVE = 1;
	public static final int GROUP_TYPE_PRIVATE = 2;
}