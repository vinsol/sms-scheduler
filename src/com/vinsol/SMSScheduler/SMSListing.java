package com.vinsol.SMSScheduler;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SMSListing extends ListActivity {
	
	ListView lv;
	
	@Override
    public void onCreate(Bundle onSavedInstanceState) {
	    	
		super.onCreate(onSavedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    	setContentView(R.layout.message_listing);
    	
    	ArrayList<HashMap<String, String>> listViewData = new ArrayList<HashMap<String, String>>();
    	
    	ArrayList<Message> messagesList = new SMSSchedulerDBHelper(this).retrieveMessages();
    	int sizeOfMessagesList = messagesList.size();
    	
		for(int i=0; i<sizeOfMessagesList; i++){
			HashMap<String, String> map = new HashMap<String, String>();
			long timeInMilliSecond = messagesList.get(i).scheduledTimeInMilliSecond;
			
			map.put(Constant.MESSAGE_BODY, messagesList.get(i).messageBody);
			map.put(Constant.SCHEDULED_DATE, new DateTimeConverter().getDateString(timeInMilliSecond));
			map.put(Constant.SCHEDULED_TIME, new DateTimeConverter().getTimeString(timeInMilliSecond));
			map.put(Constant.STATUS, "" + messagesList.get(i).status);
			
			listViewData.add(map);
		}
		
	    lv = this.getListView();
	    
	    
	   // lv.setOnItemSelectedListener(this);
	    
	    SimpleAdapter mSchedule = new SimpleAdapter
	    			(this, listViewData, 
    						R.layout.message_listing_one_message_view, 
    						new String[] {Constant.MESSAGE_BODY, 
    									  Constant.SCHEDULED_DATE, 
    									  Constant.SCHEDULED_TIME, 
    									  Constant.STATUS}, 
    						new int[] {R.id.message_listing_one_message_view_message_body,
	    							   R.id.message_listing_one_message_view_send_date,
	    							   R.id.message_listing_one_message_view_send_time,
	    							   R.id.message_listing_one_message_view_status});
		
	    if(mSchedule == null){
			Log.v("","mSchedule is null" );
		}else{
			setListAdapter(mSchedule);
		}
	 }//end method onCreate
	
}//end class MessageListing