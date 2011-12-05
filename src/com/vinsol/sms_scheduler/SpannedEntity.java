package com.vinsol.sms_scheduler;

import java.util.ArrayList;

public class SpannedEntity {

	long spanId;
	int type;
	String displayName;
	long entityId;
	long smsId;
	ArrayList<Long> groupIds = new ArrayList<Long>();
	ArrayList<Integer> groupTypes = new ArrayList<Integer>();
	
	public SpannedEntity(){
		
	}
	
	public SpannedEntity(long spanId, int type, String displayName, long entityId, long smsId){
		this.spanId = spanId;
		this.type = type;
		this.displayName = displayName;
		this.entityId = entityId;
		this.smsId = smsId;
	}
	
	
}