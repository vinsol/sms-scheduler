/* Class to structure data for sent and draft messages, referring them as 'unsent' */ 
package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

public class ScheduledSms implements Parcelable {
	
	public long 			keyId;
	public long 			keyGrpId;
	public String 			keyNumber;
	public String 			keyMessage;
	public long				keyTimeMilis;
	public String 			keyDate;
	public int 				keyImageRes;
	public String 			keyExtraReceivers;
	public ArrayList<Long>	keyIds;
	
	public ScheduledSms(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, ArrayList<Long> keyids){
		this.keyId 			= keyid;
		this.keyGrpId 		= keygrpid;
		this.keyNumber 		= keynumber;
		this.keyMessage 	= keymessage;
		this.keyDate		= keydate;
		this.keyTimeMilis 	= keytimemilis;
		this.keyIds			= keyids;
	}
	
	public ScheduledSms(){}
	
	public static final Parcelable.Creator<ScheduledSms> CREATOR = new Parcelable.Creator<ScheduledSms>() {
    	public ScheduledSms createFromParcel(Parcel in) {
    		return new ScheduledSms(in);
    	}
 
        public ScheduledSms[] newArray(int size) {
        	return new ScheduledSms[size];
        }
    };
    
    public ScheduledSms(Parcel in) {
    	this.keyId = in.readLong();
    	this.keyGrpId = in.readLong();
    	this.keyNumber = in.readString();
    	this.keyMessage = in.readString();
    	this.keyTimeMilis = in.readLong();
    };
    
    @Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.keyId);
		dest.writeLong(this.keyGrpId);
		dest.writeString(this.keyNumber);
		dest.writeString(this.keyMessage);
		dest.writeLong(this.keyTimeMilis);
    }

	@Override
	public int describeContents() {
		return 0;
	}
	
}