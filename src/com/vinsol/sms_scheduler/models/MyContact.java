package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;

public class MyContact {

	public String content_uri_id;
	public String name;
	public String number;
	public Bitmap image;
	public ArrayList<Long> groupRowId; 
	public boolean checked = false;
	
	public MyContact(){
		this.groupRowId = new ArrayList<Long>();
	}
	
	@Override
	public String toString() {
		return "";//NewScheduleActivity.numbersText.getText().toString().substring(0, NewScheduleActivity.positionTrack) + this.number;
		
	}
}
