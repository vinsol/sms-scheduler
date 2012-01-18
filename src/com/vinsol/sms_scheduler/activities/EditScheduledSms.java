package com.vinsol.sms_scheduler.activities;

import static com.vinsol.sms_scheduler.Constants.DIALOG_CONTROL_ACTION;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Sms;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

public class EditScheduledSms extends AbstractScheduleSms {
	
	private TextView 	headerText;
	
	private long editedSms;
	private boolean isDraft = false;
	private ArrayList<Long> recipientIds;
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent2) {
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					toOpen = 0;
					Intent intent = new Intent(EditScheduledSms.this, SelectContacts.class);
					intent.putExtra("ORIGIN", "edit");
					startActivityForResult(intent, 2);
				}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_sms);
		
		dataLoadWaitDialog = new Dialog(EditScheduledSms.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
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
		Sms SMS = intent.getParcelableExtra("SMS DATA");
		Log.d("Key Message : " + SMS.keyMessage);
		numbersText.setText(SMS.keyNumber);
		messageText.setText(SMS.keyMessage);
		originalMessage = SMS.keyMessage;
		processDate = new Date(SMS.keyTimeMilis);
		characterCountText.setText(String.valueOf(messageText.getText().toString().length()));
		editedSms = SMS.keyId;
		cancelButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.delete_footer_states));

		Recipients.clear();
		
		mdba.open();
		recipientIds = mdba.fetchRecipientIdsForSms(editedSms);
		
		originalRecipients.clear();
		
		for(int i = 0; i< recipientIds.size(); i++){
			Cursor spanCur = mdba.fetchRecipientDetails(recipientIds.get(i));
			spanCur.moveToFirst();
			Recipients.add(new Recipient(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)),
					spanCur.getInt(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_TYPE)),
					spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_CONTACT_ID)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SMS_ID))));
			ArrayList<Long> groupIds = mdba.fetchGroupsForRecipient(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)));
			ArrayList<Integer> groupTypes = mdba.fetchGroupTypesForSpan(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)));
			
			originalRecipients.add(new Recipient(spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)),
					spanCur.getInt(spanCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_TYPE)),
					spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_CONTACT_ID)),
					spanCur.getLong(spanCur.getColumnIndex(DBAdapter.KEY_SMS_ID))));
			
			for(int k = 0; k < groupIds.size(); k++){
				Recipients.get(i).groupIds.add(groupIds.get(k));
				Recipients.get(i).groupTypes.add(groupTypes.get(k));
				originalRecipients.get(i).groupIds.add(groupIds.get(k));
				originalRecipients.get(i).groupTypes.add(groupTypes.get(k));
			}
		}
		
		if(Recipients.size()==0){
			isDraft = true;
		}else if(messageText.getText().toString().equals("")){
			isDraft = true;
		}
		else{
			mdba.open();
			Cursor smsDetail = mdba.fetchRecipientDetails(Recipients.get(0).recipientId);
			smsDetail.moveToFirst();
			if(smsDetail.getInt(smsDetail.getColumnIndex(DBAdapter.KEY_STATUS))==0){
				isDraft = true;
			}
			mdba.close();
		}
		
		if(!isDraft){
			headerText.setText("Edit SMS");
			scheduleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
		}
		
		mdba.close();
		refreshSpannableString(false);
		loadGroupsData();
		
		// Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
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

        dataloadIntentFilter.addAction(Constants.DIALOG_CONTROL_ACTION);

        
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
	
	
	
	private void setFunctionalities(){
		
		numbersText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL){
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i< Recipients.size(); i++){
	                	 len = len + Recipients.get(i).displayName.length();
	                	 if(i!=0){
	                		 len = len + 2;
	                	 }
	                	 if(pos<=len){
	                		 numbersText.setSelection(pos - Recipients.get(i).displayName.length());
	                		 mdba.open();
	                		 mdba.deleteRecipientGroupRelsForRecipient(Recipients.get(i).recipientId);
	                		 mdba.close();
	                		 
	                		 for(int j = 0; j < nativeGroupData.size(); j++){
	                			 for(int k = 0; k< nativeChildData.get(j).size(); k++) {
	                				 if((Long)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(i).contactId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 
	                		 for(int j = 0; j< privateGroupData.size(); j++){
	                			 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                				 if((Long.parseLong((String)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Recipients.get(i).contactId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 Recipients.remove(i);
	                		 refreshSpannableString(false);
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
				for(int i = 0; i< recipientIds.size(); i++){
					if(isSending){
						break;
					}
					Cursor cur = mdba.fetchRecipientDetails(recipientIds.get(i));
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
					Toast.makeText(EditScheduledSms.this, "Message is already sent. Can't edit now", Toast.LENGTH_LONG).show();
					EditScheduledSms.this.finish();
				}else{
					onScheduleButtonPressTasks();
				}
			}
		});
		
		
		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!messageText.getText().toString().matches("(''|[' ']*)") || !numbersText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(EditScheduledSms.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Delete this message?");
					
					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.yes_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.no_dialog_states));
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mdba.open();
							mdba.deleteSms(editedSms, EditScheduledSms.this);
							mdba.close();
//							for(int i = 0; i<recipientIds.size(); i++){
//								mdba.open();
//								mdba.deleteSms(smsIds.get(i), EditScheduledSms.this);
//								mdba.close();
//							}
							EditScheduledSms.this.finish();
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
					EditScheduledSms.this.finish();
				}
			}
		});
	}
	
	
	
	
	
	
	//--------------------function to Scheduling a new sms------------------------------------
	@Override
	protected void doSmsScheduling(){
		mdba.open();
//		ArrayList<Long> editedIds = mdba.fetchRecipientIdsForSms(editedSms);
		
		mdba.deleteSms(editedSms, EditScheduledSms.this);
		
		doSmsSchedulingTask();
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
        	refreshSpannableString(false);
        	numbersText.requestFocus();
        	numbersText.setSelection(numbersText.getText().toString().length());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	
	@Override
	public void onBackPressed() {
		boolean isSending = false;
		mdba.open();
		for(int i = 0; i< recipientIds.size(); i++){
			if(isSending){
				break;
			}
			Cursor cur = mdba.fetchRecipientDetails(recipientIds.get(i));
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
			if(originalRecipients.size()==0){
				if(Recipients.size()>0){
					isChanged = true;
				}
			}else if(originalRecipients.size()!=Recipients.size()){
				isChanged = true;
			}else if(!messageText.getText().toString().equals(originalMessage)){
				isChanged = true;
			}else{
				for(int i = 0; i< Recipients.size(); i++){
					if(Recipients.get(i).contactId != originalRecipients.get(i).contactId){
						isChanged = true;
						break;
					}
				}
			}
		}else{
			if(originalRecipients.size() != Recipients.size()){
				isChanged = true;
			}else if(!messageText.getText().toString().equals(originalMessage)){
				isChanged = true;
			}else{
				for(int i = 0; i< Recipients.size(); i++){
					if(Recipients.get(i).contactId != originalRecipients.get(i).contactId){
						isChanged = true;
						break;
					}
				}
			}
		}
		
		
		
		if(!isChanged){
			EditScheduledSms.this.finish();
		}else{
			final Dialog d = new Dialog(EditScheduledSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
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
					EditScheduledSms.this.finish();
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
}