package com.vinsol.sms_scheduler.activities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.MyContact;

public class SmsSchedulerExplActivity extends Activity {
    
	public static ArrayList<childSch> childSchArray = new ArrayList<childSch>();
	public static ArrayList<childSent> childSentArray = new ArrayList<childSent>();
	public static ArrayList<childDraft> childDraftArray = new ArrayList<childDraft>();
	
	ExpandableListView 		explList;
	ImageView				newSmsButton;
	ImageView				optionsImageButton;
	
	LinearLayout blankListLayout;
	
	Button blankListAddButton;
	
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
	final String EXTRA_RECEIVERS = "ext_receivers";
	final String RECEIVER = "receiver";
	
	final int MENU_DELETE = 432142;
	final int MENU_EDIT = 432143;
	
	Dialog sentInfoDialog;
	
	Dialog dataLoadWaitDialog;
	int toOpen = 0;
	
		   

	IntentFilter mIntentFilter;
	IntentFilter dataloadIntentFilter;
	
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
	
	
	
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
//			Toast.makeText(SmsSchedulerExplActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					toOpen = 0;
					intent = new Intent(SmsSchedulerExplActivity.this, ManageGroupsActivity.class);
                    startActivity(intent);
				}
			}
		}
	};
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(!SmsApplicationLevelData.isDataLoaded){
        	ContactsAsync contactsAsync = new ContactsAsync();
    		contactsAsync.execute();
        }
        
        
        newSmsButton 		= (ImageView) findViewById(R.id.main_new_sms_imgbutton);
        explList 	 		= (ExpandableListView) findViewById(R.id.main_expandable_list);
        optionsImageButton 	= (ImageView) findViewById(R.id.main_options_menu_imgbutton);
        blankListLayout		= (LinearLayout) findViewById(R.id.blank_list_layout);
        blankListAddButton	= (Button) findViewById(R.id.blank_list_add_button);
        
        registerForContextMenu(explList);
        
        dataLoadWaitDialog = new Dialog(SmsSchedulerExplActivity.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        newSmsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SmsSchedulerExplActivity.this, NewScheduleActivity.class);
				startActivity(intent);
			}
		});
        
        
        
        
        blankListAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SmsSchedulerExplActivity.this, NewScheduleActivity.class);
				startActivity(intent);
			}
		});
        
        
        
        
        explList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View view, int groupPosition, int childPosition, long id) {
				if(groupPosition == 1){
					Intent intent = new Intent(SmsSchedulerExplActivity.this, EditScheduledSmsActivity.class);
					intent.putExtra("GROUP", childSchArray.get(childPosition).keyGrpId);
					intent.putExtra("NUMBER", childSchArray.get(childPosition).keyNumber);
					intent.putExtra("MESSAGE", childSchArray.get(childPosition).keyMessage);
					intent.putExtra("TIME", childSchArray.get(childPosition).keyTimeMilis);
					startActivity(intent);
				}else if(groupPosition == 2){
					showSentInfoDialog(childPosition);
				}else if(groupPosition == 0){
					Intent intent = new Intent(SmsSchedulerExplActivity.this, EditScheduledSmsActivity.class);
					intent.putExtra("GROUP", childDraftArray.get(childPosition).keyGrpId);
					intent.putExtra("NUMBER", childDraftArray.get(childPosition).keyNumber);
					intent.putExtra("MESSAGE", childDraftArray.get(childPosition).keyMessage);
					intent.putExtra("TIME", childDraftArray.get(childPosition).keyTimeMilis);
					startActivity(intent);
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
        
        dataloadIntentFilter = new IntentFilter();
        dataloadIntentFilter.addAction(SmsApplicationLevelData.DIALOG_CONTROL_ACTION);
        
        setExplData();
        
        explList.setAdapter(mAdapter);
        registerForContextMenu(explList);
    }
    
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	mdba.open();
    	Cursor cur = mdba.fetchAllScheduled();
    	if(cur.getCount()>0){
    		explList.setVisibility(LinearLayout.VISIBLE);
    		blankListLayout.setVisibility(LinearLayout.GONE);
    	}else{
    		cur = null;
    		cur = mdba.fetchAllSent();
    		if(cur.getCount()>0){
    			explList.setVisibility(LinearLayout.VISIBLE);
    			blankListLayout.setVisibility(LinearLayout.GONE);
    		}else{
    			explList.setVisibility(LinearLayout.GONE);
    			blankListLayout.setVisibility(LinearLayout.VISIBLE);
    		}
    	}
    	mdba.close();
    
    	setExplData();
    	explList.setAdapter(mAdapter);
    	explList.expandGroup(0);
    	explList.expandGroup(1);
    	explList.expandGroup(2);
    	
    	registerReceiver(mUpdateReceiver, mIntentFilter);
    	registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
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
					
					if(groupPos == 1){
						selectedIds = childSchArray.get(childPos).keyIds;	
					}else if(groupPos == 2){
						selectedIds = childSentArray.get(childPos).keyIds;
					}else if(groupPos == 0){
						selectedIds = childDraftArray.get(childPos).keyIds;
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
					
					
			        Cursor cur = mdba.fetchAllScheduled();
			        if(cur.getCount()>0){
			        	explList.setVisibility(LinearLayout.VISIBLE);
			        	blankListLayout.setVisibility(LinearLayout.GONE);
			        }else{
			        	cur = null;
			        	cur = mdba.fetchAllSent();
			        	if(cur.getCount()>0){
			        		explList.setVisibility(LinearLayout.VISIBLE);
			            	blankListLayout.setVisibility(LinearLayout.GONE);
			        	}else{
			        		explList.setVisibility(LinearLayout.GONE);
			            	blankListLayout.setVisibility(LinearLayout.VISIBLE);
			        	}
			        }
			        mdba.close();
			        
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
    		
//    		@Override
//	    	public View newChildView(boolean isLastChild, ViewGroup parent) {
//	    		
//	    		return layoutInflater.inflate(R.layout.main_row_layout, null, false);
//	    	}
    		
    		
    		
    		@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    			GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.expandable_list_group_view, null);
        			holder = new GroupListHolder();
        			holder.groupHeading = (TextView) convertView.findViewById(R.id.group_heading);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			
    			
    			holder.groupHeading.setText(headerData.get(groupPosition).get(NAME));
    			
    			return convertView;
    		}



			@Override
    		public android.view.View getChildView(int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
				ChildRowHolder holder;
				if(convertView==null){
					convertView = layoutInflater.inflate(R.layout.main_row_layout, null, false);
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
    				holder.messageTextView.setText(childSchArray.get(childPosition).keyMessage);
    				holder.statusImageView.setImageResource(childSchArray.get(childPosition).keyImageRes);
    				holder.dateTextView.setText(childSchArray.get(childPosition).keyDate);
    				holder.receiverTextView.setText(numbersLengthRectify(childSchArray.get(childPosition).keyNumber));
    				holder.extraReceiversTextView.setText(extraReceiversCal(childSchArray.get(childPosition).keyNumber));
    			} else if(groupPosition == 2) {
    				holder.messageTextView.setText(childSentArray.get(childPosition).keyMessage);
    				holder.statusImageView.setImageResource(childSentArray.get(childPosition).keyImgRes);
    				holder.dateTextView.setText(childSentArray.get(childPosition).keyDate);
    				holder.receiverTextView.setText(numbersLengthRectify(childSentArray.get(childPosition).keyNumber));
    				holder.extraReceiversTextView.setText(extraReceiversCal(childSentArray.get(childPosition).keyNumber));
    			} else if(groupPosition == 0){
    				if(!childDraftArray.get(childPosition).keyMessage.matches("^(''|[' ']*)$")){
    					holder.messageTextView.setText(childDraftArray.get(childPosition).keyMessage);
    				}else{
    					holder.messageTextView.setText("[No Message Written]");
    					holder.messageTextView.setTextColor(0xff777777);
    				}
    				holder.statusImageView.setImageResource(childDraftArray.get(childPosition).keyImageRes);
    				holder.dateTextView.setText(childDraftArray.get(childPosition).keyDate);
    				if(!childDraftArray.get(childPosition).keyNumber.matches("^(''|[' ']*)$")){
    					holder.receiverTextView.setText(numbersLengthRectify(childDraftArray.get(childPosition).keyNumber));
        				holder.extraReceiversTextView.setText(extraReceiversCal(childDraftArray.get(childPosition).keyNumber));
    				}else{
    					holder.receiverTextView.setText("[No Recepients Added]");
    					holder.receiverTextView.setTextColor(0xff777777);
    				}
    				
    			}
    			
    			
    			
    			
    			if(groupPosition == 1){
    				holder.statusImageView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							final Dialog d = new Dialog(SmsSchedulerExplActivity.this);
							d.requestWindowFeature(Window.FEATURE_NO_TITLE);
							d.setContentView(R.layout.confirmation_dialog_layout);
							TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
							Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
							Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
							
							questionText.setText("Delete this scheduled message?");
							
							yesButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									ArrayList<Long> selectedIds = new ArrayList<Long>();
									selectedIds = childSchArray.get(childPosition).keyIds;
									mdba.open();
									for(int i = 0; i<selectedIds.size(); i++){
										mdba.deleteSms(selectedIds.get(i), SmsSchedulerExplActivity.this);
									}
									Intent mIntent = new Intent();
					                 
					                 mIntent.setAction("My special action");
					                 PendingIntent pi = PendingIntent.getBroadcast(SmsSchedulerExplActivity.this, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
					         		
					         		 AlarmManager am = (AlarmManager) SmsSchedulerExplActivity.this.getSystemService(SmsSchedulerExplActivity.this.ALARM_SERVICE);
					         		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
					         		 
									Toast.makeText(SmsSchedulerExplActivity.this, "Message Deleted", Toast.LENGTH_SHORT).show();
									
									
							        Cursor cur = mdba.fetchAllScheduled();
							        if(cur.getCount()>0){
							        	explList.setVisibility(LinearLayout.VISIBLE);
							        	blankListLayout.setVisibility(LinearLayout.GONE);
							        }else{
							        	cur = null;
							        	cur = mdba.fetchAllSent();
							        	if(cur.getCount()>0){
							        		explList.setVisibility(LinearLayout.VISIBLE);
							            	blankListLayout.setVisibility(LinearLayout.GONE);
							        	}else{
							        		explList.setVisibility(LinearLayout.GONE);
							            	blankListLayout.setVisibility(LinearLayout.VISIBLE);
							        	}
							        }
							        mdba.close();
							        d.cancel();
								}
							});
							
							noButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									d.cancel();
								}
							});
							
							d.show();
							
						}
					});
    				
    			}else if(groupPosition == 0){
    				holder.statusImageView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							final Dialog d = new Dialog(SmsSchedulerExplActivity.this);
							d.requestWindowFeature(Window.FEATURE_NO_TITLE);
							d.setContentView(R.layout.confirmation_dialog_layout);
							TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
							Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
							Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
							
							questionText.setText("Delete this draft?");
							
							yesButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									ArrayList<Long> selectedIds = new ArrayList<Long>();
									selectedIds = childDraftArray.get(childPosition).keyIds;
									mdba.open();
									for(int i = 0; i<selectedIds.size(); i++){
										mdba.deleteSms(selectedIds.get(i), SmsSchedulerExplActivity.this);
									}
									Intent mIntent = new Intent();
					                 
					                 mIntent.setAction("My special action");
					                 PendingIntent pi = PendingIntent.getBroadcast(SmsSchedulerExplActivity.this, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
					         		
					         		 AlarmManager am = (AlarmManager) SmsSchedulerExplActivity.this.getSystemService(SmsSchedulerExplActivity.this.ALARM_SERVICE);
					         		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
					         		 
									Toast.makeText(SmsSchedulerExplActivity.this, "Message Deleted", Toast.LENGTH_SHORT).show();
									
									
							        Cursor cur = mdba.fetchAllScheduled();
							        if(cur.getCount()>0){
							        	explList.setVisibility(LinearLayout.VISIBLE);
							        	blankListLayout.setVisibility(LinearLayout.GONE);
							        }else{
							        	cur = null;
							        	cur = mdba.fetchAllSent();
							        	if(cur.getCount()>0){
							        		explList.setVisibility(LinearLayout.VISIBLE);
							            	blankListLayout.setVisibility(LinearLayout.GONE);
							        	}else{
							        		explList.setVisibility(LinearLayout.GONE);
							            	blankListLayout.setVisibility(LinearLayout.VISIBLE);
							        	}
							        }
							        mdba.close();
							        d.cancel();
								}
							});
							
							noButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									d.cancel();
								}
							});
							
							d.show();
						}
					});
    				
    			}
    			return convertView;
    		}
    		
    	};
    }
    
    
    public void loadData(){
    	
    	childData.clear();
    	
    	mdba.open();
    	Cursor schCur  = mdba.fetchAllScheduledNoDraft();
    	Cursor sentCur = mdba.fetchAllSent();
    	Cursor draftCur = mdba.fetchAllDrafts();
    	
    	
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
    			
    			Cursor spanCur = mdba.fetchSpanForSms(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)));
    			
    			spanCur.moveToFirst();
    			String displayName = spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_DN));
    			
    			
    			if(z == -1 || childSchArray.get(z).keyGrpId != schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_GRPID))){
    				z++;
    				ArrayList<Long> tempIds = new ArrayList<Long>();
    				tempIds.add(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)));
    				childSchArray.add(new childSch(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)),
    						schCur.getLong	(schCur.getColumnIndex(DBAdapter.KEY_GRPID)),
    						displayName,
    						schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_MESSAGE)),
    						schCur.getLong	(schCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)),
    						schCur.getString(schCur.getColumnIndex(DBAdapter.KEY_DATE)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_SENT)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_DELIVER)),
    						schCur.getInt	(schCur.getColumnIndex(DBAdapter.KEY_MSG_PARTS)),
    						tempIds));
    			}else{
    				childSchArray.get(z).keyNumber = childSchArray.get(z).keyNumber + ", " + displayName;
    				childSchArray.get(z).keyIds.add(schCur.getLong(schCur.getColumnIndex(DBAdapter.KEY_ID)));
    			}
    			
    		}while(schCur.moveToNext());
    	}
    	
    	Log.i("MESSAGE", z + "");
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSchArray.get(i).keyMessage);
    		boolean bool = true;
    		

    		childSchArray.get(i).keyImageRes = R.drawable.delete_image_states;    		
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSchArray.get(i).keyDate);
    		child.put(RECEIVER, childSchArray.get(i).keyNumber);
    		child.put(EXTRA_RECEIVERS, extraReceiversCal(childSchArray.get(i).keyNumber));
    		groupChildSch.add(child);
    		
    	}
    	
    	
    	
    	//-------------------------------------------------------------------------end of scheduled msgs load-------- 
    	
    	
    	
    	
    	//--------------------------loading sent messages------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildSent = new ArrayList<HashMap<String, Object>>();
    	z = -1;
    	childSentArray.clear();
    	Log.i("MESSAGE", "Number of Sent Messages : " + sentCur.getCount());
    	if(sentCur.moveToFirst()){
    		z = -1;
    		do{
    			
    			Cursor spanCur = mdba.fetchSpanForSms(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)));
    			
    			spanCur.moveToFirst();
    			String displayName = spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_DN));
    			
    			
    			
    			if(z == -1 || childSentArray.get(z).keyGrpId != sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_GRPID))){
    				z++;
    				ArrayList<Long> tempIds = new ArrayList<Long>();
    				tempIds.add(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)));
    				childSentArray.add(new childSent(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)),
    						sentCur.getLong	 (sentCur.getColumnIndex(DBAdapter.KEY_GRPID)),
    						displayName,
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
    				
    				childSentArray.get(z).keyNumber = childSentArray.get(z).keyNumber + ", " + displayName;
    				childSentArray.get(z).keyIds.add(sentCur.getLong(sentCur.getColumnIndex(DBAdapter.KEY_ID)));
    			}
    			
    		}while(sentCur.moveToNext());
    	}
    	Log.i("MESSAGE", "sents :" + z);
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childSentArray.get(i).keyMessage);
    		int condition = 1;
    		
    		for(int k = 0; k< childSentArray.get(i).keyIds.size(); k++){
    			Cursor cur = mdba.fetchSmsDetails(childSentArray.get(i).keyIds.get(k));
    			cur.moveToFirst();
    			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT)) == 0){
    				condition = 1;
    				break;
    			}
    			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT)) > 0 && !mdba.checkDeliver(childSentArray.get(i).keyIds.get(k))){
    				condition = 2;
    				break;
    			}
    			if(mdba.checkDeliver(childSentArray.get(i).keyIds.get(k))){
    				condition = 3;
    			}
    		}
    		
    		
    		switch (condition) {
			case 1:
				childSentArray.get(i).keyImgRes = R.drawable.sent_failure_icon;
				break;
				
			case 2:
				childSentArray.get(i).keyImgRes = R.drawable.sending_sms_icon;
				break;
				
			case 3:
				childSentArray.get(i).keyImgRes = R.drawable.sent_success_icon;
				break; 
				
			default:
				break;
			}
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childSentArray.get(i).keyDate);
    		
    		child.put(RECEIVER, numbersLengthRectify(childSentArray.get(i).keyNumber));
    		child.put(EXTRA_RECEIVERS, extraReceiversCal(childSentArray.get(i).keyNumber));
    		groupChildSent.add(child);

    	}
    	
    	
    	
    	//--------------------------------------------------------------------------end of sent msgs load-----------
    	
    	
    	
    	//------------------------Loading Drafts----------------------------------------------------
    	
    	ArrayList<HashMap<String, Object>> groupChildDraft = new ArrayList<HashMap<String, Object>>();
    	z = -1;
    	childDraftArray.clear();
    	if(draftCur.moveToFirst()){
    		z = -1;
    		do{
    			
    			Cursor spanCur = mdba.fetchSpanForSms(draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_ID)));
    			
    			spanCur.moveToFirst();
    			String displayName = spanCur.getString(spanCur.getColumnIndex(DBAdapter.KEY_SPAN_DN));
    			
    			if(z == -1 || childDraftArray.get(z).keyGrpId != draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_GRPID))){
    				z++;
    				ArrayList<Long> tempIds = new ArrayList<Long>();
    				tempIds.add(draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_ID)));
    				childDraftArray.add(new childDraft(draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_ID)),
    						draftCur.getLong	(draftCur.getColumnIndex(DBAdapter.KEY_GRPID)),
    						displayName,
    						draftCur.getString(draftCur.getColumnIndex(DBAdapter.KEY_MESSAGE)),
    						draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_TIME_MILLIS)),
    						draftCur.getString(draftCur.getColumnIndex(DBAdapter.KEY_DATE)),
    						draftCur.getInt	(draftCur.getColumnIndex(DBAdapter.KEY_SENT)),
    						draftCur.getInt	(draftCur.getColumnIndex(DBAdapter.KEY_DELIVER)),
    						draftCur.getInt	(draftCur.getColumnIndex(DBAdapter.KEY_MSG_PARTS)),
    						tempIds));
    			}else{
    				childDraftArray.get(z).keyNumber = childDraftArray.get(z).keyNumber + ", " + displayName;
    				childDraftArray.get(z).keyIds.add(draftCur.getLong(draftCur.getColumnIndex(DBAdapter.KEY_ID)));
    				//childDraftArray.get(z).keyExtraReceivers = extraReceiversCal(childDraftArray.get)
    			}
    			
    		}while(draftCur.moveToNext());
    	}
    	
    	Log.i("MESSAGE", z + "");
    	for(int i = 0; i<= z; i++){
    		HashMap<String, Object> child = new HashMap<String, Object>();
    		child.put(NAME, childDraftArray.get(i).keyMessage);
    		boolean bool = true;
    		
//    		if(childDraftArray.get(i).keyNumber!=""){
    			childDraftArray.get(i).keyImageRes = R.drawable.delete_image_states;
//    		}else{
//    			childDraftArray.get(i).keyImageRes = R.drawable.ic_btn_write_sms;
//    		}
    		
    		child.put(IMAGE, this.getResources().getDrawable(R.drawable.icon));
    		child.put(DATE, childDraftArray.get(i).keyDate);
    		child.put(RECEIVER, numbersLengthRectify(childDraftArray.get(i).keyNumber));
    		try{
    			child.put(EXTRA_RECEIVERS, extraReceiversCal(childSentArray.get(i).keyNumber));
    		}catch (IndexOutOfBoundsException e) {
    			child.put(EXTRA_RECEIVERS, "");
			}
    		
    		groupChildDraft.add(child);
    		
    	}
    	
    	childData.add(groupChildDraft);
    	childData.add(groupChildSch);
    	childData.add(groupChildSent);
    	
    	//-------------------------------------------------------------------------end of drafts load--------
    	
    	
    	
    	//--------------------------------------------------------------------------end of child load--------------
    	sizeOfChildSchArray = childSchArray.size();
    	mdba.close();
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
		String 		keyExtraReceivers;
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
		String 		keyExtraReceivers;
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
	
	
	
	class childDraft{
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
		String 		keyExtraReceivers;
		ArrayList<Long>	keyIds;
		
		childDraft(long keyid, long keygrpid, String keynumber, String keymessage, long keytimemilis, String keydate, int keysent, int keydeliver, int keymsgparts, ArrayList<Long> keyids){
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
	        					if(SmsApplicationLevelData.isDataLoaded){
	        						intent = new Intent(SmsSchedulerExplActivity.this, ManageGroupsActivity.class);
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
	
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mUpdateReceiver);
		unregisterReceiver(mDataLoadedReceiver);
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
		messageSpace.setMovementMethod(new ScrollingMovementMethod());
		SentDialogNumberListAdapter sentDialogAdapter = new SentDialogNumberListAdapter();
		numbersList.setAdapter(sentDialogAdapter);
		
		sentInfoDialog.show();
	}
    
	
	
	
	//********* Adapter for the list of recipients and msg status, in the show dialog of sent msgs ***********************
	class SentDialogNumberListAdapter extends ArrayAdapter{
		
		SentDialogNumberListAdapter(){
    		super(SmsSchedulerExplActivity.this, R.layout.sent_details_number_list_row, numbersForSentDialog);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		SentDialogListHolder holder;
    		if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.sent_details_number_list_row, parent, false);
        		holder = new SentDialogListHolder();
        		holder.numberLabel = (TextView)convertView.findViewById(R.id.sent_details_number_list_number_text);
        		holder.statusImage = (ImageView)convertView.findViewById(R.id.sent_details_number_list_status_image);
        		convertView.setTag(holder);
    		}else{
    			holder = (SentDialogListHolder) convertView.getTag();
    		}

    		holder.numberLabel.setText(numbersForSentDialog[position]);
    		
    		long currentId = idsForSentDialog.get(position);
    		
    		int condition = 1;
    		Cursor cur = mdba.fetchSmsDetails(currentId);
			cur.moveToFirst();
			if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT)) > 0 && !(mdba.checkDeliver(currentId))){
				condition = 2;
			}else
			if(mdba.checkDeliver(currentId)){
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
    		
    		return convertView;
    	}
    }
	
	
	
	//------------------- For displaying appropriate number of recipients in sms listing---------------------
	
	private String numbersLengthRectify(String number){
		if(number.length()<= 30){
			return number;
		}
		int delimiterCount = 0;
		int validDelimiterCount = 0;
		int validLength = 0;
		for(int i = 0; i< number.length(); i++){
			if(number.charAt(i)==' ' && number.charAt(i-1)==','){
				delimiterCount++;
				if(i<=30){
					validDelimiterCount++;
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
		int validLength = 0;
		for(int i = 0; i< number.length(); i++){
			if(number.charAt(i)==' ' && number.charAt(i-1)==','){
				delimiterCount++;
				if(i<=30){
					validDelimiterCount++;
					validLength = i-2;
				}
			}
		}
		
		
		return "+" + (delimiterCount - validDelimiterCount + 1);
	}
	
	//----------------------------------------------------------------------------------------
	
	
	
	
	
	//------------------------Contacts Data Load functions---------------------------------------------
	
	public void loadContactsData(){
		// SAZWQA: NR
//		ScontactsList.clear();
		if(SmsApplicationLevelData.contactsList.size()==0){
			long t1 = System.currentTimeMillis();
			ContentResolver cr = getContentResolver();
		    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		    if(cursor.moveToFirst()){
		    	do{
		    	  if(!(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0"))){
		    		String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		    	    
		    	    Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
		    	    if(phones.moveToFirst()){
		    	    	MyContact contact = new MyContact();
			    		contact.content_uri_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			    		contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			    		// SAZWQA: Why?
//			    		contact.number = " ";
		    	    	contact.number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
		    	    	
		    	    	Cursor cur = SmsSchedulerExplActivity.this.managedQuery(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID}, ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID + "=" + contact.content_uri_id, null, null);
		    	    	
		    	    	if(cur.moveToFirst()){
		    	    		do{
		    	    			// SAZWQA: Should we add a rule that if GROUP_ROW_ID == 0 or it's equal to phone no. don't ADD it?
		    	    			contact.groupRowId.add(cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)));
		    	    		}while(cur.moveToNext());
		    	    	}
		    	    	
		    	    	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.content_uri_id));
			    	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
			    	    try{
			    	    	contact.image = BitmapFactory.decodeStream(input);
			    	    	contact.image.getHeight();
			    	    } catch (NullPointerException e){
			    	    	contact.image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail);
			    	    }
			    	    
			    	    SmsApplicationLevelData.contactsList.add(contact);
			    	    
			    	    //Log.i("MSG", contact.groupRowId.size() + "");
			    	    
		    	    }
		    	  }  
		    	}while(cursor.moveToNext());
		    }
		}
	}
	
	
	
	class ContactsAsync extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			loadContactsData();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			SmsApplicationLevelData.isDataLoaded = true;
			Intent mIntent = new Intent();
			mIntent.setAction(SmsApplicationLevelData.DIALOG_CONTROL_ACTION);
			
			PendingIntent pi = PendingIntent.getBroadcast(SmsSchedulerExplActivity.this, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			AlarmManager am = (AlarmManager) SmsSchedulerExplActivity.this.getSystemService(SmsSchedulerExplActivity.this.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
		}
	}
	
	
	
	
	
	
	class GroupListHolder{
		TextView groupHeading;
	}
	
	
	class ChildRowHolder{
		TextView messageTextView;
		ImageView statusImageView;
		TextView dateTextView;
		TextView receiverTextView;
		TextView extraReceiversTextView;
	}
	
	
	class SentDialogListHolder{
		TextView numberLabel;
		ImageView statusImage;
	}
}
