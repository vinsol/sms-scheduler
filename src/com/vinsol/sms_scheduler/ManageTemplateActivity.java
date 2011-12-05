package com.vinsol.sms_scheduler;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

	ImageButton 	newTemplateButton;
	LinearLayout 	newTemplateSpaceLayout;
	EditText 		newTemplateBody;
	Button 			newTemplateAddButton;
	Button 			newTemplateCancelButton;
	ListView 		templatesList;
	
	MyAdapter mAdapter;
	DBAdapter mdba = new DBAdapter(ManageTemplateActivity.this);
	ArrayList<String> 	templatesArray = new ArrayList<String>();
	ArrayList<Long>		templatesIdArray = new ArrayList<Long>();
	
	
	InputMethodManager inputMethodManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_template_layout);
		
		newTemplateButton 			= (ImageButton) 	findViewById(R.id.new_template_image_button);
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
						mdba.open();
						long newId = mdba.addTemplate(newTemplateBody.getText().toString());
						mdba.close();
						templatesArray.add(newTemplateBody.getText().toString());
						templatesIdArray.add(newId);
//						loadData();
						mAdapter.notifyDataSetChanged();
						newTemplateSpaceLayout.setVisibility(LinearLayout.GONE);
						Toast.makeText(ManageTemplateActivity.this, "Template added", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		
		
		newTemplateCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
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
    	public View getView(int position, View convertView, ViewGroup parent) {
//    		if(convertView!=null){
//    			return convertView;
//    		}
    		final int _position  = position;
    		LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.template_manage_row, parent, false);
    		
    		Log.i("MESSAGE", _position + "yo");
    		
    		TextView templateBodyLabel = (TextView)row.findViewById(R.id.template_manage_row_body);
    		templateBodyLabel.setText(templatesArray.get(position));
    		
    		ImageView deleteTemplateButton = (ImageView)row.findViewById(R.id.template_manage_row_delete_button);
    		deleteTemplateButton.setImageResource(R.drawable.icon);
    		deleteTemplateButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("MSG", templatesIdArray.get(_position) + "");
					Log.i("MSG", _position + "");
					mdba.open();
					mdba.removeTemplate(templatesIdArray.get(_position));
					mdba.close();
					loadData();
					mAdapter = new MyAdapter();
					templatesList.setAdapter(mAdapter);
				}
			});
    		return row;
    	}
    }
}
