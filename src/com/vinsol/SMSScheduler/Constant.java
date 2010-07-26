package com.vinsol.SMSScheduler;

public class Constant {
	
	/**============================================
	 * Status Of Message
	 *=============================================*/
	public static final int STATUS_SCHEDULED = 1;
	public static final int STATUS_SEND = 2;
	public static final int STATUS_ALL = 3;
	
	public static final String STATUS_SCHEDULED_STRING = "SCHEDULED";
	public static final String STATUS_SEND_STRING = "SEND";
	
	/**============================================
	 * Schedule SMS Page Type
	 *=============================================*/
	public static final String TYPE_OF_SCHEDULE_SMS_PAGE = "pageType";
	
	public static final int PAGE_TYPE_ADD = 1;
	public static final int PAGE_TYPE_EDIT = 2;
	
	/**============================================
	 * Strings for HashMap for message listing
	 *=============================================*/
	public static final String MESSAGE_BODY = "body";
	public static final String SCHEDULED_DATE = "date";
	public static final String SCHEDULED_TIME = "time";
	public static final String STATUS = "status";
	
	/**============================================
	 * other Strings 
	 *=============================================*/
	public static final String UNKNOWN_NAME = "Unknown Name";
	
	/**============================================
	 * other Constants 
	 *=============================================*/
	public static final long ALL_TIME = -1;
	public static final long NO_NEXT_SCHEDULED_TIME = -1;
	
}//end class Constants