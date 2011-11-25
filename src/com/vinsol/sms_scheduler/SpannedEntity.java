package com.vinsol.sms_scheduler;

public class SpannedEntity {

	long spanId;
	int type;
	String displayName;
	long entityId;
	long smsId;
	
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