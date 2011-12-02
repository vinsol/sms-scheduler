package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;

public class MyContact {

	String content_uri_id;
	String name;
	String number;
	Bitmap image;
	ArrayList<Long> groupRowId; 
	boolean checked = false;
	
	public MyContact(){
		this.groupRowId = new ArrayList<Long>();
	}
	
	@Override
	public String toString() {
		return "";//NewScheduleActivity.numbersText.getText().toString().substring(0, NewScheduleActivity.positionTrack) + this.number;
		
	}
}
