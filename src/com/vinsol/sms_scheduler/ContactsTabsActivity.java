package com.vinsol.sms_scheduler;

import java.util.ArrayList;
import java.util.HashMap;

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
	ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
	//boolean empty;
	
	private final String GROUP_NAME = "group_name";
	private final String GROUP_CHECK = "group_check";
	private final String GROUP_IMAGE = "group_image";
	private final String GROUP_TYPE = "group_type";
	private final String GROUP_ID = "group_id";
	
	private final String CHILD_NAME = "child_name";
	private final String CHILD_CHECK = "child_check";
	private final String CHILD_NUMBER = "child_number";
	private final String CHILD_IMAGE = "child_contact_image";
	private final String CHILD_CONTACT_ID = "child_contact_id";

	SimpleExpandableListAdapter mAdapter;
	ArrayList<GroupStructure> GroupTemp = new ArrayList<GroupStructure>();
	//-----------------------------------------------------------------------------
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tabs_layout);
		
		
		//----------------------Setting up the Tabs--------------------------------
		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.contacts_tab);
        spec1.setIndicator("Contacts");

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Groups");
        spec2.setContent(R.id.groups_tab);

        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Recents");
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
//			for(int i = 0; i < NewScheduleActivity.Groups.size(); i++){
//				GroupTemp.add(NewScheduleActivity.Groups.get(i));
//			}
		}else if(origin.equals("edit")){
			for(int i = 0; i < EditScheduledSmsActivity.Spans.size(); i++){
				SpansTemp.add(EditScheduledSmsActivity.Spans.get(i));
			}
//			for(int i = 0; i < EditScheduledSmsActivity.Groups.size(); i++){
//				GroupTemp.add(EditScheduledSmsActivity.Groups.get(i));
//			}
		}
