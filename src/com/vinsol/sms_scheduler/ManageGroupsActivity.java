package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import com.vinsol.sms_scheduler.ManageTemplateActivity.MyAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ManageGroupsActivity extends Activity {

	
	ImageButton okImageButton;
	ImageButton addGroupImageButton;
	TextView manageGroupsHeading;
	ListView groupsList;
	
	
	DBAdapter mdba = new DBAdapter(this);
	Cursor cur;
	MyAdapter myAdapter;
	
	ArrayList<Long> grpIdsArray = new ArrayList<Long>();
	ArrayList<String> grpNamesArray = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_group_layout);
		
		okImageButton 		= (ImageButton) findViewById(R.id.manage_group_ok_image_button);
		addGroupImageButton = (ImageButton) findViewById(R.id.manage_group_add_group_image_button);
		manageGroupsHeading = (TextView) 	findViewById(R.id.manage_template_layout_heading);
		groupsList 			= (ListView) 	findViewById(R.id.group_manager_list);
		
		
		loadGroupsData();
		
		myAdapter = new MyAdapter();
		groupsList.setAdapter(myAdapter);
		
		okImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ManageGroupsActivity.this.finish();
			}
		});
		
		addGroupImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ManageGroupsActivity.this, GroupAddActivity.class);
				intent.putExtra("STATE", "new");
				startActivity(intent);
			}
		});
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		loadGroupsData();
		myAdapter.notifyDataSetChanged();
	}
	
	
	public void loadGroupsData(){
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
		Log.i("MSG", "Total Groups " + grpIdsArray.size());
	}
	
	
	class MyAdapter extends ArrayAdapter{
    	MyAdapter(){
    		super(ManageGroupsActivity.this, R.layout.manage_goups_row_design, grpIdsArray);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
//    		if(convertView!=null){
//    			return convertView;
//    		}
    		final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.manage_goups_row_design, parent, false);
    		
    		TextView groupNameLabel = (TextView) row.findViewById(R.id.manage_groups_row_group_name);
    		ImageView groupDeleteButton = (ImageView) row.findViewById(R.id.manage_groups_row_group_delete_image);
    		
    		groupNameLabel.setText(grpNamesArray.get(position));
    		
    		groupDeleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mdba.open();
					mdba.removeGroup(grpIdsArray.get(position));
					mdba.close();
					grpIdsArray.remove(position);
					grpNamesArray.remove(position);
					notifyDataSetChanged();
				}
			});
    		row.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					ArrayList<Long> ids = new ArrayList<Long>(); 
//					mdba.open();
//					ids = mdba.fetchIdsForGroups(grpIdsArray.get(position));
//					mdba.close();
//					ArrayList<String> idsString = new ArrayList<String>();
//					for(int i = 0; i< ids.size(); i++){
//						idsString.add(String.valueOf(ids.get(i)));
//					}
					Intent intent = new Intent(ManageGroupsActivity.this, GroupAddActivity.class);
					intent.putExtra("STATE", "edit");
					intent.putExtra("GROUPNAME", grpNamesArray.get(position));
					intent.putExtra("GROUPID", grpIdsArray.get(position));
					//intent.putStringArrayListExtra("IDARRAY", idsString);
					startActivity(intent);
				}
			});
    		return row;
    	}
    }
	
}
