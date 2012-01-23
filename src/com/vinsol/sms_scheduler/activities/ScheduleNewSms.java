/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.R;

public class ScheduleNewSms extends AbstractScheduleSms {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mode = 1;
	}
	
	
	@Override
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
				
				@Override
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
				
				@Override
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
				@Override
				public void onClick(View v) {
					d.cancel();
					new AsyncScheduling().execute();
					Toast.makeText(ScheduleNewSms.this, "Message saved as draft", Toast.LENGTH_SHORT).show();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				@Override
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


	@Override
	protected void scheduleButtonOnClickListener() {
		onScheduleButtonPressTasks();
	}
}