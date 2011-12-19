package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;

import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.R.drawable;
import com.vinsol.sms_scheduler.R.id;
import com.vinsol.sms_scheduler.R.layout;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManageTemplateActivity extends Activity{

	ImageView	 	newTemplateButton;
	LinearLayout 	newTemplateSpaceLayout;
	EditText 		newTemplateBody;
	Button 			newTemplateAddButton;
	Button 			newTemplateCancelButton;
	ListView 		templatesList;
	
	MyAdapter mAdapter;
	DBAdapter mdba = new DBAdapter(ManageTemplateActivity.this);
	ArrayList<String> 	templatesArray = new ArrayList<String>();
	ArrayList<Long>		templatesIdArray = new ArrayList<Long>();
	
	boolean isEditing = false;
	int editRowId = 0;
	
	InputMethodManager inputMethodManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_template_layout);
		
		newTemplateButton 			= (ImageView) 		findViewById(R.id.new_template_image_button);
		newTemplateSpaceLayout 		= (LinearLayout) 	findViewById(R.id.new_template_input_space);
		newTemplateBody 			= (EditText) 		findViewById(R.id.new_template_input_edit_text);
		newTemplateAddButton 		= (Button) 			findViewById(R.id.new_template_add_button);
		newTemplateCancelButton 	= (Button) 			findViewById(R.id.new_template_cancel_button);
		templatesList				= (ListView)		findViewById(R.id.template_manager_list);
		
		loadData();
		
		inputMethodManager =(InputMethodManager)getSystemService(ManageTemplateActivity.this.INPUT_METHOD_SERVICE);
		
		mAdapter = new MyAdapter();
		templatesList.setAdapter(mAdapter);
		
		
		
		newTemplateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(newTemplateSpaceLayout.getVisibility()==LinearLayout.VISIBLE){
					newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
					inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
					templatesList.requestFocus();
				}else{
					newTemplateSpaceLayout.setVisibility(LinearLayout.VISIBLE);
					newTemplateBody.setText("");
					newTemplateBody.requestFocus();
					newTemplateAddButton.setText("Add");
					isEditing = false;
					inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
					
					
				}
			}
		});
		
		
		newTemplateAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
				if(newTemplateBody.getText().toString().equals("")){
					Toast.makeText(ManageTemplateActivity.this, "Cannot add blank template", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(ManageTemplateActivity.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}else{
						
						if(isEditing){
							mdba.editTemplate(templatesIdArray.get(editRowId), newTemplateBody.getText().toString());
						}else{
							long newId = mdba.addTemplate(newTemplateBody.getText().toString());
							templatesArray.add(newTemplateBody.getText().toString());
							templatesIdArray.add(newId);
						}
						
						
						loadData();
						mAdapter.notifyDataSetChanged();
						newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
						
						
						if(isEditing){
							Toast.makeText(ManageTemplateActivity.this, "Template edited", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(ManageTemplateActivity.this, "Template added", Toast.LENGTH_SHORT).show();
						}
						
					}
					mdba.close();
				}
			}
		});
		
		
		
		newTemplateCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(inputMethodManager.isActive()){
					inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				}
				newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
			}
		});
		
		
	}
	
	
	
	
	public void loadData(){
		mdba.open();
		Cursor cur = mdba.fetchAllTemplates();
		mdba.close();
		
		templatesArray.clear();
		templatesIdArray.clear();
		if(cur.moveToFirst()){
			do{
				Log.i("MSG", cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)));
				templatesArray.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)));
				templatesIdArray.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TEMP_ID)));
			}while(cur.moveToNext());
		}
	}
	
	
	
	class MyAdapter extends ArrayAdapter{
    	MyAdapter(){
    		super(ManageTemplateActivity.this, R.layout.template_manage_row, templatesIdArray);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final TemplateViewHolder holder;
    		if(convertView==null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.template_manage_row, parent, false);
    			holder = new TemplateViewHolder();
    			holder.templateBodyLabel = (TextView)convertView.findViewById(R.id.template_manage_row_body);
    			holder.deleteTemplateButton = (ImageView)convertView.findViewById(R.id.template_manage_row_delete_button);
    			convertView.setTag(holder);
    		}else{
    			holder = (TemplateViewHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		holder.templateBodyLabel.setText(templatesArray.get(position));
    		
    		
    		holder.deleteTemplateButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog d = new Dialog(ManageTemplateActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
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
						newTemplateSpaceLayout.setVisibility(LinearLayout.VISIBLE);
						newTemplateBody.setText(holder.templateBodyLabel.getText().toString());
						newTemplateBody.requestFocus();
						newTemplateAddButton.setText("Edit");
						editRowId = position;
						isEditing = true;
						inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
					}
				}
			});
    		
    		return convertView;
    	}
    }
	
	
	
	
	static class TemplateViewHolder{
		TextView templateBodyLabel;
		ImageView deleteTemplateButton;
	}
}
