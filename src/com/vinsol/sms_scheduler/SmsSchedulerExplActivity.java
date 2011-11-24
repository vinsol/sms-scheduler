package com.vinsol.sms_scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

public class SmsSchedulerExplActivity extends Activity {
    /** Called when the activity is first created. */
	
	public static ArrayList<childSch> childSchArray = new ArrayList<childSch>();
	public static ArrayList<childSent> childSentArray = new ArrayList<childSent>();
	
	ExpandableListView 		explList;
	ImageButton				newSmsButton;
	ImageButton				optionsImageButton;
	
	SimpleExpandableListAdapter mAdapter;
	private ArrayList<HashMap<String, String>> headerData;
	private ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	
	private String[] numbersForSentDialog = new String[]{};
	private ArrayList<Long> idsForSentDialog = new ArrayList<Long>();
	
	int sizeOfChildSchArray = 0;
	
	DBAdapter mdba = new DBAdapter(SmsSchedulerExplActivity.this);
	
	final String NAME = "name";
	final String IMAGE = "image";
	final String MESSAGE = "message";
	final String DATE = "date";
	final String RECEIVER = "receiver";
	
	final int MENU_DELETE = 432142;
	final int MENU_EDIT = 432143;
	
	Dialog sentInfoDialog;
	
	IntentFilter mIntentFilter;
	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("MESSAGE", "INTO UPDATES RECEIVER");
			
			
			
			
			loadData();
			Log.i("MESSAGE", "==========================" + childSchArray.size());
			mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();
			
		}
	};
	
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        newSmsButton 		= (ImageButton) findViewById(R.id.main_new_sms_imgbutton);
        explList 	 		= (ExpandableListView) findViewById(R.id.main_expandable_list);
        optionsImageButton 	= (ImageButton) findViewById(R.id.main_options_menu_imgbutton);
        registerForContextMenu(explList);
        
        newSmsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SmsSchedulerExplActivity.this, NewScheduleActivity.class);
				startActivity(intent);
			}
		});
        
        
        
        
        explList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View view, int groupPosition, int childPosition, long id) {
				if(groupPosition == 0){
					Intent intent = new Intent(SmsSchedulerExplActivity.this, EditSmsActivity.class);
					intent.putExtra("GROUP", childSchArray.get(childPosition).keyGrpId);
					intent.putExtra("NUMBER", childSchArray.get(childPosition).keyNumber);
					intent.putExtra("MESSAGE", childSchArray.get(childPosition).keyMessage);
					intent.putExtra("TIME", childSchArray.get(childPosition).keyTimeMilis);
					startActivity(intent);
				}else if(groupPosition == 1){
					showSentInfoDialog(childPosition);
				}
				
				return false;
			}
		});
	    
        
        
        
        
        optionsImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});
        
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("My special action");
        
        setExplData();
        
        explList.setAdapter(mAdapter);
        registerForContextMenu(explList);
    }
    
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	setExplData();
    	explList.setAdapter(mAdapter);
    	registerReceiver(mUpdateReceiver, mIntentFilter);
    }
    
    
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type  = ExpandableListView.getPackedPositionType (info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		if(type == 1){
			final String MENU_TITLE_DELETE = "Delete";
			CharSequence menu_title = MENU_TITLE_DELETE.subSequence(0, MENU_TITLE_DELETE.length());
			menu.add(0, MENU_DELETE, 1, menu_title);
		}
    }
    
    
    
    
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupPos = 0, childPos = 0;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			ArrayList<Long> selectedIds = new ArrayList<Long>();
			
			switch (item.getItemId()) {
				case MENU_DELETE:
					//--------------------------------------Delete context option ------------------------------------
					mdba.open();
					
					if(groupPos == 0){
						selectedIds = childSchArray.get(childPos).keyIds;	
					}else if(groupPos == 1){
						selectedIds = childSentArray.get(childPos).keyIds;
					}
					for(int i = 0; i<selectedIds.size(); i++){
						mdba.deleteSms(selectedIds.get(i), this.getApplicationContext());
					}
					Intent mIntent = new Intent();
	                 
	                 mIntent.setAction("My special action");
	                 PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	         		
	         		 AlarmManager am = (AlarmManager) this.getApplicationContext().getSystemService(this.getApplicationContext().ALARM_SERVICE);
	         		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
	         		 
					Toast.makeText(this.getApplicationContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
					break;
					//--------------------------------------------------------------------------------------------------
					
			}
		}
		return super.onContextItemSelected(item);
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
    			
    			Log.i("MESSAGE", "------------------------Value of ChildPosition : " + childSchArray.size());
    			
    			if(groupPosition == 0){
    				messageTextView.setText(childSchArray.get(childPosition).keyMessage);
    				statusImageView.setImageResource(childSchArray.get(childPosition).keyImageRes);
    				dateTextView.setText(childSchArray.get(childPosition).keyDate);
    				receiverTextView.setText(childSchArray.get(childPosition).keyNumber);
    			}else if(groupPosition == 1){
    				messageTextView.setText(childSentArray.get(childPosition).keyMessage);
    				statusImageView.setImageResource(childSentArray.get(childPosition).keyImgRes);
    				dateTextView.setText(childSentArray.get(childPosition).keyDate);
    				receiverTextView.setText(childSentArray.get(childPosition).keyNumber);
    			}
    			
    			return v;
    		}
    		
    	};
    }
    
    
    public void loadData(){
    	
    	childData.clear();
    	
    	mdba.open();
    	Cursor schCur  = mdba.fetchAllScheduled();
    	Cursor sentCur = mdba.fetchAllSent();
    	mdba.close();
    	
    	
    	//-----------------------Putting group headers for Expandable list---------------------------- 
    	headerData = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> group1 = new HashMap<String, String>();
    	group1.put(NAME, "Scheduled");
    	headerData.add(group1);
    	
    	HashMap<String, String> group2 = new HashMap<String, String>();
    	group2.put(NAME, "Sent");
    	headerData.add(group2);
    	Log.i("MESSAGE", "results : " + schCur.getCount() + " ");
    	//---------------------------------------------------------------------------------------------
    	
    	
    	//----------------------Putting child data into Child Hash-------------------------------------
    	//childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
    	
    	
    	//------------------------Loading scheduled msgs----------------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildSch = new ArrayList<HashMap<String, Object>>();
    	int z = -1;
    	childSchArray.clear();
    	if(schCur.moveToFirst()){
    		z = -1;
    		do{
    			if(z == -1 || childSchArray.get(z).keyGrpId != schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_GRPID))){
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
    	
    	Log.i("MESSAGE", z + "");
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSchArray.get(i).keyMessage);
    		boolean bool = true;
    		
    		if(childSchArray.get(i).keyNumber!=""){
    			childSchArray.get(i).keyImageRes = R.drawable.icon;
    		}else{
    			childSchArray.get(i).keyImageRes = R.drawable.ic_btn_write_sms;
    		}
    		
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSchArray.get(i).keyDate);
    		child.put(RECEIVER, childSchArray.get(i).keyNumber);
    		groupChildSch.add(child);
    		
    	}
    	
    	childData.add(groupChildSch);
    	
    	//-------------------------------------------------------------------------end of scheduled msgs load-------- 
    	
    	
    	
    	
    	//--------------------------loading sent messages------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildSent = new ArrayList<HashMap<String, Object>>();
    	z = -1;
    	childSentArray.clear();
    	Log.i("MESSAGE", "Number of Sent Messages : " + sentCur.getCount());
    	if(sentCur.moveToFirst()){
    		z = -1;
    		do{
    			if(z == -1 || childSentArray.get(z).keyGrpId != sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_GRPID))){
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
    	Log.i("MESSAGE", "sents :" + z);
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSentArray.get(i).keyMessage);
    		int condition = 1;
    		mdba.open();
    		for(int k = 0; k< childSentArray.get(i).keyIds.size(); k++){
    			Cursor cur = mdba.fetchSmsDetails(childSentArray.get(i).keyIds.get(k));
    			cur.moveToFirst();
    			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT)) == cur.getInt(cur.getColumnIndex(DBAdapter.KEY_MSG_PARTS))){
    				condition = 2;
    			}
    			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_DELIVER))>0 && (condition == 2)){
    				condition = 3;
    			}
    			if(mdba.checkDeliver(childSentArray.get(i).keyIds.get(k))){
    				condition = 4;
    			}
    		}
    		
    		switch (condition) {
			case 1:
				childSentArray.get(i).keyImgRes = R.drawable.icon;
				break;
				
			case 2:
				childSentArray.get(i).keyImgRes = R.drawable.ic_btn_write_sms;
				break;
				
			case 3:
				childSentArray.get(i).keyImgRes = R.drawable.icon;
				break;
				
			case 4:
				childSentArray.get(i).keyImgRes = R.drawable.ic_btn_write_sms;
				break;
				
			default:
				break;
			}
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSentArray.get(i).keyDate);
    		child.put(RECEIVER, childSentArray.get(i).keyNumber);
    		groupChildSent.add(child);
    		mdba.close();
    	}
    	
    	childData.add(groupChildSent);
    	
    	//--------------------------------------------------------------------------end of sent msgs load-----------
    	
    	//--------------------------------------------------------------------------end of child load--------------
    	sizeOfChildSchArray = childSchArray.size();
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
		int 		keyImageRes;
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
		int			keyImgRes;
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
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
		switch (item.getItemId()) {
	        case R.id.template_opt_menu:
	        					intent = new Intent(SmsSchedulerExplActivity.this, ManageTemplateActivity.class);
	        					startActivity(intent);
	                            break;
	        case R.id.group_opt_menu:
	                            intent = new Intent(SmsSchedulerExplActivity.this, ManageGroupsActivity.class);
	                            startActivity(intent);
	        					break;
	    }
	    return true;
	}
	
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mUpdateReceiver);
	}
	
	
	
	
	
	public void showSentInfoDialog(int childPos){
		sentInfoDialog = new Dialog(SmsSchedulerExplActivity.this);
		sentInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		sentInfoDialog.setContentView(R.layout.sent_details_layout);
		ListView numbersList = (ListView) sentInfoDialog.findViewById(R.id.sent_details_dialog_number_list);
		TextView timeLabel = (TextView) sentInfoDialog.findViewById(R.id.sent_details_dialog_time_label);
		TextView messageSpace = (TextView) sentInfoDialog.findViewById(R.id.sent_details_dialog_message_space);
		mdba.open();
		numbersForSentDialog = childSentArray.get(childPos).keyNumber.split(", ");
		idsForSentDialog = mdba.getIds(childSentArray.get(childPos).keyGrpId);
		timeLabel.setText(childSentArray.get(childPos).keyDate);
		messageSpace.setText(childSentArray.get(childPos).keyMessage);
		SentDialogNumberListAdapter sentDialogAdapter = new SentDialogNumberListAdapter();
		numbersList.setAdapter(sentDialogAdapter);
		
		sentInfoDialog.show();
	}
    
	
	
	
	class SentDialogNumberListAdapter extends ArrayAdapter{
		SentDialogNumberListAdapter(){
    		super(SmsSchedulerExplActivity.this, R.layout.sent_details_number_list_row, numbersForSentDialog);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		if(convertView!=null){
    			return convertView;
    		}
    		final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.sent_details_number_list_row, parent, false);
    		
    		
    		TextView numberLabel = (TextView)row.findViewById(R.id.sent_details_number_list_number_text);
    		numberLabel.setText(numbersForSentDialog[position]);
    		
    		ImageView statusImage = (ImageView)row.findViewById(R.id.sent_details_number_list_status_image);
    		
    		long currentId = idsForSentDialog.get(position);
    		
    		int condition = 1;
    		Cursor cur = mdba.fetchSmsDetails(currentId);
			cur.moveToFirst();
			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT)) == cur.getInt(cur.getColumnIndex(DBAdapter.KEY_MSG_PARTS))){
				condition = 2;
			}
			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_DELIVER))>0 && (condition == 2)){
				condition = 3;
			}
			if(mdba.checkDeliver(currentId)){
				condition = 4;
			}
			
			
			switch (condition) {
			case 1:
				statusImage.setImageResource(R.drawable.icon);
				break;
				
			case 2:
				statusImage.setImageResource(R.drawable.ic_btn_write_sms);
				break;
				
			case 3:
				statusImage.setImageResource(R.drawable.icon);
				break;
				
			case 4:
				statusImage.setImageResource(R.drawable.ic_btn_write_sms);
				break;
				
			default:
				break;
			}
    		
    		return row;
    	}
    }
	
	
	
}