/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import java.util.Date;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.models.Sms;
import com.vinsol.sms_scheduler.utils.Log;

public class EditScheduledSms extends AbstractScheduleSms {
	
	private TextView headerText;
	private boolean isDraft = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mode = 2;
		
		headerText	= (TextView)findViewById(R.id.header);
		
		Sms SMS = getIntent().getParcelableExtra("SMS DATA");
		
		numbersText.setText(SMS.keyNumber);
		messageText.setText(SMS.keyMessage);
		originalMessage = SMS.keyMessage;
		processDate = new Date(SMS.keyTimeMilis);
		characterCountText.setText(String.valueOf(messageText.getText().toString().length()));
		editedSms = SMS.keyId;
		
		Recipients.clear();
		originalRecipients.clear();
		Recipients = SMS.keyRecipients;
		for(Recipient r : Recipients){
			originalRecipients.add(r);
		}
		
		cancelButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.delete_footer_states));
		
		mdba.open();
		isDraft = mdba.isDraft(editedSms); 
		if(!isDraft){
			headerText.setText("Edit SMS");
			scheduleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
		}
		
		setSuperFunctionalities();
		loadGroupsData();
		
		mdba.close();
		refreshSpannableString(false);
	}
	
	
	
	@Override
	public void onBackPressed() {
		
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
			Log.d("Recipeints size : " + Recipients.size());
			Log.d("originalRecipeints size : " + originalRecipients.size());
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



	@Override
	protected void scheduleButtonOnClickListener() {
		mdba.open();
		if(mdba.isSmsSent(editedSms)){
			Toast.makeText(this, "Message has already been sent. Can't edit now", Toast.LENGTH_LONG).show();
			finish();
			mdba.close();
		}else{
			mdba.close();
			onScheduleButtonPressTasks();
		}
	}
}