package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class Span {

	public long spanId;
	public int type;
	public String displayName;
	public long entityId;
	public long smsId;
	public ArrayList<Long> groupIds = new ArrayList<Long>();
	public ArrayList<Integer> groupTypes = new ArrayList<Integer>();
	
	public Span(){
		
	}
	
	public Span(long spanId, int type, String displayName, long entityId, long smsId){
		this.spanId = spanId;
		this.type = type;
		this.displayName = displayName;
		this.entityId = entityId;
		this.smsId = smsId;
	}
	
	
}