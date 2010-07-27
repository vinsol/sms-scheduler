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

		TabHost host = getTabHost();
		host.addTab(host.newTabSpec("one").setIndicator("Scheduled SMS").setContent(new Intent(this, SMSListing.class)));
		host.addTab(host.newTabSpec("two").setIndicator("Sent SMS").setContent(new Intent(this, SMSListing.class)));

	}
}