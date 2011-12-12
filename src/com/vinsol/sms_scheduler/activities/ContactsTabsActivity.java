package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.HashMap;

import com.vinsol.sms_scheduler.ConstantsClass;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.drawable;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;
import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class ContactsTabsActivity extends ExpandableListActivity {
	
	TabHost tabHost;
	DBAdapter mdba = new DBAdapter(this);
	Cursor cur;
	
	
	
	
	private static final String LIST_STATE_KEY = "listState";
	private static final String LIST_POSITION_KEY = "listPosition";
	private static final String ITEM_POSITION_KEY = "itemPosition";

	private Parcelable mListState = null;
	private int mListPosition = 0;
	private int mItemPosition = 0;
	
	
	
	//---------------- Variables relating to Contacts tab -----------------------
	ListView nativeContactsList;
	Button doneButton;
	Button cancelButton;
	
	ContactsAdapter contactsAdapter;
	String origin;
	
	ArrayList<MyContact> selectedIds = new ArrayList<MyContact>();
	ArrayList<SpannedEntity> SpansTemp = new ArrayList<SpannedEntity>();
	ArrayList<String> idsString = new ArrayList<String>();
	ArrayList<Long> ids = new ArrayList<Long>();
	//---------------------------------------------------------------------------
	
	
	
	//----------- Variables relating to Groups Tab-------------------------------
	ExpandableListView groupExplList;
	ArrayList<ArrayList<HashMap<String, Object>>> childDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	ArrayList<HashMap<String, Object>> groupDataTemp = new ArrayList<HashMap<String, Object>>();
	//boolean empty;
	
//	private final String GROUP_NAME = "group_name";
//	private final String GROUP_CHECK = "group_check";
//	private final String GROUP_IMAGE = "group_image";
//	private final String GROUP_TYPE = "group_type";
//	private final String GROUP_ID = "group_id";
//	
//	private final String CHILD_NAME = "child_name";
//	private final String CHILD_CHECK = "child_check";
//	private final String CHILD_NUMBER = "child_number";
//	private final String CHILD_IMAGE = "child_contact_image";
//	private final String CHILD_CONTACT_ID = "child_contact_id";

	SimpleExpandableListAdapter mAdapter;
//	ArrayList<GroupStructure> GroupTemp = new ArrayList<GroupStructure>();
	//-----------------------------------------------------------------------------
	
	
	
	
	//----------------------Variables relating to Recents Tab-----------------------
	ArrayList<Long> recentIds = new ArrayList<Long>();
	ArrayList<Long> recentContactIds = new ArrayList<Long>();
	ArrayList<String> recentContactNumbers = new ArrayList<String>();
	ListView recentsList;
	RecentsAdapter recentsAdapter;
	//------------------------------------------------------------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tabs_layout);
		
		
		//----------------------Setting up the Tabs--------------------------------
		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.contacts_tab);
        spec1.setIndicator("Contacts", getResources().getDrawable(R.drawable.contacts_tab_states));

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Groups", getResources().getDrawable(R.drawable.groups_tab_states));
        spec2.setContent(R.id.groups_tab);

        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Recents", getResources().getDrawable(R.drawable.recent_tab_states));
        spec3.setContent(R.id.recents_tab);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        //----------------------------------------------------end of Tabs Setup-----------
        
        
        
		
		Intent intent = getIntent();
		//idsString = intent.getStringArrayListExtra("IDSARRAY");
		origin = intent.getStringExtra("ORIGIN");
		
		if(origin.equals("new")){
			for(int i = 0; i < NewScheduleActivity.Spans.size(); i++){
				SpansTemp.add(NewScheduleActivity.Spans.get(i));
			}
			for(int groupCount = 0; groupCount< NewScheduleActivity.groupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(ConstantsClass.GROUP_ID, NewScheduleActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_ID));
				group.put(ConstantsClass.GROUP_NAME, NewScheduleActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_NAME));
				group.put(ConstantsClass.GROUP_IMAGE, NewScheduleActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_IMAGE));
				group.put(ConstantsClass.GROUP_TYPE, NewScheduleActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_TYPE));
				group.put(ConstantsClass.GROUP_CHECK, NewScheduleActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_CHECK));
				
				groupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< NewScheduleActivity.childData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(ConstantsClass.CHILD_CONTACT_ID, NewScheduleActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_CONTACT_ID));
					childParams.put(ConstantsClass.CHILD_NAME, NewScheduleActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_NAME));
					childParams.put(ConstantsClass.CHILD_NUMBER, NewScheduleActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_NUMBER));
					childParams.put(ConstantsClass.CHILD_IMAGE, NewScheduleActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_IMAGE));
					childParams.put(ConstantsClass.CHILD_CHECK, NewScheduleActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_CHECK));
					child.add(childParams);
				}
				childDataTemp.add(child);
			}
			
		}else if(origin.equals("edit")){
			for(int i = 0; i < EditScheduledSmsActivity.Spans.size(); i++){
				SpansTemp.add(EditScheduledSmsActivity.Spans.get(i));
			}
			Log.i("MSG", EditScheduledSmsActivity.groupData.size()+"group data size from edit activity");
			for(int groupCount = 0; groupCount< EditScheduledSmsActivity.groupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(ConstantsClass.GROUP_ID, EditScheduledSmsActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_ID));
				group.put(ConstantsClass.GROUP_NAME, EditScheduledSmsActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_NAME));
				group.put(ConstantsClass.GROUP_IMAGE, EditScheduledSmsActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_IMAGE));
				group.put(ConstantsClass.GROUP_TYPE, EditScheduledSmsActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_TYPE));
				group.put(ConstantsClass.GROUP_CHECK, EditScheduledSmsActivity.groupData.get(groupCount).get(ConstantsClass.GROUP_CHECK));
				
				groupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.i("MSG", EditScheduledSmsActivity.childData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSmsActivity.childData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(ConstantsClass.CHILD_CONTACT_ID, EditScheduledSmsActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_CONTACT_ID));
					childParams.put(ConstantsClass.CHILD_NAME, EditScheduledSmsActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_NAME));
					childParams.put(ConstantsClass.CHILD_NUMBER, EditScheduledSmsActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_NUMBER));
					childParams.put(ConstantsClass.CHILD_IMAGE, EditScheduledSmsActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_IMAGE));
					childParams.put(ConstantsClass.CHILD_CHECK, EditScheduledSmsActivity.childData.get(groupCount).get(childCount).get(ConstantsClass.CHILD_CHECK));
					child.add(childParams);
				}
				childDataTemp.add(child);
			}
		}

		
		
        doneButton			= (Button) 		findViewById(R.id.contacts_tab_done_button);
        cancelButton		= (Button) 		findViewById(R.id.contacts_tab_cancel_button);
        
        
        doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				for(int i = 0; i< ids.size(); i++){
					idsString.add(String.valueOf(ids.get(i)));
				}
				
				intent.putExtra("IDSARRAY", idsString);
				if(origin.equals("new")){
					NewScheduleActivity.groupData.clear();
					NewScheduleActivity.childData.clear();

					NewScheduleActivity.groupData = groupDataTemp;
					NewScheduleActivity.childData = childDataTemp;
					
					NewScheduleActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						NewScheduleActivity.Spans.add(SpansTemp.get(i));
					}
				}else if(origin.equals("edit")){
					EditScheduledSmsActivity.groupData.clear();
					EditScheduledSmsActivity.childData.clear();
					
					EditScheduledSmsActivity.groupData = groupDataTemp;
					EditScheduledSmsActivity.childData = childDataTemp;
					
					EditScheduledSmsActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						EditScheduledSmsActivity.Spans.add(SpansTemp.get(i));
					}
				}
					
				setResult(2, intent);
				ContactsTabsActivity.this.finish();
			}
		});
        
        
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("IDSARRAY", idsString);
	
				
				setResult(2, intent);
				ContactsTabsActivity.this.finish();
			}
		});
        
        
        
        //------------------Setting up the Contacts Tab ---------------------------------------------
        nativeContactsList 	= (ListView) 	findViewById(R.id.contacts_tabs_native_contacts_list);
		contactsAdapter = new ContactsAdapter();
        nativeContactsList.setAdapter(contactsAdapter);
        //------------------------------------------------------------end of setting up Contacts Tab--------
        
        
        
        
        //---------------- Setting up the Groups Tab -----------------------------------------------
        groupExplList = (ExpandableListView) findViewById(android.R.id.list);
		customGroupsAdapterSetup();
		
		groupExplList.setAdapter(mAdapter);
		groupExplList.setOnGroupExpandListener(new OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {

			}
		});
		
		groupExplList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			
			@Override
			public void onGroupCollapse(int groupPosition) {

			}
		});
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
	
	
	
	
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		intent.putExtra("IDSARRAY", idsString);
		setResult(2, intent);
		
