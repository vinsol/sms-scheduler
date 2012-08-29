/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities.ScheduleSms;

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
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.models.Sms;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.utils.MyGson;

public class EditScheduledSms extends AbstractScheduleSms {
	
	private TextView headerText;
	private boolean isDraft = false;
	private boolean isReschedule = false;
	Sms SMS;
	
	
	@Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FlurryAgent.logEvent("Edit Scheduled Sms Activity started");
		
		mode = MODE_EDIT;
		
		headerText	= (TextView)findViewById(R.id.header);
		
		SMS = getIntent().getParcelableExtra("SMS DATA");
		
		messageText.setText(SMS.keyMessage);
		originalMessage = SMS.keyMessage;
		processDate = new Date(SMS.keyTimeMilis);
		characterCountText.setText(String.valueOf(messageText.getText().toString().length()));
		editedSms = SMS.keyId;
		
		
		//loading defaultRepeatHash...
		defaultRepeatMode = SMS.keyRepeatMode;
		if(defaultRepeatMode>0){
			String repeatHashString = SMS.keyRepeatString;
			Log.d("Repeat Hash String : " + repeatHashString);
			defaultRepeatHash = new MyGson().deserializeRepeatHash(repeatHashString);
		}
		
		
		Recipients.clear();
		originalRecipients.clear();
		Recipients = SMS.keyRecipients;
		for(Recipient r : Recipients){
			originalRecipients.add(r);
		}
		
		
		
		mdba.open();
		if(SMS.keyTimeMilis < System.currentTimeMillis())
			isReschedule = true;
			
		isDraft = mdba.isDraft(editedSms); 
		
		if(!isReschedule)
			cancelButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.delete_footer_states));
		
		if(!isDraft && !isReschedule){
			headerText.setText("Edit SMS");
			scheduleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
		}
		
		if(isReschedule && !isDraft){
			mode = MODE_NEW;
			headerText.setText("Reschedule SMS");
			processDate = new Date(System.currentTimeMillis());
		}
		
		if(isDraft)
			cancelButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancel_footer_states));
		
		setSuperFunctionalities();
		loadGroupsData();
		
		mdba.close();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		widthOfContainerInDp = (int)(metrics.widthPixels/dpi - 128);
		displayViews();
		
		if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")){
			numbersText.setHint("Recipients");
			Recipients.remove(0);
		}else{
			numbersText.setHint(" ");
		}
	}
	
	
	
	
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

				public void onClick(View v) {
					d.cancel();
					EditScheduledSms.this.finish();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					d.cancel();
					numbersText.requestFocus();
				}
			});
				
			d.show();
		}
	}



	
	protected void scheduleButtonOnClickListener() {
		mdba.open();
		if(mdba.isSmsSent(editedSms) && !isReschedule){
			Toast.makeText(this, "Message has already been sent. Can't edit now", Toast.LENGTH_LONG).show();
			finish();
			mdba.close();
		}else{
			mdba.close();
			onScheduleButtonPressTasks();
		}
	}
}