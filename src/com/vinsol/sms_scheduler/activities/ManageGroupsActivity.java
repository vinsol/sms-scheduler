package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;
import com.vinsol.sms_scheduler.activities.ManageTemplateActivity.MyAdapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManageGroupsActivity extends Activity {

	
	ImageButton okImageButton;
	ImageButton addGroupImageButton;
	TextView manageGroupsHeading;
	ListView groupsList;
	
	LinearLayout listLayout;
	LinearLayout blankLayout;
	
	
	DBAdapter mdba = new DBAdapter(this);
	Cursor cur;
	MyAdapter myAdapter;
	
	ArrayList<Long> grpIdsArray = new ArrayList<Long>();
	ArrayList<String> grpNamesArray = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_group_layout);
		
		addGroupImageButton = (ImageButton) findViewById(R.id.manage_group_add_group_image_button);
		manageGroupsHeading = (TextView) 	findViewById(R.id.manage_template_layout_heading);
		groupsList 			= (ListView) 	findViewById(R.id.group_manager_list);
		listLayout			= (LinearLayout) findViewById(R.id.group_manager_list_layout);
		blankLayout			= (LinearLayout) findViewById(R.id.group_manager_blank_layout);
		
		
		
		loadGroupsData();
		
		myAdapter = new MyAdapter();
		groupsList.setAdapter(myAdapter);
		
		addGroupImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ManageGroupsActivity.this, GroupEditActivity.class);
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
    		ManageGroupsListHolder holder;
    		if(convertView == null){
    			LayoutInflater inflater = getLayoutInflater();
        		convertView = inflater.inflate(R.layout.manage_goups_row_design, parent, false);
        		holder = new ManageGroupsListHolder();
        		holder.groupNameLabel = (TextView) convertView.findViewById(R.id.manage_groups_row_group_name);
        		holder.groupDeleteButton = (ImageView) convertView.findViewById(R.id.manage_groups_row_group_delete_image);
        		convertView.setTag(holder);
    		}else{
    			holder = (ManageGroupsListHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		
    		
    		
    		holder.groupNameLabel.setText(grpNamesArray.get(position));
    		
    		holder.groupDeleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog d = new Dialog(ManageGroupsActivity.this);
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
							mdba.removeGroup(grpIdsArray.get(position));
							mdba.close();
							grpIdsArray.remove(position);
							grpNamesArray.remove(position);
							notifyDataSetChanged();
							d.cancel();
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
//					ArrayList<Long> ids = new ArrayList<Long>(); 
//					mdba.open();
//					ids = mdba.fetchIdsForGroups(grpIdsArray.get(position));
//					mdba.close();
//					ArrayList<String> idsString = new ArrayList<String>();
//					for(int i = 0; i< ids.size(); i++){
//						idsString.add(String.valueOf(ids.get(i)));
//					}
					Intent intent = new Intent(ManageGroupsActivity.this, GroupEditActivity.class);
					intent.putExtra("STATE", "edit");
					intent.putExtra("GROUPNAME", grpNamesArray.get(position));
					intent.putExtra("GROUPID", grpIdsArray.get(position));
					//intent.putStringArrayListExtra("IDARRAY", idsString);
					startActivity(intent);
				}
			});
    		return convertView;
    	}
    }
	
	
	static class ManageGroupsListHolder{
		TextView groupNameLabel;
		ImageView groupDeleteButton;
	}
	
	
}
