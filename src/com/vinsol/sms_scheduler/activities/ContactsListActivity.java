package com.vinsol.sms_scheduler.activities;

import java.io.InputStream;
import java.util.ArrayList;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;
import com.vinsol.sms_scheduler.models.MyContact;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsListActivity extends Activity {

	
	ListView contactsList;
	Button doneButton;
	Button cancelButton;
	
	DBAdapter mdba;
	long groupId;
	boolean newCall;
	String groupName = "";
	
	ArrayList<MyContact> contacts = new ArrayList<MyContact>();
	ArrayList<Long> ids = new ArrayList<Long>();
	ArrayList<Long> ids2 = new ArrayList<Long>();
	ArrayList<String> idsString = new ArrayList<String>();
	
	String callingActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_list_layout);
		
		final DBAdapter mdba = new DBAdapter(this);
		contactsList = (ListView) findViewById(R.id.contacts_list_main_list);
		doneButton = (Button) findViewById(R.id.contacts_list_layout_done_button);
		cancelButton = (Button) findViewById(R.id.contacts_list_layout_cancel_button);
		
		Intent intent = getIntent();
		contacts = SmsApplicationLevelData.contactsList;
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
				groupId = intent.getLongExtra("GROUPID", 0);
			}else if(callingActivity.equals("Group Add Activity")){
				newCall = intent.getBooleanExtra("NEWCALL", true);
			}
			Log.i("MSG", "Contacts size : " + String.valueOf(contacts.size()));
		
		
		
		
		
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
					final Dialog d = new Dialog(ContactsListActivity.this);
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
								Toast.makeText(ContactsListActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(ContactsListActivity.this, "Group Name Exists. Try another", Toast.LENGTH_SHORT).show();
								}else{
									groupName = groupNameEdit.getText().toString();
									d.cancel();
									mdba.open();
									mdba.createGroup(groupName, ids);
									mdba.close();
									setResult(10, intent);
									ContactsListActivity.this.finish();
								}
							}
						}
					});
					d.show();
				}else{
					intent.putStringArrayListExtra("IDSLIST", idsStringChanged);
					intent.putExtra("CANCEL", "no");
					setResult(10, intent);
					ContactsListActivity.this.finish();
				}
				
			}
		});
		
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putStringArrayListExtra("IDSLIST", idsString);
				intent.putExtra("CANCEL", "yes");
						
				if(callingActivity.equals("Group Add Activity")){
					intent.putExtra("NEWCALL", newCall);
				}
				setResult(10, intent);
				ContactsListActivity.this.finish();
			}
		});
		
		
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putStringArrayListExtra("IDSLIST", idsString);
		intent.putExtra("CANCEL", "yes");
		//intent.putExtra(name, value)
		setResult(10, intent);
		ContactsListActivity.this.finish();
	}
	
	
	
	
	class MyAdapter extends ArrayAdapter{
    	MyAdapter(){
    		super(ContactsListActivity.this, R.layout.contacts_list_row_design, contacts);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
//    		if(convertView!=null){
//    			return convertView;
//    		}
    		final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.contacts_list_row_design, parent, false);
    		
    		ImageView contactImage 	= (ImageView) 	row.findViewById(R.id.contact_list_row_contact_pic);
    		TextView nameText 		= (TextView) 	row.findViewById(R.id.contact_list_row_contact_name);
    		TextView numberText 	= (TextView) 	row.findViewById(R.id.contact_list_row_contact_number);
    		CheckBox contactCheck 	= (CheckBox) 	row.findViewById(R.id.contact_list_row_contact_check);
    		
    		contactImage.setImageBitmap(contacts.get(position).image);
    		nameText.setText(contacts.get(position).name);
    		numberText.setText(contacts.get(position).number);
    		
    		contactCheck.setChecked(contacts.get(position).checked);
    		contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
						if(isChecked){
//							if(callingActivity.equals("Group Edit Activity")){
//								mdba.open();
//								mdba.addContactToGroup(Long.parseLong(contacts.get(_position).content_uri_id), groupId);
//								mdba.close();
//							}
							ids.add(Long.parseLong(contacts.get(_position).content_uri_id));
							contacts.get(_position).checked = true;	
						}else{
							for(int i = 0; i< ids.size(); i++){
								if(ids.get(i) == Long.parseLong(contacts.get(_position).content_uri_id)){
//									if(callingActivity.equals("Group Edit Activity")){
//										mdba.open();
//										mdba.removeContactFromGroup(Long.parseLong(contacts.get(_position).content_uri_id), groupId);
//										mdba.close();
//									}
									ids.remove(i);
									contacts.get(_position).checked = false;
								}
							}
						}
					
					
					MyAdapter.this.notifyDataSetChanged();
				}
			});
    		
    		return row;
    	}
    }
}
