package com.vinsol.sms_scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class EditSmsActivity extends Activity {

	//---------References to the widgets-----------------
	AutoCompleteTextView 	numbersText;
	ImageButton 			addFromContactsImgButton;
	Button 					dateButton;
	TextView 				characterCountText;
	EditText 				messageText;
	ImageButton 			smileyImageButton;
	ImageButton 			templateImageButton;
	ImageButton				spellCheckImageButton;
	ImageButton 			speechImageButton;
	ImageButton 			addTemplateImageButton;
	Button 					scheduleButton;
	Button 					cancelButton;
	LinearLayout			smileyLinearLayout;
	GridView				smileysGrid;
	//--------------------------------------------------------
	
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts = new ArrayList<String>();
	ArrayList<String> templatesArray = new ArrayList<String>();
	
	DBAdapter mdba = new DBAdapter(EditSmsActivity.this);
	
	Dialog dateSelectDialog;
	Dialog templateDialog;
	boolean suggestionsBoolean = true;
	
	Date refDate = new Date();
	Calendar refCal = new GregorianCalendar();
	Date processDate = new Date();
	
	boolean smileyVisible = false;
	
	SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
	
	int [] images = {R.drawable.icon, R.drawable.ic_btn_write_sms};
	String [] smileys = {":-) ", ":-( "};
	
	long editedGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_schedule_layout);
		
		Intent intent = getIntent();
		
		
		numbersText 				= (AutoCompleteTextView) 	findViewById(R.id.new_numbers_text);
		addFromContactsImgButton 	= (ImageButton) 		 	findViewById(R.id.new_add_from_contact_imgbutton);
		dateButton 					= (Button) 					findViewById(R.id.new_date_button);
		characterCountText 			= (TextView) 				findViewById(R.id.new_char_count_text);
		messageText 				= (EditText) 				findViewById(R.id.new_message_space);
		smileyImageButton 			= (ImageButton) 			findViewById(R.id.smiley_imgbutton);
		templateImageButton 		= (ImageButton) 			findViewById(R.id.template_imgbutton);
		spellCheckImageButton		= (ImageButton)				findViewById(R.id.spell_check_imgbutton);
		speechImageButton 			= (ImageButton) 			findViewById(R.id.speech_imgbutton);
		addTemplateImageButton 		= (ImageButton) 			findViewById(R.id.add_template_imgbutton);
		scheduleButton 				= (Button) 					findViewById(R.id.new_schedule_button);
		cancelButton 				= (Button) 					findViewById(R.id.new_cancel_button);
		smileyLinearLayout			= (LinearLayout) 			findViewById(R.id.smiley_layout);
		smileysGrid					= (GridView) 				findViewById(R.id.smileysGrid);
		
		
		numbersText.setText(intent.getStringExtra("NUMBER"));
		messageText.setText(intent.getStringExtra("MESSAGE"));
		processDate = new Date(intent.getLongExtra("TIME", 0));
		editedGroup = intent.getLongExtra("GROUP", 0);
		
		setFunctionalities();
		
		
	}
	
	
	
	public void setFunctionalities(){
		
		//------------Date Select Button set to current date--------------------
		Date currentDate = processDate;
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
		dateButton.setText(sdf.format(currentDate));
		processDate = currentDate; 
		
		dateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dateSelectDialog = new Dialog(EditSmsActivity.this);
				dateSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dateSelectDialog.setContentView(R.layout.date_input_dialog);
				
				final DatePicker datePicker   	= (DatePicker)  dateSelectDialog.findViewById(R.id.new_date_picker);
				final TimePicker timePicker   	= (TimePicker)  dateSelectDialog.findViewById(R.id.new_time_picker);
				final TextView dateLabel 		= (TextView) 	dateSelectDialog.findViewById(R.id.new_date_label);
				Button okDateButton 			= (Button) 		dateSelectDialog.findViewById(R.id.new_date_dialog_ok_button);
				Button cancelDateButton 		= (Button) 		dateSelectDialog.findViewById(R.id.new_date_dialog_cancel_button);
				
				//---Setting DatePicker value change listner--------
				
				timePicker.setCurrentHour(processDate.getHours());
				timePicker.setCurrentMinute(processDate.getMinutes());
				final int mYear = processDate.getYear() + 1900;
				final int mMonth = processDate.getMonth();
				final int mDay = processDate.getDate();
				datePicker.init(mYear, mMonth, mDay, new OnDateChangedListener() {
					
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						String temp = sdf.format(new Date(year-1900, monthOfYear, dayOfMonth, timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
						dateLabel.setText(temp);
						if(checkDateValidity(new Date(year-1900, monthOfYear, dayOfMonth, timePicker.getCurrentHour(), timePicker.getCurrentMinute()))){
							dateLabel.setBackgroundColor(Color.rgb(0, 180, 0));
						}else{
							dateLabel.setBackgroundColor(Color.rgb(180, 0, 0));
						}
					}
				});
				//---------------------------------------end of DatePicker setup------
				String temp = sdf.format(new Date(datePicker.getYear()-1900, datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
				dateLabel.setText(temp);
				refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				refDate = refCal.getTime();
//				String dateString = refDate.toString();
//				dateLabel.setText(dateString);
				if(checkDateValidity(refDate)){
					dateLabel.setBackgroundColor(Color.rgb(0, 180, 0));
				}else{
					dateLabel.setBackgroundColor(Color.rgb(180, 0, 0));
				}
				
				okDateButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
						refDate = refCal.getTime();
						String dateStr = refDate.toLocaleString();
						Log.i("MESSAGE", dateStr);
						if(checkDateValidity(refDate)){
							processDate = refDate;
							dateSelectDialog.cancel();
							String temp = sdf.format(new Date(processDate.getYear(), processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes()));
							dateButton.setText(temp);
						}else{
//							Toast.makeText(EditSmsActivity.this, "Invalid Date", Toast.LENGTH_SHORT).show();
//							dateLabel.setBackgroundColor(Color.rgb(180, 0, 0));
							processDate = refDate;
							dateSelectDialog.cancel();
							String temp = sdf.format(new Date(processDate.getYear(), processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes()));
							dateButton.setText(temp);
						}
					}
				});
				
				cancelDateButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dateSelectDialog.cancel();
					}
				});

				
				
				
				
				
				
				//---Setting TimePicker value change listner--------
				timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
					
					@Override
					public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
						String temp = sdf.format(new Date(datePicker.getYear()-1900, datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute));
						dateLabel.setText(temp);
						if(checkDateValidity(new Date(datePicker.getYear()-1900, datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute))){
							dateLabel.setBackgroundColor(Color.rgb(0, 180, 0));
						}else{
							dateLabel.setBackgroundColor(Color.rgb(180, 0, 0));
						}
					}
				});
				//--------------------------------------end of TimePicker setup-------
				
				
				
				dateSelectDialog.show();
			}
		});
		
		//-----------------------------------------------------------end of Date select setup---------
		
		
		
		
		//------------setting functionality of character count-------------------
		messageText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				int length 		= s.length();
				parts 		 	= smsManager.divideMessage(s.toString());
				characterCountText.setText(String.valueOf(length) + " (" + String.valueOf(parts.size()) + ")");
			}
		});
		
		
		
		
		//---------------functionality of smiley button-------------------------
		smileyImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(smileyVisible){
					smileyVisible = false;
					smileyLinearLayout.setVisibility(LinearLayout.GONE);
				}else{
					smileyVisible = true;
					smileyLinearLayout.setVisibility(LinearLayout.VISIBLE);
				}
			}
		});
		//------------------------------------------------end of smiley button func----------------
		
		
		
		//-------------------Setting up the smileys Grid---------------------------------
		smileysGrid.setAdapter(new SmileysAdapter(this));
		smileysGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int cursorPos = messageText.getSelectionStart();
				String beforeString = messageText.getText().toString().substring(0, cursorPos);
				String afterString = messageText.getText().toString().substring(cursorPos, messageText.length());
				if(cursorPos!=0){
					messageText.setText(beforeString + " " + smileys[position] + afterString);
					messageText.setSelection(cursorPos + 5);
				}else
					messageText.setText(smileys[position] + afterString);
					messageText.setSelection(cursorPos + 4);
				}
		
		});
		//-----------------------------------------------end of smiley Grid set up--------
		
		
		
		
		
		//---------------functionality of template button-----------------------
		templateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadTemplates();
				TemplateAdapter templateAdapter = new TemplateAdapter();
				templateDialog = new Dialog(EditSmsActivity.this);
				templateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				templateDialog.setContentView(R.layout.templates_dialog);
				ListView templateList = (ListView) templateDialog.findViewById(R.id.dialog_template_list);
				templateList.setAdapter(templateAdapter);
				templateDialog.show();
			}
		});
		//----------------------------------------end of template button functionality----------
		
		
		
		
		
		//-------------------functionality of add template button-------------------------------
		addTemplateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mdba.open();
				if(mdba.addTemplate(messageText.getText().toString()) > 0){
					Toast.makeText(EditSmsActivity.this, "Template added", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(EditSmsActivity.this, "Template couldn't be added", Toast.LENGTH_SHORT).show();
				}
				mdba.close();
			}
		});
		
		
		
		//-----------------functionality for spell check  button ---------------------------
		spellCheckImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(suggestionsBoolean){
					suggestionsBoolean = false;
					messageText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					//spellCheckImageButton.setImageResource(R.drawable.);
				}else{
					suggestionsBoolean = true;
					messageText = (EditText) findViewById(R.id.new_message_space);
					//spellCheckImageButton.setImageResource(R.drawable.);
				}
			}
		});
		
		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(numbersText.getText().toString().matches("(''|(' ')+)")){
					Toast.makeText(EditSmsActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
					numbersText.requestFocus();
				}else{
					if(!checkDateValidity(processDate)){
						Toast.makeText(EditSmsActivity.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
					}
					doSmsScheduling();
				}
			EditSmsActivity.this.finish();
			}
		});
	}
	
	
	
	//-------------------Adapter for list in the templates dialog--------------------
	class TemplateAdapter extends ArrayAdapter{
		TemplateAdapter(){
			super(EditSmsActivity.this, R.layout.template_list_row, templatesArray);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int _position = position;
			LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.template_list_row, parent, false);
    		TextView templateText = (TextView) row.findViewById(R.id.template_content_space);
    		templateText.setText(templatesArray.get(position));
    		row.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(messageText.getText().toString().equals("")){
						messageText.setText(templatesArray.get(_position));
					}else{
						messageText.setText(messageText.getText().toString() + "\n" + templatesArray.get(_position));
					}
					templateDialog.cancel();
				}
			});
    		
			return row;
		}
	}
	
	//------------------------------------------------------------------end of adapter--------------
	
	
	
	
	//-------------------Adapter for smileys Grid------------------------------------------
	public class SmileysAdapter extends BaseAdapter {
	    private Context mContext;

	    public SmileysAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return images.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//pos = position;
			ImageView imageView;
			if(convertView==null){
				imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
			}else{
				imageView = (ImageView) convertView;
			}
			
			imageView.setImageResource(images[position]);
			return imageView;
		}
	} // ...End of ImageAdapter...
	
	
	
	
	
	
	//------------------function to fetch template data from database----------------------------
	public void loadTemplates(){
		mdba.open();
		Cursor cur = mdba.fetchAllTemplates();
		mdba.close();
		
		templatesArray.clear();
		
		if(cur.moveToFirst()){
			Log.i("MESSAGE", "cursor has some records");
			do{
				templatesArray.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)));
			}while(cur.moveToNext());
		}
	}
	//----------------------------------------------------------------end of template data fetch-----
	
	
	
	//--------------------function to check date validity-------------------------------
	boolean checkDateValidity(Date date){
		Calendar cal = new GregorianCalendar(date.getYear() + 1900, date.getMonth(), date.getDate(), date.getHours(), date.getMinutes());
		if((cal.getTimeInMillis()-System.currentTimeMillis()) <= 0){
			return false;
		}else{
			return true;
		}
	}
	
	
	
	
	//--------------------function to Scheduling a new sms------------------------------------
	public void doSmsScheduling(){
		Log.i("MESSAGE", "into doScheduling");
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
		String dateString = sdf.format(cal.getTime());
		mdba.open();
		long groupId = mdba.getNextGroupId();
		String[] numbers = numbersText.getText().toString().split(",(' ')*");
		if(!(messageText.getText().toString().matches("(''|(' ')+)"))){
			ArrayList<Long> editedIds = mdba.getIds(editedGroup);
			for(int i = 0; i< editedIds.size(); i++){
				mdba.deleteSms(editedIds.get(i), EditSmsActivity.this);
			}
			
			for(int i = 0; i< numbers.length; i++){
				Log.i("MESSAGE", "processing for" + numbers[i]);
				long received_id = mdba.scheduleSms(numbers[i], messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
				if(mdba.getCurrentPiFiretime() == -1){
					Log.i("MESSAGE", "Step 1");
					handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
				}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
					Log.i("MESSAGE", "Step 1 alt");
					handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
				}
				Log.i("MESSAGE", "succesful");
			}
		}
		mdba.close();
	}
	
	
	public void handlePiUpdate(String number, long groupId, long id, long time){
		//Cancel the pi conditionally----------------------
		Log.i("MESSAGE", "Step 2");
		Cursor cur = mdba.getPiDetails();
		cur.moveToFirst();
		
		Intent intent = new Intent(EditSmsActivity.this, SMSHandleReceiver.class);
		intent.setAction(DBAdapter.PRIVATE_SMS_ACTION);
		
		PendingIntent pi;
		if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME))>0){
			intent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_SMS_ID)));
			intent.putExtra("NUMBER", " ");
			intent.putExtra("MESSAGE", " ");
			
			pi = PendingIntent.getBroadcast(EditSmsActivity.this, (int)cur.getLong(cur.getColumnIndex(DBAdapter.KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
			pi.cancel();
			
		}
		intent = new Intent(EditSmsActivity.this, SMSHandleReceiver.class);
	
		intent.setAction(DBAdapter.PRIVATE_SMS_ACTION);
		intent.putExtra("ID", id);
		intent.putExtra("NUMBER", number);
		intent.putExtra("MESSAGE", messageText.getText().toString());
		
		Random rand = new Random();
		int piNumber = rand.nextInt();
		pi = PendingIntent.getBroadcast(EditSmsActivity.this, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mdba.updatePi(piNumber, id, time);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    	alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
    	Toast.makeText(this.getApplicationContext(), "Message Scheduled", Toast.LENGTH_SHORT).show();
		
	}
}