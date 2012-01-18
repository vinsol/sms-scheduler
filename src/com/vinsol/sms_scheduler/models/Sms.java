/* Class to structure data for sent and draft messages, referring them as 'unsent' */ 
package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;


public class Sms implements Parcelable {
	
	public long 					keyId;
	public String 					keyNumber;
	public String 					keyMessage;
	public long						keyTimeMilis;
	public String 					keyDate;
	public int 						keyImageRes;
	public String 					keyExtraReceivers;
	public ArrayList<Recipient>		keyRecipients;
	
	
	public Sms(long keyid, String keynumber, String keymessage, long keytimemilis, String keydate, ArrayList<Recipient> keyrecipients){
		this.keyId 			= keyid;
		this.keyNumber 		= keynumber;
		this.keyMessage 	= keymessage;
		this.keyDate		= keydate;
		this.keyTimeMilis 	= keytimemilis;
		this.keyRecipients	= keyrecipients;
	}
	
	public Sms(){}
	
	public static final Parcelable.Creator<Sms> CREATOR = new Parcelable.Creator<Sms>() {
    	public Sms createFromParcel(Parcel in) {
    		return new Sms(in);
    	}
 
        public Sms[] newArray(int size) {
        	return new Sms[size];
        }
    };
    
    public Sms(Parcel in) {
    	this.keyId = in.readLong();
    	this.keyNumber = in.readString();
    	this.keyMessage = in.readString();
    	this.keyTimeMilis = in.readLong();
    };
    
    @Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.keyId);
		dest.writeString(this.keyNumber);
		dest.writeString(this.keyMessage);
		dest.writeLong(this.keyTimeMilis);
    }

	@Override
	public int describeContents() {
		return 0;
	}
}