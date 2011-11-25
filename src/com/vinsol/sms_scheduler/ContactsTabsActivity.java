package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class ContactsTabsActivity extends Activity {
	
	
	//TabHost tabHost;
	ListView nativeContactsList;
	//ListView privateGroupsList;
	Button doneButton;
	Button cancelButton;
	
	ContactsAdapter contactsAdapter;
	//GroupsAdapter groupsAdapter;
	
	ArrayList<MyContact> selectedIds = new ArrayList<MyContact>();
	
	
	
	DBAdapter mdba = new DBAdapter(this);
	Cursor cur;
	
	ArrayList<String> idsString = new ArrayList<String>();
	
	//ArrayList<Long> groupIds = new ArrayList<Long>();
	//ArrayList<String> groupNames = new ArrayList<String>();
	
	ArrayList<Long> ids = new ArrayList<Long>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tabs_layout);
		
		Intent intent = getIntent();
		idsString = intent.getStringArrayListExtra("IDSARRAY");
		for(int i = 0; i< idsString.size(); i++){
			ids.add(Long.parseLong(idsString.get(i)));
		}
		
		Log.i("MSG", ids.size()+ "");
		
		//tabHost = (TabHost)findViewById(R.id.tabHost);
		//tabHost.setup();
		
		
		
		//TabSpec spec1=tabHost.newTabSpec("Contacts");
        //spec1.setContent(R.id.contacts_tabs_native_contacts_list);
        //spec1.setIndicator("Contacts");
        
        //TabSpec spec2=tabHost.newTabSpec("Groups");
        //spec2.setContent(R.id.contacts_tabs_private_groups_list);
        //spec2.setIndicator("Groups");
        
        //tabHost.addTab(spec1);
       // tabHost.addTab(spec2);
        
        //tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);
        
//        mdba.open();
//        cur = mdba.fetchAllGroups();
//        mdba.close();
//        
//        
//        if(cur.moveToFirst()){
//        	do{
//        		groupIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
//        		groupNames.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
//        	}while(cur.moveToNext());
//        }
//		
        nativeContactsList 	= (ListView) 	findViewById(R.id.contacts_tabs_native_contacts_list);
        //privateGroupsList 	= (ListView) 	findViewById(R.id.contacts_tabs_private_groups_list);
        doneButton			= (Button) 		findViewById(R.id.contacts_tab_done_button);
        cancelButton		= (Button) 		findViewById(R.id.contacts_tab_cancel_button);
        
        
        
        doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				idsString.clear();
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
				
				ContactsTabsActivity.this.finish();
			}
		});
        
        
        
        
		contactsAdapter = new ContactsAdapter();
        nativeContactsList.setAdapter(contactsAdapter);
        
        //groupsAdapter = new GroupsAdapter();
       // privateGroupsList.setAdapter(groupsAdapter);
        
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		intent.putExtra("IDSARRAY", idsString);
		setResult(2, intent);
		
		ContactsTabsActivity.this.finish();
	}
	
	
	
	
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
    		
    		for(int i = 0; i< ids.size(); i++){
    			if(Long.parseLong(SplashActivity.contactsList.get(position).content_uri_id) == ids.get(i)){
    				contactCheck.setChecked(true);
    			}else{
    				contactCheck.setChecked(false);
    			}
    		}
    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						ids.add(Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id));
					}else{
						for(int i = 0; i< ids.size(); i++){
			    			if(Long.parseLong(SplashActivity.contactsList.get(_position).content_uri_id) == ids.get(i)){
			    				ids.remove(i);
			    			}
			    		}
					}
				}
			});
    		
    		return row;
		}
	}
	
	
	
//	class GroupsAdapter extends ArrayAdapter{
//		GroupsAdapter(){
//    		super(ContactsTabsActivity.this, R.layout.contacts_list_row_design, groupIds);
//    	}
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			
//			final int _position  = position;
//    		LayoutInflater inflater = getLayoutInflater();
//    		View row = inflater.inflate(R.layout.groups_list_row, parent, false);
//			
//    		TextView groupNameText = (TextView) row.findViewById(R.id.group_list_row_group_name);
//    		CheckBox groupCheckBox = (CheckBox) row.findViewById(R.id.group_list_row_group_check);
//    		
//    		ArrayList<Long> ids = new ArrayList<Long>();
//    		mdba.open();
//    		ids = mdba.fetchIdsForGroups(groupIds.get(position));
//    		groupNameText.setText(groupNames.get(position) + " (" + ids.size() + ")");
//    		
//    		groupCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//					if(isChecked){
//						
//					}else{
//						
//					}
//				}
//			});
//    		
//			return row;
//		}
//	}
//	
	
	
	

}
