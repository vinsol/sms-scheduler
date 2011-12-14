package com.vinsol.sms_scheduler.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vinsol.sms_scheduler.ConstantsClass;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.GroupStructure;
import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
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
	//TextView				messageCountText;
	EditText 				messageText;
	ImageButton 			templateImageButton;
	ImageButton 			speechImageButton;
	ImageButton 			addTemplateImageButton;
	Button 					scheduleButton;
	Button 					cancelButton;
	GridView				smileysGrid;
	//--------------------------------------------------------
	
	
	//-----------For expanded list data of contactsTabActivity------------------------
	static ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	static ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
	//boolean empty;
	
	
	//-----------------------------------------------------------------------------
	
	
	
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts = new ArrayList<String>();
	ArrayList<String> templatesArray = new ArrayList<String>();
	
	//---------------------------------------------------------------
	//static ArrayList<GroupStructure> Groups = new ArrayList<GroupStructure>();
	static ArrayList<SpannedEntity> Spans = new ArrayList<SpannedEntity>();
	private SpannableStringBuilder ssb = new SpannableStringBuilder();
	private int spanStartPosition = 0;
	private ArrayList<ClickableSpan> clickableSpanArrayList = new ArrayList<ClickableSpan>();
	//--------------------------------------------------------------------
	
	
	
	
	
	//-----------------------Variables related to Voice recognition-------------------

     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

     //----------------------------------------------------------------------------------
	
     
    
	DBAdapter mdba = new DBAdapter(NewScheduleActivity.this);
	
	AutoCompleteAdapter myAutoCompleteAdapter;
	
	Dialog dateSelectDialog;
	Dialog templateDialog;
	
	boolean suggestionsBoolean = true;
	Pattern p = Pattern.compile("");
	
	Date refDate = new Date();
	Calendar refCal = new GregorianCalendar();
	Date processDate = new Date();
	
	ArrayList<MyContact> shortlist = new ArrayList<MyContact>();
	
	boolean smileyVisible = false;
	
	static int positionTrack;
	
	static ArrayList<SpannedEntity> spannables = new ArrayList<SpannedEntity>();
	
	
	ArrayList<Long> ids = new ArrayList<Long>();
	ArrayList<String> idsString = new ArrayList<String>();
	
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
			":-)",
			":-D",
			"B-D",
			":-P",
			";-)",
			"o:-)",
			"$-)",
			":-(",
			":'-(",
			":-\\",
			":-O", 
			":-X"
	};
	
	int toOpen = 0;
	Dialog dataLoadWaitDialog;
	IntentFilter dataloadIntentFilter;
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent2) {
//			Toast.makeText(SmsSchedulerExplActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					Intent intent = new Intent(NewScheduleActivity.this, ContactsTabsActivity.class);
					intent.putExtra("IDSARRAY", idsString);
					intent.putExtra("ORIGIN", "new");
					toOpen = 0;
					startActivityForResult(intent, 2);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_schedule_layout);
		
		dataLoadWaitDialog = new Dialog(NewScheduleActivity.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		numbersText 				= (AutoCompleteTextView) 	findViewById(R.id.new_numbers_text);
		addFromContactsImgButton 	= (ImageButton) 		 	findViewById(R.id.new_add_from_contact_imgbutton);
		dateButton 					= (Button) 					findViewById(R.id.new_date_button);
		characterCountText 			= (TextView) 				findViewById(R.id.new_char_count_text);
		//messageCountText			= (TextView) 				findViewById(R.id.new_msg_count_text);
		messageText 				= (EditText) 				findViewById(R.id.new_message_space);
		templateImageButton 		= (ImageButton) 			findViewById(R.id.template_imgbutton);
		speechImageButton 			= (ImageButton) 			findViewById(R.id.speech_imgbutton);
		addTemplateImageButton 		= (ImageButton) 			findViewById(R.id.add_template_imgbutton);
		scheduleButton 				= (Button) 					findViewById(R.id.new_schedule_button);
		cancelButton 				= (Button) 					findViewById(R.id.new_cancel_button);
		smileysGrid					= (GridView) 				findViewById(R.id.smileysGrid);
		
		
		//Groups.clear();
		Spans.clear();
		
		numbersText.setThreshold(1);
		
		
		// Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speechImageButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
        } else {
            speechImageButton.setEnabled(false);
        }
		//---------------------------------------------------------------------
		
        dataloadIntentFilter = new IntentFilter();
        dataloadIntentFilter.addAction(SmsApplicationLevelData.DIALOG_CONTROL_ACTION);
		
		setFunctionalities();
		loadGroupsData();
		
		myAutoCompleteAdapter = new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
	}
	
	
	
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
	}
	
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		unregisterReceiver(mDataLoadedReceiver);
	}
	
	
	
	public void setFunctionalities(){
		
		addFromContactsImgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.i("MSG", "isDataLoaded : " + SmsApplicationLevelData.isDataLoaded);
				if(SmsApplicationLevelData.isDataLoaded){
					Log.i("MSG", "entering into if and isDataLoaded : " + SmsApplicationLevelData.isDataLoaded);
					Intent intent = new Intent(NewScheduleActivity.this, ContactsTabsActivity.class);
					intent.putExtra("IDSARRAY", idsString);
					intent.putExtra("ORIGIN", "new");
					startActivityForResult(intent, 2);
				}else{
					toOpen = 1;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialogue_layout);
					
//					dataLoadWaitDialog.setOnCancelListener(new OnCancelListener() {
//						
//						@Override
//						public void onCancel(DialogInterface dialog) {
//							// TODO Auto-generated method stub
//							Log.i("MSG", "2");
//							toOpen = 0;
//							dataLoadWaitDialog.cancel();
//						}
//					});
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		
		
		
		
		numbersText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(spanStartPosition > 0){
					numbersText.setSelection(spanStartPosition);
				}else{
					numbersText.setSelection(numbersText.getText().toString().length());
				}
				
			}
		});
		
		
		numbersText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				Log.v("cccc", "" + keyCode + "  " + KeyEvent.KEYCODE_COMMA);				
				if(keyCode == KeyEvent.KEYCODE_DEL){
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i< Spans.size(); i++){
	                	 len = len + Spans.get(i).displayName.length();
	                	 if(i!=0){
	                		 len = len + 2;
	                	 }
	                	 if(pos<=len){
	                		 numbersText.setSelection(pos - Spans.get(i).displayName.length());
	                		 for(int j = 0; j< groupData.size(); j++){
	                			 for(int k = 0; k< childData.get(j).size(); k++){
	                				 if((Long.parseLong((String)childData.get(j).get(k).get(ConstantsClass.CHILD_CONTACT_ID))) == Spans.get(i).entityId && (Boolean)childData.get(j).get(k).get(ConstantsClass.CHILD_CHECK)){
	                					 childData.get(j).get(k).put(ConstantsClass.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 Spans.remove(i);
	                		 refreshSpannableString();
	                		 myAutoCompleteAdapter.notifyDataSetInvalidated();
	                		 myAutoCompleteAdapter.notifyDataSetChanged();
	                		 break;
	                	 }
	                 }
	            }
				
				return false;
			}
		});
		
		
		
		
		
		numbersText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				int pos = numbersText.getSelectionStart();
				Log.i("MSG", pos + "");
				if(pos>1){
					if(numbersText.getText().toString().charAt(numbersText.getSelectionStart()-1) == ' '){
						if(numbersText.getText().toString().charAt(pos-2)== '0' ||
								numbersText.getText().toString().charAt(pos-2)== '1' ||
								numbersText.getText().toString().charAt(pos-2)== '2' ||
								numbersText.getText().toString().charAt(pos-2)== '3' ||
								numbersText.getText().toString().charAt(pos-2)== '4' ||
								numbersText.getText().toString().charAt(pos-2)== '5' ||
								numbersText.getText().toString().charAt(pos-2)== '6' ||
								numbersText.getText().toString().charAt(pos-2)== '7' ||
								numbersText.getText().toString().charAt(pos-2)== '8' ||
								numbersText.getText().toString().charAt(pos-2)== '9'){
							
							numbersText.setText(numbersText.getText().toString().substring(0, pos-1));// + numbersText.getText().toString().substring(pos, numbersText.getText().toString().length()-1));
							int start = 0;
							for(int i= 0; i < pos-1 ; i++){
								if(numbersText.getText().toString().charAt(i) == ' '){
									start = i+1;
								}
							}
							boolean isPresent = false;
							for(int i = 0; i< Spans.size(); i++){
								if(Spans.get(i).displayName.equals(numbersText.getText().toString().substring(start, pos-1))){
									isPresent = true;
									break;
								}
							}
							if(!isPresent){
								SpannedEntity span = new SpannedEntity(-1, 1, numbersText.getText().toString().substring(start, pos-1), -1, -1);
								Spans.add(span);
								
							}
							refreshSpannableString();
							
						}
						
					}
				}
				
			}
		});
		
		
		
		
		
		numbersText.setLongClickable(false);
		numbersText.setMovementMethod(LinkMovementMethod.getInstance());
		
		numbersText.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				boolean isPresent = false;
				for(int i = 0; i< Spans.size(); i++){
					if(Spans.get(i).entityId == Long.parseLong(shortlist.get(position).content_uri_id)){
						isPresent = true;
						break;
					}
				}
				if(!isPresent){
					final SpannedEntity span = new SpannedEntity(-1, 2, shortlist.get(position).name, Long.parseLong(shortlist.get(position).content_uri_id), -1);
					Spans.add(span);
					
				}
				refreshSpannableString();
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
						//String temp = sdf.format(new Date(year-1900, monthOfYear, dayOfMonth, timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
						//dateLabel.setText(temp);
						if(checkDateValidity(new Date(year-1900, monthOfYear, dayOfMonth, timePicker.getCurrentHour(), timePicker.getCurrentMinute()))){
							dateLabel.setBackgroundColor(Color.rgb(0, 0, 0));
							dateLabel.setText("");
						}else{
							dateLabel.setBackgroundColor(Color.rgb(180, 180, 0));
							dateLabel.setText("Past time, message will be sent now");
						}
					}
				});
				//---------------------------------------end of DatePicker setup------
				
				
				
				refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				refDate = refCal.getTime();

				if(checkDateValidity(refDate)){
					dateLabel.setBackgroundColor(Color.rgb(0, 0, 0));
					dateLabel.setText("");
				}else{
					dateLabel.setBackgroundColor(Color.rgb(180, 180, 0));
					dateLabel.setText("Past time, message will be sent now");
				}
				
				okDateButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
						refDate = refCal.getTime();
						
						if(checkDateValidity(refDate)){
							processDate = refDate;
							dateSelectDialog.cancel();
							String temp = sdf.format(new Date(processDate.getYear(), processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes()));
							dateButton.setText(temp);
						}else{
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
							dateLabel.setBackgroundColor(Color.rgb(0, 0, 0));
							dateLabel.setText("");
						}else{
							dateLabel.setBackgroundColor(Color.rgb(180, 180, 0));
							dateLabel.setText("Past time, message will be sent now");
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
				characterCountText.setText(String.valueOf(length));
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
					if(messageText.getText().toString().charAt(cursorPos-1) == ' '){
						if(messageText.getText().length()>0){
							messageText.setText(beforeString + smileys[position] + " " + afterString);
							messageText.setSelection(cursorPos + smileys[position].length() + 1);
						}else{
							messageText.setText(beforeString + smileys[position]);
							messageText.setSelection(cursorPos + smileys[position].length());
						}
						
					}else{
						if(afterString.length()>0){
							messageText.setText((beforeString.length()>0 ? beforeString + " " : "") + smileys[position] + " " + afterString);
							messageText.setSelection(cursorPos + smileys[position].length() + 2);
						}else{
							messageText.setText(beforeString + " " + smileys[position]);
							messageText.setSelection(cursorPos + smileys[position].length() + 1);
						}
						
					}
					
				}else
					if(messageText.getText().length()==0){
						messageText.setText(smileys[position]);
						messageText.setSelection(cursorPos + smileys[position].length());
					}else{
						messageText.setText(smileys[position] + " " + afterString);
						messageText.setSelection(cursorPos + smileys[position].length() + 1);
					}
					
				}
		
		});
		//-----------------------------------------------end of smiley Grid set up--------
		
		
		
		
		
		//-------------------------functionality of speech input button-----------------------------
