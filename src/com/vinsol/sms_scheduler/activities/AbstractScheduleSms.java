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

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;

import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
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


abstract class AbstractScheduleSms extends Activity{

	//---------References to the widgets-----------------
	protected AutoCompleteTextView 		numbersText;
	protected ImageButton 				addFromContactsImgButton;
	protected Button 					dateButton;
	protected TextView 					characterCountText;
	protected EditText 					messageText;
	protected ImageButton 				templateImageButton;
	protected ImageButton 				speechImageButton;
	protected ImageButton 				addTemplateImageButton;
	protected Button 					scheduleButton;
	protected Button 					cancelButton;
	protected GridView					smileysGrid;
	protected LinearLayout				pastTimeDateLabel;
	//---------------------------------------------------------
	
	
	//-----------For expanded list data of contactsTabActivity------------------------
	protected static ArrayList<ArrayList<HashMap<String, Object>>> nativeChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	protected static ArrayList<HashMap<String, Object>> nativeGroupData = new ArrayList<HashMap<String, Object>>();
	
	protected static ArrayList<ArrayList<HashMap<String, Object>>> privateChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	protected static ArrayList<HashMap<String, Object>> privateGroupData = new ArrayList<HashMap<String, Object>>();
	//--------------------------------------------------------------------------------------
	
	
	
	//---------------------------------------------------------------
	protected static ArrayList<Recipient> Recipients = new ArrayList<Recipient>();
	protected static ArrayList<Recipient> originalRecipients = new ArrayList<Recipient>();
	protected ArrayList<Long> recipientIds;
	protected SpannableStringBuilder ssb = new SpannableStringBuilder();
	protected int spanStartPosition = 0;
	protected static String originalMessage;
	protected ArrayList<ClickableSpan> clickableSpanArrayList = new ArrayList<ClickableSpan>();
	//--------------------------------------------------------------------
	
	
	
	protected int [] images = {
			 R.drawable.emoticon_01, R.drawable.emoticon_02,
			 R.drawable.emoticon_03, R.drawable.emoticon_04,
			 R.drawable.emoticon_05, R.drawable.emoticon_06,
			 R.drawable.emoticon_07, R.drawable.emoticon_08,
			 R.drawable.emoticon_09, R.drawable.emoticon_10,
			 R.drawable.emoticon_11, R.drawable.emoticon_12,
			};

