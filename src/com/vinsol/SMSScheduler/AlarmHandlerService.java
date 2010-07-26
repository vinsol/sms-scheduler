package com.vinsol.SMSScheduler;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmHandlerService extends Service {
	
	/**===========================================================
	 * method onBind
	 *============================================================*/
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}//end method onBind
	
	/**===========================================================
	 * method onCreate
	 *============================================================*/
	@Override
    public void onCreate() {
		super.onCreate();
		
		long currentTime = Calendar.getInstance().getTimeInMillis();
		
		sendScheduledMessages(currentTime);
		
		new ScheduleAlarm().scheduleAlarm(this, currentTime);
		
		stopSelf();
	
	}//end method onCreate
	    
    void sendScheduledMessages(long currentTime) {
		ArrayList<Message> messageList = new SMSSchedulerDBHelper(this).retrieveMessages(currentTime, Constant.STATUS_SCHEDULED);
		
		if(!(messageList == null || messageList.isEmpty())) {
			//find the receivers of these messages and send messages
			for(Message message: messageList){
				ArrayList<Receiver> receiversList = new SMSSchedulerDBHelper(this).retrieveReceivers(message.id);
				new SMSSender().sendSMS(this, receiversList, message);
			}
		}
		
	}//end method sendUnsentMessages
}//end class SendSMSService