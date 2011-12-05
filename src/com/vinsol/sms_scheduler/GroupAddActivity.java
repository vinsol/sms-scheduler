package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DialerFilter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupAddActivity extends Activity {

	
	ImageButton doneImageButton;
	ImageButton addContactsImageButton;
	//TextView groupNameLabel;
	Button groupNameButton;
	ListView groupContactsList;
	
	DBAdapter mdba = new DBAdapter(GroupAddActivity.this);
	
	MyAdapter myAdapter;
	
	Long groupId;
	String groupName = "";
	
	String callingState;
	boolean newCall = true;
	
	ArrayList<Long> ids = new ArrayList<Long>();
	ArrayList<Long> ids2 = new ArrayList<Long>();
	ArrayList<MyContact> newGroupContacts = new ArrayList<MyContact>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_add_edit_layout);
		
		Intent intent = getIntent();
		callingState = intent.getStringExtra("STATE");
		
		doneImageButton 		= (ImageButton) findViewById(R.id.group_add_edit_done_image_button);
		addContactsImageButton 	= (ImageButton) findViewById(R.id.group_add_edit_add_contact_image_button);
		//groupNameLabel 		= (TextView) 	findViewById(R.id.group_add_edit_name_label);
		groupContactsList 		= (ListView) 	findViewById(R.id.group_add_edit_list);
		groupNameButton			= (Button) findViewById(R.id.group_add_edit_name_label);
		
		//groupNameButton.setText("");
		
		if(callingState.equals("new") && ids.size()==0){
			ArrayList<String> idsString = new ArrayList<String>();
			intent = new Intent(GroupAddActivity.this, ContactsListActivity.class);
			intent.putStringArrayListExtra("IDARRAY", idsString);
			intent.putExtra("ORIGINATOR", "Group Add Activity");
			intent.putExtra("NEWCALL", newCall);
			startActivityForResult(intent, 1);
		}
		
		if(callingState.equals("edit")){
			Log.i("MSG", "Ids Size : ");
			groupId = intent.getLongExtra("GROUPID", 0);
			groupName = intent.getStringExtra("GROUPNAME");
			ids.clear();
			mdba.open();
			ids = mdba.fetchIdsForGroups(groupId);
			ids2 = mdba.fetchIdsForGroups(groupId);
			mdba.close();
			Log.i("MSG", "Ids Size : " + ids.size());
			groupNameButton.setText(groupName);
		}
		
		
		
		
		groupNameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog d = new Dialog(GroupAddActivity.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.new_group_name_dialog_design);
				final EditText 	groupNameEdit 		= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
				ImageButton groupNameOkButton 	= (ImageButton) d.findViewById(R.id.group_name_dialog_name_ok_button);
				
				groupNameEdit.setText(groupName);
				
				groupNameOkButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(groupNameEdit.getText().toString().matches("(''|[' ']+)")){
							Toast.makeText(GroupAddActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
							groupNameEdit.setText("");
						}else{
							groupName = groupNameEdit.getText().toString();
							groupNameButton.setText(groupName);
							d.cancel();
						}
					}
				});
				d.show();
			}
		});
		
		
		
		addContactsImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupAddActivity.this, ContactsListActivity.class);
				if(callingState.equals("new")){
					intent.putExtra("ORIGINATOR", "Group Add Activity");
				}else if(callingState.equals("edit")){
					intent.putExtra("ORIGINATOR", "Group Edit Activity");
					intent.putExtra("GROUPID", groupId);
				}
				ArrayList<String> idsString = new ArrayList<String>();
				for(int i = 0; i< ids.size(); i++){
					idsString.add(String.valueOf(ids.get(i)));
				}
				intent.putStringArrayListExtra("IDARRAY", idsString);
				startActivityForResult(intent, 1);
			}
		});
		
		
		doneImageButton.setOnClickListener(new OnClickListener() {
			
		  
			@Override
			public void onClick(View v) {
				
				if(callingState.equals("new")){
					Log.i("MSG", "Size of Ids : " + ids.size());
					mdba.open();
					Log.i("MSG", "Size of Ids : " + ids.size());
					mdba.createGroup(groupName, ids);
					mdba.close();
					GroupAddActivity.this.finish();
				
				
				
				}else if(callingState.equals("edit")){
					mdba.open();
					ids2 = mdba.fetchIdsForGroups(groupId);
					for(int i = 0; i< ids2.size(); i++){
						mdba.removeContactFromGroup(ids2.get(i), groupId);
					}
					for(int i = 0; i< ids.size(); i++){
						mdba.addContactToGroup(ids.get(i), groupId);
					}
					mdba.setGroupName(groupName, groupId);
					mdba.close();
					GroupAddActivity.this.finish();
				}
				
				
			}
		});
		
		
		
		loadContactsForGroups();
		myAdapter = new MyAdapter();
		groupContactsList.setAdapter(myAdapter);
		
		
		
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		if(callingState.equals("new")){
			GroupAddActivity.this.finish();
		}else if(callingState.equals("edit")){
			mdba.open();
			ids = mdba.fetchIdsForGroups(groupId);
			for(int i = 0; i< ids.size(); i++){
				mdba.removeContactFromGroup(ids.get(i), groupId);
			}
			for(int i = 0; i< ids2.size(); i++){
				mdba.addContactToGroup(ids2.get(i), groupId);
			}
			mdba.close();
			GroupAddActivity.this.finish();
		}
		super.onBackPressed();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
	}
	
	
	public void loadContactsForGroups(){
		Log.i("MSG", "Ids Size in LoadData : " + ids.size());
		newGroupContacts.clear();
		for(int j = 0; j< ids.size(); j++){
			for(int i = 0; i< SplashActivity.contactsList.size(); i++){
				if(ids.get(j)==Long.parseLong(SplashActivity.contactsList.get(i).content_uri_id)){
					MyContact myContact = new MyContact();
					myContact.content_uri_id = SplashActivity.contactsList.get(i).content_uri_id;
					myContact.name = SplashActivity.contactsList.get(i).name;
					myContact.number = SplashActivity.contactsList.get(i).number;
					myContact.image = SplashActivity.contactsList.get(i).image;
					newGroupContacts.add(myContact);
				}
			}
		}
		Log.i("MSG", "GroupSize " + newGroupContacts.size());
	}
	
	
	
	
	class MyAdapter extends ArrayAdapter{
		MyAdapter(){
    		super(GroupAddActivity.this, R.layout.group_add_edit_row_design, ids);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.group_add_edit_row_design, parent, false);
    		final int _position = position;
    		
    		ImageView contactImage 			= (ImageView) 	row.findViewById(R.id.group_add_edit_row_contact_pic);
    		TextView contactName 			= (TextView) 	row.findViewById(R.id.group_add_edit_row_contact_name);
    		TextView contactNumber 			= (TextView) 	row.findViewById(R.id.group_add_edit_row_contact_number);
    		ImageView contactRemoveButton 	= (ImageView) 	row.findViewById(R.id.group_add_edit_row_delete_button);
    		
    		contactImage.setImageBitmap(newGroupContacts.get(position).image);
    		contactName.setText(newGroupContacts.get(position).name);
    		contactNumber.setText(newGroupContacts.get(position).number);
    		
    		contactRemoveButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("MSG", "List position :" + _position);
//					if(callingState.equals("edit")){
//						mdba.open();
//						mdba.removeContactFromGroup(Long.parseLong(newGroupContacts.get(_position).content_uri_id), groupId);
//						mdba.close();
//					}
					
					newGroupContacts.remove(_position);
					ids.remove(_position);
					MyAdapter.this.notifyDataSetChanged();
					
				}
			});
    		
			return row;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		String isCancelled = data.getStringExtra("CANCEL");
		if(isCancelled.equals("yes")){
			GroupAddActivity.this.finish();
		}
		
		ArrayList<String> idsString = new ArrayList<String>();
		idsString = data.getStringArrayListExtra("IDSLIST");
		ids.clear();
		
		for(int i = 0; i< idsString.size(); i++){
			ids.add(Long.parseLong(idsString.get(i)));
		}
		loadContactsForGroups();
		myAdapter.notifyDataSetChanged();
		
		if(callingState.equals("new") && newCall && (ids.size()==0)){
			GroupAddActivity.this.finish();
		}
		
		if(callingState.equals("new") && newCall && (ids.size()>0)){
			final Dialog d = new Dialog(GroupAddActivity.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.new_group_name_dialog_design);
			d.setCancelable(false);
			final EditText 	groupNameEdit 		= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
			ImageButton groupNameOkButton 	= (ImageButton) d.findViewById(R.id.group_name_dialog_name_ok_button);
			
			groupNameEdit.setText(groupName);
			
			groupNameOkButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(groupNameEdit.getText().toString().matches("(''|[' ']+)")){
						Toast.makeText(GroupAddActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
						groupNameEdit.setText("");
					}else{
						groupName = groupNameEdit.getText().toString();
						groupNameButton.setText(groupName);
						d.cancel();
						newCall = false;
					}
				}
			});
			d.show();
		}
	}
}
