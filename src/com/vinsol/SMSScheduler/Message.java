package com.vinsol.SMSScheduler;

public class Message {
	long id;
	long scheduledTimeInMilliSecond;
	String messageBody;
	int status;
	
	public String getStatusInString() {
		String statusInString = null;
		if(status == Constant.STATUS_SCHEDULED) {
			statusInString = Constant.STATUS_SCHEDULED_STRING;
		} else if(status == Constant.STATUS_SENT) {
			statusInString = Constant.STATUS_SEND_STRING;
		}
		return statusInString;
	}
	
	
}//end class Message