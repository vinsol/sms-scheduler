package com.vinsol.SMSScheduler;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class SMSListingTabActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Intent intentForScheduledSMS = new Intent(this, SMSListing.class);
		intentForScheduledSMS.putExtra(Constant.TYPE_OF_SMS_LISTING_PAGE, Constant.PAGE_TYPE_SCHEDULED);
		
		Intent intentForSentSMS = new Intent(this, SMSListing.class);
		intentForSentSMS.putExtra(Constant.TYPE_OF_SMS_LISTING_PAGE, Constant.PAGE_TYPE_SENT);

		TabHost host = getTabHost();
		host.addTab(host.newTabSpec("one").setIndicator("Scheduled SMS").setContent(intentForScheduledSMS));
		host.addTab(host.newTabSpec("two").setIndicator("Sent SMS").setContent(intentForSentSMS));

	}
}