//		dspeechImageButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//		
		
		
		
		
		
		
		//---------------functionality of template button-----------------------
		templateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadTemplates();
				if(templatesArray.size()>0){
					TemplateAdapter templateAdapter = new TemplateAdapter();
					templateDialog = new Dialog(NewScheduleActivity.this);
					templateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					templateDialog.setContentView(R.layout.templates_dialog);
					ListView templateList = (ListView) templateDialog.findViewById(R.id.dialog_template_list);
					templateList.setAdapter(templateAdapter);
					templateDialog.show();
				}else{
					Toast.makeText(NewScheduleActivity.this, "No templates, please add some", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		//----------------------------------------end of template button functionality----------
		
		
		
		
		
		//-------------------functionality of add template button-------------------------------
		addTemplateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(messageText.getText().toString().matches("(''|[' ']*)")){
					Toast.makeText(NewScheduleActivity.this, "Text is blank. Couldn't add it as Template", Toast.LENGTH_SHORT).show();
				}else{
					mdba.open();
					Cursor cur = mdba.fetchAllTemplates();
					boolean z = true;
					if(cur.moveToFirst()){
						do{
							if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)).equals(messageText.getText().toString())){
								z = false;
								break;
							}
						}while(cur.moveToNext());
					}
					if(z){
						if(mdba.addTemplate(messageText.getText().toString()) > 0){
							Toast.makeText(NewScheduleActivity.this, "Template added", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(NewScheduleActivity.this, "Template couldn't be added", Toast.LENGTH_SHORT).show();
						}
						mdba.close();
					}else{
						Toast.makeText(NewScheduleActivity.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});
		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
//				if(numbersText.getText().toString().matches("(''|[' ']*)")){
////					Toast.makeText(NewScheduleActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
////					numbersText.requestFocus();
//					doSmsScheduling();
//				}else{
//					if(!checkDateValidity(processDate)){
//						Toast.makeText(NewScheduleActivity.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
//					}
				if(Spans.size()==0 && messageText.getText().toString().matches("(''|[' ']*)")){
					//Toast.makeText(NewScheduleActivity.this, "Mention Recipients and Message to proceed", Toast.LENGTH_SHORT).show();
					//NewScheduleActivity.this.finish();
					final Dialog d = new Dialog(NewScheduleActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("Nothing to schedule");
					yesButton.setText("Schedule SMS");
					noButton.setText("Discard");
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							//NewScheduleActivity.this.finish();
							numbersText.requestFocus();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							NewScheduleActivity.this.finish();
						}
					});
					
					d.show();
				}else
				if(Spans.size()==0){
					final Dialog d = new Dialog(NewScheduleActivity.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog_layout);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					questionText.setText("No recipients added!");
					yesButton.setText("Save as Draft");
					noButton.setText("Add Recipients");
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							doSmsScheduling();
							d.cancel();
							NewScheduleActivity.this.finish();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
							numbersText.requestFocus();
						}
					});
					
					d.show();
					
				}else
					if(messageText.getText().toString().matches("(''|[' ']*)")){
						final Dialog d = new Dialog(NewScheduleActivity.this);
						d.requestWindowFeature(Window.FEATURE_NO_TITLE);
						d.setContentView(R.layout.confirmation_dialog_layout);
						TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
						Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
						Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
						
						questionText.setText("Message is blank!");
						yesButton.setText("Save as Draft");
						noButton.setText("Write Message");
						yesButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								doSmsScheduling();
								d.cancel();
								NewScheduleActivity.this.finish();
							}
						});
						
						noButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								messageText.requestFocus();
								d.cancel();
							}
						});
						
						d.show();
				}else{	
					doSmsScheduling();
					NewScheduleActivity.this.finish();
				}
				
			
			}
		});
		
		
		
		
		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
					messageText.setSelection(messageText.getText().toString().length());
					templateDialog.cancel();
				}
			});
    		
			return row;
		}
	}
	
	//---------------------------------------------------end of adapter for template list in dialog--------------
	
	
	
	
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
		//String[] numbers = numbersText.getText().toString().split(",(' ')*");
		ArrayList<String> numbers = new ArrayList<String>();
		
		Log.i("MSG", Spans.size()+"");
		
		if(Spans.size()==0){
			SpannedEntity span = new SpannedEntity(-1, 1, " ", -1, -1);  // for adding as a fake span to create a draft
			Spans.add(span);
		}
		
		for(int i = 0; i< Spans.size(); i++){
			if(Spans.get(i).type == 2){
			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
				if(Spans.get(i).entityId == Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
					numbers.add(SmsApplicationLevelData.contactsList.get(j).number);
					long received_id = mdba.scheduleSms(SmsApplicationLevelData.contactsList.get(j).number, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
					if(!Spans.get(i).displayName.equals(" ")){
						Log.i("MESSAGE", "entered to add to recents");
						mdba.addRecentContact(Spans.get(i).entityId, "");
					}
					
					
					Log.i("MSG", "before if else");
					
					if(messageText.getText().toString().length() == 0){
						Log.i("MSG", "inside messageText if else");
						mdba.setAsDraft(received_id);
					}else{
						if(!(Spans.size()==0) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
							if(mdba.getCurrentPiFiretime() == -1){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, groupId, received_id, cal.getTimeInMillis());
							}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
								handlePiUpdate(SmsApplicationLevelData.contactsList.get(j).number, groupId, received_id, cal.getTimeInMillis());
							}
						}
					}
					Log.i("MSG", "after if else");
					Spans.get(i).smsId = received_id;
					Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
					for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
						mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), 0);
					}
				}
			}
			}else
			if(Spans.get(i).type == 1){
				long received_id = mdba.scheduleSms(Spans.get(i).displayName, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
				
				
				if(Spans.size()==0 || messageText.toString().matches("(''|[' ']*)") || Spans.get(i).displayName.equals(" ")){
					mdba.setAsDraft(received_id);
				}else{
					mdba.addRecentContact(-1, Spans.get(i).displayName);
					if(mdba.getCurrentPiFiretime() == -1){
						handlePiUpdate(Spans.get(i).displayName, groupId, received_id, cal.getTimeInMillis());
					}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
						handlePiUpdate(Spans.get(i).displayName, groupId, received_id, cal.getTimeInMillis());
					}
				}
				Spans.get(i).smsId = received_id;
				Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
			}
		}
		mdba.close();
