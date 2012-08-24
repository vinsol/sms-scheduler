package com.vinsol.sms_scheduler.activities.Template;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.R;

@SuppressWarnings("rawtypes")
public class TemplateAdapter extends ArrayAdapter {
	
	ArrayList<String> templatesArray;
	EditText messageText;
	Dialog templateDialog;
	Context context;
	
	@SuppressWarnings("unchecked")
	public TemplateAdapter(ArrayList<String> templatesArray, EditText messageText, Dialog templateDialog, Context context) {
		super(context, R.layout.template_list_row, templatesArray);
		this.templatesArray = templatesArray;
		this.messageText = messageText;
		this.templateDialog = templateDialog;
		this.context = context;
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TemplateHolder holder;
		if(convertView==null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(R.layout.template_list_row, parent, false);
			holder = new TemplateHolder();
			holder.templateText = (TextView) convertView.findViewById(R.id.template_content_space);
			convertView.setTag(holder);
		}else{
			holder = (TemplateHolder) convertView.getTag();
		}
		final int _position = position;
	
		holder.templateText.setText(templatesArray.get(position));
		convertView.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Template Used");
				if(messageText.getText().toString().equals("")){
					messageText.setText(templatesArray.get(_position));
				}else{
					messageText.setText(messageText.getText().toString() + "\n" + templatesArray.get(_position));
				}
				messageText.setSelection(messageText.getText().toString().length());
				templateDialog.dismiss();
				messageText.requestFocus();
				messageText.setSelection(messageText.getText().toString().length());
			}
		});
		
		return convertView;
	}
}



//--------------------------------------
//Holder for Template Adapter
//--------------------------------------
class TemplateHolder{
	TextView templateText;
}