/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

import com.vinsol.sms_scheduler.utils.Log;

import android.os.Parcel;
import android.os.Parcelable;

public class Recipient implements Parcelable{

	public long recipientId;
	public int type;
	public String displayName;
	public String number;
	public long contactId;
	public long smsId;
	public int keyImageRes;
	public int sent;
	public int delivered;
	public int operated;
	public ArrayList<Long> groupIds = new ArrayList<Long>();
	public ArrayList<Integer> groupTypes = new ArrayList<Integer>();
	
	public Recipient(){}
	
	public Recipient(long recipientId, int type, String displayName, long contactId, long smsId, int sent, int delivered){
		this.recipientId = recipientId;
		this.type = type;
		this.displayName = displayName;
		this.contactId = contactId;
		this.smsId = smsId;
		this.sent = sent;
		this.delivered = delivered;
	}
	
	
	public static final Parcelable.Creator<Recipient> CREATOR = new Parcelable.Creator<Recipient>() {
    	public Recipient createFromParcel(Parcel in) {
    		return new Recipient(in);
    	}
 
        public Recipient[] newArray(int size) {
        	return new Recipient[size];
        }
    };
    
    
    public Recipient(Parcel in) {
    	this.recipientId = in.readLong();
    	this.type = in.readInt();
    	this.displayName = in.readString();
    	this.contactId = in.readLong();
    	this.smsId = in.readLong();
    };
    

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.recipientId);
		dest.writeInt(this.type);
		dest.writeString(this.displayName);
		dest.writeLong(this.contactId);
		dest.writeLong(this.smsId);
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	
	public void print(){
		Log.d("------------------------------------------------");
		Log.d("Sms Id 		: " + this.smsId);
		Log.d("Recipient Id : " + this.recipientId);
		Log.d("Type 		: " + this.type);
		Log.d("Display Name : " + this.displayName);
		Log.d("Contact Id 	: " + this.contactId);
		Log.d("------------------------------------------------");
	}
}