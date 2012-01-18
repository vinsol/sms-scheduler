package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

public class SelectContacts extends Activity {
	
	private TabHost mtabHost;
	private DBAdapter mdba = new DBAdapter(this);
	private Cursor cur;
	private LinearLayout listLayout;
	private LinearLayout blankLayout;
	private Button blankListAddButton;
	
	
	
	//---------------- Variables relating to Contacts tab -----------------------
	private ListView nativeContactsList;
	private Button doneButton;
	private Button cancelButton;
	
	private ContactsAdapter contactsAdapter;
	private String origin;
	
	private ArrayList<Recipient> RecipientsTemp 	= new ArrayList<Recipient>();
	//---------------------------------------------------------------------------
	
	
	
	//----------- Variables relating to Groups Tab-------------------------------
	private ExpandableListView nativeGroupExplList;
	private ExpandableListView privateGroupExplList;
	private ArrayList<ArrayList<HashMap<String, Object>>> nativeChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private ArrayList<HashMap<String, Object>> nativeGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	
	private ArrayList<ArrayList<HashMap<String, Object>>> privateChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private ArrayList<HashMap<String, Object>> privateGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	
	private SimpleExpandableListAdapter nativeGroupAdapter;
	private SimpleExpandableListAdapter privateGroupAdapter;
	
