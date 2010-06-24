package com.vinsol.SMSScheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarDateConverter {
	
	static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	static DateFormat timeFormatter = new SimpleDateFormat("hh:mm");
	
	/**====================================================================
	 * method getDateString
	 *=====================================================================*/
	public static String getDateString(long timeInMilliSecond) {
		
		Calendar calendar = Calendar.getInstance();     
		calendar.setTimeInMillis(timeInMilliSecond);
        
        return getDateString(calendar);
	}//end method getDateString
	
	/**====================================================================
	 * method getDateString
	 *=====================================================================*/
	public static String getTimeString(long timeInMilliSecond) {
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        
        return getTimeString(calendar);
	}//end method getTimeString
	
	/**====================================================================
	 * method getDateString
	 *=====================================================================*/
	public static String getDateString(Calendar calendar) {
		
        return dateFormatter.format(calendar.getTime());
	}//end method getDateString
	
	/**====================================================================
	 * method getDateString
	 *=====================================================================*/
	public static String getTimeString(Calendar calendar) {
		
        return timeFormatter.format(calendar.getTime());
	}//end method getTimeString
}//end class CalendarDateConverter