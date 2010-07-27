package com.vinsol.SMSScheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Intent alarmHandlerServiceIntent = new Intent(context, AlarmHandlerService.class);
			context.startService(alarmHandlerServiceIntent);
		} catch (Exception e) {
			Log.e("In SMSScheduler -> in AlarmReceiver -> in onReceive", "Exception has occurred " + e);
		}
	}
}//end class AlarmReceiver
