/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Groups;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.ContactNumber;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.models.Sms;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

public class Home extends Activity {
    
	private ArrayList<Sms> scheduledSMSs = new ArrayList<Sms>();
	private ArrayList<Sms> sentSMSs = new ArrayList<Sms>();
	private ArrayList<Sms> drafts = new ArrayList<Sms>();
	
	private ExpandableListView 		explList;
	private ImageView				newSmsButton;
	private ImageView				optionsImageButton;
	
	private LinearLayout blankListLayout;
	
	private Button blankListAddButton;
	
	private SimpleExpandableListAdapter mAdapter;
	private ArrayList<HashMap<String, String>> headerData;
	private ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	
	private String[] numbersForSentDialog = new String[]{};
	private int smsPositionForSentDialog;
	
	private DBAdapter mdba = new DBAdapter(Home.this);
	
	private final String NAME = "name";
	private final String IMAGE = "image";
	private final String DATE = "date";
	private final String EXTRA_RECEIVERS = "ext_receivers";
	private final String RECEIVER = "receiver";
	
	private final int MENU_DELETE =	R.id.home_options_delete;
	private final int MENU_RESCHEDULE = R.id.home_options_reschedule;
	private final int MENU_ADD_TO_TEMPLATE = R.id.home_options_add_to_template;
	
	private Dialog sentInfoDialog;
	
	private Dialog dataLoadWaitDialog;
	private int toOpen = 0;
	
	Long selectedSms;
	
	private Cursor groupCursor;

