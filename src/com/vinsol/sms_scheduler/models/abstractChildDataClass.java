package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class abstractChildDataClass {
	public long 			keyId;
	public long 			keyGrpId;
	public String 			keyNumber;
	public String 			keyMessage;
	public long				keyTimeMilis;
	public String 			keyDate;
	public int				keySent;
	public int				keyDeliver;
	public int				keyMsgParts;
	public int 				keyImageRes;
	public String 			keyExtraReceivers;
	public ArrayList<Long>	keyIds;
}
