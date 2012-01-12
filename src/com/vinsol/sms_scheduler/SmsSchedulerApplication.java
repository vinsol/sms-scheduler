package com.vinsol.sms_scheduler;

import java.util.ArrayList;
import com.vinsol.sms_scheduler.models.Contact;
import android.app.Activity;
import android.app.Application;

public class SmsSchedulerApplication extends Application{

	public static ArrayList<Contact> contactsList = new ArrayList<Contact>();
	public static boolean isDataLoaded = false;
	public static String DIALOG_CONTROL_ACTION = "com.vinsol.sms_scheduler.DIALOG_CONTROL_ACTION";
	Activity myActivity = new Activity();
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
	}	
		
	
}
