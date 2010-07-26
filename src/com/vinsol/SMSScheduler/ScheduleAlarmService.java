package com.vinsol.SMSScheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class ScheduleAlarmService {

	void scheduleAlarm(Context context){
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//alarmManager.set(AlarmManager.RTC_WAKEUP, timeForAlarm, pi);
	}
	
}//end class ScheduleAlarmService