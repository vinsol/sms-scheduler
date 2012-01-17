package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import android.app.Application;

import com.vinsol.sms_scheduler.models.Contact;

public class SmsSchedulerApplication extends Application {

	public static ArrayList<Contact> contactsList = new ArrayList<Contact>();
	public static boolean isDataLoaded = false;
}
