package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.HashMap;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.drawable;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class GroupsTestActivity extends Activity {

	ExpandableListView groupExplList;
	ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
	
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
	
	DBAdapter mdba = new DBAdapter(this);
	SimpleExpandableListAdapter mAdapter;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_expl_list_layout);
		
		groupExplList = (ExpandableListView) findViewById(R.id.group_expl_list);
		
		loadData();
		customAdapterSetup();
		
		groupExplList.setAdapter(mAdapter);
	}
	
	public void loadData(){
		
		//------------------------ Setting up data for native groups ---------------------------
		String[] projection = new String[] {
                Groups._ID,
                Groups.TITLE,
                Groups.SYSTEM_ID,
                Groups.NOTES,
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        
        Cursor groupCursor = managedQuery(groupsUri, projection, null, null, null);
        if(groupCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		group.put(GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
        		group.put(GROUP_CHECK, false);
        		group.put(GROUP_TYPE, 1);
        		group.put(GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		
        		groupData.add(group);
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		
        		for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsApplicationLevelData.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(CHILD_NAME, SmsApplicationLevelData.contactsList.get(i).name);
        					childParameters.put(CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(i).number);
        					childParameters.put(CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(i).image);
        					childParameters.put(CHILD_CHECK, false);
        					childParameters.put(CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        				}
        			}
        		}
        		Log.i("MSG", child.size() + "");
        		childData.add(child);
        		
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
        		group.put(GROUP_CHECK, false);
        		group.put(GROUP_TYPE, 2);
        		
        		groupData.add(group);
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(CHILD_CHECK, false);
        					
        					child.add(childParameters);
        				}
        			}
        		}
        		Log.i("MSG", child.size() + "");
        		childData.add(child);
        		
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
	
	
	
	
	
	public void customAdapterSetup(){
		
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
//			   return (Long)(childData.get(groupPosition).get(childPosition).get(CHILD_CONTACT_ID));
				return 0;
			}
			
    		
			
    		@Override
	    	public View newChildView(boolean isLastChild, ViewGroup parent) {
	    		
	    		return layoutInflater.inflate(R.layout.contacts_list_row_design, null, false);
	    	}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return childData.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return groupData.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return groupData.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return 0;
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
    			
    			groupExplList.expandGroup(groupPosition);
    			
//    			convertView.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						if(isExpanded){
//							groupExplList.collapseGroup(groupPosition);
//						}else{
//							groupExplList.expandGroup(groupPosition);
//						}
//						
//					}
//				});
    			
    			return convertView;
    			
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
    			final View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    			
    			final TextView childNameText  		= (TextView)  v.findViewById(R.id.contact_list_row_contact_name);
    			final ImageView childContactImage 	= (ImageView) v.findViewById(R.id.contact_list_row_contact_pic);
    			final TextView childNumberText		= (TextView)  v.findViewById(R.id.contact_list_row_contact_number);
    			final CheckBox childCheck			= (CheckBox)  v.findViewById(R.id.contact_list_row_contact_check);
    			
    			childNameText.setText((String)childData.get(groupPosition).get(childPosition).get(CHILD_NAME));
    			childNumberText.setText((String)childData.get(groupPosition).get(childPosition).get(CHILD_NUMBER));
    			childContactImage.setImageBitmap((Bitmap)childData.get(groupPosition).get(childPosition).get(CHILD_IMAGE));
    			childCheck.setChecked((Boolean)childData.get(groupPosition).get(childPosition).get(CHILD_CHECK));
    			
    			childCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Log.v("sazwqa", "111111111111111111111111111111111111111111111111111111111");
						Log.v("sazwqa", "groupPosition = " + groupPosition);
						Log.v("sazwqa", "childPosition = " + childPosition);
						Log.v("sazwqa", "isChecked = " + isChecked);
						Log.v("sazwqa", "111111111111111111111111111111111111111111111111111111111");
						
						if(isChecked){
							childData.get(groupPosition).get(childPosition).put(CHILD_CHECK, true);
						}else{
							childData.get(groupPosition).get(childPosition).put(CHILD_CHECK, false);
						}
					}
				});
//    			v.setOnClickListener(new OnClickListener() {
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
    			
    			return v;
    		}
			
			
			
			@Override
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			@Override
			public boolean hasStableIds() {
			   return true;
			}
			 
			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    		
    	};
    }
	
	
	
	
	
}
