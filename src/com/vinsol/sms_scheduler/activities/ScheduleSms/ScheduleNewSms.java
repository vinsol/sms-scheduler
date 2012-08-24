/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities.ScheduleSms;

import java.util.ArrayList;
import java.util.Date;

import android.app.Dialog;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.R;

public class ScheduleNewSms extends AbstractScheduleSms {
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FlurryAgent.logEvent("Schedule New Sms Activity Started");
		
		mode = 1;
		
		ArrayList<Boolean> weekBools = new ArrayList<Boolean>();
		
		defaultRepeatMode = 0;
		for(int i = 0; i< 7; i++)
			weekBools.add(false);
		
		
		//setting RepeatHash
		defaultRepeatHash.put(Constants.REPEAT_HASH_FREQ, 1);
		defaultRepeatHash.put(Constants.REPEAT_HASH_WEEK_BOOL, weekBools);
		defaultRepeatHash.put(Constants.REPEAT_HASH_END_MODE, 1);
		defaultRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, 1);
		defaultRepeatHash.put(Constants.REPEAT_HASH_END_DATE, new Date());
		defaultRepeatHash.put(Constants.REPEAT_HASH_LAST_SENT_TIME, 0);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		widthOfContainerInDp = (int)((metrics.widthPixels - 159)/dpi);
		setSuperFunctionalities();
		loadGroupsData();
	}
	
	
	
	@Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    	
    	FlurryAgent.logEvent("New SMS");
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
	
	
	
	public void onBackPressed() {
		
		if(!(Recipients.size()==0) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
			final Dialog d = new Dialog(ScheduleNewSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("Schedule Message?");
			
			yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.yes_dialog_states));
			noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.no_dialog_states));
			
			yesButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					d.cancel();
					Toast.makeText(ScheduleNewSms.this, "Message Scheduled", Toast.LENGTH_SHORT).show();
					if(!checkDateValidity(processDate)){
						Toast.makeText(ScheduleNewSms.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
					}
					new AsyncScheduling().execute();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					d.cancel();
					ScheduleNewSms.this.finish();
				}
			});
			
			d.show();
		}else if(!(Recipients.size()==0) || !(messageText.getText().toString().matches("(''|[' ']*)"))){
			final Dialog d = new Dialog(ScheduleNewSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("Save as Draft?");
			
			yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.yes_dialog_states));
			noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.no_dialog_states));
			
			yesButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					d.cancel();
					new AsyncScheduling().execute();
					Toast.makeText(ScheduleNewSms.this, "Message saved as draft", Toast.LENGTH_SHORT).show();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				
				public void onClick(View v) {
					d.cancel();
					ScheduleNewSms.this.finish();
				}
			});
			
			d.show();
		}else{
			ScheduleNewSms.this.finish();
		}
	}


	
	protected void scheduleButtonOnClickListener() {
		onScheduleButtonPressTasks();
	}
}