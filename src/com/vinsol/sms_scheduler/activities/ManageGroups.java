/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;

public class ManageGroups extends Activity {

	private ImageView addGroupImageButton;
	private ListView groupsList;
	private Button blankListAddButton;
	
	private LinearLayout listLayout;
	private LinearLayout blankLayout;
	
	private DBAdapter mdba = new DBAdapter(this);
	private Cursor cur;
	private MyAdapter myAdapter;
	
	private ArrayList<Long> grpIdsArray = new ArrayList<Long>();
	private ArrayList<String> grpNamesArray = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_groups);
		
		addGroupImageButton = (ImageView) 	findViewById(R.id.manage_group_add_group_image_button);
		groupsList 			= (ListView) 	findViewById(R.id.group_manager_list);
		listLayout			= (LinearLayout)findViewById(R.id.group_manager_list_layout);
		blankLayout			= (LinearLayout)findViewById(R.id.group_manager_blank_layout);
		blankListAddButton 	= (Button) 		findViewById(R.id.blank_list_add_button);
		
		loadGroupsData();
		
		myAdapter = new MyAdapter();
		groupsList.setAdapter(myAdapter);
		

		blankListAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startNewGroupActivity();
			}
		});
		

		addGroupImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startNewGroupActivity();
			}
		});
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		mdba.open();
		if((cur = mdba.fetchAllGroups()).getCount()==0){
			cur.close();
			listLayout.setVisibility(LinearLayout.GONE);
			blankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			cur.close();
			listLayout.setVisibility(LinearLayout.VISIBLE);
			blankLayout.setVisibility(LinearLayout.GONE);
			loadGroupsData();
			myAdapter.notifyDataSetChanged();
		}
		mdba.close();
	}
	
	
	private void loadGroupsData(){
		grpIdsArray.clear();
		grpNamesArray.clear();
		
		mdba.open();
		cur = mdba.fetchAllGroups();
		
		if(cur.moveToFirst()){
			do{
				grpIdsArray.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
				grpNamesArray.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
			}while(cur.moveToNext());
		}
		cur.close();
		mdba.close();
	}
	
	
	@SuppressWarnings("rawtypes")
	private class MyAdapter extends ArrayAdapter{
    	@SuppressWarnings("unchecked")
		MyAdapter(){
    		super(ManageGroups.this, R.layout.manage_groups_list_row, grpIdsArray);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		ManageGroupsListHolder holder;
    		if(convertView == null){
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.manage_groups_list_row, parent, false);
        		holder = new ManageGroupsListHolder();
        		holder.groupNameLabel = (TextView) convertView.findViewById(R.id.manage_groups_row_group_name);
        		holder.groupDeleteButton = (ImageView) convertView.findViewById(R.id.manage_groups_row_group_delete_image);
        		convertView.setTag(holder);
    		}else{
    			holder = (ManageGroupsListHolder) convertView.getTag();
    		}
    		holder.groupNameLabel.setText(grpNamesArray.get(position));
    		
    		holder.groupDeleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog d = new Dialog(ManageGroups.this);
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
							mdba.removeGroup(grpIdsArray.get(position));
							mdba.close();
							grpIdsArray.remove(position);
							grpNamesArray.remove(position);
							notifyDataSetChanged();
							d.cancel();
							mdba.open();
							cur = mdba.fetchAllGroups();
							if(cur.getCount()==0){
								listLayout.setVisibility(LinearLayout.GONE);
								blankLayout.setVisibility(LinearLayout.VISIBLE);
							}
							mdba.close();
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
    		
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ManageGroups.this, EditGroup.class);
					intent.putExtra("GROUPNAME", grpNamesArray.get(position));
					intent.putExtra("GROUPID", grpIdsArray.get(position));
					startActivity(intent);
				}
			});
    		return convertView;
    	}
    }
	
	
	private void startNewGroupActivity(){
		Intent intent = new Intent(ManageGroups.this, ContactsList.class);
		intent.putExtra("ORIGINATOR", "Group Add Activity");
		startActivity(intent);
	}
	
	
	private class ManageGroupsListHolder{
		TextView groupNameLabel;
		ImageView groupDeleteButton;
	}
}