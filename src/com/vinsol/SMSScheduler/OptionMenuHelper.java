package com.vinsol.SMSScheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class OptionMenuHelper {
	
	public boolean createOptionMenu(Menu menu){
		menu.add(0, R.id.OPTION_MENU_SCHEDULE_SMS, 0, "SCHEDULE SMS");
		menu.add(0, R.id.OPTION_MENU_SMS_LISTING, 0, "SMS LISTING");
		menu.add(0, R.id.OPTION_MENU_ABOUT, 0, "ABOUT");
		menu.add(0, R.id.OPTION_MENU_HELP, 0, "HELP");
		menu.add(0, R.id.OPTION_MENU_SETTINGS, 0, "SETTING");
	    return true;
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(Context ctx, MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.OPTION_MENU_SCHEDULE_SMS: {
		    	Intent i = new Intent(ctx, ScheduleSMS.class);
		    	((Activity) ctx).finish();
		    	ctx.startActivity(i);
		    	return true;
		    }
		    case R.id.OPTION_MENU_SMS_LISTING: {
		    	Intent i = new Intent(ctx, SMSListing.class);
		    	((Activity) ctx).finish();
		    	ctx.startActivity(i);
		    	return true;
		    }
		    case R.id.OPTION_MENU_ABOUT: {
		    	Toast.makeText(ctx, "Under Development :)", Toast.LENGTH_SHORT).show();
		    	return true;
		    }
		    case R.id.OPTION_MENU_HELP: {
		    	Toast.makeText(ctx, "Under Development :)", Toast.LENGTH_SHORT).show();
		    	return true;
		    }
		    case R.id.OPTION_MENU_SETTINGS: {
		    	Toast.makeText(ctx, "Under Development :)", Toast.LENGTH_SHORT).show();
		    	return true;
		    }
		        
	    }	
	    return false;
	}   
}