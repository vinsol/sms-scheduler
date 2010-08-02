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
		new SMSSchedulerDBHelper(context).updateMessage(message.id, message.messageBody, "" + message.scheduledTimeInMilliSecond, Constant.STATUS_SENT);
		               
    }//end method sendSMS()
    
    
/*	void sendSMS(final Context context, ArrayList<Receiver> receiversList, Message message) {
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 	
 		//PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
 		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);        
 	
    	for(Receiver receiver:receiversList) {
    		Intent sentIntent = new Intent(SENT);
    		//sentIntent.putExtra("message", message.messageBody);
    		//sentIntent.putExtra("receiver", receiver.getPhoneNumber());
    		
    		Uri uri = Uri.parse(message.messageBody + ", " + receiver.getPhoneNumber());
    		sentIntent.setData(uri);
    		
    		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);
    		
    		SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(receiver.getPhoneNumber(), null, message.messageBody, sentPI, deliveredPI);
		}
    	//updating message status
		new SMSSchedulerDBHelper(context).updateMessage(message.id, message.messageBody, "" + message.scheduledTimeInMilliSecond, Constant.STATUS_SENT);
		               
    }//end method sendSMS()
*/             
}//end class SMSSender
