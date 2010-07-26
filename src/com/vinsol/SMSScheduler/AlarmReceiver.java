package com.vinsol.SMSScheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Intent sendSMSServiceIntent = new Intent(context, AlarmHandlerService.class);
			context.startService(sendSMSServiceIntent);
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}//end class AlarmReceiver
