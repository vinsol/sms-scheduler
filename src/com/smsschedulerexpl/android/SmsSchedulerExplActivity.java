package com.smsschedulerexpl.android;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class SmsSchedulerExplActivity extends Activity {
    /** Called when the activity is first created. */
	
	public static ArrayList<childSch> childSchArray;
	public static ArrayList<childSent> childSentArray;
	
	ExpandableListView 		explList;
	ImageButton				newSmsButton;
	
	SimpleExpandableListAdapter mAdapter;
	private ArrayList<HashMap<String, String>> headerData;
	private ArrayList<ArrayList<HashMap<String, Object>>> childData;
	
	DBAdapter mdba = new DBAdapter(SmsSchedulerExplActivity.this);
	
	final String NAME = "name";
	final String IMAGE = "image";
	final String MESSAGE = "message";
	final String DATE = "date";
	final String RECEIVER = "receiver";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        newSmsButton = (ImageButton) findViewById(R.id.main_new_sms_imgbutton);
        explList 	 = (ExpandableListView) findViewById(R.id.main_expandable_list);
        registerForContextMenu(explList);
        
        newSmsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
//				Intent intent = new Intent(SmsSchedulerExplActivity.this, newScheduleActivity.class);
//				startActivity(intent);
			}
		});
        
        setExplData();
    }
    
    
    
    
    public void setExplData(){
    	loadData();
    	
    	final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	headerData,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[] { NAME },
    	    	new int[] { android.R.id.text1 },
    	    	childData,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
    		
    		@Override
	    	public View newChildView(boolean isLastChild, ViewGroup parent) {
	    		
	    		return layoutInflater.inflate(R.layout.main_row_layout, null, false);
	    	}
    		
    		
    		
    		@Override
    		public android.view.View getChildView(int groupPosition, int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			final View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    			
    			final TextView messageTextView  = (TextView)  v.findViewById(R.id.main_row_message_area);
    			final ImageView statusImageView = (ImageView) v.findViewById(R.id.main_row_image_area);
    			final TextView dateTextView		= (TextView)  v.findViewById(R.id.main_row_date_area);
    			final TextView receiverTextView = (TextView)  v.findViewById(R.id.main_row_recepient_area);
    			
    			if(groupPosition == 0){
    				messageTextView.setText(childSchArray.get(childPosition).keyMessage);
    				statusImageView.setImageResource(R.drawable.icon);
    				dateTextView.setText(childSchArray.get(childPosition).keyDate);
    				receiverTextView.setText(childSchArray.get(childPosition).keyNumber);
    			}else if(groupPosition == 1){
    				messageTextView.setText(childSentArray.get(childPosition).keyMessage);
    				statusImageView.setImageResource(R.drawable.icon);
    				dateTextView.setText(childSentArray.get(childPosition).keyDate);
    				receiverTextView.setText(childSentArray.get(childPosition).keyNumber);
    			}
    			
    			return v;
    		}
    		
    	};
    }
    
    
    public void loadData(){
    	
    	
    	mdba.open();
    	Cursor schCur  = mdba.fetchAllScheduled();
    	Cursor sentCur = mdba.fetchAllSent();
    	mdba.close();
    	
    	
    	//-----------------------Putting group headers for Expandable list---------------------------- 
    	headerData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> group = new HashMap<String, String>();
    	group.put(NAME, "Scheduled");
    	headerData.add(group);
    	group.put(NAME, "Sent");
    	headerData.add(group);
    	//---------------------------------------------------------------------------------------------
    	
    	
    	//----------------------Putting child data into Child Hash-------------------------------------
    	childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
    	
    	
    	//------------------------Loading scheduled msgs----------------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildSch = new ArrayList<HashMap<String, Object>>();
    	int z = 0;
    	if(schCur.moveToFirst()){
    		z = 0;
    		do{
    			if(z == 0 || childSchArray.get(z).keyGrpId != schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_GRPID))){
    				z++;
    				ArrayList<Long> tempIds = new ArrayList<Long>();
    				tempIds.add(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)));
    				childSchArray.add(new childSch(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)),
    						schCur.getLong	(schCur.getColumnIndex(DBAdapter.KEY_GRPID)),
    						schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_NUMBER)),
    						schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_MESSAGE)),
    						schCur.getLong	(schCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)),
    						schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_DATE)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_SENT)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_DELIVER)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_MSG_PARTS)),
    						tempIds));
    			}else{
    				childSchArray.get(z).keyNumber = childSchArray.get(z).keyNumber + ", " + schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_NUMBER));
    				childSchArray.get(z).keyIds.add(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)));
    			}
    			
    		}while(schCur.moveToNext());
    	}
    	
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSchArray.get(i).keyMessage);
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSchArray.get(i).keyDate);
    		child.put(RECEIVER, childSchArray.get(i).keyNumber);
    		groupChildSch.add(child);
    	}
    	
    	childData.add(groupChildSch);
    	
    	//-------------------------------------------------------------------------end of scheduled msgs load-------- 
    	
    	
    	
    	
    	//--------------------------loading sent messages------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildSent = new ArrayList<HashMap<String, Object>>();
    	z = 0;
    	if(sentCur.moveToFirst()){
    		z = 0;
    		do{
    			if(z == 0 || childSentArray.get(z).keyGrpId != sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_GRPID))){
    				z++;
    				ArrayList<Long> tempIds = new ArrayList<Long>();
    				tempIds.add(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)));
    				childSentArray.add(new childSent(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)),
    						sentCur.getLong	 (sentCur.getColumnIndex(DBAdapter.KEY_GRPID)),
    						sentCur.getString(sentCur.getColumnIndex(DBAdapter.KEY_NUMBER)),
    						sentCur.getString(sentCur.getColumnIndex(DBAdapter.KEY_MESSAGE)),
    						sentCur.getLong	 (sentCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)),
    						sentCur.getString(sentCur.getColumnIndex(DBAdapter.KEY_DATE)),
    						sentCur.getInt	 (sentCur.getColumnIndex(DBAdapter.KEY_SENT)),
    						sentCur.getInt	 (sentCur.getColumnIndex(DBAdapter.KEY_DELIVER)),
    						sentCur.getInt	 (sentCur.getColumnIndex(DBAdapter.KEY_MSG_PARTS)),
    						sentCur.getInt	 (sentCur.getColumnIndex(DBAdapter.KEY_S_MILLIS)),
    						sentCur.getInt	 (sentCur.getColumnIndex(DBAdapter.KEY_D_MILLIS)),
    						tempIds));
    			}else{
    				childSentArray.get(z).keyNumber = childSentArray.get(z).keyNumber + ", " + sentCur.getString(sentCur.getColumnIndex(DBAdapter.KEY_NUMBER));
    				childSentArray.get(z).keyIds.add(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)));
    			}
    			
    		}while(sentCur.moveToNext());
    	}
    	
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSentArray.get(i).keyMessage);
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSentArray.get(i).keyDate);
    		child.put(RECEIVER, childSentArray.get(i).keyNumber);
    		groupChildSent.add(child);
    	}
    	
    	childData.add(groupChildSent);
    	
    	//--------------------------------------------------------------------------end of sent msgs load-----------
    	
    	//--------------------------------------------------------------------------end of child load--------------
    	
    }
    
    
    
    
    
    class childSch{
		long 		keyId;
		long 		keyGrpId;
		String 		keyNumber;
		String 		keyMessage;
		long		keyTimeMilis;
		String 		keyDate;
		int			keySent;
		int			keyDeliver;
		int			keyMsgParts;
		ArrayList<Long>	keyIds;
		
		childSch(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, int keysent, int keydeliver, int keymsgparts, ArrayList<Long> keyids){
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
	
	
	class childSent{
		long 		keyId;
		long 		keyGrpId;
		String 		keyNumber;
		String 		keyMessage;
		long		keyTimeMilis;
		String 		keyDate;
		int			keySent;
		int			keyDeliver;
		int			keyMsgParts;
		long		keySMillis;
		long		keyDMillis;
		ArrayList<Long> keyIds;
		
		childSent(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, int keysent, int keydeliver, int keymsgparts, long keysmillis, long keydmillis, ArrayList<Long> keyids){
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
}