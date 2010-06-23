package com.vinsol.SMSScheduler;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SMSListing extends ListActivity {
	
	ListView lv;
	
	ArrayList<Message> messagesList;
	
	/**==========================================================================
	 * method onCreate()
	 *===========================================================================*/
	@Override
    public void onCreate(Bundle onSavedInstanceState) {
	    	
		super.onCreate(onSavedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    	setContentView(R.layout.message_listing);
    	
    	ArrayList<HashMap<String, String>> listViewData = new ArrayList<HashMap<String, String>>();
    	
    	messagesList = new SMSSchedulerDBHelper(this).retrieveMessages();
    	
    	if(messagesList == null || messagesList.isEmpty()) {
    		Toast.makeText(this, getString(R.string.toast_message_sms_listing_no_sms_to_show), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	int sizeOfMessagesList = messagesList.size();
    	
		for(int i=0; i<sizeOfMessagesList; i++){
			HashMap<String, String> map = new HashMap<String, String>();
			
			map.put(Constant.MESSAGE_BODY, messagesList.get(i).messageBody);
			map.put(Constant.SCHEDULED_DATE, messagesList.get(i).getDateString());
			map.put(Constant.SCHEDULED_TIME, messagesList.get(i).getTimeString());
			map.put(Constant.STATUS, messagesList.get(i).getStatusInString());
			
			listViewData.add(map);
		}
		
	    lv = this.getListView();
	    
	    
	   lv.setOnItemClickListener(new OnItemClickListener() {
		   @Override
		   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			   int idOfClickedMessage = messagesList.get(position).id;
			   
			   //sfdasfsdgfdg
		   }
	   });
	    
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
	
	/**==================================================================
	 * method onCreateOptionsMenu(Menu menu)
	 * create optionMenu
	 *===================================================================*/
	public boolean onCreateOptionsMenu(Menu menu) {
	    new OptionMenuHelper().createOptionMenu(menu);
	    return true;
	}//end method onCreateOptionsMenu

	/**==================================================================
	 * method onOptionsItemSelected(MenuItem item)
	 * called when an option item is being clicked
	 *===================================================================*/
	public boolean onOptionsItemSelected(MenuItem item) {
	    return new OptionMenuHelper().onOptionsItemSelected(this, item);
	}//end method onOptionsItemSelected
	
}//end class MessageListing