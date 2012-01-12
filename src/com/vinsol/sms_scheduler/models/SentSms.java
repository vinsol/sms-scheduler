package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class SentSms extends AbstractSms{
	long		keySMillis;
	long		keyDMillis;
	int 		keySent;
	int			keyDeliver;
	int 		keyMsgParts;
	
	public SentSms(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, int keysent, int keydeliver, int keymsgparts, long keysmillis, long keydmillis, ArrayList<Long> keyids){
		this.keyId 			= keyid;
		this.keyGrpId 		= keygrpid;
		this.keyNumber 		= keynumber;
		this.keyMessage 	= keymessage;
		this.keyTimeMilis 	= keytimemilis;
		this.keyDate 		= keydate;
		this.keySent 		= keysent;
		this.keyDeliver 	= keydeliver;
		this.keyMsgParts 	= keymsgparts;
		this.keySMillis		= keysmillis;
		this.keyDMillis		= keydmillis;
		this.keyIds			= keyids;
	}
}
