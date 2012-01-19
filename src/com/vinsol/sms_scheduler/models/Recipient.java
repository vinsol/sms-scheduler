package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class Recipient{

	public long recipientId;
	public int type;
	public String displayName;
	public String number;
	public long contactId;
	public long smsId;
	public int keyImageRes;
	public ArrayList<Long> groupIds = new ArrayList<Long>();
	public ArrayList<Integer> groupTypes = new ArrayList<Integer>();
	
	public Recipient(){
		
	}
	
	public Recipient(long recipientId, int type, String displayName, long contactId, long smsId){
		this.recipientId = recipientId;
		this.type = type;
		this.displayName = displayName;
		this.contactId = contactId;
		this.smsId = smsId;
	}
}