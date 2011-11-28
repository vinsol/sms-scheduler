package com.vinsol.sms_scheduler;

import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;

public class MyContact {

	String content_uri_id;
	String name;
	String number;
	Bitmap image;
	boolean checked = false;
	
	public MyContact(){
		
	}
	
	@Override
	public String toString() {
		return "";//NewScheduleActivity.numbersText.getText().toString().substring(0, NewScheduleActivity.positionTrack) + this.number;
		
	}
}