	private IntentFilter mIntentFilter;
	private IntentFilter dataloadIntentFilter;
	
	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			loadData();
			mAdapter.notifyDataSetChanged();
		}
	};
	
	
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		
		public void onReceive(Context context, Intent intent) {
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					toOpen = 0;
					intent = new Intent(Home.this, ManageGroups.class);
                    startActivity(intent);
				}
			}
		}
	};
	
	
	
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        FlurryAgent.logEvent("Home Activity Started");
        
        if(!SmsSchedulerApplication.isDataLoaded){
        	ContactsAsync contactsAsync = new ContactsAsync();
    		contactsAsync.execute();
        }
        
        newSmsButton 		= (ImageView) findViewById(R.id.main_new_sms_imgbutton);
        explList 	 		= (ExpandableListView) findViewById(R.id.main_expandable_list);
        optionsImageButton 	= (ImageView) findViewById(R.id.main_options_menu_imgbutton);
        blankListLayout		= (LinearLayout) findViewById(R.id.blank_list_layout);
        blankListAddButton	= (Button) findViewById(R.id.blank_list_add_button);
        
        registerForContextMenu(explList);
        
        dataLoadWaitDialog = new Dialog(Home.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        newSmsButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View arg0) {
				FlurryAgent.logEvent("New SMS");
				if(SmsSchedulerApplication.screenWidthInPixels==0){
					SmsSchedulerApplication.screenWidthInPixels = explList.getWidth();
				}
				Intent intent = new Intent(Home.this, ScheduleNewSms.class);
				startActivity(intent);
			}
		});
        
        
        blankListAddButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View arg0) {
				FlurryAgent.logEvent("New SMS");
				if(SmsSchedulerApplication.screenWidthInPixels==0){
					SmsSchedulerApplication.screenWidthInPixels = blankListLayout.getWidth();
				}
				Intent intent = new Intent(Home.this, ScheduleNewSms.class);
				startActivity(intent);
			}
		});
        
        
        explList.setOnChildClickListener(new OnChildClickListener() {
			
			
			public boolean onChildClick(ExpandableListView arg0, View view, int groupPosition, int childPosition, long id) {
				if(groupPosition == 1){
					FlurryAgent.logEvent("Edit Scheduled Message");
					if(SmsSchedulerApplication.screenWidthInPixels==0){
						SmsSchedulerApplication.screenWidthInPixels = explList.getWidth();
					}
					Intent intent = new Intent(Home.this, EditScheduledSms.class);
					intent.putExtra("SMS DATA", scheduledSMSs.get(childPosition));
					startActivity(intent);
				}else if(groupPosition == 2){
					FlurryAgent.logEvent("Checked Sent Message");
					openContextMenu(view);
//					showSentInfoDialog(childPosition);
				}else if(groupPosition == 0){
					FlurryAgent.logEvent("Edit Draft");
					if(SmsSchedulerApplication.screenWidthInPixels==0){
						SmsSchedulerApplication.screenWidthInPixels = explList.getWidth();
					}
					Intent intent = new Intent(Home.this, EditScheduledSms.class);
					intent.putExtra("SMS DATA", drafts.get(childPosition));
					startActivity(intent);
				}
				return false;
			}
		});
	    
        
        
        optionsImageButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Options Menu Button Clicked");
				openOptionsMenu();
			}
		});
        
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getResources().getString(R.string.update_action));
        
        dataloadIntentFilter = new IntentFilter();

        dataloadIntentFilter.addAction(Constants.DIALOG_CONTROL_ACTION);

        setExplData();
        
        explList.setAdapter(mAdapter);
        registerForContextMenu(explList);
    }
    
    
    
    
    
    protected void onResume() {
    	super.onResume();
    	
    	doScreenUpdate();
    	setExplData();
    	explList.setAdapter(mAdapter);
    	explList.expandGroup(0);
    	explList.expandGroup(1);
    	explList.expandGroup(2);
    	
    	registerReceiver(mUpdateReceiver, mIntentFilter);
    	registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
    }
    
    
    
    
    @Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
    
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	//TODO
    	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		CharSequence menu_title;
		
		if(group == 2){
			final String MENU_TITLE_RESCHEDULE = "Reschedule";
			menu_title = MENU_TITLE_RESCHEDULE.subSequence(0, MENU_TITLE_RESCHEDULE.length());
			menu.add(0, MENU_RESCHEDULE, 1, menu_title);
		
			final String MENU_TITLE_ADD_TO_TEMPLATES = "Add to Templates";
			menu_title = MENU_TITLE_ADD_TO_TEMPLATES.subSequence(0, MENU_TITLE_ADD_TO_TEMPLATES.length());
			menu.add(0, MENU_ADD_TO_TEMPLATE, 2, menu_title);
			
			final String MENU_TITLE_DELETE = "Delete";
			menu_title = MENU_TITLE_DELETE.subSequence(0, MENU_TITLE_DELETE.length());
			menu.add(0, MENU_DELETE, 3, menu_title);
		}else{
			final String MENU_TITLE_DELETE = "Delete";
			menu_title = MENU_TITLE_DELETE.subSequence(0, MENU_TITLE_DELETE.length());
			menu.add(0, MENU_DELETE, 1, menu_title);
		}
		
		
    }
    
    
    
    
    
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupPos = 0, childPos = 0;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			switch (item.getItemId()) {
				case MENU_DELETE:
					mdba.open();
					HashMap<String, String> deletedGroup = new HashMap<String, String>();
					if(groupPos == 1){
						selectedSms = scheduledSMSs.get(childPos).keyId;
						deletedGroup.put("deleted SMS", "Scheduled SMS");

					}else if(groupPos == 2){
						selectedSms = sentSMSs.get(childPos).keyId;
						deletedGroup.put("deleted SMS", "Edited SMS");
						
					}else if(groupPos == 0){
						selectedSms = drafts.get(childPos).keyId;
						deletedGroup.put("deleted SMS", "Draft");
						
					}
					
					FlurryAgent.logEvent("Message Deleted", deletedGroup);
					deleteSms();
			        break;
			     
				case MENU_RESCHEDULE:
					if(SmsSchedulerApplication.screenWidthInPixels==0){
						SmsSchedulerApplication.screenWidthInPixels = explList.getWidth();
					}
					Intent intent = new Intent(Home.this, EditScheduledSms.class);
					intent.putExtra("SMS DATA", sentSMSs.get(childPos));
					startActivity(intent);
					break;
				
				case MENU_ADD_TO_TEMPLATE:
					showAddToTemplateDialog(sentSMSs.get(childPos).keyMessage);
					break;
			}
		}
		return super.onContextItemSelected(item);
	}
    
    
    
    
    
    private void showAddToTemplateDialog(String keyMessage) {
		final Dialog dialog = new Dialog(Home.this);
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.add_to_templates_dialog);
		dialog.setTitle("Add to Templates");
		
		final EditText templateText 		= (EditText) dialog.findViewById(R.id.new_template_dialog_input_edit_text);
		Button addTemplateButton 	= (Button) dialog.findViewById(R.id.new_template_dialog_add_button);
		Button cancelTemplateButton = (Button) dialog.findViewById(R.id.new_template_dialog_cancel_button);
		
		templateText.setText(keyMessage);
		addTemplateButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				if(templateText.getText().toString().equals("")){
					Toast.makeText(Home.this, "Cannot add blank template", Toast.LENGTH_SHORT).show();
				}else{
					mdba.open();
					Cursor cur = mdba.fetchAllTemplates();
					mdba.close();
					boolean z = true;
					if(cur.moveToFirst()){
						do{
							if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)).equals(templateText.getText().toString())){
								z = false;
								break;
							}
						}while(cur.moveToNext());
					}
					if(!z){
						Toast.makeText(Home.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}else{
						mdba.open();
						mdba.close();
						Toast.makeText(Home.this, "Template added", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				}
			}
		});
		
		cancelTemplateButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		
		dialog.show();
	}




	private void setExplData(){
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
    		
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    			GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.home_expandable_list_group, null);
        			holder = new GroupListHolder();
        			holder.groupHeading = (TextView) convertView.findViewById(R.id.group_heading);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			
    			holder.groupHeading.setText(headerData.get(groupPosition).get(NAME));
    			
    			return convertView;
    		}


			
    		public android.view.View getChildView(int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
				ChildRowHolder holder;
				if(convertView==null){
					convertView = layoutInflater.inflate(R.layout.home_expandable_list_child, null, false);
					holder = new ChildRowHolder();
					holder.messageTextView  		= (TextView)  convertView.findViewById(R.id.main_row_message_area);
	    			holder.statusImageView 			= (ImageView) convertView.findViewById(R.id.main_row_image_area);
	    			holder.dateTextView				= (TextView)  convertView.findViewById(R.id.main_row_date_area);
	    			holder.receiverTextView 		= (TextView)  convertView.findViewById(R.id.main_row_recepient_area);
	    			holder.extraReceiversTextView 	= (TextView)  convertView.findViewById(R.id.main_row_extra_recepient_area);
	    			convertView.setTag(holder);
				}else{
					holder = (ChildRowHolder) convertView.getTag();
				}
				
    			if(groupPosition == 1) {
    				holder.messageTextView.setText(scheduledSMSs.get(childPosition).keyMessage);
    				holder.statusImageView.setImageResource(scheduledSMSs.get(childPosition).keyImageRes);
    				holder.dateTextView.setText(scheduledSMSs.get(childPosition).keyDate);
    				holder.receiverTextView.setText(numbersLengthRectify(scheduledSMSs.get(childPosition).keyNumber));
    				holder.extraReceiversTextView.setText(extraReceiversCal(scheduledSMSs.get(childPosition).keyNumber));
    				holder.messageTextView.setTextColor(getResources().getColor(R.color.black));
    				holder.receiverTextView.setTextColor(getResources().getColor(R.color.black));
    			} else if(groupPosition == 2) {
    				holder.messageTextView.setText(sentSMSs.get(childPosition).keyMessage);
    				holder.statusImageView.setImageResource(sentSMSs.get(childPosition).keyImageRes);
    				holder.dateTextView.setText(sentSMSs.get(childPosition).keyDate);
    				holder.receiverTextView.setText(numbersLengthRectify(sentSMSs.get(childPosition).keyNumber));
    				holder.extraReceiversTextView.setText(extraReceiversCal(sentSMSs.get(childPosition).keyNumber));
    				holder.messageTextView.setTextColor(getResources().getColor(R.color.black));
    				holder.receiverTextView.setTextColor(getResources().getColor(R.color.black));
    			} else if(groupPosition == 0){
    				if(!drafts.get(childPosition).keyMessage.matches("^(''|[' ']*)$")){
    					holder.messageTextView.setText(drafts.get(childPosition).keyMessage);
    				}else{
    					holder.messageTextView.setText("[No Message Written]");
    					holder.messageTextView.setTextColor(getResources().getColor(R.color.grey));
    				}
    				holder.statusImageView.setImageResource(drafts.get(childPosition).keyImageRes);
    				holder.dateTextView.setText(drafts.get(childPosition).keyDate);
    				if(!drafts.get(childPosition).keyNumber.matches("^(''|[' ']*)$")){
    					holder.receiverTextView.setText(numbersLengthRectify(drafts.get(childPosition).keyNumber));
        				holder.extraReceiversTextView.setText(extraReceiversCal(drafts.get(childPosition).keyNumber));
    				}else{
    					holder.receiverTextView.setText("[No Recepients Added]");
    					holder.receiverTextView.setTextColor(getResources().getColor(R.color.grey));
    					holder.extraReceiversTextView.setText("");
    				}
    			}
    			
    			
    			final HashMap<String, String> deletedGroup = new HashMap<String, String>();
    			
    			if(groupPosition == 1){
    				holder.statusImageView.setOnClickListener(new OnClickListener() {
						
						
						public void onClick(View v) {
							showDeleteDialog(scheduledSMSs, childPosition, "Delete this Scheduled Message?");
							deletedGroup.put("deleted SMS", "Scheduled SMS");
						}
					});
    				
    			}else if(groupPosition == 0){
    				holder.statusImageView.setOnClickListener(new OnClickListener() {
						
						
						public void onClick(View v) {
							showDeleteDialog(drafts, childPosition, "Delete this Draft?");
							deletedGroup.put("deleted SMS", "Draft");
						}
    				});
    			}else if(groupPosition == 2){
    				holder.statusImageView.setOnClickListener(new OnClickListener() {
						
						
						public void onClick(View v) {
							showSentInfoDialog(childPosition);
						}
					});
						
    			}
    			FlurryAgent.logEvent("Message Deleted", deletedGroup);
    			return convertView;
			}
    	};
    }
    
    
    private void loadData(){
    	
    	Log.d("Into LoadData");
    	childData.clear();
    	
    	mdba.open();
    	drafts.clear();
    	scheduledSMSs.clear();
    	sentSMSs.clear();
    	
    	
    	//-----------------------Putting group headers for Expandable list---------------------------- 
    	headerData = new ArrayList<HashMap<String, String>>();
    	
//    	if(draftCur.getCount()>0){
    		HashMap<String, String> group3 = new HashMap<String, String>();
        	group3.put(NAME, "Drafts");
        	headerData.add(group3);
//    	}
    	
//    	if(schCur.getCount()>0){
    		HashMap<String, String> group1 = new HashMap<String, String>();
        	group1.put(NAME, "Scheduled");
        	headerData.add(group1);
//    	}
    	
//    	if(sentCur.getCount()>0){
    		HashMap<String, String> group2 = new HashMap<String, String>();
        	group2.put(NAME, "Sent");
        	headerData.add(group2);
//    	}
    	//---------------------------------------------------------------------------------------------
    	
        
    	
    	//--------------Extracting Sent, Draft and Scheduled messages from Database------------------------
        Cursor SMSsCur = mdba.fetchAllRecipientDetails();
        
        long previousSmsId = -1;
        int previousSmsType = -1;
        	
        Sms SMS = new Sms();
        Log.d("size of SMSsCur : " + SMSsCur.getCount());
        if(SMSsCur.moveToFirst()){
        	do{
        		if(previousSmsId!=SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_ID))){
        			
        			if(previousSmsType == 0){
        				drafts.add(SMS);
        			}else if(previousSmsType == 1 || previousSmsType == 3){
        				scheduledSMSs.add(SMS);
        			}else if(previousSmsType == 2){
        				sentSMSs.add(SMS);
        			}
        			
        			previousSmsId = SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_ID));
        			previousSmsType = SMSsCur.getInt(SMSsCur.getColumnIndex(DBAdapter.KEY_STATUS));
        			
        			String displayName = "";
        			ArrayList<Recipient> tempRecipients = new ArrayList<Recipient>();
    				
    				SMS = new Sms(SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_ID)),
    						displayName,
    						SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_MESSAGE)),
    						SMSsCur.getInt(SMSsCur.getColumnIndex(DBAdapter.KEY_MSG_PARTS)),
    						SMSsCur.getLong	(SMSsCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)),
    						SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_DATE)),
    						tempRecipients);
        		}
        		Recipient recipient = new Recipient(SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_ID)),
        											SMSsCur.getInt(SMSsCur.getColumnIndex(DBAdapter.KEY_RECIPIENT_TYPE)),
        											SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME)),
        											SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_CONTACT_ID)),
        											SMSsCur.getLong(SMSsCur.getColumnIndex(DBAdapter.KEY_ID)),
        											SMSsCur.getInt(SMSsCur.getColumnIndex(DBAdapter.KEY_SENT)),
        											SMSsCur.getInt(SMSsCur.getColumnIndex(DBAdapter.KEY_DELIVER)),
        											SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_NUMBER)));
        		if(SMS.keyNumber.equals("")){
        			SMS.keyNumber = SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME));
        		}else{
        			SMS.keyNumber = SMS.keyNumber + ", " + SMSsCur.getString(SMSsCur.getColumnIndex(DBAdapter.KEY_DISPLAY_NAME));
        		}
        		
    			SMS.keyRecipients.add(recipient);
        		
        	}while(SMSsCur.moveToNext());
        	
        	if(previousSmsType == 0){
				drafts.add(SMS);
			}else if(previousSmsType == 1 || previousSmsType == 3){
				Log.d("getting added to schedules");
				scheduledSMSs.add(SMS);
			}else if(previousSmsType == 2){
				sentSMSs.add(SMS);
			}
        }
        SMSsCur.close();
        //------------------------------------------------Messages Extracted From Database---------------------
        
        
        
        //------------------------Loading scheduled msgs----------------------------------------------------
    	ArrayList<HashMap<String, Object>> groupChildSch = new ArrayList<HashMap<String, Object>>();
    	
    	for(int i = 0; i< scheduledSMSs.size(); i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, scheduledSMSs.get(i).keyMessage);
    		scheduledSMSs.get(i).keyImageRes = R.drawable.delete_icon_states;
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, scheduledSMSs.get(i).keyDate);
    		child.put(RECEIVER, scheduledSMSs.get(i).keyNumber);
    		child.put(EXTRA_RECEIVERS, extraReceiversCal(scheduledSMSs.get(i).keyNumber));
    		groupChildSch.add(child);
    	}
    	//-------------------------------------------------------------------------end of scheduled msgs load-------- 
    	
    	
    	
    	
    	//--------------------------loading sent messages------------------------------------------
    	ArrayList<HashMap<String, Object>> groupChildSent = new ArrayList<HashMap<String, Object>>();

    	for(int i = sentSMSs.size()-1; i > -1; i--){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, sentSMSs.get(i).keyMessage);
    		int condition = 1;
    		
    		for(int k = 0; k< sentSMSs.get(i).keyRecipients.size(); k++){
    			if(sentSMSs.get(i).keyRecipients.get(k).sent == 0){
    				condition = 1;
    				sentSMSs.get(i).keyImageRes = R.drawable.sent_failure_icon;
    				break;
    			}
    			if(sentSMSs.get(i).keyRecipients.get(k).sent > 0 && !mdba.checkDelivery(sentSMSs.get(i).keyRecipients.get(k).recipientId)){
    				condition = 2;
    				sentSMSs.get(i).keyImageRes = R.drawable.sending_sms_icon;
    				break;
    			}
    			if(sentSMSs.get(i).keyRecipients.get(k).sent == sentSMSs.get(i).keyRecipients.get(k).delivered){
    				condition = 3;
    			}
    		}
    		
    		if(condition==3){
    			sentSMSs.get(i).keyImageRes = R.drawable.sent_success_icon;
    		}
    		
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, sentSMSs.get(i).keyDate);
    		
    		child.put(RECEIVER, numbersLengthRectify(sentSMSs.get(i).keyNumber));
    		child.put(EXTRA_RECEIVERS, extraReceiversCal(sentSMSs.get(i).keyNumber));
    		groupChildSent.add(child);
    	}
    	//--------------------------------------------------------------------------end of sent msgs load-----------
    	
    	
    	
    	//------------------------Loading Drafts----------------------------------------------------
    	ArrayList<HashMap<String, Object>> groupChildDraft = new ArrayList<HashMap<String, Object>>();

    	for(int i = 0; i< drafts.size(); i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, drafts.get(i).keyMessage);
    		drafts.get(i).keyImageRes = R.drawable.delete_icon_states;
    		
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, drafts.get(i).keyDate);
    		child.put(RECEIVER, numbersLengthRectify(drafts.get(i).keyNumber));
    		try{
    			child.put(EXTRA_RECEIVERS, extraReceiversCal(sentSMSs.get(i).keyNumber));
    		}catch (IndexOutOfBoundsException e) {
    			child.put(EXTRA_RECEIVERS, "");
			}
    		
    		groupChildDraft.add(child);
    	}
    	
    	childData.add(groupChildDraft);
    	childData.add(groupChildSch);
    	childData.add(groupChildSent);
    	//-------------------------------------------------------------------------end of drafts load--------
    	
    	mdba.close();
    }

	
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
		switch (item.getItemId()) {
	        case R.id.template_opt_menu:
	        	FlurryAgent.logEvent("Manage Templates");
	        	intent = new Intent(Home.this, ManageTemplates.class);
	        	startActivity(intent);
	            break;
	        case R.id.group_opt_menu:
	        	FlurryAgent.logEvent("Manage Groups");
	        	if(SmsSchedulerApplication.isDataLoaded){
	        		intent = new Intent(Home.this, ManageGroups.class);
		            startActivity(intent);
	        	}else{
	        		dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
	        		toOpen = 1;
	        		dataLoadWaitDialog.show();
	        	}
	            break;
	    }
	    return true;
	}
	
	
	
	
	
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mUpdateReceiver);
		unregisterReceiver(mDataLoadedReceiver);
	}
	
	
	
	
	private void showSentInfoDialog(int childPos){
		sentInfoDialog = new Dialog(Home.this);
		sentInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		sentInfoDialog.setContentView(R.layout.sent_sms_details);
		ListView numbersList = (ListView) sentInfoDialog.findViewById(R.id.sent_details_dialog_number_list);
		TextView timeLabel = (TextView) sentInfoDialog.findViewById(R.id.sent_details_dialog_time_label);
		TextView messageSpace = (TextView) sentInfoDialog.findViewById(R.id.sent_details_dialog_message_space);
		mdba.open();
		numbersForSentDialog = sentSMSs.get(childPos).keyNumber.split(", ");
		smsPositionForSentDialog = childPos;
		timeLabel.setText(sentSMSs.get(childPos).keyDate);
		messageSpace.setText(sentSMSs.get(childPos).keyMessage);
		messageSpace.setMovementMethod(new ScrollingMovementMethod());
		SentDialogNumberListAdapter sentDialogAdapter = new SentDialogNumberListAdapter();
		numbersList.setAdapter(sentDialogAdapter);
		mdba.close();
		sentInfoDialog.show();
	}
    
	
	
	
	//********* Adapter for the list of recipients and msg status, in the show dialog of sent msgs ***********************
	@SuppressWarnings("rawtypes")
	private class SentDialogNumberListAdapter extends ArrayAdapter{
		
		@SuppressWarnings({ "unchecked" })
		SentDialogNumberListAdapter(){
    		super(Home.this, R.layout.sent_sms_recepients_list_row, numbersForSentDialog);
    	}
    	
    	
    	
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		SentDialogListHolder holder;
    		if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.sent_sms_recepients_list_row, parent, false);
        		holder = new SentDialogListHolder();
        		holder.numberLabel = (TextView)convertView.findViewById(R.id.sent_details_number_list_number_text);
        		holder.statusImage = (ImageView)convertView.findViewById(R.id.sent_details_number_list_status_image);
        		convertView.setTag(holder);
    		}else{
    			holder = (SentDialogListHolder) convertView.getTag();
    		}

    		holder.numberLabel.setText(numbersForSentDialog[position]);
    		
    		int condition = 1;
    		mdba.open();
			if(sentSMSs.get(smsPositionForSentDialog).keyRecipients.get(position).sent > 0 && (sentSMSs.get(smsPositionForSentDialog).keyRecipients.get(position).delivered != sentSMSs.get(smsPositionForSentDialog).keyMessageParts)){
				condition = 2;
			}else
			if(sentSMSs.get(smsPositionForSentDialog).keyRecipients.get(position).delivered == sentSMSs.get(smsPositionForSentDialog).keyMessageParts){
				condition = 3;
			}
			
			switch (condition) {
			case 1:
				holder.statusImage.setImageResource(R.drawable.sent_failure_icon);
				break;
				
			case 2:
				holder.statusImage.setImageResource(R.drawable.sending_sms_icon);
				break;
				
			case 3:
				holder.statusImage.setImageResource(R.drawable.sent_success_icon);
				break;
					
			default:
				break;
			}
    		mdba.close();
    		return convertView;
    	}
    }
	
	
	
	//------------------- For displaying appropriate number of recipients in sms listing---------------------
	
	private String numbersLengthRectify(String number){
		if(number.length()<= 30){
			return number;
		}
		int validLength = 0;
		for(int i = 0; i< number.length(); i++){
			if(number.charAt(i)==' ' && number.charAt(i-1)==','){
				if(i<=30){
					validLength = i;
				}
			}
		}
		String validLengthNumber = number.substring(0, validLength);
		
		return validLengthNumber;
	}
	
	
	
	private String extraReceiversCal(String number){
		if(number.length()<= 30){
			return "";
		}
		int delimiterCount = 0;
		int validDelimiterCount = 0;
		for(int i = 0; i< number.length(); i++){
			if(number.charAt(i)==' ' && number.charAt(i-1)==','){
				delimiterCount++;
				if(i<=30){
					validDelimiterCount++;
				}
			}
		}
		
		return "+" + (delimiterCount - validDelimiterCount + 1);
	}
	//----------------------------------------------------------------------------------------
	
	
	
	
	
	//------------------------Contacts Data Load functions---------------------------------------------
	public void loadContactsByPhone(){
    	Long startTime = System.currentTimeMillis();
    	
    	if(SmsSchedulerApplication.contactsList.size()==0){
    		ContentResolver cr = getContentResolver();
    		
    		ArrayList<String> contactIds = new ArrayList<String>();
    		ArrayList<Long> groups = new ArrayList<Long>();
    		
    		String[] projection = new String[] {Groups._ID,};
			Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
    		groupCursor = cr.query(groupsUri, projection, null, null, null);
    		while(groupCursor.moveToNext()){
    			groups.add(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
//    			Log.i("MSG", "Group : " + groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
    		}
    		
    		Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        	
    		Log.d("Cursor size : " + phones.getCount());
        	
    		while (phones.moveToNext())
        	{
        	  boolean isContactPresent = false;
        	  String contactId = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
        	  ContactNumber cn = new ContactNumber(Long.parseLong(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))),
        			    refineNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))), 
					  	resolveType(phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))));
        	  
        	  if(phones.getInt(phones.getColumnIndex(Phone.IS_PRIMARY))!=0){
				  cn.isPrimary = true;
			  }
        	  
        	  for(int i =0; i< SmsSchedulerApplication.contactsList.size(); i++){
        		  if(Long.parseLong(contactId)==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
        			  isContactPresent = true;
        			  SmsSchedulerApplication.contactsList.get(i).numbers.add(cn);
        			  break;
        		  }
        	  }
        	  if(!isContactPresent){
        		  contactIds.add(contactId);
        		  Contact contact = new Contact();
        		  contact.content_uri_id = Long.parseLong(contactId);
		    	  contact.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		    	  contact.numbers.add(cn);
		    	  Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.content_uri_id);
		    	  InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		    	  try{
		    		  BitmapFactory.Options o = new BitmapFactory.Options();
		    	      o.inPurgeable = true;
		    	      o.inInputShareable = true;
		    	      contact.image = BitmapFactory.decodeStream(input, null, o);
		    	      contact.image.getHeight();
		    	  } catch (NullPointerException e){
		    	      contact.image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail);
		    	  }
		    	  
		    	  SmsSchedulerApplication.contactsList.add(contact);
		    	  
        	  }
        	}
        	phones.close();
		    	  
		    	  String[] contactIdsArray = new String[contactIds.size()];
		    	  for(int i = 0; i< contactIds.size(); i++){
		    		  contactIdsArray[i] = contactIds.get(i);
		    	  }
		    	  
		    	  Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID}, null, null, null);
		    	  
		    	  if(cur.moveToFirst()){
		    		  do{
		    			  Long groupId = cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
		    			  Long contactIdOfGroup = cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
		    			  boolean isValid = false;
  	    				  for(int m = 0; m< groups.size(); m++){
  	    				    	if(cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)) == groups.get(m)){
  	    							isValid = true;
  	    							break;
  	    						}
  	    				  }
		    			  if(!(groupId==0) && isValid){
		    				  for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
		    					  if(contactIdOfGroup==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
		    						  SmsSchedulerApplication.contactsList.get(i).groupRowId.add(groupId);
//		    						  groups.remove(groupId);
		    					  }
		    				  }
		    			  }
		    				  
		    		  }while(cur.moveToNext());
		    	  }
		    	  
		    	  
		    //To set primary number for contacts...	  
		    for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
		    	boolean primaryPresent = false;
		    	for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
		    		if(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).isPrimary){
		    			SmsSchedulerApplication.contactsList.get(i).numbers.add(0, SmsSchedulerApplication.contactsList.get(i).numbers.remove(j));
		    			primaryPresent = true;
		    		}
		    	}
		    	if(!primaryPresent)
		    		SmsSchedulerApplication.contactsList.get(i).numbers.get(0).isPrimary=true;
		    }	  
		    
		    
		    
        	for(int i = 0; i< SmsSchedulerApplication.contactsList.size()-1; i++){
		    	for(int j = i+1; j< SmsSchedulerApplication.contactsList.size(); j++){
		    		if(SmsSchedulerApplication.contactsList.get(i).name.toUpperCase().compareTo(SmsSchedulerApplication.contactsList.get(j).name.toUpperCase())>0){
		    			SmsSchedulerApplication.contactsList.set(j, SmsSchedulerApplication.contactsList.set(i, SmsSchedulerApplication.contactsList.get(j)));
		    		}
		    	}
		    }
        	
        	for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
        		Log.d("=====================================================");
        		Log.d(SmsSchedulerApplication.contactsList.get(i).name + " ; " + 
        				SmsSchedulerApplication.contactsList.get(i).content_uri_id);
        		
        		for(int j = 0 ; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
        			if(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).isPrimary){
        				Log.d(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).type + " : " +
            					SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number + " PRIMARY");
        			}else{
        				Log.d(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).type + " : " +
            					SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number);
        			}
        		}
        		for(int j = 0 ; j< SmsSchedulerApplication.contactsList.get(i).groupRowId.size(); j++){
        			Log.d("Group : " + SmsSchedulerApplication.contactsList.get(i).groupRowId.get(j));
        		}
        	}
    	}
    	
    	
    	
    	Long endTime = System.currentTimeMillis();
		Log.d("===================================\nTime taken : " + (endTime-startTime));
		
		HashMap<String, Long> param = new HashMap<String, Long>();
		param.put("Time Taken", (endTime-startTime));
		FlurryAgent.logEvent("Contacts Loaded", param);
		
    }
	
	
	
	public static String refineNumber(String number) {
		if(number.matches("[0-9]+")){
			return number;
		}
		ArrayList<Character> chars = new ArrayList<Character>();
		for(int i = 0; i< number.length(); i++){
			chars.add(number.charAt(i));
		}
		for(int i = 0; i< chars.size(); i++){
			if(!(chars.get(i)=='0' || chars.get(i)=='1' || chars.get(i)=='2' || chars.get(i)=='3' || chars.get(i)=='4' ||
					chars.get(i)=='5' || chars.get(i)=='6' || chars.get(i)=='7' || chars.get(i)=='8' || chars.get(i)=='9'|| chars.get(i)=='+')){
				chars.remove(i);
				i--;
			}
		}
		//if(number.matches("[0-9]{10}")){
			number = new String();
			for(int i = 0; i< chars.size(); i++){
				number = number + chars.get(i);
			}
			return number;
		//}
	}
	
	
	
	public void loadContactsData(){
		if(SmsSchedulerApplication.contactsList.size()==0){
			System.currentTimeMillis();
			
			String[] projection = new String[] {Groups._ID};
			Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
			
			
			ContentResolver cr = getContentResolver();
			groupCursor = cr.query(groupsUri, projection, null, null, null);
		    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		    if(cursor.moveToFirst()){
		    	do{
		    	  if(!(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0"))){
		    		String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		    		Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
		    	    if(phones.moveToFirst()){
		    	    	Contact contact = new Contact();
			    		contact.content_uri_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
			    		contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			    		do{
			    			contact.numbers.add(new ContactNumber(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID), phones.getString(phones.getColumnIndex(Phone.NUMBER)), resolveType(Integer.parseInt(phones.getString(phones.getColumnIndex(Phone.TYPE))))));
			    		}while(phones.moveToNext());
		    	    	Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID}, ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID + "=" + contact.content_uri_id, null, null);
		    	    	if(cur.moveToFirst()){
		    	    		do{
		    	    			// SAZWQA: Should we add a rule that if GROUP_ROW_ID == 0 or it's equal to phone no. don't ADD it?
		    	    			boolean equalsNumber = false;
		    	    			for(int i=0; i< contact.numbers.size(); i++){
		    	    				if(String.valueOf(cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID))).equals(contact.numbers.get(i))){
		    	    					equalsNumber = true;
		    	    					break;
		    	    				}
		    	    			}
		    	    			if(!equalsNumber && cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID))!=0){
		    	    				boolean isValid = false;
		    	    				if(groupCursor.moveToFirst()){
		    	    					do{
		    	    						if(!cur.isClosed() && !groupCursor.isClosed() && cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)) == groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID))){
		    	    							isValid = true;
		    	    							break;
		    	    						}
		    	    					}while(groupCursor.moveToNext());
		    	    				}
		    	    				if(isValid){
		    	    					contact.groupRowId.add(cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)));
			    	    			}
		    	    			}
		    	    		}while(cur.moveToNext());
		    	    	}
		    	    	cur.close();
		    	    	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.content_uri_id);
			    	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
			    	    try{
			    	    	BitmapFactory.Options o = new BitmapFactory.Options();
			    	        o.inPurgeable = true;
			    	        o.inInputShareable = true;
			    	    	contact.image = BitmapFactory.decodeStream(input, null, o);
			    	    	contact.image.getHeight();
			    	    } catch (NullPointerException e){
			    	    	contact.image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail);
			    	    }
			    	    
			    	    SmsSchedulerApplication.contactsList.add(contact);
		    	    }
		    	    phones.close();
		    	  }
		    	}while(cursor.moveToNext());
		    }
		    cursor.close();
		    groupCursor.close();
		    
		    for(int i = 0; i< SmsSchedulerApplication.contactsList.size()-1; i++){
		    	for(int j = i+1; j< SmsSchedulerApplication.contactsList.size(); j++){
		    		if(SmsSchedulerApplication.contactsList.get(i).name.toUpperCase().compareTo(SmsSchedulerApplication.contactsList.get(j).name.toUpperCase())>0){
		    			SmsSchedulerApplication.contactsList.set(j, SmsSchedulerApplication.contactsList.set(i, SmsSchedulerApplication.contactsList.get(j)));
		    		}
		    	}
		    }
		}
	}
	
	
	
	public String resolveType(int type){
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
	
	
	private class ContactsAsync extends AsyncTask<Void, Void, Void>{

		
		protected Void doInBackground(Void... params) {
			loadContactsByPhone();
			return null;
		}
		
		
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			SmsSchedulerApplication.isDataLoaded = true;
			Intent mIntent = new Intent();

			mIntent.setAction(Constants.DIALOG_CONTROL_ACTION);

			sendBroadcast(mIntent);
		}
	}
	
	
	private class GroupListHolder{
		TextView groupHeading;
	}
	
	
	private class ChildRowHolder{
		TextView messageTextView;
		ImageView statusImageView;
		TextView dateTextView;
		TextView receiverTextView;
		TextView extraReceiversTextView;
	}
	
	
	private class SentDialogListHolder{
		TextView numberLabel;
		ImageView statusImage;
	}
	
	
	private void deleteSms(){
		mdba.open();
		mdba.deleteSms(selectedSms, Home.this);
		Toast.makeText(Home.this, "Message Deleted", Toast.LENGTH_SHORT).show();
		loadData();
		mAdapter.notifyDataSetChanged();
		doScreenUpdate();
	}
	
	
	private void doScreenUpdate(){
		mdba.open();
		boolean ifSmsExist = mdba.ifSmsExist();
		mdba.close();
        if(ifSmsExist){
        	explList.setVisibility(LinearLayout.VISIBLE);
        	blankListLayout.setVisibility(LinearLayout.GONE);
        }else{
        	explList.setVisibility(LinearLayout.GONE);
            blankListLayout.setVisibility(LinearLayout.VISIBLE);
        }
	}
	
	
	public void showDeleteDialog(final ArrayList<Sms> SMSList, final int childPosition, String questionString){
		final Dialog d = new Dialog(Home.this);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.confirmation_dialog);
		TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
		Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
		Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
		
		questionText.setText(questionString);
		
		yesButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				selectedSms = SMSList.get(childPosition).keyId;
				deleteSms();
		        d.cancel();
			}
		});
		
		noButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				d.cancel();
			}
		});
		d.show();
	}
}