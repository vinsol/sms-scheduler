package com.vinsol.sms_scheduler.models;

import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactNumber {

	public String number;
	public String type;
	public long contactId;
	public boolean isPrimary = false;
	
	public ContactNumber(long contactId, String number, String type){
		this.contactId = contactId;
		this.number=number;
		this.type=type;
	}
	
	public static String resolveType(int type){
		switch(type){
			case Phone.TYPE_ASSISTANT:
				return "Assistant";
			case Phone.TYPE_CALLBACK:
				return "Callback";
			case Phone.TYPE_CAR:
				return "Car";
			case Phone.TYPE_COMPANY_MAIN:
				return "Company Main";
			case Phone.TYPE_FAX_HOME:
				return "Fax Home";
			case Phone.TYPE_FAX_WORK:
				return "Fax Work";
			case Phone.TYPE_HOME:
				return "Home";
			case Phone.TYPE_ISDN:
				return "ISDN";
			case Phone.TYPE_MAIN:
				return "Main";
			case Phone.TYPE_MMS:
				return "MMS";
			case Phone.TYPE_MOBILE:
				return "Mobile";
			case Phone.TYPE_OTHER:
				return "Other";
			case Phone.TYPE_OTHER_FAX:
				return "Other Fax";
			case Phone.TYPE_PAGER:
				return "Pager";
			case Phone.TYPE_RADIO:
				return "Radio";
			case Phone.TYPE_TELEX:
				return "Telex";
			case Phone.TYPE_TTY_TDD:
				return "TTY TDD";
			case Phone.TYPE_WORK:
				return "Work";
			case Phone.TYPE_WORK_MOBILE:
				return "Work Mobile";
			case Phone.TYPE_WORK_PAGER:
				return "Work Pager";
			case Phone.TYPE_CUSTOM:
				return "Custom";
			default:
				return "Other";
		}
	}
}