	protected String [] smileys = {
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
	
	protected int toOpen = 0;
	protected Dialog dataLoadWaitDialog;
	protected IntentFilter dataloadIntentFilter;
	
	protected int mode;
	protected long editedSms;
	
	protected InputMethodManager inputMethodManager;
	
	protected AutoCompleteAdapter myAutoCompleteAdapter;
	
	private Dialog dateSelectDialog;
	private Dialog templateDialog;
	
	protected int positionTrack;
	
	protected boolean suggestionsBoolean = true;
	private Date refDate = new Date();
	private Calendar refCal = new GregorianCalendar();
	protected Date processDate = new Date();
	
	protected ArrayList<Contact> shortlist = new ArrayList<Contact>();
	
	private SmsManager smsManager = SmsManager.getDefault();
	private ArrayList<String> parts = new ArrayList<String>();
	private ArrayList<String> templatesArray = new ArrayList<String>();
	protected ArrayList<String> matches;
	
	protected ArrayList<Long> ids = new ArrayList<Long>();
	protected ArrayList<String> idsString = new ArrayList<String>();
	
	
	protected SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
	protected DBAdapter mdba = new DBAdapter(AbstractScheduleSms.this);
	
	
	//-----------------------Variable related to Voice recognition-------------------
	protected final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	//--------------------------------------------------------------------------------
	
	
	private BroadcastReceiver mDataLoadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent2) {
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == 1){
					toOpen = 0;
					Intent intent = new Intent(AbstractScheduleSms.this, SelectContacts.class);
					intent.putExtra("ORIGIN", "edit");
					startActivityForResult(intent, 2);
				}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_sms);
		
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
		pastTimeDateLabel			= (LinearLayout) 			findViewById(R.id.past_time_label);
		
		
		
		// Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
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
        dataloadIntentFilter.addAction(Constants.DIALOG_CONTROL_ACTION);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mDataLoadedReceiver);
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            showMatchesDialog();
        }
        else if(resultCode == 2) {
        	refreshSpannableString(false);
        	numbersText.requestFocus();
        	numbersText.setSelection(numbersText.getText().toString().length());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	protected void setSuperFunctionalities(){
		numbersText.setThreshold(1);
		
		addFromContactsImgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.i("MSG", "isDataLoaded : " + SmsSchedulerApplication.isDataLoaded);
				if(SmsSchedulerApplication.isDataLoaded){
					Log.d("entering into if and isDataLoaded : " + SmsSchedulerApplication.isDataLoaded);
					Intent intent = new Intent(AbstractScheduleSms.this, SelectContacts.class);
					intent.putExtra("IDSARRAY", idsString);
					intent.putExtra("ORIGIN", "new");
					startActivityForResult(intent, 2);
				}else{
					toOpen = 1;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		numbersText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(SmsSchedulerApplication.isDataLoaded) {
					numbersText.setSelection(numbersText.getText().toString().length());
					inputMethodManager.restartInput(numbersText);
				} else {
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		numbersText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(android.text.Editable s) {
				int pos = numbersText.getSelectionStart();
				if(pos > 1) {
					if(numbersText.getText().toString().charAt(numbersText.getSelectionStart()-1) == ' ') {
						int pos2 = 0;
						for(int i = pos-2; i>=0; i--){
							if(numbersText.getText().toString().charAt(i)== ' '){
								pos2 = i;
								break;
							}
						}
						boolean invalidRecipient = false;
						for(int i = pos-2; i>= pos2; i--){
						if(!(numbersText.getText().toString().charAt(pos-2)== '0' ||
								numbersText.getText().toString().charAt(pos-2)== '1' ||
								numbersText.getText().toString().charAt(pos-2)== '2' ||
								numbersText.getText().toString().charAt(pos-2)== '3' ||
								numbersText.getText().toString().charAt(pos-2)== '4' ||
								numbersText.getText().toString().charAt(pos-2)== '5' ||
								numbersText.getText().toString().charAt(pos-2)== '6' ||
								numbersText.getText().toString().charAt(pos-2)== '7' ||
								numbersText.getText().toString().charAt(pos-2)== '8' ||
								numbersText.getText().toString().charAt(pos-2)== '9')){
							invalidRecipient = true;
							break;
						}
						}
						if(!invalidRecipient){
							numbersText.setText(numbersText.getText().toString().substring(0, pos-1));// + numbersText.getText().toString().substring(pos, numbersText.getText().toString().length()-1));
							int start = 0;
							for(int i= 0; i < pos-1 ; i++) {
								if(numbersText.getText().toString().charAt(i) == ' '){
									start = i+1;
								}
							}
							boolean isPresent = false;
							for(int i = 0; i< Recipients.size(); i++) {
								if(Recipients.get(i).displayName.equals(numbersText.getText().toString().substring(start, pos-1))){
									isPresent = true;
									break;
								}
							}
							if(!isPresent){
								Recipient recipient = new Recipient(-1, 1, numbersText.getText().toString().substring(start, pos-1), -1, -1);
								Recipients.add(recipient);
							}
							refreshSpannableString(false);
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
				for(int i = 0; i< Recipients.size(); i++){
					if(Recipients.get(i).contactId == shortlist.get(position).content_uri_id) {
						isPresent = true;
						break;
					}
				}
				if(!isPresent){
					final Recipient recipient = new Recipient(-1, 2, shortlist.get(position).name, shortlist.get(position).content_uri_id, -1);
					Recipients.add(recipient);
			
				}
				refreshSpannableString(false);
			}
		});
		
		

		
		numbersText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL){
	                 int pos = numbersText.getSelectionStart();
	                 int len = 0;
	                 for(int i = 0; i< Recipients.size(); i++){
	                	 len = len + Recipients.get(i).displayName.length();
	                	 if(i!=0){
	                		 len = len + 2;
	                	 }
	                	 if(pos<=len){
	                		 if(mode==2){
	                			 numbersText.setSelection(pos - Recipients.get(i).displayName.length());
	                			 mdba.open();
	                			 mdba.deleteRecipientGroupRelsForRecipient(Recipients.get(i).recipientId);
	                			 mdba.close();
	                		 }
	                		 
	                		 for(int j = 0; j < nativeGroupData.size(); j++){
	                			 for(int k = 0; k< nativeChildData.get(j).size(); k++) {
	                				 if((Long)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(i).contactId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 
	                		 for(int j = 0; j< privateGroupData.size(); j++){
	                			 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                				 if((Long.parseLong((String)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Recipients.get(i).contactId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                					 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                				 }
	                			 }
	                		 }
	                		 Recipients.remove(i);
	                		 refreshSpannableString(false);
	                		 myAutoCompleteAdapter.notifyDataSetChanged();
	                		 break;
	                	 }
	                 }
	            }
				return false;
			}
		});
		
		
		

		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mode==1){
					onScheduleButtonPressTasks();
				}else if(mode==2){
					boolean isSending = false;
					mdba.open();
					for(int i = 0; i< recipientIds.size(); i++){
						if(isSending){
							break;
						}
						Cursor cur = mdba.fetchRecipientDetails(recipientIds.get(i));
						if(cur.moveToFirst()){
							do{
								if(cur.getInt(cur.getColumnIndex(DBAdapter.KEY_SENT))>0){
									isSending = true;
									break;
								}
							}while(cur.moveToNext());
						}
					}
					
					if(isSending){
						Toast.makeText(AbstractScheduleSms.this, "Message is already sent. Can't edit now", Toast.LENGTH_LONG).show();
						AbstractScheduleSms.this.finish();
					}else{
						onScheduleButtonPressTasks();
					}
				}
			}
		});
		
		
		
		//------------Date Select Button set to current date--------------------
		Date currentDate = processDate;
		dateButton.setText(sdf.format(currentDate));
		processDate = currentDate; 
		if(checkDateValidity(processDate)){
			pastTimeDateLabel.setVisibility(View.GONE);
		}else{
			pastTimeDateLabel.setVisibility(View.VISIBLE);
		}
		dateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dateSelectDialog = new Dialog(AbstractScheduleSms.this);
				dateSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dateSelectDialog.setContentView(R.layout.date_time_picker);
				
				final DatePicker datePicker   	= (DatePicker)  dateSelectDialog.findViewById(R.id.new_date_picker);
				final TimePicker timePicker   	= (TimePicker)  dateSelectDialog.findViewById(R.id.new_time_picker);
				final View dateLabel 		    = dateSelectDialog.findViewById(R.id.new_date_label);
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
						if(checkDateValidity(new Date(year-1900, monthOfYear, dayOfMonth, timePicker.getCurrentHour(), timePicker.getCurrentMinute()))){
							dateLabel.setVisibility(View.INVISIBLE);
							pastTimeDateLabel.setVisibility(View.GONE);
						}else{
							dateLabel.setVisibility(View.VISIBLE);
							pastTimeDateLabel.setVisibility(View.VISIBLE);
						}
					}
				});
				//---------------------------------------end of DatePicker setup------
				
				
				refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				refDate = refCal.getTime();
