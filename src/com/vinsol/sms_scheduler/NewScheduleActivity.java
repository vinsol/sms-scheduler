package com.vinsol.sms_scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class NewScheduleActivity extends Activity {
	
	//---------References to the widgets-----------------
	AutoCompleteTextView 	numbersText;
	ImageButton 			addFromContactsImgButton;
	Button 					dateButton;
	TextView 				characterCountText;
	EditText 				messageText;
	ImageButton 			templateImageButton;
	ImageButton 			speechImageButton;
	ImageButton 			addTemplateImageButton;
	Button 					scheduleButton;
	Button 					cancelButton;
	GridView				smileysGrid;
	//--------------------------------------------------------
	
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	
	private ListView mList;
	
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts = new ArrayList<String>();
	ArrayList<String> templatesArray = new ArrayList<String>();
	
	DBAdapter mdba = new DBAdapter(NewScheduleActivity.this);
	
	Dialog dateSelectDialog;
	Dialog templateDialog;
	
	boolean suggestionsBoolean = true;
	Pattern p = Pattern.compile("");
	
	Date refDate = new Date();
	Calendar refCal = new GregorianCalendar();
	Date processDate = new Date();
	
	ArrayList<MyContact> shortlist = new ArrayList<MyContact>();
	
	boolean smileyVisible = false;
	
	static ArrayList<SpannedEntity> spannables = new ArrayList<SpannedEntity>();
	
	
	ArrayList<Long> ids = new ArrayList<Long>();
	
	SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
	
	static int [] images = {
					 R.drawable.emoticon_01, R.drawable.emoticon_02,
					 R.drawable.emoticon_03, R.drawable.emoticon_04,
					 R.drawable.emoticon_05, R.drawable.emoticon_06,
					 R.drawable.emoticon_07, R.drawable.emoticon_08,
					 R.drawable.emoticon_09, R.drawable.emoticon_10,
					 R.drawable.emoticon_11, R.drawable.emoticon_12,
					};
	
	static String [] smileys = {
			":-) ",
			":-D ",
			"B-D ",
			":-P ",
			";-) ",
			"o:-) ",
			"$-) ",
			":-( ",
			":'-( ",
			":-\\ ",
			":-O ", 
			":-X "
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_schedule_layout);
		
		numbersText 				= (AutoCompleteTextView) 	findViewById(R.id.new_numbers_text);
		addFromContactsImgButton 	= (ImageButton) 		 	findViewById(R.id.new_add_from_contact_imgbutton);
		dateButton 					= (Button) 					findViewById(R.id.new_date_button);
		characterCountText 			= (TextView) 				findViewById(R.id.new_char_count_text);
		messageText 				= (EditText) 				findViewById(R.id.new_message_space);
		templateImageButton 		= (ImageButton) 			findViewById(R.id.template_imgbutton);
		speechImageButton 			= (ImageButton) 			findViewById(R.id.speech_imgbutton);
		addTemplateImageButton 		= (ImageButton) 			findViewById(R.id.add_template_imgbutton);
		scheduleButton 				= (Button) 					findViewById(R.id.new_schedule_button);
		cancelButton 				= (Button) 					findViewById(R.id.new_cancel_button);
		smileysGrid					= (GridView) 				findViewById(R.id.smileysGrid);
		
		
		
		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List activities = pm.queryIntentActivities(
		  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
		  speechImageButton.setEnabled(false);
		  Toast.makeText(this, "Tragedy!", Toast.LENGTH_SHORT).show();
		}
		//---------------------------------------------------------------------
		
		
		setFunctionalities();
		
		AutoCompleteAdapter myAutoCompleteAdapter = new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
	}
	
	
	
	public void setFunctionalities(){
		
		addFromContactsImgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewScheduleActivity.this, ContactsTabsActivity.class);
				ArrayList<String> idsString = new ArrayList<String>();
				for(int i = 0; i<ids.size(); i++){
					idsString.add(String.valueOf(ids.get(i)));
				}
				intent.putExtra("IDSARRAY", idsString);
				startActivityForResult(intent, 2);
			}
		});
		
		
		//------------Date Select Button set to current date--------------------
		Date currentDate = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
		dateButton.setText(sdf.format(currentDate));
		processDate = currentDate; 
		
		dateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dateSelectDialog = new Dialog(NewScheduleActivity.this);
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
//							Toast.makeText(NewScheduleActivity.this, "Invalid Date", Toast.LENGTH_SHORT).show();
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
					messageText.setSelection(cursorPos + smileys[position].length());
				}else
					messageText.setText(smileys[position] + afterString);
					messageText.setSelection(cursorPos + smileys[position].length());
				}
		
		});
		//-----------------------------------------------end of smiley Grid set up--------
		
		
		
		
		
		//-------------------------functionality of speech input button-----------------------------
		speechImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		        // Specify the calling package to identify your application
		        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

		        // Display an hint to the user about what he should say.
		        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");

		        // Given an hint to the recognizer about what the user is going to say
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

		        // Specify how many results you want to receive. The results will be sorted
		        // where the first result is the one with higher confidence.
		        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

		        // Specify the recognition language. This parameter has to be specified only if the
		        // recognition has to be done in a specific language and not the default one (i.e., the
		        // system locale). Most of the applications do not have to set this parameter.
