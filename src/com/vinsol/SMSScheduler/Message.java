package com.vinsol.SMSScheduler;

public class Message {
	int id;
	long scheduledTimeInMilliSecond;
	String messageBody;
	int status;
	
	public String getStatusInString() {
		String statusInString = null;
		if(status == Constant.STATUS_SCHEDULED) {
			statusInString = Constant.STATUS_SCHEDULED_STRING;
		} else if(status == Constant.STATUS_SEND) {
			statusInString = Constant.STATUS_SEND_STRING;
		}
		return statusInString;
	}
	
	
}//end class Message