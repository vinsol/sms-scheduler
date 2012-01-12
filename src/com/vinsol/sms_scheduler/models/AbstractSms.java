package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public abstract class AbstractSms {
	public long 			keyId;
	public long 			keyGrpId;
	public String 			keyNumber;
	public String 			keyMessage;
	public long				keyTimeMilis;
	public String 			keyDate;
	public int 				keyImageRes;
	public String 			keyExtraReceivers;
	public ArrayList<Long>	keyIds;
}
