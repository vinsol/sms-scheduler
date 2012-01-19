package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.utils.Log;


public class EditGroup extends Activity {

	
	private Button addContactsButton;
	private TextView groupNameLabel;
	private ListView groupContactsList;
	private Button saveGroupButton;
	private Button deleteGroupButton;
	
	
	private DBAdapter mdba = new DBAdapter(EditGroup.this);
	
	private MyAdapter myAdapter;
	
	private Long groupId;
	private String groupName = "";
	
	private ArrayList<Long> ids = new ArrayList<Long>();
	private ArrayList<Long> idsTemp = new ArrayList<Long>();
	private ArrayList<Contact> newGroupContacts = new ArrayList<Contact>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_group);
		
		Intent intent = getIntent();
		
		groupNameLabel 		= (TextView) 	findViewById(R.id.group_name_label);
		addContactsButton 	= (Button) findViewById(R.id.add_contacts_button);
		groupContactsList 	= (ListView) 	findViewById(R.id.group_members_listing);
		saveGroupButton 	= (Button) findViewById(R.id.save_group_button);
		deleteGroupButton   = (Button) findViewById(R.id.delete_group_button);
		
	
		groupId = intent.getLongExtra("GROUPID", 0);
		groupName = intent.getStringExtra("GROUPNAME");
		ids.clear();
		mdba.open();
		ids = idsTemp = mdba.fetchIdsForGroups(groupId);
		
		mdba.close();
		groupNameLabel.setText(groupName);
			
		deleteGroupButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog d = new Dialog(EditGroup.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.confirmation_dialog);
				TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
				Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
				Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
				
				questionText.setText("Delete this Group?");
				
				yesButton.setOnClickListener(new OnClickListener() {
						
					@Override
						public void onClick(View v) {
						mdba.open();
						mdba.removeGroup(groupId);
						mdba.close();
						d.cancel();
						EditGroup.this.finish();
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
		
		
		
		
		groupNameLabel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog d = new Dialog(EditGroup.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.group_name_input_dialog);
				final EditText 	groupNameEdit 	= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
				Button groupNameOkButton 	= (Button) d.findViewById(R.id.group_name_dialog_name_ok_button);
				Button groupNameCancelButton= (Button) d.findViewById(R.id.group_name_dialog_name_cancel_button);
				
				groupNameEdit.setText(groupName);
				
				groupNameOkButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(groupNameEdit.getText().toString().matches("(''|[' ']*)")){
							Toast.makeText(EditGroup.this, "Group name can't be blank", Toast.LENGTH_SHORT).show();
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
							if(groupNameExists){
								Toast.makeText(EditGroup.this, "Group name already exists", Toast.LENGTH_SHORT).show();
							}else{
								groupName = groupNameEdit.getText().toString();
								groupNameLabel.setText(groupName);
								d.cancel();
							}
							
						}
					}
				});
				
				groupNameCancelButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						d.cancel();
					}
				});
				
				d.show();
			}
		});
		
		
		
		addContactsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditGroup.this, ContactsList.class);
				intent.putExtra("ORIGINATOR", "Group Edit Activity");
				intent.putExtra("GROUPID", groupId);
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
				
				mdba.open();
				if(ids.size()>0){
					idsTemp = mdba.fetchIdsForGroups(groupId);
					for(int i = 0; i< idsTemp.size(); i++){
						mdba.removeContactFromGroup(idsTemp.get(i), groupId);
					}
					for(int i = 0; i< ids.size(); i++){
						mdba.addContactToGroup(ids.get(i), groupId);
					}
					mdba.setGroupName(groupName, groupId);
					mdba.close();
					EditGroup.this.finish();
				}else{
					Toast.makeText(EditGroup.this, "Cannot make group with no Contact", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		
		loadContactsForGroups();
		myAdapter = new MyAdapter();
		groupContactsList.setAdapter(myAdapter);
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		boolean isChanged = false;
		if(ids.size() != idsTemp.size()){
			isChanged = true;
		}else{
			for(int i = 0; i< ids.size(); i++){
				if (!ids.get(i).equals(idsTemp.get(i))){
					isChanged = true;
					break;
				}
			}
		}
		if(isChanged){
			final Dialog d = new Dialog(EditGroup.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
		
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("Discard the changes?");
			yesButton.setText("");
			noButton.setText("");
			yesButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mdba.open();
					ids = mdba.fetchIdsForGroups(groupId);
					for(int i = 0; i< ids.size(); i++){
						mdba.removeContactFromGroup(ids.get(i), groupId);
					}
					for(int i = 0; i< idsTemp.size(); i++){
						mdba.addContactToGroup(idsTemp.get(i), groupId);
					}
					mdba.close();
					d.cancel();
					EditGroup.this.finish();
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
			EditGroup.this.finish();
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
	}
	
	
	private void loadContactsForGroups(){
		Log.d("Ids Size in LoadData : " + ids.size());
		newGroupContacts.clear();
		for(int j = 0; j< ids.size(); j++){
			for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
				if(ids.get(j)==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
					Contact Contact = new Contact();
					Contact.content_uri_id = SmsSchedulerApplication.contactsList.get(i).content_uri_id;
					Contact.name = SmsSchedulerApplication.contactsList.get(i).name;
					Contact.number = SmsSchedulerApplication.contactsList.get(i).number;
					Contact.image = SmsSchedulerApplication.contactsList.get(i).image;
					newGroupContacts.add(Contact);
				}
			}
		}
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	private class MyAdapter extends ArrayAdapter{
		@SuppressWarnings("unchecked")
		MyAdapter(){
    		super(EditGroup.this, R.layout.edit_group_list_row, ids);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GroupsAddListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.edit_group_list_row, parent, false);
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
					Log.d("List position :" + _position);
					if(newGroupContacts.size()==1){
						
						final Dialog d = new Dialog(EditGroup.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog);
						
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("If you delete the last member, the group will be deleted.\n\nAre you sure?");
						yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.delete_dialog_states));
						noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancel_dialog_states));
						yesButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								
								newGroupContacts.remove(_position);
								ids.remove(_position);
								MyAdapter.this.notifyDataSetChanged();
								mdba.open();
								idsTemp = mdba.fetchIdsForGroups(groupId);
								for(int i = 0; i< idsTemp.size(); i++){
									mdba.removeContactFromGroup(idsTemp.get(i), groupId);
								}
								mdba.removeGroup(groupId);
								mdba.close();
								EditGroup.this.finish();
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
					else{
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
		
		boolean isCancelled = data.getBooleanExtra("CANCEL", false);
		if(!isCancelled){
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
	
	
	
	private class GroupsAddListHolder{
		ImageView 	contactImage;
		TextView 	contactName;
		TextView 	contactNumber;
		ImageView 	contactRemoveButton;
	}
}