//		if(GroupTemp.size()==0){
//			empty = true;
//		}else{
//			empty = false;
//		}
		
		
		
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
				
				if(origin.equals("new")){
					NewScheduleActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						NewScheduleActivity.Spans.add(SpansTemp.get(i));
					}
				}else if(origin.equals("edit")){
					EditScheduledSmsActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						EditScheduledSmsActivity.Spans.add(SpansTemp.get(i));
					}
				}
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
		loadGroupsData();
		customGroupsAdapterSetup();
		
		groupExplList.setAdapter(mAdapter);
		groupExplList.setOnGroupExpandListener(new OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
//				loadGroupsData();
//				mAdapter.notifyDataSetChanged();
			}
		});
		
		groupExplList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			
			@Override
			public void onGroupCollapse(int groupPosition) {
//				loadGroupsData();
//				mAdapter.notifyDataSetChanged();
			}
		});
		//----------------------------------------------------end of Groups Tab setup-------------------
        
        
	}
	
	
	
	
	
	
	protected void onRestoreInstanceState(Bundle state) {
	    super.onRestoreInstanceState(state);

	    // Retrieve list state and list/item positions
	    mListState = state.getParcelable(LIST_STATE_KEY);
	    mListPosition = state.getInt(LIST_POSITION_KEY);
	    mItemPosition = state.getInt(ITEM_POSITION_KEY);
	}

	protected void onResume() {
	    super.onResume();

	    // Load data from DB and put it onto the list
	    loadGroupsData();

	    // Restore list state and list/item positions
	    ExpandableListView listView = getExpandableListView();
	    if (mListState != null)
	        listView.onRestoreInstanceState(mListState);
	    listView.setSelectionFromTop(mListPosition, mItemPosition);
	}

	protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);

	    // Save list state
	    ExpandableListView listView = getExpandableListView();
	    mListState = listView.onSaveInstanceState();
	    state.putParcelable(LIST_STATE_KEY, mListState);

	    // Save position of first visible item
	    mListPosition = listView.getFirstVisiblePosition();
	    state.putInt(LIST_POSITION_KEY, mListPosition);

	    // Save scroll position of item
	    View itemView = listView.getChildAt(0);
	    mItemPosition = itemView == null ? 0 : itemView.getTop();
	    state.putInt(ITEM_POSITION_KEY, mItemPosition);
	}
	
	
	
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		intent.putExtra("IDSARRAY", idsString);
		setResult(2, intent);
		
		if(origin.equals("new")){
			NewScheduleActivity.Spans.clear();
			for(int i = 0; i< SpansTemp.size(); i++){
				NewScheduleActivity.Spans.add(SpansTemp.get(i));
			}
		}else if(origin.equals("edit")){
			EditScheduledSmsActivity.Spans.clear();
			for(int i = 0; i< SpansTemp.size(); i++){
				EditScheduledSmsActivity.Spans.add(SpansTemp.get(i));
			}
		}
		
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
    		
    		if(origin.equals("new")){
    			for(int i = 0; i< NewScheduleActivity.Spans.size(); i++){
        			if(Long.parseLong(SplashActivity.contactsList.get(position).content_uri_id) == NewScheduleActivity.Spans.get(i).entityId){
        				contactCheck.setChecked(true);
        			}
        		}
    		}else if(origin.equals("edit")){
    			for(int i = 0; i< EditScheduledSmsActivity.Spans.size(); i++){
        			if(Long.parseLong(SplashActivity.contactsList.get(position).content_uri_id) == EditScheduledSmsActivity.Spans.get(i).entityId){
        				contactCheck.setChecked(true);
        			}
        		}
    		}
    		
    		
    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						SpannedEntity span = new SpannedEntity(-1, 2, SplashActivity.contactsList.get(_position).name, Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id), -1);
						if(origin.equals("new")){
							NewScheduleActivity.Spans.add(span);
						}else if(origin.equals("edit")){
							EditScheduledSmsActivity.Spans.add(span);
						}
						
					}else{
						if(origin.equals("new")){
							for(int i = 0; i< NewScheduleActivity.Spans.size(); i++){
				    			if(Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id) == NewScheduleActivity.Spans.get(i).entityId){
				    				NewScheduleActivity.Spans.remove(i);
				    			}
				    		}
						}else if(origin.equals("edit")){
							for(int i = 0; i< EditScheduledSmsActivity.Spans.size(); i++){
				    			if(Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id) == EditScheduledSmsActivity.Spans.get(i).entityId){
				    				EditScheduledSmsActivity.Spans.remove(i);
				    			}
				    		}
						}
						
					}
				}
			});
    		
    		return row;
		}
	}
	//************************************************************** end of ContactsAdapter******************
	
	
	
	public void loadGroupsData(){
		
		groupData.clear();
		childData.clear();
		
		//------------------------ Setting up data for native groups ---------------------------
		String[] projection = new String[] {
                Groups._ID,
                Groups.TITLE,
                Groups.SYSTEM_ID,
                Groups.NOTES,
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        int count = 0;
        
        Cursor groupCursor = managedQuery(groupsUri, projection, null, null, null);
        Log.i("MSG", GroupTemp.size() + "");
        if(groupCursor.moveToFirst()){
        	
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		group.put(GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
//        		if(empty){
        			group.put(GROUP_CHECK, false);
//        		}else{
//        			group.put(GROUP_CHECK, GroupTemp.get(count).isChecked);
//        		}
        		group.put(GROUP_TYPE, 1);
        		group.put(GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		
//        		GroupStructure groupStructure;
//        		if(GroupTemp.size() == 0){
//        			groupStructure = new GroupStructure();
//        			groupStructure.groupId = groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID));
//        			groupStructure.isChecked = false;
//        			ArrayList<Long> ids = new ArrayList<Long>();
//        			groupStructure.CheckedContactsIds = ids;
//        			GroupTemp.add(groupStructure);
//        		}
        		
        		groupData.add(group);
        		
        		for(int i = 0; i < SplashActivity.contactsList.size(); i++){
        			for(int j = 0; j< SplashActivity.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SplashActivity.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(CHILD_NAME, SplashActivity.contactsList.get(i).name);
        					childParameters.put(CHILD_NUMBER, SplashActivity.contactsList.get(i).number);
        					childParameters.put(CHILD_IMAGE, SplashActivity.contactsList.get(i).image);
//        					if(empty){
        						childParameters.put(CHILD_CHECK, false);
//        					}else{
//        						for(int k = 0; k< GroupTemp.get(count).CheckedContactsIds.size(); k++){
//        							if(GroupTemp.get(count).CheckedContactsIds.get(k)==Long.parseLong(SplashActivity.contactsList.get(i).content_uri_id)){
//        								childParameters.put(CHILD_CHECK, true);
//        							}else{
//        								childParameters.put(CHILD_CHECK, false);
//        							}
//        						}
//        					}
        					childParameters.put(CHILD_CONTACT_ID, SplashActivity.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        					
        				}
        			}
        		}
        		childData.add(child);
        		count++;
        	}while(groupCursor.moveToNext());
        }
        
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
//        		if(empty){
        			group.put(GROUP_CHECK, false);
//        		}else{
//        			group.put(GROUP_CHECK, GroupTemp.get(count).isChecked);
//        		}
        		group.put(GROUP_TYPE, 2);
        		group.put(GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		groupData.add(group);
        		GroupStructure groupStructure;
        		if(GroupTemp.size() == 0){
        			groupStructure = new GroupStructure();
        			groupStructure.groupId = Long.parseLong(groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        			groupStructure.isChecked = false;
        			ArrayList<Long> ids = new ArrayList<Long>();
        			groupStructure.CheckedContactsIds = ids;
        			GroupTemp.add(groupStructure);
        		}
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SplashActivity.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SplashActivity.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(CHILD_NAME, SplashActivity.contactsList.get(j).name);
        					childParameters.put(CHILD_NUMBER, SplashActivity.contactsList.get(j).number);
        					childParameters.put(CHILD_CONTACT_ID, SplashActivity.contactsList.get(j).content_uri_id);
        					childParameters.put(CHILD_IMAGE, SplashActivity.contactsList.get(j).image);
//        					if(empty){
        						childParameters.put(CHILD_CHECK, false);
//        					}else{
//        						for(int k = 0; k< GroupTemp.get(count).CheckedContactsIds.size(); k++){
//        							if(GroupTemp.get(count).CheckedContactsIds.get(k)==Long.parseLong(SplashActivity.contactsList.get(j).content_uri_id)){
//        								childParameters.put(CHILD_CHECK, true);
//        							}else{
//        								childParameters.put(CHILD_CHECK, false);
//        							}
//        						}
//        					}
        					
        					child.add(childParameters);
        				}
        			}
        		}
        		
        		childData.add(child);
        		count++;
        	}while(groupsCursor.moveToNext());
        }
        
        mdba.close();

