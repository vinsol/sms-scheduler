package com.vinsol.SMSScheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTimeConverter {
	
	
	public String getDateString(long timeInMilliSecond) {
	
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy ");
                
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        
        return formatter.format(calendar.getTime());
	}
	
	public String getTimeString(long timeInMilliSecond) {
		DateFormat formatter = new SimpleDateFormat("hh:mm");
        
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        
        return formatter.format(calendar.getTime());
	}
}//end class DateTimeConverter