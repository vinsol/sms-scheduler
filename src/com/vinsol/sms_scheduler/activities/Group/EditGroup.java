/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities.Group;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;
import com.vinsol.sms_scheduler.activities.Contact.ContactsList;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.ContactNumber;
import com.vinsol.sms_scheduler.utils.DisplayImage;
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
	private ArrayList<String> numbers = new ArrayList<String>();
	private ArrayList<String> numbersTemp = new ArrayList<String>();
	private ArrayList<Contact> newGroupContacts = new ArrayList<Contact>();
	private ArrayList<GroupMember> groupMembers = new ArrayList<GroupMember>();
	
	private DisplayImage displayImage = new DisplayImage();
	
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
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_group);
		
		FlurryAgent.logEvent("Edit Group Activity Started");
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
		numbers = numbersTemp = mdba.fetchNumbersForGroup(groupId);
		
		groupMembers = organizeIds(ids, numbers);
		
		mdba.close();
		groupNameLabel.setText(groupName);
			
		deleteGroupButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				final Dialog d = new Dialog(EditGroup.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.confirmation_dialog);
				TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
				Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
				Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
				
				questionText.setText("Delete this Group?");
				
				yesButton.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
						mdba.open();
						mdba.removeGroup(groupId);
						mdba.close();
						FlurryAgent.logEvent("Group Deleted");
						d.cancel();
						EditGroup.this.finish();
					}
				});
					
				noButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						d.cancel();
					}
				});
				
				d.show();
			}
		});
		
		
		groupNameLabel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				FlurryAgent.logEvent("Group Name Change Label Clicked");
				
				final Dialog d = new Dialog(EditGroup.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.group_name_input_dialog);
				final EditText 	groupNameEdit 	= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
				Button groupNameOkButton 	= (Button) d.findViewById(R.id.group_name_dialog_name_ok_button);
				Button groupNameCancelButton= (Button) d.findViewById(R.id.group_name_dialog_name_cancel_button);
				
				groupNameEdit.setText(groupName);
				
				groupNameOkButton.setOnClickListener(new OnClickListener() {
					
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

					public void onClick(View v) {
						d.cancel();
					}
				});
				
				d.show();
			}
		});
		
		
		
		addContactsButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				FlurryAgent.logEvent("Add Contacts");
				Intent intent = new Intent(EditGroup.this, ContactsList.class);
				intent.putExtra("ORIGINATOR", "Group Edit Activity");
				intent.putExtra("GROUPID", groupId);
				ArrayList<String> idsString = new ArrayList<String>();
				for(int i = 0; i< ids.size(); i++){
					idsString.add(String.valueOf(ids.get(i)));
				}
				intent.putStringArrayListExtra("IDARRAY", idsString);
				intent.putStringArrayListExtra("NUMBERARRAY", numbers);
				startActivityForResult(intent, 1);
			}
		});

		saveGroupButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mdba.open();
				if(ids.size()>0){
					idsTemp = mdba.fetchIdsForGroups(groupId);
					for(int i = 0; i< idsTemp.size(); i++){
						mdba.removeContactFromGroup(idsTemp.get(i), groupId);
					}
					for(int i = 0; i< ids.size(); i++){
						mdba.addContactToGroup(ids.get(i), groupId, numbers.get(i));
					}
					mdba.setGroupName(groupName, groupId);
					mdba.close();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("Size", String.valueOf(ids.size()));
					FlurryAgent.logEvent("Group Saved", params);
					
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
	
	
	private ArrayList<GroupMember> organizeIds(ArrayList<Long> ids, ArrayList<String> numbers) {
		ArrayList<GroupMember> groupMembers = new ArrayList<EditGroup.GroupMember>();
		for(int i = 0; i< ids.size(); i++){
			boolean isPresent = false;
			for(int j=0; j< groupMembers.size(); j++){
				if(groupMembers.get(j).contactId == ids.get(i)){
					isPresent = true;
				}
			}
			if(!isPresent){
				GroupMember groupMember = new GroupMember(ids.get(i));
				
				ContactNumber cn = new ContactNumber(ids.get(i), numbers.get(i), getType(ids.get(i), numbers.get(i)));
				groupMember.numbers.add(cn);
				
				for(int k = i+1; k< ids.size(); k++){
					if(ids.get(i).equals(ids.get(k))){
						cn = new ContactNumber(ids.get(i), numbers.get(k), getType(ids.get(i), numbers.get(k)));
						groupMember.numbers.add(cn);
					}
				}
				groupMembers.add(groupMember);
			}
		}
		return groupMembers;
	}
	
	
	private String getType(long id, String number){
		for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
			if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == id){
				for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
					if(number.equals(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number)){
						return SmsSchedulerApplication.contactsList.get(i).numbers.get(j).type;
					}
				}
			}
		}
		return null;
	}


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

				public void onClick(View v) {
					mdba.open();
					ids = mdba.fetchIdsForGroups(groupId);
					for(int i = 0; i< ids.size(); i++){
						mdba.removeContactFromGroup(ids.get(i), groupId);
					}
					for(int i = 0; i< idsTemp.size(); i++){
						mdba.addContactToGroup(idsTemp.get(i), groupId, numbersTemp.get(i));
					}
					mdba.close();
					d.cancel();
					EditGroup.this.finish();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					d.cancel();
				}
			});
			
			d.show();
		}else{
			EditGroup.this.finish();
		}
	}
	
	
	protected void onResume() {
		super.onResume();
	}
	
	
	private void loadContactsForGroups(){
		newGroupContacts.clear();
		for(int j = 0; j< ids.size(); j++){
			for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
				if(ids.get(j)==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
					Contact Contact = new Contact();
					Contact.content_uri_id = SmsSchedulerApplication.contactsList.get(i).content_uri_id;
					Contact.name = SmsSchedulerApplication.contactsList.get(i).name;
					Contact.numbers.add(new ContactNumber(SmsSchedulerApplication.contactsList.get(i).numbers.get(0).contactId, SmsSchedulerApplication.contactsList.get(i).numbers.get(0).number, SmsSchedulerApplication.contactsList.get(i).numbers.get(0).type));//TODO
					newGroupContacts.add(Contact);
				}
			}
		}
	}
	
	
	private class MyAdapter extends ArrayAdapter<GroupMember>{
		MyAdapter(){
    		super(EditGroup.this, R.layout.edit_group_list_row, groupMembers);
    	}
		
		public int getCount() {
			return groupMembers.size();
		}
		
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(position > groupMembers.size()-1){
				return null;
			}
			GroupsAddListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.edit_group_list_row, parent, false);
	    		holder = new GroupsAddListHolder();
	    		holder.contactImage 			= (ImageView) 	convertView.findViewById(R.id.group_add_edit_row_contact_pic);
	    		holder.contactName 				= (TextView) 	convertView.findViewById(R.id.group_add_edit_row_contact_name);
	    		holder.contactNumber 			= (TextView) 	convertView.findViewById(R.id.group_add_edit_row_contact_number);
	    		holder.contactRemoveButton 		= (ImageView) 	convertView.findViewById(R.id.group_add_edit_row_delete_button);
	    		holder.extraNumbersLayout 		= (LinearLayout) convertView.findViewById(R.id.extra_numbers_layout);
	    		convertView.setTag(holder);
			}else{
				holder = (GroupsAddListHolder) convertView.getTag();
			}
			
    		final int _position = position;
    		
    		displayImage.submitImage(holder.contactImage, groupMembers.get(position).contactId, EditGroup.this);
    		holder.contactName.setText(groupMembers.get(position).displayName);
    		holder.contactNumber.setText(groupMembers.get(position).numbers.get(0).type + ": " + groupMembers.get(position).numbers.get(0).number);//TODO
    		
    		if(groupMembers.get(position).numbers.size()>1){
    			holder.extraNumbersLayout.setVisibility(View.VISIBLE);
    			holder.extraNumbersLayout.removeAllViews();
    			holder.extraNumbersViews.clear();
    			ArrayList<ContactNumber> extraNumbers = new ArrayList<ContactNumber>();
        		for(int i=1; i< groupMembers.get(position).numbers.size(); i++){
        			extraNumbers.add(groupMembers.get(position).numbers.get(i));
        		}
        		for(int i = 0; i< extraNumbers.size(); i++){
        			View view = createView(extraNumbers.get(i), groupMembers.get(position), getLayoutInflater());
        			holder.extraNumbersViews.add(view);
        		}
        		for(int i = 0; i< holder.extraNumbersViews.size(); i++){
        			holder.extraNumbersLayout.addView(holder.extraNumbersViews.get(i));
        		}
    		}else{
    			holder.extraNumbersLayout.setVisibility(View.GONE);
    		}
    		
    		holder.contactRemoveButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					FlurryAgent.logEvent("Contact Removed From Group");
					
					if(ids.size()==1){
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

							public void onClick(View v) {
								d.cancel();
							}
						});
						
						d.show();
					}
					else{
						newGroupContacts.remove(_position);
						for(int i = 0; i< ids.size(); i++){
							if(ids.get(i)==groupMembers.get(_position).contactId && numbers.get(i).equals(groupMembers.get(_position).numbers.get(0).number)){
								ids.remove(i);
								numbers.remove(i);
								groupMembers = organizeIds(ids, numbers);
								Log.d("groupMembers size : " + groupMembers.size());
								break;
							}
						}
						MyAdapter.this.notifyDataSetChanged();
					}
				}
			});
    		
			return convertView;
		}
	}
	
	
	
	private View createView(final ContactNumber contactNumber, final GroupMember groupMember, LayoutInflater inflater){
		View view = inflater.inflate(R.layout.extra_numbers_list_row_del, null);
		
		TextView tv = (TextView) view.findViewById(R.id.extra_number);
		ImageView delButton = (ImageView) view.findViewById(R.id.extra_number_del_button);
		
		tv.setText(contactNumber.type +": "+contactNumber.number);
		
		delButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Contact Removed From Group");
				for(int i = 0; i< ids.size(); i++){
					if(ids.get(i)==contactNumber.contactId && numbers.get(i).equals(contactNumber.number)){
						groupMember.numbers.remove(contactNumber);
						if(groupMember.numbers.size()==0){
							groupMembers.remove(groupMember);
						}
						ids.remove(i);
						numbers.remove(i);
						groupMembers = organizeIds(ids, numbers);
						break;
					}
				}
				myAdapter.notifyDataSetChanged();
			}
		});
		
		return view;
	}
	
	
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		boolean isCancelled = data.getBooleanExtra("CANCEL", false);
		if(!isCancelled){
			ArrayList<String> idsString = new ArrayList<String>();
			idsString = data.getStringArrayListExtra("IDSLIST");
			ids.clear();
			numbers.clear();
			for(int i = 0; i< idsString.size(); i++){
				ids.add(Long.parseLong(idsString.get(i)));
			}
			numbers = data.getStringArrayListExtra("NUMBERSLIST");
			groupMembers = organizeIds(ids, numbers);
			loadContactsForGroups();
			myAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	private class GroupsAddListHolder{
		ImageView 	contactImage;
		TextView 	contactName;
		TextView 	contactNumber;
		ImageView 	contactRemoveButton;
		LinearLayout extraNumbersLayout;
		
		ArrayList<View> extraNumbersViews = new ArrayList<View>();
	}
	
	
	protected class GroupMember{
		long contactId;
		ArrayList<ContactNumber> numbers = new ArrayList<ContactNumber>();
		String displayName;
		
		GroupMember(long id){
			for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
				if(id == SmsSchedulerApplication.contactsList.get(i).content_uri_id){
					this.displayName = SmsSchedulerApplication.contactsList.get(i).name;
					this.contactId = SmsSchedulerApplication.contactsList.get(i).content_uri_id;
					break;
				}
			}
		}
	}
}