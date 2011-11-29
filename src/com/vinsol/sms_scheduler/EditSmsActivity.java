package com.vinsol.sms_scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
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
import android.widget.Filter;
import android.widget.Filterable;
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
	ImageButton 			templateImageButton;
	ImageButton 			speechImageButton;
	ImageButton 			addTemplateImageButton;
	Button 					scheduleButton;
	Button 					cancelButton;
	GridView				smileysGrid;
	//--------------------------------------------------------
	
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts = new ArrayList<String>();
	ArrayList<String> templatesArray = new ArrayList<String>();
	
	DBAdapter mdba = new DBAdapter(EditSmsActivity.this);
	
	Dialog dateSelectDialog;
	Dialog templateDialog;
	
	Date refDate = new Date();
	Calendar refCal = new GregorianCalendar();
	Date processDate = new Date();

	ArrayList<Person> mContacts = new ArrayList<Person>();
	ArrayList<Person> shortlist = new ArrayList<Person>();
	
	boolean smileyVisible = false;
	
	SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
	

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
		templateImageButton 		= (ImageButton) 			findViewById(R.id.template_imgbutton);
		speechImageButton 			= (ImageButton) 			findViewById(R.id.speech_imgbutton);
		addTemplateImageButton 		= (ImageButton) 			findViewById(R.id.add_template_imgbutton);
		scheduleButton 				= (Button) 					findViewById(R.id.new_schedule_button);
		cancelButton 				= (Button) 					findViewById(R.id.new_cancel_button);
		smileysGrid					= (GridView) 				findViewById(R.id.smileysGrid);
		
		
		numbersText.setText(intent.getStringExtra("NUMBER"));
		messageText.setText(intent.getStringExtra("MESSAGE"));
		processDate = new Date(intent.getLongExtra("TIME", 0));
		editedGroup = intent.getLongExtra("GROUP", 0);
		
		mContacts = getContactList();
		Toast.makeText(this, mContacts.size()+"", Toast.LENGTH_SHORT).show();
		
		setFunctionalities();
		
		AutoCompleteAdapter myAutoCompleteAdapter = new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
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
		
		//-------------------Setting up the smileys Grid---------------------------------
		smileysGrid.setAdapter(new SmileysAdapter(this));
		smileysGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int cursorPos = messageText.getSelectionStart();
				String beforeString = messageText.getText().toString().substring(0, cursorPos);
				String afterString = messageText.getText().toString().substring(cursorPos, messageText.length());
				if(cursorPos!=0){
					messageText.setText(beforeString + " " + NewScheduleActivity.smileys[position] + afterString);
					messageText.setSelection(cursorPos + 5);
				}else
					messageText.setText(NewScheduleActivity.smileys[position] + afterString);
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
	        return NewScheduleActivity.images.length;
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
			
			imageView.setImageResource(NewScheduleActivity.images[position]);
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
	
	
	
	
	
	public ArrayList<Person> getContactList(){
		ArrayList<Person> contactList = new ArrayList<Person>();

		Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
		String[] PROJECTION = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER,
		};
		String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
		Cursor contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);


		if (contacts.getCount() > 0)
		{
			while(contacts.moveToNext()) {
				Person aContact = new Person();
				int idFieldColumnIndex = 0;
				int nameFieldColumnIndex = 0;
				int numberFieldColumnIndex = 0;

				String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));

           	 	nameFieldColumnIndex = contacts.getColumnIndex(PhoneLookup.DISPLAY_NAME);
           	 	if (nameFieldColumnIndex > -1)
           	 	{
           	 		aContact.setName(contacts.getString(nameFieldColumnIndex));
           	 	}

            	PROJECTION = new String[] {Phone.NUMBER};
            	final Cursor phone = managedQuery(Phone.CONTENT_URI, PROJECTION, Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
            	if(phone.moveToFirst()) {
            		while(!phone.isAfterLast())
            		{
            			numberFieldColumnIndex = phone.getColumnIndex(Phone.NUMBER);
            			if (numberFieldColumnIndex > -1)
            			{
            				aContact.setNumber(phone.getString(numberFieldColumnIndex));
            				phone.moveToNext();
                        	TelephonyManager mTelephonyMgr;
                        	mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        	if (!mTelephonyMgr.getLine1Number().contains(aContact.getNumber()))
                        	{
                        		contactList.add(aContact);
                        	}
            			}
            		}
            	}
            	phone.close();
			}

			contacts.close();
		}

		return contactList;
	}
	
	
	
	
	
	
	
//--------------------------Setting up the Autocomplete text-----------------------------// 
	
	public ArrayList<Person> shortlistContacts(CharSequence constraint){
		String text = (String) constraint;
		shortlist.clear();
		Log.i("MESSAGE", "f : " + text);
		Pattern p = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
		for(int i = 0; i < mContacts.size(); i++){
			Log.i("MESSAGE", mContacts.get(i).getName());
			mContacts.get(i).personNumber = refineNumber(mContacts.get(i).getNumber());
			Matcher m = p.matcher(mContacts.get(i).getName());
			if(m.find()){
				shortlist.add(mContacts.get(i));
				Log.i("MESSAGE", "shortlist size fins : " + shortlist.size());
			}
			else
			{
				m = p.matcher(mContacts.get(i).getNumber());
				if(m.find()){
					shortlist.add(mContacts.get(i));
					
				}
			}
		}
		Log.v("MESSAGE", "shortlist size : " + shortlist.size());
		Log.v("YO", "Yo");
		return shortlist;
					
	}
	
	
	
	
	
	
	
	//--------------------------Adapter for Autocomplete text----------------------------
	class AutoCompleteAdapter extends ArrayAdapter<Person> implements Filterable{
    	
    	private ArrayList<Person> mData;
    	
		public AutoCompleteAdapter(Context context) {
			super(context, android.R.layout.simple_dropdown_item_1line);
			mData = new ArrayList<Person>();
			mData.add(new Person("hi", "hi"));
			Log.i("MSG", "into the adapter");
		}
			
		@Override
		public int getCount() {
			return mData.size();
		}
		
		@Override
		public Person getItem(int position) {
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
			nameLabel.setText(shortlist.get(position).getName());
			numberLabel.setText(shortlist.get(position).getNumber());
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