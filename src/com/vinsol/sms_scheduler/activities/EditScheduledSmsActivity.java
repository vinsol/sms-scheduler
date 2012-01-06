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
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;

public class EditScheduledSmsActivity extends AbstractScheduleClass {
	
	TextView 	headerText;
	
	long editedGroup;
	
	boolean smileyVisible = false;
	boolean isDraft = false;
	boolean isDeletingASpan = false;
	
	ArrayList<Long> smsIds;
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent2) {
//			Toast.makeText(SmsSchedulerExplActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					toOpen = 0;
					Intent intent = new Intent(EditScheduledSmsActivity.this, ContactsTabsActivity.class);
					intent.putExtra("ORIGIN", "edit");
					startActivityForResult(intent, 2);
				}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_schedule_layout);
		
		dataLoadWaitDialog = new Dialog(EditScheduledSmsActivity.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		inputMethodManager =(InputMethodManager)getSystemService(EditScheduledSmsActivity.this.INPUT_METHOD_SERVICE);
		
		headerText					= (TextView) 				findViewById(R.id.header);
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
		
		
		Intent intent = getIntent();
		
		numbersText.setText(intent.getStringExtra("NUMBER"));
		messageText.setText(intent.getStringExtra("MESSAGE"));
		originalMessage = intent.getStringExtra("MESSAGE");
		processDate = new Date(intent.getLongExtra("TIME", 0));
		characterCountText.setText(String.valueOf(messageText.getText().toString().length()));
		editedGroup = intent.getLongExtra("GROUP", 0);
		cancelButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.delete_footer_states));
		
		Log.d("group Id : " + editedGroup);
		
		Spans.clear();
		
		numbersText.setThreshold(1);
		
		mdba.open();
		smsIds = mdba.getIds(editedGroup);
		
		Log.d("size of smsIds : " + smsIds.size());
		
		originalSpans.clear();
		
		for(int i = 0; i< smsIds.size(); i++){
			Cursor spanCur = mdba.fetchSpanForSms(smsIds.get(i));
			Log.d("size of Span cursor : " + spanCur.getCount());
			spanCur.moveToFirst();
			Spans.add(new SpannedEntity(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ID)),
					spanCur.getInt(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_TYPE)),
					spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_DN)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ENTITY_ID)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_SMS_ID))));
			ArrayList<Long> groupIds = mdba.fetchGroupsForSpan(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ID)));
			ArrayList<Integer> groupTypes = mdba.fetchGroupTypesForSpan(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ID)));
			
			originalSpans.add(new SpannedEntity(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ID)),
					spanCur.getInt(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_TYPE)),
					spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_DN)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_ENTITY_ID)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_SMS_ID))));
			
			for(int k = 0; k < groupIds.size(); k++){
				Spans.get(i).groupIds.add(groupIds.get(k));
				Spans.get(i).groupTypes.add(groupTypes.get(k));
				originalSpans.get(i).groupIds.add(groupIds.get(k));
				originalSpans.get(i).groupTypes.add(groupTypes.get(k));
			}
		}
		
		if(Spans.size()==0){
			isDraft = true;
		}else if(messageText.getText().toString().equals("")){
			isDraft = true;
		}
		else{
			mdba.open();
			Cursor smsDetail = mdba.fetchSmsDetails(Spans.get(0).smsId);
			smsDetail.moveToFirst();
			if(smsDetail.getInt(smsDetail.getColumnIndex(DBAdapter.KEY_DRAFT))==1){
				isDraft = true;
			}
			mdba.close();
		}
		
		if(!isDraft){
			headerText.setText("Edit SMS");
			scheduleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
		}
		
		Log.d("size of Spans : " + Spans.size());
		mdba.close();
		refreshSpannableString(false);
		loadGroupsData();
		
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
		
		myAutoCompleteAdapter = new AutoCompleteAdapter(this);
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
	
	
	
	
	
	public void setFunctionalities(){
		
		
		numbersText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL){  
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i< Spans.size(); i++){
	                	 len = len + Spans.get(i).displayName.length();
	                	 if(i!=0){
	                		 len = len + 2;
	                	 }
	                	 if(pos<=len){
	                		 
	                		 numbersText.setSelection(pos - Spans.get(i).displayName.length());
	                		 mdba.open();
	                		 mdba.deleteSpanGroupRelsForSpan(Spans.get(i).spanId);
	                		 mdba.close();
	                		 
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
	                					// Log.d("coming into IF to uncheck a private group child");
	                					 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 
	                		 Spans.remove(i);
	                		 refreshSpannableString(false);
	                		 myAutoCompleteAdapter.notifyDataSetInvalidated();
	                		 myAutoCompleteAdapter.notifyDataSetChanged();
	                		 
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
				
				boolean isSending = false;
				mdba.open();
				for(int i = 0; i< smsIds.size(); i++){
					if(isSending){
						break;
					}
					Cursor cur = mdba.fetchSmsDetails(smsIds.get(i));
					if(cur.moveToFirst()){
						do{
							if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT))>0){
								isSending = true;
								break;
							}
						}while(cur.moveToNext());
					}
				}

				
				if(isSending){
					Toast.makeText(EditScheduledSmsActivity.this, "Message is already sent. Can't edit now", Toast.LENGTH_LONG).show();
					EditScheduledSmsActivity.this.finish();
				}else{
					if(Spans.size()==0 && messageText.getText().toString().matches("(''|[' ']*)")){
						//Toast.makeText(EditScheduledSmsActivity.this, "Mention Recipients and Message to proceed", Toast.LENGTH_SHORT).show();
						//EditScheduledSmsActivity.this.finish();
						final Dialog d = new Dialog(EditScheduledSmsActivity.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog_layout);
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("Nothing to schedule");
						
						yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.schedule_dialog_states));
						noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancel_dialog_states));
						
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
								EditScheduledSmsActivity.this.finish();
							}
						});
						
						d.show();
					}else
					if(Spans.size()==0){
						final Dialog d = new Dialog(EditScheduledSmsActivity.this);
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
//								doSmsScheduling();
								new AsyncScheduling().execute();
//								EditScheduledSmsActivity.this.finish();
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
						final Dialog d = new Dialog(EditScheduledSmsActivity.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog_layout);
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("Message is Blank!");
						
						yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.save_as_draft_dialog_states));
						noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.write_message_dialog_states));
						
						yesButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
