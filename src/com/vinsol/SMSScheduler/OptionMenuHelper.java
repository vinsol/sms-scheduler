package com.vinsol.SMSScheduler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

public class OptionMenuHelper {
	
	public boolean createOptionMenu(Menu menu, int forWhichActivity){
		if(forWhichActivity == Constant.OPTION_MENU_FOR_SCHEDULED_SMS) {
			menu.add(0, R.id.OPTION_MENU_SMS_LISTING, 0, "SMS LISTING");
		} else if(forWhichActivity == Constant.OPTION_MENU_FOR_SMS_LISTING) {
			menu.add(0, R.id.OPTION_MENU_SCHEDULE_SMS, 0, "SCHEDULE SMS");
		}
		menu.add(0, R.id.OPTION_MENU_HELP, 0, "HELP");
		menu.add(0, R.id.OPTION_MENU_ABOUT, 0, "ABOUT");
		//menu.add(0, R.id.OPTION_MENU_SETTINGS, 0, "SETTING");
	    return true;
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(Context context, MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.OPTION_MENU_SCHEDULE_SMS: {
		    	new IntentHandler().gotoScheduleSMSPage(context);
		    	return true;
		    }
		    case R.id.OPTION_MENU_SMS_LISTING: {
		    	new IntentHandler().gotoSMSListingTabActivity(context, Constant.SELECTED_TAB_SCHEDULED_SMS);
		    	return true;
		    }
		    case R.id.OPTION_MENU_ABOUT: {
		    	String titleOfDialog = context.getString(R.string.app_name);
		    	String messageOfDialog = context.getString(R.string.option_menu_about);
		    	
		    	new ShowAlertDialog(context, titleOfDialog, messageOfDialog).showDialog();
		    	return true;
		    }
		    case R.id.OPTION_MENU_HELP: {
		    	Intent helpIntent = new Intent();
		    	helpIntent.setAction(Intent.ACTION_VIEW);
		    	Uri uri = Uri.parse(context.getString(R.string.option_menu_help_uri));
		    	helpIntent.setData(uri);
		    	context.startActivity(helpIntent);
		    	return true;
		    }
		  /*  case R.id.OPTION_MENU_SETTINGS: {
		    	Toast.makeText(context, "Under Development :)", Toast.LENGTH_SHORT).show();
		    	return true;
		    }
		    */
		        
	    }	
	    return false;
	}   
}