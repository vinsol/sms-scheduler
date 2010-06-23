package com.vinsol.SMSScheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


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
	
	public String getDateString() {
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy ");
                
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(scheduledTimeInMilliSecond);
        
        return formatter.format(calendar.getTime());
	}
	
	public String getTimeString() {
		DateFormat formatter = new SimpleDateFormat("hh:mm");
        
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(scheduledTimeInMilliSecond);
        
        return formatter.format(calendar.getTime());
	}
}//end class Message