//								doSmsScheduling();
								d.cancel();
								new AsyncScheduling().execute();
//								EditScheduledSmsActivity.this.finish();
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
						
					}else{
						new AsyncScheduling().execute();
//						doSmsScheduling();
//						EditScheduledSmsActivity.this.finish();
					}
				}
				
			}
		});
		
		
		
		
		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!messageText.getText().toString().matches("(''|[' ']*)") || !numbersText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(EditScheduledSmsActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Delete this message?");
					
					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.yes_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.no_dialog_states));
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							for(int i = 0; i<smsIds.size(); i++){
								mdba.open();
								mdba.deleteSms(smsIds.get(i), EditScheduledSmsActivity.this);
								mdba.close();
							}
							EditScheduledSmsActivity.this.finish();
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
					EditScheduledSmsActivity.this.finish();
				}
				
			}
		});
		
	}
	
	
	
	
	
	
	//--------------------function to Scheduling a new sms------------------------------------
	@Override
	public void doSmsScheduling(){
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
		String dateString = sdf.format(cal.getTime());
		mdba.open();
		
		ArrayList<String> numbers = new ArrayList<String>();
		
		Log.d(Spans.size()+"");
		
			ArrayList<Long> editedIds = mdba.getIds(editedGroup);
			for(int i = 0; i< editedIds.size(); i++){
				mdba.deleteSms(editedIds.get(i), EditScheduledSmsActivity.this);
			}
			Log.d("scheduling : deleted the previous record");
			if(Spans.size()==0){
				SpannedEntity span = new SpannedEntity(-1, 1, " ", -1, -1);  //for adding a fake span to save as a draft
				Spans.add(span);
			}
		
			for(int i = 0; i< Spans.size(); i++){
				if(Spans.get(i).type == 2){
				for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
					if(Spans.get(i).entityId == Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
						numbers.add(SmsApplicationLevelData.contactsList.get(j).number);
						long received_id = mdba.scheduleSms(SmsApplicationLevelData.contactsList.get(j).number, messageText.getText().toString(), dateString, parts.size(), editedGroup, cal.getTimeInMillis());
						if(!Spans.get(i).displayName.equals(" ")){
							mdba.addRecentContact(Spans.get(i).entityId, "");
						}
						
						if(messageText.getText().toString().length() == 0){
							Log.d("inside messageText if else");
							mdba.setAsDraft(received_id);
						}else{
							if(mdba.getCurrentPiFiretime() == -1){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, editedGroup, received_id, cal.getTimeInMillis());
							}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, editedGroup, received_id, cal.getTimeInMillis());
							}
						}
						Log.d("creating span for " + Spans.get(i).displayName );
						Spans.get(i).smsId = received_id;
						Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
						Log.d("size of groupIds for " + Spans.get(i).displayName +" : " + Spans.get(i).groupIds.size());
						for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
							mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), Spans.get(i).groupTypes.get(k));
						}
					}
				}
				}else
				if(Spans.get(i).type == 1){
					
					long received_id = mdba.scheduleSms(Spans.get(i).displayName, messageText.getText().toString(), dateString, parts.size(), editedGroup, cal.getTimeInMillis());
					
					if((Spans.size()==1 && Spans.get(0).displayName.equals(" ")) || messageText.toString().matches("(''|[' ']*)")){
						mdba.setAsDraft(received_id);
					}else{
						mdba.addRecentContact(-1, Spans.get(i).displayName);
						if(mdba.getCurrentPiFiretime() == -1){
							handlePiUpdate(Spans.get(i).displayName, editedGroup, received_id, cal.getTimeInMillis());
						}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
							handlePiUpdate(Spans.get(i).displayName, editedGroup, received_id, cal.getTimeInMillis());
						}
					}
					
					Spans.get(i).smsId = received_id;
					Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
					for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
						mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), Spans.get(i).groupTypes.get(i));
					}
				}
			}
			
			Log.d("scheduling : new records created");
		
		mdba.close();

	}
	
	
	
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
        	matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
        	showMatchesDialog();
        }
        
        else if(resultCode == 2){
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
		
		boolean isSending = false;
		mdba.open();
		for(int i = 0; i< smsIds.size(); i++){
			if(isSending){
				break;
			}
			Cursor cur = mdba.fetchSmsDetails(smsIds.get(i));
			if(cur.moveToFirst()){
				do{
					if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT))>0){
						isSending = true;
						break;
					}
				}while(cur.moveToNext());
			}
		}
		mdba.close();
		
		boolean isChanged = false;
		
		if(isDraft){
			if(originalSpans.size()==1 && originalSpans.get(0).displayName.equals(" ")){
				if(Spans.size()>0){
					isChanged = true;
				}
			}else if(originalSpans.size()!=Spans.size()){
				isChanged = true;
			}else if(!messageText.getText().toString().equals(originalMessage)){
				Log.d("Changed : 2");
				isChanged = true;
		
			}else{
				for(int i = 0; i< Spans.size(); i++){
					if(Spans.get(i).entityId != originalSpans.get(i).entityId){
						isChanged = true;
						Log.d("Changed : 3");
						break;
					}
				}
			}
		}else{
			if(originalSpans.size() != Spans.size()){
				Log.d("Changed : 1");
				isChanged = true;
			}else if(!messageText.getText().toString().equals(originalMessage)){
				Log.d("Changed : 2");
				isChanged = true;
		
			}else{
				for(int i = 0; i< Spans.size(); i++){
					if(Spans.get(i).entityId != originalSpans.get(i).entityId){
						isChanged = true;
						Log.d("Changed : 3");
						break;
					}
				}
			}
		}
		
		
		
		if(!isChanged){
			EditScheduledSmsActivity.this.finish();
		}else{
			final boolean _isSending = isSending; 
			final Dialog d = new Dialog(EditScheduledSmsActivity.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog_layout);
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("Discard Changes?");
			
			yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.ok_dialog_states));
			noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancel_dialog_states));
			
			yesButton.setOnClickListener(new OnClickListener() {
					
				@Override
				public void onClick(View v) {
					d.cancel();
					EditScheduledSmsActivity.this.finish();
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
        		ArrayList<Long> spanIdsForGroup = mdba.fetchSpansForGroup(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)), 1);
        		group.put(Constants.GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
       			if(spanIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< Spans.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==Spans.get(i).spanId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       					
       				}
       				
       			}
        		
        		group.put(Constants.GROUP_TYPE, 1);
        		group.put(Constants.GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        	
        		
        		nativeGroupData.add(group);
        		
        		for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsApplicationLevelData.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(i).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(i).number);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(i).image);
        					childParameters.put(Constants.CHILD_CHECK, false);//doubted
        					boolean ischeck = false;
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< Spans.size(); m++){
       								if(Spans.get(m).spanId == spanIdsForGroup.get(k) && Spans.get(m).entityId ==Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       									ischeck = true;
       								}
       							}
       						}
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        					
        				}
        			}
        		}
        		nativeChildData.add(child);
        		count++;
        	}while(groupCursor.moveToNext());
        	mdba.close();
        }
        
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> spanIdsForGroup = mdba.fetchSpansForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		Log.d("Group size in private : " + spanIdsForGroup.size());
        		group.put(Constants.GROUP_CHECK, false);
        		if(spanIdsForGroup.size()>0){
       				for(int i = 0; i< Spans.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==Spans.get(i).spanId){
       							Log.d("-------is entering into the TRUE condition");
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		privateGroupData.add(group);
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			Log.d("contact_id for group : " + contactIds.get(i));
        		}
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					boolean isCheck = false;
        					Log.d("size of spanIdsForGroup " + spanIdsForGroup.size());
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< Spans.size(); m++){
       								Log.d("Spans.get(m).entityId : " + Spans.get(m).entityId);
       								Log.d("contact_uri_id : " + Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id));
       								if(Spans.get(m).spanId == spanIdsForGroup.get(k) && Spans.get(m).entityId == contactIds.get(i)){
       									Log.d("checking the child in private for " + Spans.get(m).displayName);
       									childParameters.put(Constants.CHILD_CHECK, true);
       									isCheck = true;
       								}
       							}
       						}       					
        					child.add(childParameters);
        				}
        			}
        		}
        		privateChildData.add(child);
        		count++;
        	}while(groupsCursor.moveToNext());
        }
        mdba.close();
	}
}