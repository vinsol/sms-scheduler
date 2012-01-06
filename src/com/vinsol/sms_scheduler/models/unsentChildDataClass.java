/* Class to structure data for sent and draft messages, referring them as 'unsent' */ 
package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class unsentChildDataClass extends abstractChildDataClass{
	public unsentChildDataClass(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, int keysent, int keydeliver, int keymsgparts, ArrayList<Long> keyids){
		this.keyId 			= keyid;
		this.keyGrpId 		= keygrpid;
		this.keyNumber 		= keynumber;
		this.keyMessage 	= keymessage;
		this.keyTimeMilis 	= keytimemilis;
		this.keyDate 		= keydate;
		this.keySent 		= keysent;
		this.keyDeliver 	= keydeliver;
		this.keyMsgParts 	= keymsgparts;
		this.keyIds			= keyids;
	}
}