//				String dateString = refDate.toString();
//				dateLabel.setText(dateString);
				if(checkDateValidity(refDate)){
					dateLabel.setVisibility(View.INVISIBLE);
					pastTimeDateLabel.setVisibility(View.GONE);
				}else{
					dateLabel.setVisibility(View.VISIBLE);
					pastTimeDateLabel.setVisibility(View.VISIBLE);
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
						if(checkDateValidity(processDate)){
							dateLabel.setVisibility(View.INVISIBLE);
							pastTimeDateLabel.setVisibility(View.GONE);
						}else{
							dateLabel.setVisibility(View.VISIBLE);
							pastTimeDateLabel.setVisibility(View.VISIBLE);
						}
						dateSelectDialog.cancel();
					}
				});

				
				
				
				
				
				
				//---Setting TimePicker value change listner--------
				timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
					
					@Override
					public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
						if(checkDateValidity(new Date(datePicker.getYear()-1900, datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute))){
							dateLabel.setVisibility(View.INVISIBLE);
							pastTimeDateLabel.setVisibility(View.GONE);
						} else {
							dateLabel.setVisibility(View.VISIBLE);
							pastTimeDateLabel.setVisibility(View.VISIBLE);
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
		
		//-------------------------------------------------------end of character count setup----------
		
		
		
		
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
					messageText.requestFocus();
					messageText.setSelection(messageText.getText().toString().length());
				}else
					if(messageText.getText().length()==0){
						messageText.setText(smileys[position]);
						messageText.setSelection(cursorPos + smileys[position].length());
					}else{
						messageText.setText(smileys[position] + " " + afterString);
						messageText.setSelection(cursorPos + smileys[position].length() + 1);
					}
					messageText.requestFocus();
					messageText.setSelection(messageText.getText().toString().length());
			}
		});
		//-----------------------------------------------end of smiley Grid set up--------
		
		
		
		
		//---------------functionality of template button-----------------------
		templateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadTemplates();
				if(templatesArray.size()>0){
					TemplateAdapter templateAdapter = new TemplateAdapter();
					templateDialog = new Dialog(AbstractScheduleSms.this);
					templateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					templateDialog.setContentView(R.layout.templates_dialog);
					ListView templateList = (ListView) templateDialog.findViewById(R.id.dialog_template_list);
					templateList.setAdapter(templateAdapter);
					templateDialog.show();
				}else{
					Toast.makeText(AbstractScheduleSms.this, "No templates, please add some", Toast.LENGTH_SHORT).show();
				}
			}
		});
		//----------------------------------------end of template button functionality----------
		
		
		
		
		//-------------------functionality of add template button-------------------------------
		addTemplateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(messageText.getText().toString().matches("(''|[' ']*)")){
					Toast.makeText(AbstractScheduleSms.this, "Empty message, can't add it as template", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(AbstractScheduleSms.this, "Template added", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(AbstractScheduleSms.this, "Template couldn't be added", Toast.LENGTH_SHORT).show();
						}
						mdba.close();
					}else{
						Toast.makeText(AbstractScheduleSms.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});
		//------------------------------------------------------end of add template button setup ----------------
		

		
		//--------------------------functionality for Cancel Button--------------------------
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!messageText.getText().toString().matches("(''|[' ']*)") || !numbersText.getText().toString().matches("(''|[' ']*)")){
					final Dialog d = new Dialog(AbstractScheduleSms.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.confirmation_dialog);
					TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
					Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
					Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
					
					if(mode==2){
						questionText.setText("Delete this message?");
					}else if(mode==1){
						questionText.setText("Discard this message?");
					}
					
					yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.yes_dialog_states));
					noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.no_dialog_states));
					
					yesButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if(mode==2){
								mdba.open();
								mdba.deleteSms(editedSms, AbstractScheduleSms.this);
								mdba.close();
							}
							d.cancel();
							AbstractScheduleSms.this.finish();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
						}
					});
					
					d.show();
				}else{
					AbstractScheduleSms.this.finish();
				}
			}
		});
		
		
		//-------------------------functionality of speech input button------------------------------
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
		
	}
	
	
	
	
	//=======================setting up voice recognition functionality============================
	protected void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}
	
	
	
	protected void showMatchesDialog(){
		final Dialog d = new Dialog(AbstractScheduleSms.this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.voice_input_matches_dialog);
        
        ListView matchesList = (ListView) d.findViewById(R.id.matches_list);
        matchesList.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item, matches));
        
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
	//===================================================end of voice recognition functionality================
	
	
	
	
	//=======================function to handle updation of Pending Intent===================================
	private void handlePiUpdate(String number, long smsId, long recipientid, long time){
		//Cancel the pi conditionally----------------------
		Cursor cur = mdba.getPiDetails();
		cur.moveToFirst();
		
		Intent intent = new Intent(AbstractScheduleSms.this, SMSHandleReceiver.class);
		intent.setAction(Constants.PRIVATE_SMS_ACTION);
		
		PendingIntent pi;
		if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME))>0){
			intent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_SMS_ID)));
			intent.putExtra("NUMBER", " ");
			intent.putExtra("MESSAGE", " ");
			
			pi = PendingIntent.getBroadcast(AbstractScheduleSms.this, (int)cur.getLong(cur.getColumnIndex(DBAdapter.KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
			pi.cancel();
		}
		intent.putExtra("SMS_ID", smsId);
		intent.putExtra("RECIPIENT_ID", recipientid);
		intent.putExtra("NUMBER", number);
		intent.putExtra("MESSAGE", messageText.getText().toString());
		
		Random rand = new Random();
		int piNumber = rand.nextInt();
		pi = PendingIntent.getBroadcast(AbstractScheduleSms.this, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mdba.updatePi(piNumber, recipientid, time);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    	alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
	}
	//=============================================end of Pending Intent updation function==================
	
	
	
	
	
	
	//------------------------------------------
	//Adapter for smileys Grid
	//------------------------------------------
	private class SmileysAdapter extends BaseAdapter {
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
			if(convertView==null) {
				imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
			}else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(images[position]);
			return imageView;
		}
	}
	
	
	
	
	
	//-----------------------------------
	//Adapter for Auto-complete text
	//-----------------------------------
	protected class AutoCompleteAdapter extends ArrayAdapter<Contact> implements Filterable {
    	
    	private ArrayList<Contact> mData;
    	
		public AutoCompleteAdapter(Context context) {
			super(context, android.R.layout.simple_dropdown_item_1line);
			mData = new ArrayList<Contact>();
		}
			
		@Override
		public int getCount() {
			return mData.size();
		}
		
		@Override
		public Contact getItem(int position) {
			return mData.get(position);
		}
		
		@Override
		public Filter getFilter() {
			Filter myFilter = new Filter() {
					
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					mData.clear();
					
					FilterResults filterResults = new FilterResults();
					String text= constraint == null ? " " : constraint.toString();
										
					shortlist.clear();
					
					int positionTrack = 0;
					
					if(text.length() > 0) {
				
						positionTrack = text.lastIndexOf(",");
						positionTrack += 1; //if -1 then it will become 0 otherwise will point to character after ',' 
						
						String textForFiltering = text.substring(positionTrack, text.length()).trim();
					
						if(textForFiltering.length()>0 && !textForFiltering.equals("")){
							if(Recipients.size()>0 && !textForFiltering.equals(Recipients.get(Recipients.size()-1).displayName)){
								mData = shortlistContacts(textForFiltering);
								filterResults.values = mData;
								filterResults.count = mData.size();
							}else if(Recipients.size()==0){
								mData = shortlistContacts(textForFiltering);
								filterResults.values = mData;
								filterResults.count = mData.size();
							}
						}
					}
					
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
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final AutoCompleteListHolder holder;
			if(convertView == null) {
        		convertView = getLayoutInflater().inflate(R.layout.suggestions_dropdown_row, parent, false);
        		holder = new AutoCompleteListHolder();
        		holder.nameText 		= (TextView) 	convertView.findViewById(R.id.row_name_label);
        		holder.numberText 		= (TextView) 	convertView.findViewById(R.id.row_number_label);
        		
        		convertView.setTag(holder);
    		} else {
    			holder = (AutoCompleteListHolder) convertView.getTag();
    		}
    		
			if(shortlist != null && shortlist.size() > position) {
				holder.nameText.setText(shortlist.get(position).name);
				holder.numberText.setText(shortlist.get(position).number);
			}
    		return convertView;
		}
	}
	
	
	
	
	private ArrayList<Contact> shortlistContacts(CharSequence constraint) {
		
		String text2 = (String) constraint;
		
		if(text2.length() > 0) {
	
			Pattern p = Pattern.compile(text2, Pattern.CASE_INSENSITIVE);
			for(int i = 0; i < SmsSchedulerApplication.contactsList.size(); i++) {
				SmsSchedulerApplication.contactsList.get(i).number = refineNumber(SmsSchedulerApplication.contactsList.get(i).number);
				Matcher m = p.matcher(SmsSchedulerApplication.contactsList.get(i).name);
				if(m.find()) {
					shortlist.add(SmsSchedulerApplication.contactsList.get(i));
				} else {
					m = p.matcher(SmsSchedulerApplication.contactsList.get(i).number);
					if(m.find()) {
						shortlist.add(SmsSchedulerApplication.contactsList.get(i));
					}
				}
			}
		}
		return shortlist;			
	}
	
	private String refineNumber(String number) {
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
	
	
	
	//-----------------------------------
	//Holder for Auto-complete text
	//-----------------------------------	
	private class AutoCompleteListHolder {
		TextView nameText;
		TextView numberText;
	}
	
	
	
	
	//-------------------------Matches Adapter-------------------------------------------
	@SuppressWarnings({ "rawtypes", "unused" })
	private class MatchesAdapter extends ArrayAdapter {
		@SuppressWarnings("unchecked")
		MatchesAdapter() {
			super(AbstractScheduleSms.this, R.layout.voice_input_matches_list_row, matches);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MatchesHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.voice_input_matches_list_row, parent, false);
				holder = new MatchesHolder();
				holder.matchText = (TextView) convertView.findViewById(R.id.match_text);
				convertView.setTag(holder);
			}else{
				holder = (MatchesHolder) convertView.getTag();
			}
			
			return convertView;
		}
	}
	
	
	
	//--------------------------------------
	//Holder for Matches Adapter
	//--------------------------------------
	private class MatchesHolder{
		TextView matchText;
	}
	
	
	
	//------------------------------------------------
	//Adapter for list in the templates dialog
	//------------------------------------------------
	@SuppressWarnings("rawtypes")
	private class TemplateAdapter extends ArrayAdapter {
		@SuppressWarnings("unchecked")
		TemplateAdapter() {
			super(AbstractScheduleSms.this, R.layout.template_list_row, templatesArray);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TemplateHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
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
				
				@Override
				public void onClick(View v) {
					if(messageText.getText().toString().equals("")){
						messageText.setText(templatesArray.get(_position));
					}else{
						messageText.setText(messageText.getText().toString() + "\n" + templatesArray.get(_position));
					}
					messageText.setSelection(messageText.getText().toString().length());
					templateDialog.cancel();
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
	private class TemplateHolder{
		TextView templateText;
	}
	
	
	
	

	//------------------function to fetch template data from database----------------------------
	private void loadTemplates(){
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
	protected boolean checkDateValidity(Date date){
		Calendar cal = new GregorianCalendar(date.getYear() + 1900, date.getMonth(), date.getDate(), date.getHours(), date.getMinutes());
		if((cal.getTimeInMillis()-System.currentTimeMillis()) <= 0){
			return false;
		}else{
			return true;
		}
	}
	
	
	
	
	protected void refreshSpannableString(boolean isDeleted){
		ssb.clear();
		clickableSpanArrayList.clear();
		spanStartPosition = 0;
		numbersText.setText("");
			
		if(Recipients.size()>0 && Recipients.get(0).displayName.equals(" ")){
			Recipients.remove(0);
		}
		
		for(int i = 0; i< Recipients.size(); i++){
			
			
			
			final int _i = i;
		
			clickableSpanArrayList.add(new ClickableSpan() {
				
				@Override
				public void onClick(View widget) {
					 inputMethodManager.hideSoftInputFromWindow(numbersText.getWindowToken(), 0);
					 for(int j = 0; j< nativeGroupData.size(); j++){
	                	 for(int k = 0; k< nativeChildData.get(j).size(); k++){
	                		 if((Long)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(_i).contactId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                			 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                		 }
	                	 }
	                 }	
					 for(int j = 0; j< privateGroupData.size(); j++){
	                	 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                		 if((Long)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == Recipients.get(_i).contactId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                			 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                		 }
	                	 }
	                 }
					 Recipients.remove(_i);
					 refreshSpannableString(true);
				}
			
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					//ds.bgColor = 0Xffb2d6d7;
					ds.setUnderlineText(false);
				}
			});
			if(Recipients != null && Recipients.get(i) != null) {
				ssb.append(Recipients.get(i).displayName + ", ");
	    		if((spanStartPosition + (Recipients.get(i).displayName.length()))<ssb.length() && spanStartPosition>-1 && (spanStartPosition + (Recipients.get(i).displayName.length()))>-1){
					ssb.setSpan(clickableSpanArrayList.get(clickableSpanArrayList.size() - 1), spanStartPosition, (spanStartPosition + (Recipients.get(i).displayName.length())), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				spanStartPosition += Recipients.get(i).displayName.length() + 2;
				numbersText.setText(ssb);
			}
		}
		
		
		if(!isDeleted){
			if(Recipients.size() > 0 ) {
				numbersText.setSelection(spanStartPosition);
			}
		}
	}
	
	
	
	protected class AsyncScheduling extends AsyncTask<Void, Void, Void>{

		Dialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(AbstractScheduleSms.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.wait_dialog);
			dialog.setCancelable(false);
			TextView dialogText = (TextView) dialog.findViewById(R.id.wait_dialog_text);
			dialogText.setText("Scheduling SMS\nPlease Wait...");
			dialog.show();
		}
		
		
		@Override
		protected Void doInBackground(Void... params) {
			doSmsSchedulingTask();
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.cancel();
			AbstractScheduleSms.this.finish();
		}
	}
	
	
	protected void loadGroupsData(){
		
		nativeGroupData.clear();
		nativeChildData.clear();
		
		privateGroupData.clear();
		privateChildData.clear();
		
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
        	mdba.open();
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> recipientIdsForGroup = mdba.fetchRecipientsForGroup(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)), 1);
        		group.put(Constants.GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		new BitmapFactory();
				group.put(Constants.GROUP_IMAGE, BitmapFactory.decodeResource(getResources(), R.drawable.expander_ic_maximized));
       			if(recipientIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< Recipients.size(); i++){
       					for(int j = 0; j< recipientIdsForGroup.size(); j++){
       						if(recipientIdsForGroup.get(j)==Recipients.get(i).recipientId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		
        		group.put(Constants.GROUP_TYPE, 1);
        		group.put(Constants.GROUP_ID, groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)));
        		
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        	
        		nativeGroupData.add(group);
        		
        		for(int i = 0; i < SmsSchedulerApplication.contactsList.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsSchedulerApplication.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(i).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsSchedulerApplication.contactsList.get(i).number);
        					childParameters.put(Constants.CHILD_IMAGE, SmsSchedulerApplication.contactsList.get(i).image);
        					childParameters.put(Constants.CHILD_CHECK, false);//doubted
        					for(int k = 0; k< recipientIdsForGroup.size(); k++){
       							for(int m = 0; m< Recipients.size(); m++){
       								if(Recipients.get(m).recipientId == recipientIdsForGroup.get(k) && Recipients.get(m).contactId ==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(i).content_uri_id);
        					child.add(childParameters);
        				}
        			}
        		}
        		nativeChildData.add(child);
        		count++;
        	}while(groupCursor.moveToNext());
        	mdba.close();
        }
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> spanIdsForGroup = mdba.fetchRecipientsForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		new BitmapFactory();
				group.put(Constants.GROUP_IMAGE, BitmapFactory.decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		group.put(Constants.GROUP_CHECK, false);
        		if(spanIdsForGroup.size()>0){
       				for(int i = 0; i< Recipients.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==Recipients.get(i).recipientId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		privateGroupData.add(group);
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
        				if(contactIds.get(i)==SmsSchedulerApplication.contactsList.get(j).content_uri_id){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsSchedulerApplication.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsSchedulerApplication.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< Recipients.size(); m++){
       								if(Recipients.get(m).recipientId == spanIdsForGroup.get(k) && Recipients.get(m).contactId == contactIds.get(i)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}       					
        					child.add(childParameters);
        				}
        			}
        		}
        		privateChildData.add(child);
        		count++;
        	}while(groupsCursor.moveToNext());
        }
        mdba.close();
	}







	protected void doSmsSchedulingTask(){
		
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		String dateString = sdf.format(cal.getTime());


		ArrayList<String> numbers = new ArrayList<String>();
		mdba.open();
		long smsId = mdba.scheduleSms(messageText.getText().toString(), dateString, parts.size(), cal.getTimeInMillis());
		
		if(Recipients.size()==0 || messageText.getText().toString().matches("(''|[' ']*)")){
			mdba.setAsDraft(smsId);
			
		}
		
		if(Recipients.size()==0){
			Recipient recipient = new Recipient(-1, 1, " ", -1, -1);  // for adding as a fake span to create a draft
			Recipients.add(recipient);
		}
		
		for(int i = 0; i< Recipients.size(); i++){
			if(Recipients.get(i).type == 2){
				for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
					if(Recipients.get(i).contactId == SmsSchedulerApplication.contactsList.get(j).content_uri_id){
						numbers.add(SmsSchedulerApplication.contactsList.get(j).number);
						Log.d("added Display Name : " + SmsSchedulerApplication.contactsList.get(j).name);
						long receivedRecipientId = mdba.addRecipient(smsId, SmsSchedulerApplication.contactsList.get(j).number, SmsSchedulerApplication.contactsList.get(j).name, 2, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
//						long received_id = mdba.scheduleSms(SmsSchedulerApplication.contactsList.get(j).number, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
						if(!Recipients.get(i).displayName.equals(" ")){
							mdba.addRecentContact(Recipients.get(i).contactId, "");
						}
						
						if(!(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
							if(mdba.getCurrentPiFiretime() == -1){
								handlePiUpdate(SmsSchedulerApplication.contactsList.get(j).number, smsId, receivedRecipientId, cal.getTimeInMillis());
							}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
								handlePiUpdate(SmsSchedulerApplication.contactsList.get(j).number, smsId, receivedRecipientId, cal.getTimeInMillis());
							}
						}
						
						Recipients.get(i).recipientId = receivedRecipientId;
						for(int k = 0; k< Recipients.get(i).groupIds.size(); k++){
							mdba.addRecipientGroupRel(Recipients.get(i).recipientId, Recipients.get(i).groupIds.get(k), Recipients.get(i).groupTypes.get(k));
						}
					}
				}
			}else if(Recipients.get(i).type == 1){
				long receivedRecipientId = mdba.addRecipient(smsId, Recipients.get(i).displayName, Recipients.get(i).displayName, 1, -1);
				mdba.addRecentContact(-1, Recipients.get(i).displayName);
				if((Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")) || messageText.toString().matches("(''|[' ']*)")){
					mdba.setAsDraft(receivedRecipientId);
				}else{
					if(mdba.getCurrentPiFiretime() == -1){
						handlePiUpdate(Recipients.get(i).displayName, smsId, receivedRecipientId, cal.getTimeInMillis());
					}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
						handlePiUpdate(Recipients.get(i).displayName, smsId, receivedRecipientId, cal.getTimeInMillis());
					}
				}
				
				Recipients.get(i).recipientId = receivedRecipientId;
				
				for(int k = 0; k< Recipients.get(i).groupIds.size(); k++){
					mdba.addRecipientGroupRel(Recipients.get(i).recipientId, Recipients.get(i).groupIds.get(k), Recipients.get(i).groupTypes.get(k));
				}
			}
		}
		mdba.close();
	}
	
	
	
	
	
	
	protected void onScheduleButtonPressTasks(){
		
		if(Recipients.size()==0 && messageText.getText().toString().matches("(''|[' ']*)")){
			final Dialog d = new Dialog(AbstractScheduleSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("Nothing to schedule");

			yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.schedule_dialog_states));
			noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.discard_dialog_states));
			
			yesButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					d.cancel();
					numbersText.requestFocus();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					d.cancel();
					AbstractScheduleSms.this.finish();
				}
			});
			
			d.show();
		}else
		if(Recipients.size()==0){
			final Dialog d = new Dialog(AbstractScheduleSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog);
			TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
			Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
			Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
			
			questionText.setText("No recipients added!");
			
			yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.save_as_draft_dialog_states));
			noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_recipients_dialog_states));
			yesButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					d.cancel();
					new AsyncScheduling().execute();
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
			
		}else if(messageText.getText().toString().matches("(''|[' ']*)")){
				final Dialog d = new Dialog(AbstractScheduleSms.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.confirmation_dialog);
				TextView questionText 	= (TextView) 	d.findViewById(R.id.confirmation_dialog_text);
				Button yesButton 		= (Button) 		d.findViewById(R.id.confirmation_dialog_yes_button);
				Button noButton			= (Button) 		d.findViewById(R.id.confirmation_dialog_no_button);
				
				questionText.setText("Message is blank!");
				
				yesButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.save_as_draft_dialog_states));
				noButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.write_message_dialog_states));
				
				yesButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						d.cancel();
						new AsyncScheduling().execute();
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
			new AsyncScheduling().execute();
		}
	}
}