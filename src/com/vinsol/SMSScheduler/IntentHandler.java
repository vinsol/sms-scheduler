package com.vinsol.SMSScheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class IntentHandler { 
	
	void gotoScheduleSMSPage(Context context) {
		Intent intent = new Intent(context, ScheduleSMS.class);
		intent.putExtra(Constant.TYPE_OF_SCHEDULE_SMS_PAGE, Constant.PAGE_TYPE_ADD);
		((Activity) context).finish();
		context.startActivity(intent);
	}
	
	void gotoScheduleSMSEditPage(Context context) {
		Intent intent = new Intent(context, ScheduleSMS.class);
		intent.putExtra(Constant.TYPE_OF_SCHEDULE_SMS_PAGE, Constant.PAGE_TYPE_EDIT);
		((Activity) context).finish();
		context.startActivity(intent);
	}
	
	void gotoSMSListingTabActivity(Context context, int selectedTab) {
		Intent intent = new Intent(context, SMSListingTabActivity.class);
		intent.putExtra(Constant.SMS_LISTING_TAB_ACTIVITY_SELCTED_TAB, selectedTab);
		((Activity) context).finish();
		context.startActivity(intent);
	}
}