//		if(origin.equals("new")){
//			NewScheduleActivity.Spans.clear();
//			for(int i = 0; i< SpansTemp.size(); i++){
//				NewScheduleActivity.Spans.add(SpansTemp.get(i));
//			}
//		}else if(origin.equals("edit")){
//			EditScheduledSmsActivity.Spans.clear();
//			for(int i = 0; i< SpansTemp.size(); i++){
//				EditScheduledSmsActivity.Spans.add(SpansTemp.get(i));
//			}
//		}
		
		ContactsTabsActivity.this.finish();
	}
	
	
	
	//************************* Adapter for the list *****************************************
	//**************************** in Contacts Tab ********************************************
	
	class ContactsAdapter extends ArrayAdapter {
		
		ContactsAdapter(){
    		super(ContactsTabsActivity.this, R.layout.contacts_list_row_design, SplashActivity.contactsList);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.contacts_list_row_design, parent, false);
    		
    		ImageView 	contactImage 	= (ImageView) 	row.findViewById(R.id.contact_list_row_contact_pic);
    		TextView 	nameText 		= (TextView) 	row.findViewById(R.id.contact_list_row_contact_name);
    		TextView 	numberText 		= (TextView) 	row.findViewById(R.id.contact_list_row_contact_number);
    		CheckBox 	contactCheck 	= (CheckBox) 	row.findViewById(R.id.contact_list_row_contact_check);
			
    		
    		
    		contactImage.setImageBitmap(SplashActivity.contactsList.get(position).image);
    		nameText.setText(SplashActivity.contactsList.get(position).name);
    		numberText.setText(SplashActivity.contactsList.get(position).number);
    		
    		
    		for(int i = 0; i< SpansTemp.size(); i++){
        		if(Long.parseLong(SplashActivity.contactsList.get(position).content_uri_id) == SpansTemp.get(i).entityId){
        			contactCheck.setChecked(true);
        		}
        	}
    		
    		
    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						boolean isPresent = false;
						for(int i = 0; i< SpansTemp.size(); i++){
							if(SpansTemp.get(i).entityId == Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id)){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
						SpannedEntity span = new SpannedEntity(-1, 2, SplashActivity.contactsList.get(_position).name, Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id), -1);
						span.groupIds.add((long) -1);
						span.groupTypes.add(-1);
						SpansTemp.add(span);
						}
					}else{	
					
						for(int i = 0; i<SpansTemp.size(); i++){
				    		if(Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id) == SpansTemp.get(i).entityId){
				    			SpansTemp.remove(i);
				    		}
				    	}
						
						
					}
				}
			});
    		
    		return row;
		}
	}
	//************************************************************** end of ContactsAdapter******************
	
	
	
