package com.vinsol.sms_scheduler.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.utils.Log;

public class ScheduleNewSms extends AbstractScheduleSms {
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dataLoadWaitDialog = new Dialog(ScheduleNewSms.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		Recipients.clear();
		
		setFunctionalities();
		setSuperFunctionalities();
		loadGroupsData();
		
		myAutoCompleteAdapter = (AutoCompleteAdapter) new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
	}
	
	
	
	private void setFunctionalities() {
		
		numbersText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL) {
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i < Recipients.size(); i++) {
	                	 len = len + Recipients.get(i).displayName.length();
	                	 if(i!=0) {
	                		 len = len + 2;
	                	 }
	                	 if(pos <= len) {
	                		 for(int j = 0; j < nativeGroupData.size(); j++){
	                			 for(int k = 0; k< nativeChildData.get(j).size(); k++) {
	                				 if((Long)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(i).contactId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 for(int j = 0; j< privateGroupData.size(); j++){
	                			 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                				 if((Long)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(i).contactId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 Log.d("coming into IF to uncheck a private group child");
	                					 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 Recipients.remove(i);
	                		 refreshSpannableString(false);
	                		 break;
	                	 }
	                 }
	            }
				return false;
			}
		});

		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onScheduleButtonPressTasks();
			}
		});
		
		
		
		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!messageText.getText().toString().matches("(''|[' ']*)") || !numbersText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(ScheduleNewSms.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Discard this message?");
					
					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ok_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancel_dialog_states));
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							ScheduleNewSms.this.finish();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
						}
					});
					d.show();
				}else{
					ScheduleNewSms.this.finish();
				}
			}
		});
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
}