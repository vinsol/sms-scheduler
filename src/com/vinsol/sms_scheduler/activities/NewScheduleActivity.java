package com.vinsol.sms_scheduler.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;

public class NewScheduleActivity extends AbstractScheduleClass {
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent2) {
			if(dataLoadWaitDialog.isShowing()) {
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					Intent intent = new Intent(NewScheduleActivity.this, ContactsTabsActivity.class);
					intent.putExtra("IDSARRAY", idsString);
					intent.putExtra("ORIGIN", "new");
					toOpen = 0;
					startActivityForResult(intent, 2);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_schedule_layout);
		
		dataLoadWaitDialog = new Dialog(NewScheduleActivity.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		inputMethodManager = (InputMethodManager)getSystemService(NewScheduleActivity.this.INPUT_METHOD_SERVICE);
		
		numbersText 				= (AutoCompleteTextView) 	findViewById(R.id.new_numbers_text);
		addFromContactsImgButton 	= (ImageButton) 		 	findViewById(R.id.new_add_from_contact_imgbutton);
		dateButton 					= (Button) 					findViewById(R.id.new_date_button);
		characterCountText 			= (TextView) 				findViewById(R.id.new_char_count_text);
		messageText 				= (EditText) 				findViewById(R.id.new_message_space);
		templateImageButton 		= (ImageButton) 			findViewById(R.id.template_imgbutton);
		speechImageButton 			= (ImageButton) 			findViewById(R.id.speech_imgbutton);
		addTemplateImageButton 		= (ImageButton) 			findViewById(R.id.add_template_imgbutton);
		scheduleButton 				= (Button) 					findViewById(R.id.new_schedule_button);
		cancelButton 				= (Button) 					findViewById(R.id.new_cancel_button);
		smileysGrid					= (GridView) 				findViewById(R.id.smileysGrid);
		pastTimeDateLabel			= (LinearLayout) 			findViewById(R.id.past_time_label);
		
		
		Spans.clear();
		
		numbersText.setThreshold(1);
		
		
		// Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speechImageButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
        } else {
            speechImageButton.setEnabled(false);
        }
		//---------------------------------------------------------------------
		
        dataloadIntentFilter = new IntentFilter();
        dataloadIntentFilter.addAction(SmsApplicationLevelData.DIALOG_CONTROL_ACTION);
		
		setFunctionalities();
		setSuperFunctionalities();
		loadGroupsData();
		
		myAutoCompleteAdapter = (AutoCompleteAdapter) new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mDataLoadedReceiver);
	}
	
	
	
	public void setFunctionalities() {
		
		
	
		numbersText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL) {
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i < Spans.size(); i++) {
	                	 len = len + Spans.get(i).displayName.length();
	                	 if(i!=0) {
	                		 len = len + 2;
	                	 }
	                	 if(pos <= len) {
	                		 for(int j = 0; j < nativeGroupData.size(); j++){
	                			 for(int k = 0; k< nativeChildData.get(j).size(); k++) {
	                				 if((Long.parseLong((String)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Spans.get(i).entityId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 
	                		 for(int j = 0; j< privateGroupData.size(); j++){
	                			 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                				 if((Long.parseLong((String)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Spans.get(i).entityId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 Log.d("coming into IF to uncheck a private group child");
	                					 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 
	                		 Spans.remove(i);
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
				
				if(Spans.size()==0 && messageText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(NewScheduleActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Nothing to schedule");

					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.schedule_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.discard_dialog_states));
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							//NewScheduleActivity.this.finish();
							numbersText.requestFocus();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							NewScheduleActivity.this.finish();
						}
					});
					
					d.show();
				}else
				if(Spans.size()==0){
					final Dialog d = new Dialog(NewScheduleActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("No recipients added!");
					
					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.save_as_draft_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_recipients_dialog_states));
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							new AsyncScheduling().execute();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							numbersText.requestFocus();
						}
					});
					
					d.show();
					
				}else if(messageText.getText().toString().matches("(''|[' ']*)")){
						final Dialog d = new Dialog(NewScheduleActivity.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog_layout);
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("Message is blank!");
						
						yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.save_as_draft_dialog_states));
						noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.write_message_dialog_states));
						
						yesButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								d.cancel();
								new AsyncScheduling().execute();
							}
						});
						
						noButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								messageText.requestFocus();
								d.cancel();
							}
						});
						
						d.show();
				}else{
					new AsyncScheduling().execute();
				}
			}
		});
		
		
		
		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!messageText.getText().toString().matches("(''|[' ']*)") || !numbersText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(NewScheduleActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
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
							NewScheduleActivity.this.finish();
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
					NewScheduleActivity.this.finish();
				}
				
			}
		});
	}
	
	//--------------------function to Scheduling a new sms------------------------------------
	@Override
	public void doSmsScheduling(){
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		String dateString = sdf.format(cal.getTime());
		mdba.open();
		long groupId = mdba.getNextGroupId();
		ArrayList<String> numbers = new ArrayList<String>();
		
		Log.d(Spans.size()+"span size 877");
		
		if(Spans.size()==0){
			SpannedEntity span = new SpannedEntity(-1, 1, " ", -1, -1);  // for adding as a fake span to create a draft
			Spans.add(span);
		}
		
		for(int i = 0; i< Spans.size(); i++){
			if(Spans.get(i).type == 2){
			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
				if(Spans.get(i).entityId == Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
					numbers.add(SmsApplicationLevelData.contactsList.get(j).number);
					long received_id = mdba.scheduleSms(SmsApplicationLevelData.contactsList.get(j).number, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
					if(!Spans.get(i).displayName.equals(" ")){
						Log.d("entered to add to recents");
						mdba.addRecentContact(Spans.get(i).entityId, "");
					}
					
					
					Log.d("before if else");
					
					if(messageText.getText().toString().length() == 0){
						Log.d("inside messageText if else");
						mdba.setAsDraft(received_id);
					}else{
						if(!(Spans.size()==0) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
							if(mdba.getCurrentPiFiretime() == -1){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, groupId, received_id, cal.getTimeInMillis());
							}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, groupId, received_id, cal.getTimeInMillis());
							}
						}
					}
					Log.d("after if else");
					Spans.get(i).smsId = received_id;
					Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
					for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
						mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), Spans.get(i).groupTypes.get(k));
					}
				}
			}
			}else
			if(Spans.get(i).type == 1){
				long received_id = mdba.scheduleSms(Spans.get(i).displayName, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
				
				if((Spans.size()==1 && Spans.get(0).displayName.equals(" ")) || messageText.toString().matches("(''|[' ']*)")){
					mdba.setAsDraft(received_id);
				}else{
					mdba.addRecentContact(-1, Spans.get(i).displayName);
					if(mdba.getCurrentPiFiretime() == -1){
						handlePiUpdate(Spans.get(i).displayName, groupId, received_id, cal.getTimeInMillis());
					}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
						handlePiUpdate(Spans.get(i).displayName, groupId, received_id, cal.getTimeInMillis());
					}
				}
				Spans.get(i).smsId = received_id;
				Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
			}
		}
		mdba.close();
	}
	
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
            showMatchesDialog();
        }
        
        else if(resultCode == 2) {
        	idsString.clear();
        	refreshSpannableString(false);
        	numbersText.requestFocus();
        	numbersText.setSelection(numbersText.getText().toString().length());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	
		
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		
		if(!(Spans.size()==0) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
			final Dialog d = new Dialog(NewScheduleActivity.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog_layout);
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
					Toast.makeText(NewScheduleActivity.this, "Message Scheduled", Toast.LENGTH_SHORT).show();
					if(!checkDateValidity(processDate)){
						Toast.makeText(NewScheduleActivity.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
					}
					new AsyncScheduling().execute();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					d.cancel();
					NewScheduleActivity.this.finish();
				}
			});
			
			d.show();
			
			
		}else
			
		if(!(Spans.size()==0) || !(messageText.getText().toString().matches("(''|[' ']*)"))){
			final Dialog d = new Dialog(NewScheduleActivity.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog_layout);
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
					Toast.makeText(NewScheduleActivity.this, "Message saved as draft", Toast.LENGTH_SHORT).show();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					d.cancel();
					NewScheduleActivity.this.finish();
				}
			});
			
			d.show();
		}else{
			NewScheduleActivity.this.finish();
		}
	}
	
	public void loadGroupsData(){
		
		nativeGroupData.clear();
		nativeChildData.clear();
		
		privateGroupData.clear();
		privateChildData.clear();
		
		//------------------------ Setting up data for native groups ---------------------------
		String[] projection = new String[] {
				
				  Groups._ID,
	              Groups.TITLE,
	              Groups.SYSTEM_ID,
	              Groups.NOTES,
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        int count = 0;
        
        Cursor groupCursor = managedQuery(groupsUri, projection, null, null, null);
        if(groupCursor.moveToFirst()){
        	mdba.open();
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(Constants.GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		group.put(Constants.GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
       			group.put(Constants.GROUP_CHECK, false);
        		group.put(Constants.GROUP_TYPE, 1);
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        	
        		
        		nativeGroupData.add(group);
        		
        		for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsApplicationLevelData.contactsList.get(i).groupRowId.get(j)){
        					Log.d(group.get(Constants.GROUP_NAME) + " has " + SmsApplicationLevelData.contactsList.get(i).name);
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(i).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(i).number);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(i).image);
       						childParameters.put(Constants.CHILD_CHECK, false);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        					
        				}
        			}
        		}
        		nativeChildData.add(child);
        		count++;
        	}while(groupCursor.moveToNext());
        }
        
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		group.put(Constants.GROUP_CHECK, false);
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< Spans.size(); i++){
        			for(int j = 0; j< Spans.get(i).groupIds.size(); j++){
        				if((Spans.get(i).groupIds.get(j)==group.get(Constants.GROUP_ID)) && Spans.get(i).groupTypes.get(j) == 2){
        					group.put(Constants.GROUP_CHECK, true);
        					break;
        				}
        			}
        		}
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		boolean hasAChild = false;
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int m = 0; m< Spans.size(); m++){
        	        			for(int n = 0; n< Spans.get(m).groupIds.size(); n++){
        	        				if((Spans.get(m).groupIds.get(n)==group.get(Constants.GROUP_ID)) && (Spans.get(m).groupTypes.get(n) == 2) && (Spans.get(m).entityId == contactIds.get(i))){
        	        					group.put(Constants.GROUP_CHECK, true);
        	        					hasAChild = true;
        	        					break;
        	        				}
        	        			}
        	        		}
        					child.add(childParameters);
        				}
        			}
        		}
        		if(hasAChild){
        			group.put(Constants.GROUP_CHECK, true);
        		}else{
        			group.put(Constants.GROUP_CHECK, false);
        		}
        		privateGroupData.add(group);
        		
        		privateChildData.add(child);
        		count++;
        	}while(groupsCursor.moveToNext());
        }
        mdba.close();
	}
}