//	public void loadGroupsData(){
//		
//		groupData.clear();
//		childData.clear();
//		
//		//------------------------ Setting up data for native groups ---------------------------
//		String[] projection = new String[] {
//                Groups._ID,
//                Groups.TITLE,
//                Groups.SYSTEM_ID,
//                Groups.NOTES,
//             };
//        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
//        int count = 0;
//        
//        Cursor groupCursor = managedQuery(groupsUri, projection, null, null, null);
//        Log.i("MSG", GroupTemp.size() + "");
//        if(groupCursor.moveToFirst()){
//        	
//        	do{
//        		HashMap<String, Object> group = new HashMap<String, Object>();
//        		group.put(GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
//        		group.put(GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
////        		if(empty){
//        			group.put(GROUP_CHECK, false);
////        		}else{
////        			group.put(GROUP_CHECK, GroupTemp.get(count).isChecked);
////        		}
//        		group.put(GROUP_TYPE, 1);
//        		group.put(GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
//        		
//        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
//        		
////        		GroupStructure groupStructure;
////        		if(GroupTemp.size() == 0){
////        			groupStructure = new GroupStructure();
////        			groupStructure.groupId = groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID));
////        			groupStructure.isChecked = false;
////        			ArrayList<Long> ids = new ArrayList<Long>();
////        			groupStructure.CheckedContactsIds = ids;
////        			GroupTemp.add(groupStructure);
////        		}
//        		
//        		groupData.add(group);
//        		
//        		for(int i = 0; i < SplashActivity.contactsList.size(); i++){
//        			for(int j = 0; j< SplashActivity.contactsList.get(i).groupRowId.size(); j++){
//        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SplashActivity.contactsList.get(i).groupRowId.get(j)){
//        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
//        					childParameters.put(CHILD_NAME, SplashActivity.contactsList.get(i).name);
//        					childParameters.put(CHILD_NUMBER, SplashActivity.contactsList.get(i).number);
//        					childParameters.put(CHILD_IMAGE, SplashActivity.contactsList.get(i).image);
////        					if(empty){
//        						childParameters.put(CHILD_CHECK, false);
////        					}else{
////        						for(int k = 0; k< GroupTemp.get(count).CheckedContactsIds.size(); k++){
////        							if(GroupTemp.get(count).CheckedContactsIds.get(k)==Long.parseLong(SplashActivity.contactsList.get(i).content_uri_id)){
////        								childParameters.put(CHILD_CHECK, true);
////        							}else{
////        								childParameters.put(CHILD_CHECK, false);
////        							}
////        						}
////        					}
//        					childParameters.put(CHILD_CONTACT_ID, SplashActivity.contactsList.get(i).content_uri_id);
//        					child.add(childParameters);
//        					
//        				}
//        			}
//        		}
//        		childData.add(child);
//        		count++;
//        	}while(groupCursor.moveToNext());
//        }
//        
//        // ---------------------------------------------------end of setting up native groups data-------------
//        
//        
//        
//        //---------------------------- Setting up private Groups data ------------------------------------
//        
//        mdba.open();
//        Cursor groupsCursor = mdba.fetchAllGroups();
//        if(groupsCursor.moveToFirst()){
//        	do{
//        		HashMap<String, Object> group = new HashMap<String, Object>();
//        		group.put(GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
//        		group.put(GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
////        		if(empty){
//        			group.put(GROUP_CHECK, false);
////        		}else{
////        			group.put(GROUP_CHECK, GroupTemp.get(count).isChecked);
////        		}
//        		group.put(GROUP_TYPE, 2);
//        		group.put(GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
//        		
//        		groupData.add(group);
//        		GroupStructure groupStructure;
//        		if(GroupTemp.size() == 0){
//        			groupStructure = new GroupStructure();
//        			groupStructure.groupId = Long.parseLong(groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
//        			groupStructure.isChecked = false;
//        			ArrayList<Long> ids = new ArrayList<Long>();
//        			groupStructure.CheckedContactsIds = ids;
//        			GroupTemp.add(groupStructure);
//        		}
//        		
//        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
//        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
//        		
//        		for(int i = 0; i< contactIds.size(); i++){
//        			for(int j = 0; j< SplashActivity.contactsList.size(); j++){
//        				if(contactIds.get(i)==Long.parseLong(SplashActivity.contactsList.get(j).content_uri_id)){
//        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
//        					childParameters.put(CHILD_NAME, SplashActivity.contactsList.get(j).name);
//        					childParameters.put(CHILD_NUMBER, SplashActivity.contactsList.get(j).number);
//        					childParameters.put(CHILD_CONTACT_ID, SplashActivity.contactsList.get(j).content_uri_id);
//        					childParameters.put(CHILD_IMAGE, SplashActivity.contactsList.get(j).image);
////        					if(empty){
//        						childParameters.put(CHILD_CHECK, false);
////        					}else{
////        						for(int k = 0; k< GroupTemp.get(count).CheckedContactsIds.size(); k++){
////        							if(GroupTemp.get(count).CheckedContactsIds.get(k)==Long.parseLong(SplashActivity.contactsList.get(j).content_uri_id)){
////        								childParameters.put(CHILD_CHECK, true);
////        							}else{
////        								childParameters.put(CHILD_CHECK, false);
////        							}
////        						}
////        					}
//        					
//        					child.add(childParameters);
//        				}
//        			}
//        		}
//        		
//        		childData.add(child);
//        		count++;
//        	}while(groupsCursor.moveToNext());
//        }
//        
//        mdba.close();
//
////        for(int k = 0 ; k < groupData.size(); k++){
////        	Log.i("MSG", "group");
////        	for(int n = 0 ; n< childData.get(k).size(); n++){
////        		Log.i("MSG", "fdsafdsa");
////        	}
////        }
//	}
	
	
	
	
	public void customGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	groupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ ConstantsClass.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	childDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return childDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
				for(int i = 0; i<groupPosition; i ++) {
				   id += childDataTemp.get(groupPosition).size(); 
			   }
				return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return childDataTemp.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return childDataTemp.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return groupDataTemp.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		@Override
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
//    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.group_expl_list_group_row_design, null);	
//    			}
    			
    			TextView groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
    			CheckBox groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
    			//ImageView groupImage	= (ImageView) convertView.findViewById(R.id.group_expl_list_group_row_dropdown_image);
    			
    			
    			groupHeading.setText((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_NAME));
    			groupCheck.setChecked((Boolean)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_CHECK));
    			//groupImage.setImageBitmap((Bitmap)groupData.get(groupPosition).get(GROUP_IMAGE));
    			
    			groupCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked){
							groupDataTemp.get(groupPosition).put(ConstantsClass.GROUP_CHECK, true);
							for(int i = 0; i< childDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)childDataTemp.get(groupPosition).get(i).get(ConstantsClass.CHILD_CHECK))){
									addCheck(groupPosition, i);
								}
							}
							mAdapter.notifyDataSetChanged();
						}else{
							groupDataTemp.get(groupPosition).put(ConstantsClass.GROUP_CHECK, false);
							for(int i = 0; i< childDataTemp.get(groupPosition).size(); i++){
								if((Boolean)childDataTemp.get(groupPosition).get(i).get(ConstantsClass.CHILD_CHECK)){
									removeCheck(groupPosition, i);
								}
							}
							mAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isExpanded){
							groupExplList.collapseGroup(groupPosition);
						}else{
							groupExplList.expandGroup(groupPosition);
						}
						
					}
				});
    			
    			return convertView;
    			
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				HashMap<String, Object> child = childDataTemp.get(groupPosition).get(childPosition);
				
