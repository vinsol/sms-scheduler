/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.utils;

public class Log  {
	
	private static boolean DEBUG = false;
    private static String app = "SMSScheduler";

    public static final void d(Throwable throwable) {
    	if (DEBUG)
    		android.util.Log.d(app, "", throwable);
    }

    public static final void d(Object object) {
    	if (DEBUG)
    		android.util.Log.d(app, object!=null ? object.toString() : null);
    }

    public static final void d(Object object, Throwable throwable) {
    	if (DEBUG)
    		android.util.Log.d(app, object!=null ? object.toString() : null, throwable);
    }
}