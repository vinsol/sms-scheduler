package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;
import com.vinsol.sms_scheduler.models.MyContact;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

public class GroupEditActivity extends Activity {

	
	Button addContactsButton;
	TextView groupNameLabel;
	ListView groupContactsList;
	Button saveGroupButton;
	Button deleteGroupButton;
	
	
	DBAdapter mdba = new DBAdapter(GroupEditActivity.this);
	
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
		setContentView(R.layout.group_edit_layout);
		
		Intent intent = getIntent();
		callingState = intent.getStringExtra("STATE");
		
		groupNameLabel 		= (TextView) 	findViewById(R.id.group_name_label);
		addContactsButton 	= (Button) findViewById(R.id.add_contacts_button);
		
		groupContactsList 	= (ListView) 	findViewById(R.id.group_members_listing);
		
		saveGroupButton 	= (Button) findViewById(R.id.save_group_button);
		deleteGroupButton   = (Button) findViewById(R.id.delete_group_button);
		
		//groupNameButton.setText("");
		
		if(callingState.equals("new") && ids.size()==0){
			ArrayList<String> idsString = new ArrayList<String>();
			intent = new Intent(GroupEditActivity.this, ContactsListActivity.class);
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
			groupNameLabel.setText(groupName);
			
			deleteGroupButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog d = new Dialog(GroupEditActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Delete this Group?");
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mdba.open();
							Log.i("MSG", "Group to delete : " + groupId);
							mdba.removeGroup(groupId);
							mdba.close();
							d.cancel();
							GroupEditActivity.this.finish();
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
		
		
		
		
		groupNameLabel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog d = new Dialog(GroupEditActivity.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.new_group_name_dialog_design);
				final EditText 	groupNameEdit 	= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
				ImageButton groupNameOkButton 	= (ImageButton) d.findViewById(R.id.group_name_dialog_name_ok_button);
				
				groupNameEdit.setText(groupName);
				
				groupNameOkButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(groupNameEdit.getText().toString().matches("(''|[' ']*)")){
							Toast.makeText(GroupEditActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
							groupNameEdit.setText(groupName);
						
							
						}else{
							boolean groupNameExists = false;
							mdba.open();
							Cursor cur = mdba.fetchAllGroups();
							if(cur.moveToFirst()){
								do{
									if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_GROUP_NAME)).equals(groupNameEdit.getText().toString()) && (cur.getLong(cur.getColumnIndex(DBAdapter.KEY_GROUP_ID)) != groupId)){                                       
										groupNameExists = true;
										break;
									}
								}while(cur.moveToNext());
							}
							mdba.close();
							if(groupNameExists){
								Toast.makeText(GroupEditActivity.this, "Group Name Exists. Try another", Toast.LENGTH_SHORT).show();
							}else{
								groupName = groupNameEdit.getText().toString();
								groupNameLabel.setText(groupName);
								d.cancel();
							}
							
						}
					}
				});
				d.show();
			}
		});
		
		
		
		addContactsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupEditActivity.this, ContactsListActivity.class);
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
		
		
		saveGroupButton.setOnClickListener(new OnClickListener() {
			
		  
			@Override
			public void onClick(View v) {
				
				if(callingState.equals("new")){
					Log.i("MSG", "Size of Ids : " + ids.size());
					mdba.open();
					Log.i("MSG", "Size of Ids : " + ids.size());
					mdba.createGroup(groupName, ids);
					mdba.close();
					GroupEditActivity.this.finish();
				
				
				
				}else if(callingState.equals("edit")){
					mdba.open();
					if(ids.size()>0){
						ids2 = mdba.fetchIdsForGroups(groupId);
						for(int i = 0; i< ids2.size(); i++){
							mdba.removeContactFromGroup(ids2.get(i), groupId);
						}
						for(int i = 0; i< ids.size(); i++){
							mdba.addContactToGroup(ids.get(i), groupId);
						}
						mdba.setGroupName(groupName, groupId);
						mdba.close();
						GroupEditActivity.this.finish();
					}else{
						Toast.makeText(GroupEditActivity.this, "Cannot make group with no Contact", Toast.LENGTH_LONG).show();
					}
					
					
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
			GroupEditActivity.this.finish();
		}else if(callingState.equals("edit")){
			boolean isChanged = false;
			if(ids.size() != ids2.size()){
				Log.i("MSG", "mode 1");
				isChanged = true;
			}else{
				for(int i = 0; i< ids.size(); i++){
					
					if (!ids.get(i).equals(ids2.get(i))){
						Log.i("MSG", "IDS :" + ids.get(i) + ", IDS2 :" + ids2.get(i) );
						isChanged = true;
						Log.i("MSG", "mode 2");
						break;
					}
				}
			}
			if(isChanged){
				final Dialog d = new Dialog(GroupEditActivity.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.confirmation_dialog_layout);
				
				TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
				Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
				Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
				
				questionText.setText("Discard the changes?");
				yesButton.setText("Yes");
				noButton.setText("No");
				yesButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mdba.open();
						ids = mdba.fetchIdsForGroups(groupId);
						for(int i = 0; i< ids.size(); i++){
							mdba.removeContactFromGroup(ids.get(i), groupId);
						}
						for(int i = 0; i< ids2.size(); i++){
							mdba.addContactToGroup(ids2.get(i), groupId);
						}
						mdba.close();
						d.cancel();
						GroupEditActivity.this.finish();
					}
				});
				
				noButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						d.cancel();
					}
				});
				
				d.show();
			}else{
				GroupEditActivity.this.finish();
			}
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
	}
	
	
	public void loadContactsForGroups(){
		Log.i("MSG", "Ids Size in LoadData : " + ids.size());
		newGroupContacts.clear();
		for(int j = 0; j< ids.size(); j++){
			for(int i = 0; i< SmsApplicationLevelData.contactsList.size(); i++){
				if(ids.get(j)==Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id)){
					MyContact myContact = new MyContact();
					myContact.content_uri_id = SmsApplicationLevelData.contactsList.get(i).content_uri_id;
					myContact.name = SmsApplicationLevelData.contactsList.get(i).name;
					myContact.number = SmsApplicationLevelData.contactsList.get(i).number;
					myContact.image = SmsApplicationLevelData.contactsList.get(i).image;
					newGroupContacts.add(myContact);
				}
			}
		}
		Log.i("MSG", "GroupSize " + newGroupContacts.size());
	}
	
	
	
	
	class MyAdapter extends ArrayAdapter{
		MyAdapter(){
    		super(GroupEditActivity.this, R.layout.group_add_edit_row_design, ids);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GroupsAddListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.group_add_edit_row_design, parent, false);
	    		holder = new GroupsAddListHolder();
	    		holder.contactImage 			= (ImageView) 	convertView.findViewById(R.id.group_add_edit_row_contact_pic);
	    		holder.contactName 				= (TextView) 	convertView.findViewById(R.id.group_add_edit_row_contact_name);
	    		holder.contactNumber 			= (TextView) 	convertView.findViewById(R.id.group_add_edit_row_contact_number);
	    		holder.contactRemoveButton 		= (ImageView) 	convertView.findViewById(R.id.group_add_edit_row_delete_button);
	    		convertView.setTag(holder);
			}else{
				holder = (GroupsAddListHolder) convertView.getTag();
			}
			
    		final int _position = position;
    		
    		holder.contactImage.setImageBitmap(newGroupContacts.get(position).image);
    		holder.contactName.setText(newGroupContacts.get(position).name);
    		holder.contactNumber.setText(newGroupContacts.get(position).number);
    		
    		holder.contactRemoveButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("MSG", "List position :" + _position);
