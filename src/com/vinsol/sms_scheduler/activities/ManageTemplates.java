/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;

public class ManageTemplates extends Activity{

	private ImageView	 	newTemplateButton;
	private LinearLayout 	newTemplateSpaceLayout;
	private EditText 		newTemplateBody;
	private Button 			newTemplateAddButton;
	private Button 			newTemplateCancelButton;
	private ListView 		templatesList;
	private LinearLayout	listLayout;
	private LinearLayout	blankLayout;
	private Button			addATemplateButton;
	
	private MyAdapter mAdapter;
	private DBAdapter mdba = new DBAdapter(ManageTemplates.this);
	private ArrayList<String> 	templatesArray = new ArrayList<String>();
	private ArrayList<Long>		templatesIdArray = new ArrayList<Long>();
	
	private boolean isEditing = false;
	private int editRowId = 0;
	
	private InputMethodManager inputMethodManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_templates);
		
		newTemplateButton 			= (ImageView) 		findViewById(R.id.new_template_image_button);
		newTemplateSpaceLayout 		= (LinearLayout) 	findViewById(R.id.new_template_input_space);
		newTemplateBody 			= (EditText) 		findViewById(R.id.new_template_input_edit_text);
		newTemplateAddButton 		= (Button) 			findViewById(R.id.new_template_add_button);
		newTemplateCancelButton 	= (Button) 			findViewById(R.id.new_template_cancel_button);
		templatesList				= (ListView)		findViewById(R.id.template_manager_list);
		listLayout					= (LinearLayout) 	findViewById(R.id.list_layout);
		blankLayout					= (LinearLayout) 	findViewById(R.id.blank_layout);
		addATemplateButton			= (Button) 			findViewById(R.id.blank_list_add_template_button);
		
		loadData();
		
		inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mAdapter = new MyAdapter();
		templatesList.setAdapter(mAdapter);
		
		addATemplateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listLayout.setVisibility(LinearLayout.VISIBLE);
				blankLayout.setVisibility(LinearLayout.GONE);
				handleAddNewRequisites();
			}
		});
		
		
		newTemplateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleAddNewRequisites();
			}
		});
		
		
		newTemplateAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				inputMethodManager.hideSoftInputFromWindow(newTemplateBody.getWindowToken(), 0);
				if(newTemplateBody.getText().toString().equals("")){
					Toast.makeText(ManageTemplates.this, "Cannot add blank template", Toast.LENGTH_SHORT).show();
				}else if(isEditing && newTemplateBody.getText().toString().equals(templatesArray.get(editRowId))){
					newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
				}else{
					mdba.open();
					Cursor cur = mdba.fetchAllTemplates();
					boolean z = true;
					if(cur.moveToFirst()){
						do{
							if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)).equals(newTemplateBody.getText().toString())){
								z = false;
								break;
							}
						}while(cur.moveToNext());
					}
					if(!z){
						Toast.makeText(ManageTemplates.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}else{
						mdba.open();
						if(isEditing){
							mdba.editTemplate(templatesIdArray.get(editRowId), newTemplateBody.getText().toString());
						}else{
							long newId = mdba.addTemplate(newTemplateBody.getText().toString());
							templatesArray.add(newTemplateBody.getText().toString());
							templatesIdArray.add(newId);
						}
						mdba.close();
						loadData();
						mAdapter.notifyDataSetChanged();
						newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
						
						if(isEditing){
							Toast.makeText(ManageTemplates.this, "Template edited", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(ManageTemplates.this, "Template added", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
		
		
		
		newTemplateCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				inputMethodManager.hideSoftInputFromWindow(newTemplateBody.getWindowToken(), 0);
				newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
				loadData();
			}
		});
	}

	
	
	private void loadData(){
		mdba.open();
		Cursor cur = mdba.fetchAllTemplates();
		if(cur.getCount()==0){
			listLayout.setVisibility(LinearLayout.GONE);
			blankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			listLayout.setVisibility(LinearLayout.VISIBLE);
			blankLayout.setVisibility(LinearLayout.GONE);
			
			templatesArray.clear();
			templatesIdArray.clear();
			if(cur.moveToFirst()){
				do{
					templatesArray.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)));
					templatesIdArray.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TEMP_ID)));
				}while(cur.moveToNext());
			}
		}
		cur.close();
		mdba.close();
	}
	
	
	
	@SuppressWarnings("rawtypes")
	private class MyAdapter extends ArrayAdapter{
    	@SuppressWarnings("unchecked")
		MyAdapter(){
    		super(ManageTemplates.this, R.layout.manage_groups_list_row, templatesIdArray);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final TemplateViewHolder holder;
    		if(convertView==null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.manage_groups_list_row, parent, false);
    			holder = new TemplateViewHolder();
    			holder.templateBodyLabel = (TextView)convertView.findViewById(R.id.manage_groups_row_group_name);
    			holder.deleteTemplateButton = (ImageView)convertView.findViewById(R.id.manage_groups_row_group_delete_image);
    			convertView.setTag(holder);
    		}else{
    			holder = (TemplateViewHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		holder.templateBodyLabel.setText(templatesArray.get(position));
    		
    		holder.deleteTemplateButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog d = new Dialog(ManageTemplates.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Delete this Template?");
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mdba.open();
							mdba.removeTemplate(templatesIdArray.get(_position));
							mdba.close();
							loadData();
							mAdapter = new MyAdapter();
							templatesList.setAdapter(mAdapter);
							
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
					if(newTemplateSpaceLayout.getVisibility()==LinearLayout.GONE){
						inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
						newTemplateSpaceLayout.setVisibility(LinearLayout.VISIBLE);
						newTemplateBody.setText(holder.templateBodyLabel.getText().toString());
						newTemplateBody.requestFocus();
						newTemplateAddButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_footer_states));
						editRowId = position;
						isEditing = true;
					}
				}
			});
    		
    		return convertView;
    	}
    }
	
	
	private void handleAddNewRequisites(){
		if(newTemplateSpaceLayout.getVisibility()==LinearLayout.VISIBLE){
			newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
			inputMethodManager.hideSoftInputFromWindow(newTemplateBody.getWindowToken(), 0);
			templatesList.requestFocus();
		}else{
			newTemplateSpaceLayout.setVisibility(LinearLayout.VISIBLE);
			newTemplateBody.setText("");
			newTemplateBody.requestFocus();
			newTemplateAddButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_footer_states));
			isEditing = false;
			inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		}
	}
	
	
	private class TemplateViewHolder{
		TextView templateBodyLabel;
		ImageView deleteTemplateButton;
	}
}