/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Contact {

	public long content_uri_id;
	public String name;
	public String number;
	public Bitmap image;
	public ArrayList<Long> groupRowId = new ArrayList<Long>(); 
	public boolean checked = false;
	
}
