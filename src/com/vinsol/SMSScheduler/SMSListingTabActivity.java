package com.vinsol.SMSScheduler;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class SMSListingTabActivity extends TabActivity {
	int selectedTab;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	
		selectedTab = getIntent().getIntExtra(Constant.SMS_LISTING_TAB_ACTIVITY_SELCTED_TAB, Constant.SELECTED_TAB_SCHEDULED_SMS);
		
		Intent intentForScheduledSMS = new Intent(this, SMSListing.class);
		intentForScheduledSMS.putExtra(Constant.TYPE_OF_SMS_LISTING_PAGE, Constant.PAGE_TYPE_SCHEDULED);
		
		Intent intentForSentSMS = new Intent(this, SMSListing.class);
		intentForSentSMS.putExtra(Constant.TYPE_OF_SMS_LISTING_PAGE, Constant.PAGE_TYPE_SENT);

		TabHost host = getTabHost();
		host.addTab(host.newTabSpec("one").setIndicator("Scheduled SMS").setContent(intentForScheduledSMS));
		host.addTab(host.newTabSpec("two").setIndicator("Sent SMS").setContent(intentForSentSMS));
		
		if(selectedTab == Constant.SELECTED_TAB_SCHEDULED_SMS) {
			host.setCurrentTabByTag("one");
		} else if(selectedTab == Constant.SELECTED_TAB_SENT_SMS) {
			host.setCurrentTabByTag("two");
		}
	}
}