//		if(!(messageText.getText().toString().matches("(''|(' ')+)"))){
//		for(int i = 0; i< numbers.length; i++){
//			
//			if(mdba.getCurrentPiFiretime() == -1){
//				handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
//			}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
//				handlePiUpdate(numbers[i], groupId, received_id, cal.getTimeInMillis());
//			}
//		}
//		}
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
    	
		
	}
	
	
	
	
	
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
            final Dialog d = new Dialog(NewScheduleActivity.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.voice_matches_dialog);
            
            ListView matchesList = (ListView) d.findViewById(R.id.matches_list);
            matchesList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            
            matchesList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					if(messageText.getText().toString().length()==0){
						messageText.setText(matches.get(position));
					}else{
						messageText.setText(messageText.getText().toString() + "\n" + matches.get(position));
					}
					d.cancel();
				}
			});
            d.show();
            
            
        }
        
        else if(resultCode == 2){
        	idsString.clear();
        	refreshSpannableString();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	

	
	
	
	
	//--------------------------Setting up the Autocomplete text-----------------------------// 
	
	public ArrayList<MyContact> shortlistContacts(CharSequence constraint){
	  	
		
		String text2 = (String) constraint;
		
		
			if(text2.length()>0){
		
				Pattern p = Pattern.compile(text2, Pattern.CASE_INSENSITIVE);
				for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
					SmsApplicationLevelData.contactsList.get(i).number = refineNumber(SmsApplicationLevelData.contactsList.get(i).number);
					Matcher m = p.matcher(SmsApplicationLevelData.contactsList.get(i).name);
					if(m.find()){
						shortlist.add(SmsApplicationLevelData.contactsList.get(i));
					}
					else
					{
						m = p.matcher(SmsApplicationLevelData.contactsList.get(i).number);
						if(m.find()){
							shortlist.add(SmsApplicationLevelData.contactsList.get(i));
						}
					}
				}
			}
//		}
		  
		return shortlist;
					
	}
	
	
	
	
	
	
	
	//--------------------------Adapter for Autocomplete text----------------------------
	class AutoCompleteAdapter extends ArrayAdapter<MyContact> implements Filterable{
    	
    	private ArrayList<MyContact> mData;
    	
		public AutoCompleteAdapter(Context context) {
			super(context, android.R.layout.simple_dropdown_item_1line);
			mData = new ArrayList<MyContact>();
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
					mData.clear();
					FilterResults filterResults = new FilterResults();
					//
					String text;
					
					try{
						text = (String) constraint;
						text.length();
					}catch(NullPointerException npe){
						text = " ";
					}
					
//					if(constraint.length()>0){
//						text = (String) constraint;
//					}else{
//						text = "";
//					}
					
					shortlist.clear();
					Log.i("MESSAGE", "f : " + text);
					
					positionTrack = 0;
					
					if(text.length()>0 && !((text.charAt(text.length()-1)==' ' && text.charAt(text.length()-2) == ','))){
						
					
						for(int i = 0; i< text.length(); i++){
							if(i<text.length()-2 && text.charAt(i) == ',' && text.charAt(i+1) == ' '){
								positionTrack = i+2;
							}
						}
					
						String text2 = text.substring(positionTrack, text.length());
					
						try{
							String p = text2;
						}catch(NullPointerException npe){
							text2 = " ";
						}
						mData = shortlistContacts(text2);
						filterResults.values = mData;
						filterResults.count = mData.size();
					}
					//
					
					
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraints, FilterResults results) {
					if(results != null && results.count > 0) {
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
					chars.get(i)=='5' || chars.get(i)=='6' || chars.get(i)=='7' || chars.get(i)=='8' || chars.get(i)=='9'|| chars.get(i)=='+')){
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
	
	
	
	
	public void refreshSpannableString(){
		ssb.clear();
		clickableSpanArrayList.clear();
//		clickableSpanArrayList = new ArrayList<ClickableSpan>();
		spanStartPosition = 0;
		numbersText.setText("");
			
		
		for(int i = 0; i< Spans.size(); i++){
			
			final int _i = i;
		
			clickableSpanArrayList.add(new ClickableSpan() {
				
				@Override
				public void onClick(View widget) {
							
						Log.i("MSG", _i + "");
						if(_i< Spans.size()-1){
							for(int j = 0; j< groupData.size(); j++){
	                			 for(int k = 0; k< childData.get(j).size(); k++){
	                				 if((Long.parseLong((String)childData.get(j).get(k).get(ConstantsClass.CHILD_CONTACT_ID))) == Spans.get(_i).entityId && (Boolean)childData.get(j).get(k).get(ConstantsClass.CHILD_CHECK)){
	                					 childData.get(j).get(k).put(ConstantsClass.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
							Spans.remove(_i);
							refreshSpannableString();
						}else{
							
						}
				}
			
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					//ds.bgColor = 0Xffb2d6d7;
					ds.setUnderlineText(false);
				}
			});
			try{
    		ssb.append(Spans.get(i).displayName + ", ");
    		ssb.setSpan(clickableSpanArrayList.get(clickableSpanArrayList.size() - 1), spanStartPosition, (spanStartPosition + (Spans.get(i).displayName.length())), SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
    		spanStartPosition += Spans.get(i).displayName.length() + 2;
			
			numbersText.setText(ssb);
			
			numbersText.setSelection(spanStartPosition);
			}catch(IndexOutOfBoundsException iob){
				
			}
		}
	}
	
	
	
	
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		
		if(!(Spans.size()==0) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
			if(!checkDateValidity(processDate)){
				Toast.makeText(NewScheduleActivity.this, "Date is in Past, message will be sent immediately", Toast.LENGTH_SHORT).show();
			}
			doSmsScheduling();
		}else
			
		if(!(Spans.size()==0) || !(messageText.getText().toString().matches("(''|[' ']*)"))){
			doSmsScheduling();
			Toast.makeText(NewScheduleActivity.this, "Message saved as draft", Toast.LENGTH_SHORT).show();
			NewScheduleActivity.this.finish();
		}else{
			NewScheduleActivity.this.finish();
		}
	}
	
	
	
	
	
	public void loadGroupsData(){
		
		groupData.clear();
		childData.clear();
		
		//------------------------ Setting up data for native groups ---------------------------
		String[] projection = new String[] {
				
				  Groups._ID,
	              Groups.TITLE,
	              Groups.SYSTEM_ID,
	              Groups.NOTES,
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        int count = 0;
        
        Cursor groupCursor = managedQuery(groupsUri, projection, null, null, null);
        if(groupCursor.moveToFirst()){
        	
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(ConstantsClass.GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		group.put(ConstantsClass.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
       			group.put(ConstantsClass.GROUP_CHECK, false);
        		group.put(ConstantsClass.GROUP_TYPE, 1);
        		group.put(ConstantsClass.GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        	
        		
        		groupData.add(group);
        		
        		for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsApplicationLevelData.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(ConstantsClass.CHILD_NAME, SmsApplicationLevelData.contactsList.get(i).name);
        					childParameters.put(ConstantsClass.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(i).number);
        					childParameters.put(ConstantsClass.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(i).image);
       						childParameters.put(ConstantsClass.CHILD_CHECK, false);
        					childParameters.put(ConstantsClass.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        					
        				}
        			}
        		}
        		childData.add(child);
        		count++;
        	}while(groupCursor.moveToNext());
        }
        
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		group.put(ConstantsClass.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(ConstantsClass.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.dropdown));
        		group.put(ConstantsClass.GROUP_CHECK, false);
        		group.put(ConstantsClass.GROUP_TYPE, 2);
        		group.put(ConstantsClass.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		groupData.add(group);
        		GroupStructure groupStructure;
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(ConstantsClass.CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(ConstantsClass.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(ConstantsClass.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(ConstantsClass.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(ConstantsClass.CHILD_CHECK, false);

        					
        					child.add(childParameters);
        				}
        			}
        		}
        		
        		childData.add(child);
        		count++;
        	}while(groupsCursor.moveToNext());
        }
        
        mdba.close();
	}
	
	
	
	private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
	
}