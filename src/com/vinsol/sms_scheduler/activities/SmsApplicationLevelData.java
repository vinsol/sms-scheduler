package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import com.vinsol.sms_scheduler.models.MyContact;
import android.app.Activity;
import android.app.Application;

public class SmsApplicationLevelData extends Application{

	static ArrayList<MyContact> contactsList = new ArrayList<MyContact>();
	protected static boolean isDataLoaded = false;
	protected static String DIALOG_CONTROL_ACTION = "com.vinsol.sms_scheduler.DIALOG_CONTROL_ACTION";
	Activity myActivity = new Activity();
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
	}	
		
	
}
