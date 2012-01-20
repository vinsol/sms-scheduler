package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.Date;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Sms;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.utils.Log;

public class EditScheduledSms extends AbstractScheduleSms {
	
	private TextView headerText;
	private boolean isDraft = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mode = 2;
		
		headerText	= (TextView)findViewById(R.id.header);
		
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
		
		mdba.open();
		isDraft = mdba.isDraft(editedSms); 
		if(!isDraft){
			headerText.setText("Edit SMS");
			scheduleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
		}
		
		mdba.close();
		refreshSpannableString(false);
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
			if(originalRecipients.size()==1 && originalRecipients.get(0).displayName.equals(" ")){
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