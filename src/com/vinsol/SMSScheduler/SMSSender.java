package com.vinsol.SMSScheduler;

import java.util.ArrayList;

import android.content.Context;
import android.telephony.SmsManager;

public class SMSSender {
	
	/**===================================================================
	 * sends an SMS message to another device---
	 * @param phoneNumber
	 * @param message
	 *====================================================================*/
    void sendSMS(Context context, ArrayList<Receiver> receiversList, Message message) {
    	for(Receiver receiver:receiversList) {
    		SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(receiver.getPhoneNumber(), null, message.messageBody, null, null);
		}
    	//updating message status
		new SMSSchedulerDBHelper(context).updateMessage(message.id, message.messageBody, "" + message.scheduledTimeInMilliSecond, Constant.STATUS_SEND);
		               
    }//end method sendSMS()
}//end class SMSSender