//					if(callingState.equals("edit")){
//						mdba.open();
//						mdba.removeContactFromGroup(Long.parseLong(newGroupContacts.get(_position).content_uri_id), groupId);
//						mdba.close();
//					}
//					mdba.open();
//					Cursor cur = mdba.fetchIdsForGroups(groupId);
					if(newGroupContacts.size()==1 && callingState.equals("edit")){
						
						final Dialog d = new Dialog(GroupEditActivity.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog_layout);
						
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("Deleting last member will Delete the Group");
						yesButton.setText("Delete");
						noButton.setText("Cancel");
						yesButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								
								newGroupContacts.remove(_position);
								ids.remove(_position);
								MyAdapter.this.notifyDataSetChanged();
								mdba.open();
								ids2 = mdba.fetchIdsForGroups(groupId);
								for(int i = 0; i< ids2.size(); i++){
									mdba.removeContactFromGroup(ids2.get(i), groupId);
								}
								mdba.removeGroup(groupId);
								mdba.close();
								GroupEditActivity.this.finish();
							}
						});
						
						noButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								d.cancel();
							}
						});
						
						d.show();
					}else{
						newGroupContacts.remove(_position);
						ids.remove(_position);
						MyAdapter.this.notifyDataSetChanged();
					}
					
				}
			});
    		
			return convertView;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(callingState.equals("new")){
			GroupEditActivity.this.finish();
		}else{
			String isCancelled = data.getStringExtra("CANCEL");
			if(isCancelled.equals("yes")){
				GroupEditActivity.this.finish();
			}
			if(isCancelled.equals("no")){
				ArrayList<String> idsString = new ArrayList<String>();
				idsString = data.getStringArrayListExtra("IDSLIST");
				ids.clear();
				
				for(int i = 0; i< idsString.size(); i++){
					ids.add(Long.parseLong(idsString.get(i)));
				}
				loadContactsForGroups();
				myAdapter.notifyDataSetChanged();
				
			}
			
			
			
		}
		
		
	}
	
	
	
	static class GroupsAddListHolder{
		ImageView 	contactImage;
		TextView 	contactName;
		TextView 	contactNumber;
		ImageView 	contactRemoveButton;
	}
	
	
}
