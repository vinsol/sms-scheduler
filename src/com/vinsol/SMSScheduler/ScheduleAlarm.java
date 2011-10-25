package com.vinsol.SMSScheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ScheduleAlarm {
	
	/**========================================================================
	 * method scheduleNextAlarm()
	 * @param context
	 * @param currentTime
	 *=========================================================================*/
	void scheduleAlarm(Context context, long currentTime) {
			
		long nextScheduledTime = new SMSSchedulerDBHelper(context).findNextSMSScheduledTime(currentTime);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextScheduledTime, pi);
	}//end method scheduleAlarm()
	
	
	
	
}//end class ScheduleAlarmService