//        for(int k = 0 ; k < groupData.size(); k++){
//        	Log.i("MSG", "group");
//        	for(int n = 0 ; n< childData.get(k).size(); n++){
//        		Log.i("MSG", "fdsafdsa");
//        	}
//        }
	}
	
	
	
	
	public void customGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	groupData,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	childData,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return childData.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
				for(int i = 0; i<groupPosition; i ++) {
				   id += childData.get(groupPosition).size(); 
			   }
				return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return childData.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return childData.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return groupData.size();
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
    			
    			
    			groupHeading.setText((String)groupData.get(groupPosition).get(GROUP_NAME));
    			groupCheck.setChecked((Boolean)groupData.get(groupPosition).get(GROUP_CHECK));
    			//groupImage.setImageBitmap((Bitmap)groupData.get(groupPosition).get(GROUP_IMAGE));
    			
    			groupCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked){
							groupData.get(groupPosition).put(GROUP_CHECK, true);
//							GroupTemp.get(groupPosition).isChecked = true;
						}else{
							groupData.get(groupPosition).put(GROUP_CHECK, false);
//							GroupTemp.get(groupPosition).isChecked = false;
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
    			
				HashMap<String, Object> child = childData.get(groupPosition).get(childPosition);
				
				Log.v("sazwqa", "11111111111111111111111111111111111111111111111111111111111");
				Log.v("sazwqa", "GroupPosition = " + groupPosition);
				Log.v("sazwqa", "ChildPosition = " + childPosition);
				Log.v("sazwqa", "contact Name = " + child.get(CHILD_NAME));
				Log.v("sazwqa", "is Checked = " + child.get(CHILD_CHECK));
				Log.v("sazwqa", "11111111111111111111111111111111111111111111111111111111111");
				
				
//    			if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row_design, null, false);
//    			}
    			final TextView childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
    			final ImageView childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
    			final TextView childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
    			final CheckBox childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
    			
    			childNameText.setText((String)childData.get(groupPosition).get(childPosition).get(CHILD_NAME));
    			childNumberText.setText((String)childData.get(groupPosition).get(childPosition).get(CHILD_NUMBER));
    			childContactImage.setImageBitmap((Bitmap)childData.get(groupPosition).get(childPosition).get(CHILD_IMAGE));
    			childCheck.setChecked((Boolean)childData.get(groupPosition).get(childPosition).get(CHILD_CHECK));
    			
    			childCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked){
							childData.get(groupPosition).get(childPosition).put(CHILD_CHECK, true);
//							GroupTemp.get(groupPosition).CheckedContactsIds.add((Long)childData.get(groupPosition).get(childPosition).get(CHILD_CONTACT_ID));
						}else{
							childData.get(groupPosition).get(childPosition).put(CHILD_CHECK, false);
//		
//							for(int k = 0 ; k< GroupTemp.get(groupPosition).CheckedContactsIds.size(); k++){
//								if(GroupTemp.get(groupPosition).CheckedContactsIds.get(k) == (Long)childData.get(groupPosition).get(childPosition).get(CHILD_CONTACT_ID)){
//									GroupTemp.get(groupPosition).CheckedContactsIds.remove(k);
//									break;
//								}
//							}
						}
					}
				});
					
					
    			
//    			convertView.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						if(childCheck.isChecked()){
//							childCheck.setChecked(false);
//						}else{
//							childCheck.setChecked(true);
//						}
//					}
//				});
    			
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
	
	

}