//				Log.v("sazwqa", "11111111111111111111111111111111111111111111111111111111111");
//				Log.v("sazwqa", "GroupPosition = " + groupPosition);
//				Log.v("sazwqa", "ChildPosition = " + childPosition);
//				Log.v("sazwqa", "contact Name = " + child.get(NewScheduleActivity.CHILD_NAME));
//				Log.v("sazwqa", "is Checked = " + child.get(NewScheduleActivity.CHILD_CHECK));
//				Log.v("sazwqa", "11111111111111111111111111111111111111111111111111111111111");
//				
				
//    			if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row_design, null, false);
//    			}
    			final TextView childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
    			final ImageView childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
    			final TextView childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
    			final CheckBox childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
    			
    			childNameText.setText((String)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_NAME));
    			childNumberText.setText((String)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_NUMBER));
    			childContactImage.setImageBitmap((Bitmap)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_IMAGE));
    			childCheck.setChecked((Boolean)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_CHECK));
    			
    			childCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Log.i("MSG", NewScheduleActivity.Spans.size()+" on entering oncheckchanged");
						if(isChecked){
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							addCheck(groupPosition, childPosition);
							
								
						}else{
							removeCheck(groupPosition, childPosition);
						}
					}
				});
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(childCheck.isChecked()){
							childCheck.setChecked(false);
						}else{
							childCheck.setChecked(true);
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
	
	
	
	
	
	
	public void addCheck(int groupPosition, int childPosition){
		Log.i("MSG", "entering childcheck is checked true listner");
		childDataTemp.get(groupPosition).get(childPosition).put(ConstantsClass.CHILD_CHECK, true);
		boolean spanExist = false;
		
		
			for(int i = 0; i < SpansTemp.size(); i++){
				
				if(SpansTemp.get(i).entityId == Long.parseLong((String) childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_CONTACT_ID))){
					Log.i("MSG", "got a matching span");
					spanExist = true;
					try{
						SpansTemp.get(i).groupIds.add(Long.parseLong((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID)));
						
					}catch (ClassCastException e) {
						SpansTemp.get(i).groupIds.add(((Long)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID)));
					}
					try{
						SpansTemp.get(i).groupTypes.add(((Integer)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE)));
					}catch (ClassCastException e) {
						SpansTemp.get(i).groupTypes.add(Integer.parseInt((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE)));
					}
					Log.i("MSG", "span id : " + SpansTemp.get(i) + " , total spans : " + SpansTemp.size()+ " after group id addition");
					break;
				}
				Log.i("MSG", SpansTemp.size()+"");
				
			}
			if(!spanExist){
				Log.i("MSG", "got no matching span");
				Log.i("MSG", SpansTemp.size()+"");
				SpannedEntity span = new SpannedEntity(-1, 2, (String)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_NAME), Long.parseLong((String)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_CONTACT_ID)), -1);
				try{
					span.groupIds.add(((Long)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID)));
					
				}catch (ClassCastException e) {
					span.groupIds.add(Long.parseLong((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID)));
				}
				try{
					span.groupTypes.add(Integer.parseInt((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE)));
				}catch(ClassCastException ce){
					span.groupTypes.add(((Integer)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE)));
				}
				
				SpansTemp.add(span);
				contactsAdapter.notifyDataSetChanged();
				Log.i("MSG", SpansTemp.size()+" after add");
			}
		
	}
	
	
	
	
	
	
	public void removeCheck(int groupPosition, int childPosition){
		Log.i("MSG", "in removing a check");
		childDataTemp.get(groupPosition).get(childPosition).put(ConstantsClass.CHILD_CHECK, false);
		
		
			for(int i = 0; i < SpansTemp.size(); i++){
				if(Long.parseLong((String)childDataTemp.get(groupPosition).get(childPosition).get(ConstantsClass.CHILD_CONTACT_ID))==SpansTemp.get(i).entityId){
				Log.i("MSG", "Span found for deletion");
				for(int j = 0; j< SpansTemp.get(i).groupIds.size(); j++){
					Log.i("MSG", SpansTemp.get(i).groupIds.get(j) + "spans group Id");
					Log.i("MSG", groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID) + "group to be removed from span");
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_ID);
					}
					
					try{
						groupTypeToRemove = Integer.parseInt((String)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE));
					}catch (ClassCastException e) {
						groupTypeToRemove = (Integer)groupDataTemp.get(groupPosition).get(ConstantsClass.GROUP_TYPE);
					}
					
					if(SpansTemp.get(i).groupIds.get(j) == groupIdToRemove && SpansTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						Log.i("MSG", "group id found for deletion");
						SpansTemp.get(i).groupIds.remove(j);
						
						
						Log.i("MSG", SpansTemp.get(i).groupIds.size()+ " group size for this span");
						if(SpansTemp.get(i).groupIds.size()==0){
							SpansTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
//				boolean z = true;
//				for(int k = 0; k< childDataTemp.get(groupPosition).get(childPosition).size(); k++){
//					Log.i("MSG", childDataTemp.get(groupPosition).get(childPosition).size() + "size of childdata for group");
//					Log.i("MSG", (String)groupDataTemp.get(groupPosition).get(NewScheduleActivity.GROUP_NAME));
//					if((Boolean)childDataTemp.get(groupPosition).get(k).get(NewScheduleActivity.CHILD_CHECK)){
//						z = false;
//						break;
//					}
//				}
//				if(z){
//					groupDataTemp.get(groupPosition).put(NewScheduleActivity.GROUP_CHECK, false);
//					mAdapter.notifyDataSetChanged();
//				}
				}
			}
		
	}
	
	
	
	
	
	
	
	
	
	class RecentsAdapter extends ArrayAdapter {
		
		RecentsAdapter(){
    		super(ContactsTabsActivity.this, R.layout.contacts_list_row_design, recentIds);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		
    		if(convertView == null){
    			convertView = inflater.inflate(R.layout.contacts_list_row_design, parent, false);
    		}
    		View row = convertView;
    		
    		ImageView 	contactImage 	= (ImageView) 	row.findViewById(R.id.contact_list_row_contact_pic);
    		TextView 	nameText 		= (TextView) 	row.findViewById(R.id.contact_list_row_contact_name);
    		TextView 	numberText 		= (TextView) 	row.findViewById(R.id.contact_list_row_contact_number);
    		CheckBox 	contactCheck 	= (CheckBox) 	row.findViewById(R.id.contact_list_row_contact_check);
			
    		int i = 0;
    		
    		Log.i("MSG", recentIds.size() + " size of recent contacts arraylist");
    		
    		if(recentContactIds.get(position)> -1){
    			Log.i("MSG", recentContactIds.get(position)+ "");
    			
    			for(i = 0; i< SplashActivity.contactsList.size(); i++){
    				if(Long.parseLong(SplashActivity.contactsList.get(i).content_uri_id) == recentContactIds.get(position)){
    					contactImage.setImageBitmap(SplashActivity.contactsList.get(i).image);
    		    		nameText.setText(SplashActivity.contactsList.get(i).name);
    		    		numberText.setText(SplashActivity.contactsList.get(i).number);
    		    		
    		    		
    		    		for(int j = 0; j< SpansTemp.size(); j++){
    		    			
    		        		if(Long.parseLong(SplashActivity.contactsList.get(i).content_uri_id) == SpansTemp.get(j).entityId){
    		        			contactCheck.setChecked(true);
    		        		}
    		        	}
    		    		break;
    				}
    			}
    		}else if(recentContactIds.get(position) == -1){
    			contactImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
    			nameText.setText(recentContactNumbers.get(position));
    			numberText.setText("");
    			Log.i("MSG", "size of span : " + SpansTemp.size());
    			for(int j = 0; j< SpansTemp.size(); j++){
    				Log.i("MSG", "8888888888888888888888888888888888   entered!");
    				if(SpansTemp.get(j).displayName.equals(recentContactNumbers.get(position))){
    					contactCheck.setChecked(true);
    				}
    			}
    		}
    		final int _i = i;
    		
    		
    		
    		
    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						SpannedEntity span = new SpannedEntity();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SplashActivity.contactsList.size(); k++){
								if(Long.parseLong(SplashActivity.contactsList.get(k).content_uri_id) == recentContactIds.get(_position)){
									span = new SpannedEntity(-1, 2, SplashActivity.contactsList.get(k).name, Long.parseLong(SplashActivity.contactsList.get(k).content_uri_id), -1);
									break;
								}
							}
						}else{
							for(int k = 0; k< SplashActivity.contactsList.size(); k++){
								if(SplashActivity.contactsList.get(k).name.equals(recentContactNumbers.get(_position))){
									
								}
							}
							span = new SpannedEntity(-1, 1, recentContactNumbers.get(_position), -1, -1);
						}
						span.groupIds.add((long) -1);
						span.groupTypes.add(-1);
						
						SpansTemp.add(span);
						
						contactsAdapter.notifyDataSetChanged();
					}else{
						
							
							for(int i = 0; i< SpansTemp.size(); i++){
				    			if(recentContactIds.get(_position)>-1){
				    				if(recentContactIds.get(_position) == SpansTemp.get(i).entityId){
				    					Log.i("MSG", "got into removing a contact");
				    					Log.i("MSG", "size of Span " + SpansTemp.size());
				    					SpansTemp.remove(i);
					    				Log.i("MSG", "size of Span " + SpansTemp.size());
					    				contactsAdapter.notifyDataSetChanged();
					    				break;
					    			}
				    			}else{
				    				if(SpansTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    					SpansTemp.remove(i);
				    					contactsAdapter.notifyDataSetChanged();
				    					break;
				    				}
				    			}
								
				    		}
						
						
					}
				}
			});
    		
    		return row;
		}
	}
}