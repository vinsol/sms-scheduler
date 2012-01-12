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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

public class ContactsList extends Activity {

	
	private ListView contactsList;
	private Button doneButton;
	private Button cancelButton;
	
	private DBAdapter mdba;
	private long groupId;
	private boolean newCall;
	private String groupName = "";
	
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
	private ArrayList<Long> ids = new ArrayList<Long>();
	private ArrayList<Long> ids2 = new ArrayList<Long>();
	private ArrayList<String> idsString = new ArrayList<String>();
	
	private String callingActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_list);
		
		final DBAdapter mdba = new DBAdapter(this);
		contactsList = (ListView) findViewById(R.id.contacts_list_main_list);
		doneButton = (Button) findViewById(R.id.contacts_list_layout_done_button);
		cancelButton = (Button) findViewById(R.id.contacts_list_layout_cancel_button);
		
		Intent intent = getIntent();
		contacts = SmsSchedulerApplication.contactsList;
		callingActivity = intent.getStringExtra("ORIGINATOR");
		
		
			idsString.clear();
			idsString = intent.getStringArrayListExtra("IDARRAY");
			for(int i = 0; i< idsString.size(); i++){
				ids.add(Long.parseLong(idsString.get(i)));
				ids2.add(Long.parseLong(idsString.get(i)));
			}
			
			
			
			for(int i = 0; i< contacts.size(); i++){
				contacts.get(i).checked = false;
				for(int j = 0; j< ids.size(); j++){
					if(Long.parseLong(contacts.get(i).content_uri_id) == ids.get(j)){
						contacts.get(i).checked = true;
					}
				}
			}
			
			if(callingActivity.equals("Group Edit Activity")){
				doneButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_footer_states));
				groupId = intent.getLongExtra("GROUPID", 0);
			}else if(callingActivity.equals("Group Add Activity") || callingActivity.equals("Group Add Activity From Contacts")){
				newCall = intent.getBooleanExtra("NEWCALL", true);
			}
			Log.d("Contacts size : " + String.valueOf(contacts.size()));
		
		
		
		
		
		MyAdapter myAdapter = new MyAdapter();
		contactsList.setAdapter(myAdapter);
		
		doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				final Intent intent = new Intent();
				ArrayList<String> idsStringChanged = new ArrayList<String>();
				for(int i = 0; i< ids.size(); i++){
					idsStringChanged.add(String.valueOf(ids.get(i)));
				}
				if(callingActivity.equals("Group Add Activity")){
					intent.putExtra("NEWCALL", newCall);
					if(ids.size()==0){
						Toast.makeText(ContactsList.this, "Cannot create Group with no Contacts. Add few..", Toast.LENGTH_LONG).show();
					}else{
						if(groupName.equals("")){
							final Dialog d = new Dialog(ContactsList.this);
							d.requestWindowFeature(Window.FEATURE_NO_TITLE);
							d.setContentView(R.layout.group_name_input_dialog);
							//d.setCancelable(false);
							final EditText 	groupNameEdit 		= (EditText) 	d.findViewById(R.id.group_name_dialog_name_label);
							Button groupNameOkButton 	= (Button) d.findViewById(R.id.group_name_dialog_name_ok_button);
							Button groupNameCancelButton= (Button) d.findViewById(R.id.group_name_dialog_name_cancel_button);
							
							groupNameEdit.setText(groupName);
							
							groupNameOkButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									Log.d("GroupName : " + groupName);
									if(groupNameEdit.getText().toString().matches("(''|[' ']*)")){
										Toast.makeText(ContactsList.this, "Please enter the name for group", Toast.LENGTH_SHORT).show();
										groupNameEdit.setText("");
									}
									
									else{
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
												mdba.createGroup(groupName, ids);
												mdba.close();
												setResult(10, intent);
												ContactsList.this.finish();
											
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
						}else{
							if(ids.size() == 0){
								Toast.makeText(ContactsList.this, "No contacts selected, Please select some contacts", Toast.LENGTH_LONG).show();
							}else{
								mdba.open();
								mdba.createGroup(groupName, ids);
								mdba.close();
								setResult(10, intent);
								ContactsList.this.finish();
							}
						}
					}
					
					
					
				}else{
					intent.putStringArrayListExtra("IDSLIST", idsStringChanged);
					intent.putExtra("CANCEL", "no");
					setResult(10, intent);
					ContactsList.this.finish();
				}
				
			}
		});
		
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putStringArrayListExtra("IDSLIST", idsString);
				intent.putExtra("CANCEL", "back");
						
				if(callingActivity.equals("Group Add Activity")){
					intent.putExtra("NEWCALL", newCall);
				}
				setResult(10, intent);
				ContactsList.this.finish();
			}
		});
		
		
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putStringArrayListExtra("IDSLIST", idsString);
		intent.putExtra("CANCEL", "back");
		//intent.putExtra(name, value)
		setResult(10, intent);
		ContactsList.this.finish();
	}
	
	
	
	
	private class MyAdapter extends ArrayAdapter{
    	MyAdapter(){
    		super(ContactsList.this, R.layout.contacts_list_row, contacts);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final ContactsAddListHolder holder;
    		if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.contacts_list_row, parent, false);
        		holder = new ContactsAddListHolder();
        		holder.contactImage 	= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
        		holder.nameText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
        		holder.numberText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
        		holder.contactCheck 	= (CheckBox) 	convertView.findViewById(R.id.contact_list_row_contact_check);
        		convertView.setTag(holder);
    		} else {
    			holder = (ContactsAddListHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		holder.contactImage.setImageBitmap(contacts.get(position).image);
    		holder.nameText.setText(contacts.get(position).name);
    		holder.numberText.setText(contacts.get(position).number);
    		
    		holder.contactCheck.setChecked(contacts.get(position).checked);
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d("position : " + position);
					
					if(holder.contactCheck.isChecked()){
						ids.add(Long.parseLong(contacts.get(_position).content_uri_id));
						contacts.get(_position).checked = true;	
					}else{
						for(int i = 0; i< ids.size(); i++){
							if(ids.get(i) == Long.parseLong(contacts.get(_position).content_uri_id)){
								ids.remove(i);
								contacts.get(_position).checked = false;
							}
						}
					}
				}
			});
    		
//    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						Log.i("MSG", "position : " + position);
//						
//						if(isChecked){
//							ids.add(Long.parseLong(contacts.get(_position).content_uri_id));
//							contacts.get(_position).checked = true;	
//						}else{
//							for(int i = 0; i< ids.size(); i++){
//								if(ids.get(i) == Long.parseLong(contacts.get(_position).content_uri_id)){
//									ids.remove(i);
//									contacts.get(_position).checked = false;
//								}
//							}
//						}
//					MyAdapter.this.notifyDataSetChanged();
//				}
//			});
    		
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						ids.add(Long.parseLong(contacts.get(_position).content_uri_id));
						contacts.get(_position).checked = true;	
					}else{
						holder.contactCheck.setChecked(false);
						for(int i = 0; i< ids.size(); i++){
							if(ids.get(i) == Long.parseLong(contacts.get(_position).content_uri_id)){
								ids.remove(i);
								contacts.get(_position).checked = false;
							}
						}
					}
				}
			});
    		
    		return convertView;
    	}
    }
	
	
	
	
	private class ContactsAddListHolder{
		ImageView contactImage;
		TextView nameText;
		TextView numberText;
		CheckBox contactCheck;
	}
	
	
	
}