	private boolean hasToRefresh;
	//-----------------------------------------------------------------------------
	
	
	
	
	//----------------------Variables relating to Recents Tab-----------------------
	private ArrayList<Long> recentIds = new ArrayList<Long>();
	private ArrayList<Long> recentContactIds = new ArrayList<Long>();
	private ArrayList<String> recentContactNumbers = new ArrayList<String>();
	private ListView recentsList;
	private RecentsAdapter recentsAdapter;
	//------------------------------------------------------------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contacts);
		
		
		//----------------------Setting up the Tabs--------------------------------
		final TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        
        tabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.contacts_tabs_native_contacts_list);
        spec1.setIndicator("Contacts", getResources().getDrawable(R.drawable.contacts_tab_states));

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Groups", getResources().getDrawable(R.drawable.groups_tab_states));
        spec2.setContent(R.id.group_tabs);

        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Recents", getResources().getDrawable(R.drawable.recent_tab_states));
        spec3.setContent(R.id.contacts_tabs_recents_list);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg_selector));
        }
    	//----------------------------------------------------end of Tabs Setup-----------
        
        
        
		
		Intent intent = getIntent();
		origin = intent.getStringExtra("ORIGIN");
		
		if(origin.equals("new")){
			for(int i = 0; i < ScheduleNewSms.Recipients.size(); i++){
				RecipientsTemp.add(ScheduleNewSms.Recipients.get(i));
			}
			for(int groupCount = 0; groupCount< ScheduleNewSms.nativeGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, ScheduleNewSms.nativeGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, ScheduleNewSms.nativeGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, ScheduleNewSms.nativeGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, ScheduleNewSms.nativeGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);//ScheduleNewSms.nativeGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< ScheduleNewSms.nativeChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) ScheduleNewSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				nativeGroupDataTemp.add(group);
				nativeChildDataTemp.add(child);
			}
			
			for(int groupCount = 0; groupCount< ScheduleNewSms.privateGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);// ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< ScheduleNewSms.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				privateGroupDataTemp.add(group);
				privateChildDataTemp.add(child);
			}
			
		}else if(origin.equals("edit")){
			for(int i = 0; i < EditScheduledSms.Recipients.size(); i++){
				RecipientsTemp.add(EditScheduledSms.Recipients.get(i));
			}
			
			for(int groupCount = 0; groupCount< EditScheduledSms.nativeGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSms.nativeGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSms.nativeGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSms.nativeGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSms.nativeGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);// EditScheduledSms.nativeGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.d(EditScheduledSms.nativeChildData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSms.nativeChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) EditScheduledSms.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				nativeGroupDataTemp.add(group);
				nativeChildDataTemp.add(child);
			}
			
			
			
			
			for(int groupCount = 0; groupCount< EditScheduledSms.privateGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);//EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.d(EditScheduledSms.privateChildData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSms.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				privateGroupDataTemp.add(group);
				privateChildDataTemp.add(child);
			}
		}
		
		
		
        doneButton			= (Button) 		findViewById(R.id.contacts_tab_done_button);
        cancelButton		= (Button) 		findViewById(R.id.contacts_tab_cancel_button);
        
        
        doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if(origin.equals("new")){
					ScheduleNewSms.nativeGroupData.clear();
					ScheduleNewSms.nativeChildData.clear();

					ScheduleNewSms.nativeGroupData = nativeGroupDataTemp;
					ScheduleNewSms.nativeChildData = nativeChildDataTemp;
					
					ScheduleNewSms.privateGroupData.clear();
					ScheduleNewSms.privateChildData.clear();

					ScheduleNewSms.privateGroupData = privateGroupDataTemp;
					ScheduleNewSms.privateChildData = privateChildDataTemp;
					
					ScheduleNewSms.Recipients.clear();
					for(int i = 0; i< RecipientsTemp.size(); i++){
						ScheduleNewSms.Recipients.add(RecipientsTemp.get(i));
					}
				}else if(origin.equals("edit")){
					EditScheduledSms.nativeGroupData.clear();
					EditScheduledSms.nativeChildData.clear();
					
					EditScheduledSms.nativeGroupData = nativeGroupDataTemp;
					EditScheduledSms.nativeChildData = nativeChildDataTemp;
					
					EditScheduledSms.privateGroupData.clear();
					EditScheduledSms.privateChildData.clear();
					
					EditScheduledSms.privateGroupData = privateGroupDataTemp;
					EditScheduledSms.privateChildData = privateChildDataTemp;
					
					EditScheduledSms.Recipients.clear();
					for(int i = 0; i< RecipientsTemp.size(); i++){
						EditScheduledSms.Recipients.add(RecipientsTemp.get(i));
					}
				}
					
				setResult(2, intent);
				SelectContacts.this.finish();
			}
		});
        
        
        
        
        
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(2, intent);
				SelectContacts.this.finish();
			}
		});
        
        
        
        //------------------Setting up the Contacts Tab ---------------------------------------------
        nativeContactsList 	= (ListView) findViewById(R.id.contacts_tabs_native_contacts_list);
		contactsAdapter = new ContactsAdapter();
        nativeContactsList.setAdapter(contactsAdapter);
        //------------------------------------------------------------end of setting up Contacts Tab--------
        
        
        
        
        
        listLayout = (LinearLayout) findViewById(R.id.list_layout);
        blankLayout = (LinearLayout) findViewById(R.id.blank_layout);
        blankListAddButton = (Button) findViewById(R.id.blank_list_add_button);
        
        blankListAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SelectContacts.this, EditGroup.class);
				intent.putExtra("STATE", "new");
				startActivity(intent);
			}
		});
        
        mdba.open();
		cur = mdba.fetchAllGroups();
		if(cur.getCount()==0){
			listLayout.setVisibility(LinearLayout.GONE);
			blankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			listLayout.setVisibility(LinearLayout.VISIBLE);
			blankLayout.setVisibility(LinearLayout.GONE);
		}
		mdba.close();
		
        LinearLayout groupTabs = (LinearLayout)findViewById( R.id.group_tabs );
        mtabHost = (TabHost)groupTabs.findViewById( android.R.id.tabhost );
        
        mtabHost.setup( );
        mtabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        setupTab(new TextView(this), "Phone Groups");
    	setupTab(new TextView(this), "My Groups");

        privateGroupExplList = (ExpandableListView) findViewById(R.id.private_list);
        nativeGroupExplList = (ExpandableListView) groupTabs.findViewById(R.id.native_list);
        
        nativeGroupsAdapterSetup();
		privateGroupsAdapterSetup();
		
		nativeGroupExplList.setAdapter(nativeGroupAdapter);
		privateGroupExplList.setAdapter(privateGroupAdapter);
		//----------------------------------------------------end of Groups Tab setup-------------------
        
        
		
		//--------------------setting up Recents Tab--------------------------------
		recentIds.clear();
		recentContactIds.clear();
		recentContactNumbers.clear();
		recentsList = (ListView) findViewById(R.id.contacts_tabs_recents_list);
		mdba.open();
		Cursor cur = mdba.fetchAllRecents();
		if(cur.moveToFirst()){
			do{
				recentIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_ID)));
				recentContactIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_CONTACT_ID)));
				recentContactNumbers.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_NUMBER)));
			}while(cur.moveToNext());
		}
		recentsAdapter = new RecentsAdapter();
		recentsList.setAdapter(recentsAdapter);
		mdba.close();
	}
	
	

	private void setupTab(final View view, final String tag) {
		View tabview = createTabView(mtabHost.getContext(), tag);
		TabSpec setContent = null;
		if(tag.equals("Phone Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.native_list);
		}else if(tag.equals("My Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.private_list_parent_layout);
		}
		mtabHost.addTab(setContent);
	}

	
	
	private View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(privateGroupDataTemp.size() == 0){
			hasToRefresh = true;
		}else{
			hasToRefresh = false;
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(hasToRefresh){
			mdba.open();
			cur = mdba.fetchAllGroups();
			if(cur.getCount()==0){
				listLayout.setVisibility(LinearLayout.GONE);
				blankLayout.setVisibility(LinearLayout.VISIBLE);
			}else{
				listLayout.setVisibility(LinearLayout.VISIBLE);
				blankLayout.setVisibility(LinearLayout.GONE);
			}
			mdba.close();
			reloadPrivateGroupData();
			privateGroupAdapter.notifyDataSetChanged();
			hasToRefresh = false;
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		setResult(2, intent);
		
		SelectContacts.this.finish();
	}
	
	
	
	//************************* Adapter for the list *****************************************
	//**************************** in Contacts Tab ********************************************
	
	@SuppressWarnings("rawtypes")
	private class ContactsAdapter extends ArrayAdapter {
		
		@SuppressWarnings("unchecked")
		ContactsAdapter(){
    		super(SelectContacts.this, R.layout.contacts_list_row, SmsSchedulerApplication.contactsList);
    	}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ContactsListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
	    		holder = new ContactsListHolder();
				holder.contactImage 	= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
	    		holder.nameText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
	    		holder.numberText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
	    		holder.contactCheck     = (CheckBox) convertView.findViewById(R.id.contact_list_row_contact_check);
	    		convertView.setTag(holder);
			}else{
				holder = (ContactsListHolder) convertView.getTag();
			}
    		holder.contactImage.setImageBitmap(SmsSchedulerApplication.contactsList.get(position).image);
    		holder.nameText.setText(SmsSchedulerApplication.contactsList.get(position).name);
    		holder.numberText.setText(SmsSchedulerApplication.contactsList.get(position).number);
    		
    		for(int i = 0; i< RecipientsTemp.size(); i++){
    			
        		if(SmsSchedulerApplication.contactsList.get(position).content_uri_id == RecipientsTemp.get(i).contactId){
        			holder.contactCheck.setChecked(true);
        			break;
        		}else{
        			holder.contactCheck.setChecked(false);
        		}
        	}
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						boolean isPresent = false;
						for(int i = 0; i< RecipientsTemp.size(); i++){
							if(RecipientsTemp.get(i).contactId == SmsSchedulerApplication.contactsList.get(position).content_uri_id){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							Recipient recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(position).name, SmsSchedulerApplication.contactsList.get(position).content_uri_id, -1);
							recipient.groupIds.add((long) -1);
							recipient.groupTypes.add(-1);
							RecipientsTemp.add(recipient);
						}
					}else{
						for(int i = 0; i<RecipientsTemp.size(); i++){
				    		if(SmsSchedulerApplication.contactsList.get(position).content_uri_id == RecipientsTemp.get(i).contactId){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if((Long)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if((Long)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			RecipientsTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged(); 
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		
    		
    		
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						boolean isPresent = false;
						for(int i = 0; i< RecipientsTemp.size(); i++){
							if(RecipientsTemp.get(i).contactId == SmsSchedulerApplication.contactsList.get(position).content_uri_id){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							Recipient recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(position).name, SmsSchedulerApplication.contactsList.get(position).content_uri_id, -1);
							recipient.groupIds.add((long) -1);
							recipient.groupTypes.add(-1);
							RecipientsTemp.add(recipient);
						}
					}else{	
						holder.contactCheck.setChecked(false);
						for(int i = 0; i<RecipientsTemp.size(); i++){
				    		if(SmsSchedulerApplication.contactsList.get(position).content_uri_id == RecipientsTemp.get(i).contactId){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if((Long)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if((Long)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == RecipientsTemp.get(i).contactId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			RecipientsTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged();
				    		}
				    	}
					}
				}
			});
    		
    		return convertView;
		}
	}
	//************************************************************** end of ContactsAdapter******************
	

	
	private void nativeGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		nativeGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	nativeGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	nativeChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return nativeChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += nativeChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return nativeGroupDataTemp.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		@Override
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.select_contacts_expandable_list_group, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));
    			
    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									nativeAddCheck(groupPosition, i);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}else{
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									nativeRemoveCheck(groupPosition, i);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isExpanded){
							nativeGroupExplList.collapseGroup(groupPosition);
						}else{
							nativeGroupExplList.expandGroup(groupPosition);
						}
					}
				});
    			return convertView;
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				final ChildListHolder holder;
				if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
    			holder.childNameText.setText((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME));
    			holder.childNumberText.setText((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
    			holder.childContactImage.setImageBitmap((Bitmap)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			holder.childCheck.setChecked((Boolean)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK));
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							nativeAddCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							nativeRemoveCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							holder.childCheck.setChecked(false);
							nativeRemoveCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(true);
							nativeAddCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			
			@Override
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			@Override
			public boolean hasStableIds() {
			   return false;
			}
			 
			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    		
    	};
    }
	
	
	
	
	
	private void privateGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		privateGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	privateGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	privateChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return privateChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += privateChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return privateChildDataTemp.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return privateChildDataTemp.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return privateGroupDataTemp.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		@Override
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.select_contacts_expandable_list_group, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));

    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									privateAddCheck(groupPosition, i);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}else{
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									privateRemoveCheck(groupPosition, i);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isExpanded){
							privateGroupExplList.collapseGroup(groupPosition);
						}else{
							privateGroupExplList.expandGroup(groupPosition);
						}
						
					}
				});
    			
    			return convertView;
    			
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				privateChildDataTemp.get(groupPosition).get(childPosition);
				final ChildListHolder holder;
    			if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
    			holder.childNameText.setText((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME));
    			holder.childNumberText.setText((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
    			holder.childContactImage.setImageBitmap((Bitmap)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			holder.childCheck.setChecked((Boolean)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK));
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							privateAddCheck(groupPosition, childPosition);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
								
						}else{
							privateRemoveCheck(groupPosition, childPosition);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(!holder.childCheck.isChecked()){
							holder.childCheck.setChecked(true);
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							privateAddCheck(groupPosition, childPosition);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(false);
							privateRemoveCheck(groupPosition, childPosition);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			@Override
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			@Override
			public boolean hasStableIds() {
			   return false;
			}
			 
			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    	};
    }
	
	
	
	
	
	private void nativeAddCheck(int groupPosition, int childPosition){
		nativeChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, true);
		boolean spanExist = false;
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if(RecipientsTemp.get(i).contactId == (Long)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)){
				spanExist = true;
				try{
					RecipientsTemp.get(i).groupIds.add((Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
				}catch (ClassCastException e) {
					RecipientsTemp.get(i).groupIds.add(Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}
				RecipientsTemp.get(i).groupTypes.add(1);
				break;
			}
		}
		if(!spanExist){
			Recipient recipient = new Recipient(-1, 2, (String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME), (Long)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID), -1);
			try{
				recipient.groupIds.add(((Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}catch (ClassCastException e) {
				recipient.groupIds.add(Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}
			recipient.groupTypes.add(1);
			RecipientsTemp.add(recipient);
			contactsAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	
	private void nativeRemoveCheck(int groupPosition, int childPosition){
		nativeChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, false);
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if((Long)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)==RecipientsTemp.get(i).contactId){
				for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID);
					}
					groupTypeToRemove = 1;
					
					if(RecipientsTemp.get(i).groupIds.get(j) == groupIdToRemove && RecipientsTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						RecipientsTemp.get(i).groupIds.remove(j);
						if(RecipientsTemp.get(i).groupIds.size()==0){
							RecipientsTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	
	private void privateAddCheck(int groupPosition, int childPosition){
		Log.d("entering childcheck is checked true listner");
		privateChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, true);
		boolean spanExist = false;
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if(RecipientsTemp.get(i).contactId == (Long) privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)){
				spanExist = true;
				try{
					RecipientsTemp.get(i).groupIds.add(Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}catch (ClassCastException e) {
					RecipientsTemp.get(i).groupIds.add(((Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}
				RecipientsTemp.get(i).groupTypes.add(2);
				break;
			}
		}
		if(!spanExist){
			Recipient recipient = new Recipient(-1, 2, (String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME), (Long)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID), -1);
			try{
				recipient.groupIds.add(((Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}catch (ClassCastException e) {
				recipient.groupIds.add(Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}
			recipient.groupTypes.add(2);
			RecipientsTemp.add(recipient);
			contactsAdapter.notifyDataSetChanged();
		}
	}	
		
	
	
	
	private void privateRemoveCheck(int groupPosition, int childPosition){
		privateChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, false);
		for(int i = 0; i < RecipientsTemp.size(); i++){
			if((Long)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)==RecipientsTemp.get(i).contactId){
				for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID);
					}
					groupTypeToRemove = 2;
					if(RecipientsTemp.get(i).groupIds.get(j) == groupIdToRemove && RecipientsTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						RecipientsTemp.get(i).groupIds.remove(j);
						if(RecipientsTemp.get(i).groupIds.size()==0){
							RecipientsTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	private class RecentsAdapter extends ArrayAdapter {
		
		@SuppressWarnings("unchecked")
		RecentsAdapter(){
    		super(SelectContacts.this, R.layout.contacts_list_row, recentIds);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final RecentsListHolder holder;
			final int _position  = position;
    		if(convertView == null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
    			holder = new RecentsListHolder();
    			holder.contactImage 		= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
        		holder.nameText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
        		holder.numberText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
        		holder.contactCheck 		= (CheckBox) 	convertView.findViewById(R.id.contact_list_row_contact_check);
    			convertView.setTag(holder);
    		}else{
    			holder = (RecentsListHolder) convertView.getTag();
    		}
    		
    		int i = 0;
    		
    		if(recentContactIds.get(position)> -1){
    			for(i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
    				if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == recentContactIds.get(position)){
    					holder.contactImage.setImageBitmap(SmsSchedulerApplication.contactsList.get(i).image);
    		    		holder.nameText.setText(SmsSchedulerApplication.contactsList.get(i).name);
    		    		holder.numberText.setText(SmsSchedulerApplication.contactsList.get(i).number);
    		    		
    		    		for(int j = 0; j< RecipientsTemp.size(); j++){
    		        		if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == RecipientsTemp.get(j).contactId){
    		        			holder.contactCheck.setChecked(true);
    		        			break;
    		        		}else{
    		        			holder.contactCheck.setChecked(false);
    		        		}
    		        	}
    		    		break;
    				}
    			}
    		}else if(recentContactIds.get(position) == -1){
    			holder.contactImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
    			holder.nameText.setText(recentContactNumbers.get(position));
    			holder.numberText.setText("");
    			for(int j = 0; j< RecipientsTemp.size(); j++){
    				if(RecipientsTemp.get(j).displayName.equals(recentContactNumbers.get(position))){
    					holder.contactCheck.setChecked(true);
    					break;
    				}else{
    					holder.contactCheck.setChecked(true);
    				}
    			}
    		}
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						Recipient recipient = new Recipient();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).content_uri_id == recentContactIds.get(_position)){
									recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, SmsSchedulerApplication.contactsList.get(k).content_uri_id, -1);
									break;
								}
							}
						}else{
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).name.equals(recentContactNumbers.get(_position))){
									
								}
							}
							recipient = new Recipient(-1, 1, recentContactNumbers.get(_position), -1, -1);
						}
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						RecipientsTemp.add(recipient);
						contactsAdapter.notifyDataSetChanged();
					}else{
						holder.contactCheck.setChecked(false);
						for(int i = 0; i< RecipientsTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == RecipientsTemp.get(i).contactId){
				    				RecipientsTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(RecipientsTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				RecipientsTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						Recipient recipient = new Recipient();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsSchedulerApplication.contactsList.size(); k++){
								if(SmsSchedulerApplication.contactsList.get(k).content_uri_id == recentContactIds.get(_position)){
									recipient = new Recipient(-1, 2, SmsSchedulerApplication.contactsList.get(k).name, SmsSchedulerApplication.contactsList.get(k).content_uri_id, -1);
									break;
								}
							}
						}else{
							recipient = new Recipient(-1, 1, recentContactNumbers.get(_position), -1, -1);
						}
						recipient.groupIds.add((long) -1);
						recipient.groupTypes.add(-1);
						
						RecipientsTemp.add(recipient);
						
						contactsAdapter.notifyDataSetChanged();
					}else{
						for(int i = 0; i< RecipientsTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == RecipientsTemp.get(i).contactId){
				    				RecipientsTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(RecipientsTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				RecipientsTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		return convertView;
		}
	}
	
	
	
	private class ContactsListHolder{
		ImageView 	contactImage;
		TextView 	nameText;
		TextView 	numberText;
		CheckBox contactCheck;
	}
	
	
	private class GroupListHolder{
		TextView groupHeading;
		CheckBox groupCheck;
	}
	
	
	private class ChildListHolder{
		TextView childNameText;
		ImageView childContactImage;
		TextView childNumberText;
		CheckBox childCheck;
	}
	
	
	private class RecentsListHolder{
		ImageView 	contactImage;
		TextView 	nameText;
		TextView 	numberText;
		CheckBox 	contactCheck;
	}
	
	
	
	
	@SuppressWarnings("static-access")
	private void reloadPrivateGroupData(){
		mdba.open();
		
		privateChildDataTemp.clear();
		privateGroupDataTemp.clear();
		
		if(origin.equals("new")){
			ScheduleNewSms.privateGroupData.clear();
			ScheduleNewSms.privateChildData.clear();
		}else{
			EditScheduledSms.privateGroupData.clear();
			EditScheduledSms.privateChildData.clear();
		}
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> spanIdsForGroup = mdba.fetchRecipientsForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		if(spanIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< RecipientsTemp.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==RecipientsTemp.get(i).recipientId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		if(origin.equals("new")){
        			ScheduleNewSms.privateGroupData.add(group);
        		}else{
        			EditScheduledSms.privateGroupData.add(group);
        		}
        		
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
        				if(contactIds.get(i)==SmsSchedulerApplication.contactsList.get(j).content_uri_id){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsSchedulerApplication.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsSchedulerApplication.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< RecipientsTemp.size(); m++){
       								if(RecipientsTemp.get(m).recipientId == spanIdsForGroup.get(k) && RecipientsTemp.get(m).recipientId == contactIds.get(i)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}
        					
        					child.add(childParameters);
        				}
        			}
        		}
        		if(origin.equals("new")){
        			ScheduleNewSms.privateChildData.add(child);
        		}else{
        			EditScheduledSms.privateChildData.add(child);
        		}
        	}while(groupsCursor.moveToNext());
        }
        
        mdba.close();
        
        if(origin.equals("new")){
        	for(int groupCount = 0; groupCount< ScheduleNewSms.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, ScheduleNewSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				for(int i = 0; i< RecipientsTemp.size(); i++){
        			for(int j = 0; j< RecipientsTemp.get(i).groupIds.size(); j++){
        				if((RecipientsTemp.get(i).groupIds.get(j)==group.get(Constants.GROUP_ID)) && RecipientsTemp.get(i).groupTypes.get(j) == 2){
        					group.put(Constants.GROUP_CHECK, true);
        					break;
        				}
        			}
        		}
				
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< ScheduleNewSms.privateChildData.get(groupCount).size(); childCount++){
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, ScheduleNewSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));

					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}else{
			
			for(int groupCount = 0; groupCount< EditScheduledSms.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, EditScheduledSms.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount < EditScheduledSms.privateChildData.get(groupCount).size(); childCount++){
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSms.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}
	}
}