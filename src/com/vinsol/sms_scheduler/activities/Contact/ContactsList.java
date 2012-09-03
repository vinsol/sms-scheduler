/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities.Contact;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.activities.Home;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.ContactNumber;
import com.vinsol.sms_scheduler.utils.DisplayImage;
import com.vinsol.sms_scheduler.utils.MyGson;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;
import com.vinsol.sms_scheduler.Constants;

public class ContactsList extends Activity {

	private ListView contactsList;
	private Button doneButton;
	private Button cancelButton;
	
	private String groupName = "";
	
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
	private ArrayList<Long> ids = new ArrayList<Long>();
	private ArrayList<String> numbers = new ArrayList<String>();
	private ArrayList<Long> idsTemp = new ArrayList<Long>();
	private ArrayList<String> numbersTemp = new ArrayList<String>();
	private ArrayList<String> idsString = new ArrayList<String>();
	
	private String callingActivity;
	
	private ContactListAdapter contactListAdapter;
	
	private DisplayImage displayImage = new DisplayImage();
	
	
	@Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    	MyGson myGson = new MyGson();
    	SharedPreferences contactData = getSharedPreferences(Home.PREFS_NAME, 0);
    	
    	//if Contacts have been modified from the Native Contact app, the Contacts List is to be reloaded, serialized and saved in the shared prefs.
		if(contactData.getString("isChanged", "1").equals("1") || SmsSchedulerApplication.contactsList.size()==0){
			String data = contactData.getString("Data", "default");
			contacts = SmsSchedulerApplication.contactsList = myGson.deserializer(data);
		}
		SharedPreferences.Editor editor = contactData.edit();
	    editor.putString("isChanged", "0");
	    editor.commit();
	    contactListAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_list);
		
		final DBAdapter mdba = new DBAdapter(this);
		contactsList = (ListView)	findViewById(R.id.contacts_list_main_list);
		doneButton 	 = (Button) 	findViewById(R.id.contacts_list_layout_done_button);
		cancelButton = (Button) 	findViewById(R.id.contacts_list_layout_cancel_button);

		//This Activity can be called for two purposes, to create a Group or edit a Group. We have to treat these two situations differently. For that purpose, we are supplied with an Intent Extra called "ORIGINATOR".
		Intent intent = getIntent();
		contacts = SmsSchedulerApplication.contactsList;
		callingActivity = intent.getStringExtra("ORIGINATOR");
		
		
		if(callingActivity.equals("Group Edit Activity")){
			//case: called for Edit, we must back up the original values. So, we make a clone of values and use them to prepare the initial state of the list.
			idsString.clear();
			idsString = intent.getStringArrayListExtra("IDARRAY");
			numbers = intent.getStringArrayListExtra("NUMBERARRAY");
			for(int i = 0; i< numbers.size(); i++){
				numbersTemp.add(numbers.get(i));
			}
			for(int i = 0; i< idsString.size(); i++){
				ids.add(Long.parseLong(idsString.get(i)));
				idsTemp.add(Long.parseLong(idsString.get(i)));
			}
		
			for(int i = 0; i< contacts.size(); i++){
				contacts.get(i).checked = false;
				for(int j = 0; j< ids.size(); j++){
					if(contacts.get(i).content_uri_id == ids.get(j) && contacts.get(i).numbers.get(0).number.equals(numbers.get(j))){
						contacts.get(i).checked = true;
					}
				}
			}
			
			doneButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_footer_states));
			intent.getLongExtra("GROUPID", 0);
			
		}else if(callingActivity.equals("Group Add Activity")){
			//case: called for creating New Group.
			intent.getBooleanExtra("NEWCALL", true);
			for(int i = 0; i< contacts.size(); i++){
				contacts.get(i).checked = false;
			}
		}
		
		contactListAdapter = new ContactListAdapter();
		contactsList.setAdapter(contactListAdapter);
		
		doneButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				final Intent intent = new Intent();
				ArrayList<String> idsStringChanged = new ArrayList<String>();
				for(int i = 0; i< ids.size(); i++){
					idsStringChanged.add(String.valueOf(ids.get(i)));
				}
				if(callingActivity.equals("Group Add Activity")){
					if(ids.size()==0){
						Toast.makeText(ContactsList.this, "Cannot create Group with no Contacts. Add few..", Toast.LENGTH_LONG).show();
					}else{
						if(groupName.equals("")){
							//cannot create group with a blank name.
							final Dialog d = new Dialog(ContactsList.this);
							d.requestWindowFeature(Window.FEATURE_NO_TITLE);
							d.setContentView(R.layout.group_name_input_dialog);
							final EditText 	groupNameEdit 		= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
							Button groupNameOkButton 	= (Button) d.findViewById(R.id.group_name_dialog_name_ok_button);
							Button groupNameCancelButton= (Button) d.findViewById(R.id.group_name_dialog_name_cancel_button);
							
							groupNameEdit.setText(groupName);
							
							groupNameOkButton.setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									if(groupNameEdit.getText().toString().matches(Constants.BLANK_OR_ONLY_SPACES_PATTERN)){
										Toast.makeText(ContactsList.this, "Please enter a valid name for group", Toast.LENGTH_SHORT).show();
										groupNameEdit.setText("");
									}else{
										boolean groupNameExists = false;
										mdba.open();
										Cursor cur = mdba.fetchAllGroups();
										if(cur.moveToFirst()){
											do{
												if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_GROUP_NAME)).equals(groupNameEdit.getText().toString())){
													groupNameExists = true;
													break;
												}
											}while(cur.moveToNext());
										}
										mdba.close();
										if(groupNameExists){
											Toast.makeText(ContactsList.this, "Group name already exists", Toast.LENGTH_SHORT).show();
										}else{
											d.cancel();
											groupName = groupNameEdit.getText().toString();
											
											mdba.open();
											mdba.createGroup(groupName, ids, numbers);
											mdba.close();
											ContactsList.this.finish();
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
						}else{
							if(ids.size() == 0){
								//case: if no Contact have been selected to include in the Group, can't create the Group.
								Toast.makeText(ContactsList.this, "No contacts selected, Please select some contacts", Toast.LENGTH_LONG).show();
							}else{
								mdba.open();
								mdba.createGroup(groupName, ids, numbers);
								mdba.close();
								
								HashMap<String, String> params = new HashMap<String, String>();
								params.put("Size", String.valueOf(ids.size()));
								FlurryAgent.logEvent("Group Saved", params);
								
								setResult(10, intent);
								ContactsList.this.finish();
							}
						}
					}
				}else{
					intent.putStringArrayListExtra("IDSLIST", idsStringChanged);
					intent.putStringArrayListExtra("NUMBERSLIST", numbers);
					intent.putExtra("CANCEL", false);
					setResult(10, intent);
					ContactsList.this.finish();
				}
			}
		});
		
		
		cancelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(callingActivity.equals("Group Edit Activity")){
					handleBackForEdit();
				}
				
				FlurryAgent.logEvent("New Group Cancelled");
				
				ContactsList.this.finish();
			}
		});
	}
	
	
	
	public void onBackPressed() {
		if(callingActivity.equals("Group Edit Activity")){
			handleBackForEdit();
		}
		ContactsList.this.finish();
	}
	
	public void handleBackForEdit(){
		Intent intent = new Intent();
		intent.putStringArrayListExtra("IDSLIST", idsString);
		intent.putExtra("CANCEL", true);
		setResult(10, intent);
	}
	
	
	
	@SuppressWarnings("rawtypes")
	private class ContactListAdapter extends ArrayAdapter{
    	@SuppressWarnings("unchecked")
		ContactListAdapter(){
    		super(ContactsList.this, R.layout.contacts_list_row, contacts);
    	}
    	
    	@Override
    	public int getCount() {
    		return contacts.size();
    	}
    	
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final ContactsAddListHolder holder;
    		if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
        		holder = new ContactsAddListHolder();
        		holder.contactImage 		= (ImageView) 		convertView.findViewById(R.id.contact_list_row_contact_pic);
        		holder.nameText 			= (TextView) 		convertView.findViewById(R.id.contact_list_row_contact_name);
        		holder.numberText 			= (TextView) 		convertView.findViewById(R.id.contact_list_row_contact_number);
        		holder.contactCheck 		= (CheckBox) 		convertView.findViewById(R.id.contact_list_row_contact_check);
        		holder.primaryNumberLayout 	= (RelativeLayout) 	convertView.findViewById(R.id.contact_list_primary_contact_space);
        		
        		convertView.setTag(holder);
    		} else {
    			holder = (ContactsAddListHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		holder.extraNumbersLayout = (LinearLayout) convertView.findViewById(R.id.extra_numbers_layout);
    		holder.extraNumbersViews = new ArrayList<View>();
    		
    		displayImage.submitImage(holder.contactImage, contacts.get(position).content_uri_id, ContactsList.this);
    		holder.nameText.setText(contacts.get(position).name);
    		holder.numberText.setText(contacts.get(position).numbers.get(0).type + ": " + contacts.get(position).numbers.get(0).number);//TODO
    		holder.contactCheck.setChecked(contacts.get(position).checked);
    		
    		if(contacts.get(position).numbers.size()>1){
    			holder.extraNumbersLayout.setVisibility(View.VISIBLE);
    			holder.extraNumbersLayout.removeAllViews();
    			holder.extraNumbersViews.clear();
    			ArrayList<ContactNumber> extraNumbers = new ArrayList<ContactNumber>();
    			
    			//Load all the Extra Numbers into a data structure.
        		for(int i=1; i< contacts.get(position).numbers.size(); i++){
        			extraNumbers.add(contacts.get(position).numbers.get(i));
        		}
        		
        		//Create an Extra Number View for each extra number and store them in an ArrayList<View>
        		for(int i = 0; i< extraNumbers.size(); i++){
        			View view = createView(extraNumbers.get(i), contacts.get(position), getLayoutInflater());
        			holder.extraNumbersViews.add(view);
        			
        		}
        		
        		//Add each View into the Extra Numbers List
        		for(int i = 0; i< holder.extraNumbersViews.size(); i++){
        			holder.extraNumbersLayout.addView(holder.extraNumbersViews.get(i));
        		}
    		}else{
    			holder.extraNumbersLayout.setVisibility(View.GONE);
    		}
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					if(holder.contactCheck.isChecked()){
						ids.add(contacts.get(_position).content_uri_id);
						numbers.add(contacts.get(_position).numbers.get(0).number);
						contacts.get(_position).checked = true;	
					}else{
						for(int i = 0; i< ids.size(); i++){
							if(ids.get(i) == contacts.get(_position).content_uri_id && numbers.get(i).equals(contacts.get(_position).numbers.get(0).number)){
								ids.remove(i);
								numbers.remove(i);
								contacts.get(_position).checked = false;
							}
						}
					}
				}
			});
    		
    		
    		holder.primaryNumberLayout.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						ids.add(contacts.get(_position).content_uri_id);
						numbers.add(contacts.get(_position).numbers.get(0).number);
						contacts.get(_position).checked = true;	
					}else{
						holder.contactCheck.setChecked(false);
						for(int i = 0; i< ids.size(); i++){
							if(ids.get(i) == contacts.get(_position).content_uri_id && numbers.get(i).equals(contacts.get(_position).numbers.get(0).number)){
								ids.remove(i);
								numbers.remove(i);
								contacts.get(_position).checked = false;
							}
						}
					}
				}
			});
    		
    		return convertView;
    	}
    }
	
	
	
	/**
	 * @details Creates an Extra Number View for a given ContactNumber object.
	 * @param contactNumber
	 * @param contact
	 * @param inflater
	 * @return a fully functional Extra Number View with listeners.
	 */
	public View createView(final ContactNumber contactNumber, final Contact contact, LayoutInflater inflater){
		View view = inflater.inflate(R.layout.extra_numbers_list_row, null);
		
		TextView tv = (TextView) view.findViewById(R.id.extra_number);
		final CheckBox cb = (CheckBox) view.findViewById(R.id.extra_number_checkbox);
		
		tv.setText(contactNumber.type + ": " + contactNumber.number);
		
		boolean hasEntry = false;
		for(int i = 0; i< ids.size(); i++){
			if(ids.get(i)==contact.content_uri_id && contactNumber.number.equals(numbers.get(i))){
				hasEntry = true;
				cb.setChecked(true);
			}
		}
		if(!hasEntry){
			cb.setChecked(false);
		}
		
		view.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(!cb.isChecked()){
					cb.setChecked(true);
					ids.add(contact.content_uri_id);
					numbers.add(contactNumber.number);
				}else{
					cb.setChecked(false);
					for(int i = 0; i< ids.size(); i++){
						if(ids.get(i) == contact.content_uri_id && numbers.get(i).equals(contactNumber.number)){
							ids.remove(i);
							numbers.remove(i);
						}
					}
				}
			}
		});
		
		cb.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(cb.isChecked()){
					ids.add(contact.content_uri_id);
					numbers.add(contactNumber.number);
				}else{
					for(int i = 0; i< ids.size(); i++){
						if(ids.get(i) == contact.content_uri_id && numbers.get(i).equals(contactNumber.number)){
							ids.remove(i);
							numbers.remove(i);
						}
					}
				}
			}
		});
		
		return view;
	}
	
	
	
	
	private class ContactsAddListHolder{
		ImageView contactImage;
		TextView nameText;
		TextView numberText;
		CheckBox contactCheck;
		LinearLayout extraNumbersLayout;
		RelativeLayout primaryNumberLayout;
		ArrayList<View> extraNumbersViews;
	}
}