//		        if (!mSupportedLanguageView.getSelectedItem().toString().equals("Default")) {
//		            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
//		                    mSupportedLanguageView.getSelectedItem().toString());
//		        }

		        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			}
		});
		
		
		
		
		
		
		
		//---------------functionality of template button-----------------------
		templateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadTemplates();
				TemplateAdapter templateAdapter = new TemplateAdapter();
				templateDialog = new Dialog(NewScheduleActivity.this);
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
					Toast.makeText(NewScheduleActivity.this, "Template added", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(NewScheduleActivity.this, "Template couldn't be added", Toast.LENGTH_SHORT).show();
				}
				mdba.close();
			}
		});
		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(numbersText.getText().toString().matches("(''|(' ')+)")){
					Toast.makeText(NewScheduleActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
					numbersText.requestFocus();
				}else{
					if(!checkDateValidity(processDate)){
						Toast.makeText(NewScheduleActivity.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
					}
					doSmsScheduling();
				}
			NewScheduleActivity.this.finish();
			}
		});
	}
	
	
	
	//-------------------Adapter for list in the templates dialog--------------------
	class TemplateAdapter extends ArrayAdapter{
		TemplateAdapter(){
			super(NewScheduleActivity.this, R.layout.template_list_row, templatesArray);
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
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
		String dateString = sdf.format(cal.getTime());
		mdba.open();
		long groupId = mdba.getNextGroupId();
		String[] numbers = numbersText.getText().toString().split(",(' ')*");
		if(!(messageText.getText().toString().matches("(''|(' ')+)"))){
		for(int i = 0; i< numbers.length; i++){
			long received_id = mdba.scheduleSms(numbers[i], messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
			if(mdba.getCurrentPiFiretime() == -1){
				handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
			}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
				handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
			}
		}
		}
		mdba.close();
	}
	
	
	public void handlePiUpdate(String number, long groupId, long id, long time){
		//Cancel the pi conditionally----------------------
		Cursor cur = mdba.getPiDetails();
		cur.moveToFirst();
		
		Intent intent = new Intent(NewScheduleActivity.this, SMSHandleReceiver.class);
		intent.setAction(DBAdapter.PRIVATE_SMS_ACTION);
		
		PendingIntent pi;
		if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME))>0){
			intent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_SMS_ID)));
			intent.putExtra("NUMBER", " ");
			intent.putExtra("MESSAGE", " ");
			
			pi = PendingIntent.getBroadcast(NewScheduleActivity.this, (int)cur.getLong(cur.getColumnIndex(DBAdapter.KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
			pi.cancel();
			
		}
		intent.putExtra("ID", id);
		intent.putExtra("NUMBER", number);
		intent.putExtra("MESSAGE", messageText.getText().toString());
		
		Random rand = new Random();
		int piNumber = rand.nextInt();
		pi = PendingIntent.getBroadcast(NewScheduleActivity.this, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mdba.updatePi(piNumber, id, time);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    	alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
    	Toast.makeText(this.getApplicationContext(), "Message Scheduled", Toast.LENGTH_SHORT).show();
		
	}
	
	
	
	
	
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
        }
        
        else if(resultCode == 2){
        	ArrayList<String> idsString = new ArrayList<String>();
        	idsString = data.getStringArrayListExtra("IDSARRAY");
        	ids.clear();
        	String str = "";
        	for(int i = 0; i< idsString.size(); i++){
        		ids.add(Long.parseLong(idsString.get(i)));
        		for(int j = 0; j< SplashActivity.contactsList.size(); j++){
        			if(SplashActivity.contactsList.get(j).content_uri_id.equals(idsString.get(i))){
        				str = str + refineNumber(SplashActivity.contactsList.get(j).number);
                		if(i < idsString.size()-1){
                			str = str + ", ";
                		}
        			}
        		}
        		
        	}
        	
        	numbersText.setText(str);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	

	
	
	
	
	//--------------------------Setting up the Autocomplete text-----------------------------// 
	
	public ArrayList<MyContact> shortlistContacts(CharSequence constraint){
		String text = (String) constraint;
		shortlist.clear();
		Log.i("MESSAGE", "f : " + text);
		Pattern p = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
		for(int i = 0; i < SplashActivity.contactsList.size(); i++){
			Log.i("MESSAGE", SplashActivity.contactsList.get(i).name);
			SplashActivity.contactsList.get(i).number = refineNumber(SplashActivity.contactsList.get(i).number);
			Matcher m = p.matcher(SplashActivity.contactsList.get(i).name);
			if(m.find()){
				shortlist.add(SplashActivity.contactsList.get(i));
				Log.i("MESSAGE", "shortlist size fins : " + shortlist.size());
			}
			else
			{
				m = p.matcher(SplashActivity.contactsList.get(i).number);
				if(m.find()){
					shortlist.add(SplashActivity.contactsList.get(i));
				}
			}
		}
		Log.v("MESSAGE", "shortlist size : " + shortlist.size());
		Log.v("YO", "Yo");
		return shortlist;
					
	}
	
	
	
	
	
	
	
	//--------------------------Adapter for Autocomplete text----------------------------
	class AutoCompleteAdapter extends ArrayAdapter<MyContact> implements Filterable{
    	
    	private ArrayList<MyContact> mData;
    	
		public AutoCompleteAdapter(Context context) {
			super(context, android.R.layout.simple_dropdown_item_1line);
			mData = new ArrayList<MyContact>();
			Log.i("MSG", "into the adapter");
		}
			
		@Override
		public int getCount() {
			return mData.size();
		}
		
		@Override
		public MyContact getItem(int position) {
			return mData.get(position);
		}
		
		
		@Override
		public Filter getFilter() {
			Filter myFilter = new Filter(){
			
				
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					Log.i("MSG", "Into performing filter");
					mData.clear();
					FilterResults filterResults = new FilterResults();
					mData = shortlistContacts(constraint);
					Log.i("MSG", String.valueOf(mData.size()));
					filterResults.values = mData;
					filterResults.count = mData.size();
					
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraints, FilterResults results) {
					Log.i("MSG", "Into publish results");
					if(results != null && results.count > 0) {
						Log.i("MSG", results.count+"");
		                notifyDataSetChanged();
		            }else {
		                notifyDataSetInvalidated();
		            }
					
				}
			};
			
			return myFilter;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			
			LayoutInflater inflater = getLayoutInflater();
    		View row = inflater.inflate(R.layout.dropdown_row_layout, parent, false);
			TextView nameLabel 		= (TextView) row.findViewById(R.id.row_name_label);
			TextView numberLabel 	= (TextView) row.findViewById(R.id.row_number_label);
			nameLabel.setText(shortlist.get(position).name);
			numberLabel.setText(shortlist.get(position).number);
			return row;
		}
	
	}
	
	
	
	
	public String refineNumber(String number){
		if(number.matches("[0-9]+")){
			return number;
		}
		ArrayList<Character> chars = new ArrayList<Character>();
		for(int i = 0; i< number.length(); i++){
			chars.add(number.charAt(i));
		}
		for(int i = 0; i< chars.size(); i++){
			if(!(chars.get(i)=='0' || chars.get(i)=='1' || chars.get(i)=='2' || chars.get(i)=='3' || chars.get(i)=='4' ||
					chars.get(i)=='5' || chars.get(i)=='6' || chars.get(i)=='7' || chars.get(i)=='8' || chars.get(i)=='9' || chars.get(i)=='+')){
				chars.remove(i);
				i--;
			}
		}
		//if(number.matches("[0-9]{10}")){
			number = new String();
			for(int i = 0; i< chars.size(); i++){
				number = number + chars.get(i);
			}
			return number;
		//}
	}
	
}
