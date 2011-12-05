package com.vinsol.sms_scheduler.models;

import java.util.ArrayList;

public class GroupStructure{

	int type;
	boolean isChecked;
	long groupId;
	ArrayList<Long> CheckedContactsIds = new ArrayList<Long>();
	
}
