package com.vinsol.sms_scheduler.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;

import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
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


abstract class AbstractScheduleClass extends Activity{

	//---------References to the widgets-----------------
	public AutoCompleteTextView 	numbersText;
	public ImageButton 				addFromContactsImgButton;
	public Button 					dateButton;
	public TextView 				characterCountText;
	public EditText 				messageText;
	public ImageButton 				templateImageButton;
	public ImageButton 				speechImageButton;
	public ImageButton 				addTemplateImageButton;
	public Button 					scheduleButton;
	public Button 					cancelButton;
	public GridView					smileysGrid;
	public LinearLayout				pastTimeDateLabel;
	//---------------------------------------------------------
	
	
	//-----------For expanded list data of contactsTabActivity------------------------
	static ArrayList<ArrayList<HashMap<String, Object>>> nativeChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	static ArrayList<HashMap<String, Object>> nativeGroupData = new ArrayList<HashMap<String, Object>>();
	
	static ArrayList<ArrayList<HashMap<String, Object>>> privateChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	static ArrayList<HashMap<String, Object>> privateGroupData = new ArrayList<HashMap<String, Object>>();
	//--------------------------------------------------------------------------------------
	
	
	
	//---------------------------------------------------------------
	public static ArrayList<SpannedEntity> Spans = new ArrayList<SpannedEntity>();
	public static ArrayList<SpannedEntity> originalSpans = new ArrayList<SpannedEntity>();
	public SpannableStringBuilder ssb = new SpannableStringBuilder();
	public int spanStartPosition = 0;
	public static String originalMessage;
	public ArrayList<ClickableSpan> clickableSpanArrayList = new ArrayList<ClickableSpan>();
	//--------------------------------------------------------------------
	
	
	
	int [] images = {
			 R.drawable.emoticon_01, R.drawable.emoticon_02,
			 R.drawable.emoticon_03, R.drawable.emoticon_04,
			 R.drawable.emoticon_05, R.drawable.emoticon_06,
			 R.drawable.emoticon_07, R.drawable.emoticon_08,
			 R.drawable.emoticon_09, R.drawable.emoticon_10,
			 R.drawable.emoticon_11, R.drawable.emoticon_12,
			};

	String [] smileys = {
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
	
	
	InputMethodManager inputMethodManager;
	
	protected AutoCompleteAdapter myAutoCompleteAdapter;
	
	Dialog dateSelectDialog;
	Dialog templateDialog;
	
	static int positionTrack;
	
	boolean suggestionsBoolean = true;
	Pattern p = Pattern.compile("");
	
	Date refDate = new Date();
	Calendar refCal = new GregorianCalendar();
	Date processDate = new Date();
	
	protected ArrayList<MyContact> shortlist = new ArrayList<MyContact>();
	
	SmsManager smsManager = SmsManager.getDefault();
	ArrayList<String> parts = new ArrayList<String>();
	ArrayList<String> templatesArray = new ArrayList<String>();
	ArrayList<String> matches;
	
	ArrayList<Long> ids = new ArrayList<Long>();
	ArrayList<String> idsString = new ArrayList<String>();
	
//	ArrayList<Long> editedIds = new ArrayList<Long>();
	
	SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm aa, dd MMM yyyy");
	DBAdapter mdba = new DBAdapter(AbstractScheduleClass.this);
	
	
	//-----------------------Variable related to Voice recognition-------------------
	protected static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	//--------------------------------------------------------------------------------
	
	
	
	
	public void setSuperFunctionalities(){
		numbersText.setThreshold(1);
		
		addFromContactsImgButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.i("MSG", "isDataLoaded : " + SmsApplicationLevelData.isDataLoaded);
				if(SmsApplicationLevelData.isDataLoaded){
					Log.d("entering into if and isDataLoaded : " + SmsApplicationLevelData.isDataLoaded);
					Intent intent = new Intent(AbstractScheduleClass.this, ContactsTabsActivity.class);
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
				if(SmsApplicationLevelData.isDataLoaded) {
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
						boolean invalidSpan = false;
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
							invalidSpan = true;
							break;
						}
						}
						if(!invalidSpan){
							numbersText.setText(numbersText.getText().toString().substring(0, pos-1));// + numbersText.getText().toString().substring(pos, numbersText.getText().toString().length()-1));
							int start = 0;
							for(int i= 0; i < pos-1 ; i++) {
								if(numbersText.getText().toString().charAt(i) == ' '){
									start = i+1;
								}
							}
							boolean isPresent = false;
							for(int i = 0; i< Spans.size(); i++) {
								if(Spans.get(i).displayName.equals(numbersText.getText().toString().substring(start, pos-1))){
									isPresent = true;
									break;
								}
							}
							if(!isPresent){
								SpannedEntity span = new SpannedEntity(-1, 1, numbersText.getText().toString().substring(start, pos-1), -1, -1);
								Spans.add(span);
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
				for(int i = 0; i< Spans.size(); i++){
					if(Spans.get(i).entityId == Long.parseLong(shortlist.get(position).content_uri_id)) {
						isPresent = true;
						break;
					}
				}
				if(!isPresent){
					final SpannedEntity span = new SpannedEntity(-1, 2, shortlist.get(position).name, Long.parseLong(shortlist.get(position).content_uri_id), -1);
					Spans.add(span);
			
				}
				refreshSpannableString(false);
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
				dateSelectDialog = new Dialog(AbstractScheduleClass.this);
				dateSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dateSelectDialog.setContentView(R.layout.date_input_dialog);
				
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
					templateDialog = new Dialog(AbstractScheduleClass.this);
					templateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					templateDialog.setContentView(R.layout.templates_dialog);
					ListView templateList = (ListView) templateDialog.findViewById(R.id.dialog_template_list);
					templateList.setAdapter(templateAdapter);
					templateDialog.show();
				}else{
					Toast.makeText(AbstractScheduleClass.this, "No templates, please add some", Toast.LENGTH_SHORT).show();
				}
			}
		});
		//----------------------------------------end of template button functionality----------
		
		
		
		
		//-------------------functionality of add template button-------------------------------
		addTemplateImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(messageText.getText().toString().matches("(''|[' ']*)")){
					Toast.makeText(AbstractScheduleClass.this, "Empty message, can't add it as template", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(AbstractScheduleClass.this, "Template added", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(AbstractScheduleClass.this, "Template couldn't be added", Toast.LENGTH_SHORT).show();
						}
						mdba.close();
					}else{
						Toast.makeText(AbstractScheduleClass.this, "Template already exists", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});
		//------------------------------------------------------end of add template button setup ----------------
		

		
		
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
	public void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}
	
	
	
	protected void showMatchesDialog(){
		final Dialog d = new Dialog(AbstractScheduleClass.this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.voice_matches_dialog);
        
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
	public void handlePiUpdate(String number, long groupId, long id, long time){
		//Cancel the pi conditionally----------------------
		Cursor cur = mdba.getPiDetails();
		cur.moveToFirst();
		
		Intent intent = new Intent(AbstractScheduleClass.this, SMSHandleReceiver.class);
		intent.setAction(DBAdapter.PRIVATE_SMS_ACTION);
		
		PendingIntent pi;
		if(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_TIME))>0){
			intent.putExtra("ID", cur.getLong(cur.getColumnIndex(DBAdapter.KEY_SMS_ID)));
			intent.putExtra("NUMBER", " ");
			intent.putExtra("MESSAGE", " ");
			
			pi = PendingIntent.getBroadcast(AbstractScheduleClass.this, (int)cur.getLong(cur.getColumnIndex(DBAdapter.KEY_PI_NUMBER)), intent, PendingIntent.FLAG_CANCEL_CURRENT);
			pi.cancel();
		}
		intent.putExtra("ID", id);
		intent.putExtra("NUMBER", number);
		intent.putExtra("MESSAGE", messageText.getText().toString());
		
		Random rand = new Random();
		int piNumber = rand.nextInt();
		pi = PendingIntent.getBroadcast(AbstractScheduleClass.this, piNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mdba.updatePi(piNumber, id, time);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    	alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
	}
	//=============================================end of Pending Intent updation function==================
	
	
	
	
	
	
	//------------------------------------------
	//Adapter for smileys Grid
	//------------------------------------------
	class SmileysAdapter extends BaseAdapter {
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
	class AutoCompleteAdapter extends ArrayAdapter<MyContact> implements Filterable {
    	
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
							if(Spans.size()>0 && !textForFiltering.equals(Spans.get(Spans.size()-1).displayName)){
								mData = shortlistContacts(textForFiltering);
								filterResults.values = mData;
								filterResults.count = mData.size();
							}else if(Spans.size()==0){
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
        		convertView = getLayoutInflater().inflate(R.layout.dropdown_row_layout, parent, false);
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
	
	
	
	
	public ArrayList<MyContact> shortlistContacts(CharSequence constraint) {
		
		String text2 = (String) constraint;
			
		if(text2.length() > 0) {
	
			Pattern p = Pattern.compile(text2, Pattern.CASE_INSENSITIVE);
			for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++) {
				SmsApplicationLevelData.contactsList.get(i).number = refineNumber(SmsApplicationLevelData.contactsList.get(i).number);
				Matcher m = p.matcher(SmsApplicationLevelData.contactsList.get(i).name);
				if(m.find()) {
					shortlist.add(SmsApplicationLevelData.contactsList.get(i));
				} else {
					m = p.matcher(SmsApplicationLevelData.contactsList.get(i).number);
					if(m.find()) {
						shortlist.add(SmsApplicationLevelData.contactsList.get(i));
					}
				}
			}
		}
		return shortlist;			
	}
		
	public String refineNumber(String number) {
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
	class AutoCompleteListHolder {
		TextView nameText;
		TextView numberText;
	}
	
	
	
	
	//-------------------------Matches Adapter-------------------------------------------
	class MatchesAdapter extends ArrayAdapter {
		@SuppressWarnings("unchecked")
		MatchesAdapter() {
			super(AbstractScheduleClass.this, R.layout.matches_list_row, matches);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MatchesHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.matches_list_row, parent, false);
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
	class MatchesHolder{
		TextView matchText;
	}
	
	
	
	//------------------------------------------------
	//Adapter for list in the templates dialog
	//------------------------------------------------
	class TemplateAdapter extends ArrayAdapter {
		TemplateAdapter() {
			super(AbstractScheduleClass.this, R.layout.template_list_row, templatesArray);
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
	class TemplateHolder{
		TextView templateText;
	}
	
	
	
	

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
	
	
	
	
	public void refreshSpannableString(boolean isDeleted){
		ssb.clear();
		clickableSpanArrayList.clear();
		spanStartPosition = 0;
		numbersText.setText("");
			
		if(Spans.size()>0 && Spans.get(0).displayName.equals(" ")){
			Spans.remove(0);
		}
		
		for(int i = 0; i< Spans.size(); i++){
			
			
			
			final int _i = i;
		
			clickableSpanArrayList.add(new ClickableSpan() {
				
				@Override
				public void onClick(View widget) {
					 inputMethodManager.hideSoftInputFromWindow(numbersText.getWindowToken(), 0);
					 for(int j = 0; j< nativeGroupData.size(); j++){
	                	 for(int k = 0; k< nativeChildData.get(j).size(); k++){
	                		 if((Long.parseLong((String)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Spans.get(_i).entityId && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                			 nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                		 }
	                	 }
	                 }	
					 for(int j = 0; j< privateGroupData.size(); j++){
	                	 for(int k = 0; k< privateChildData.get(j).size(); k++){
	                		 if((Long.parseLong((String)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID))) == Spans.get(_i).entityId && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
	                			 privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
	                		 }
	                	 }
	                 }
					 Spans.remove(_i);
					 refreshSpannableString(true);
				}
			
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					//ds.bgColor = 0Xffb2d6d7;
					ds.setUnderlineText(false);
				}
			});
			if(Spans != null && Spans.get(i) != null) {
				ssb.append(Spans.get(i).displayName + ", ");
	    		if((spanStartPosition + (Spans.get(i).displayName.length()))<ssb.length() && spanStartPosition>-1 && (spanStartPosition + (Spans.get(i).displayName.length()))>-1){
					ssb.setSpan(clickableSpanArrayList.get(clickableSpanArrayList.size() - 1), spanStartPosition, (spanStartPosition + (Spans.get(i).displayName.length())), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				spanStartPosition += Spans.get(i).displayName.length() + 2;
				numbersText.setText(ssb);
			}
		}
		
		
		if(!isDeleted){
			if(Spans.size() > 0 ) {
				numbersText.setSelection(spanStartPosition);
			}
		}
	}
	
	
	
	
	public abstract void doSmsScheduling();
	
	
	class AsyncScheduling extends AsyncTask<Void, Void, Void>{

		Dialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(AbstractScheduleClass.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.wait_dialog);
			dialog.setCancelable(false);
			TextView dialogText = (TextView) dialog.findViewById(R.id.wait_dialog_text);
			dialogText.setText("Scheduling SMS\nPlease Wait...");
			dialog.show();
		}
		
		
		@Override
		protected Void doInBackground(Void... params) {
			doSmsScheduling();
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.cancel();
			AbstractScheduleClass.this.finish();
		}
	}
	
	
	
	
	
	
public void loadGroupsData(){
		
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
        		ArrayList<Long> spanIdsForGroup = mdba.fetchSpansForGroup(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)), 1);
        		group.put(Constants.GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)));
        		new BitmapFactory();
				group.put(Constants.GROUP_IMAGE, BitmapFactory.decodeResource(getResources(), R.drawable.expander_ic_maximized));
       			if(spanIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< Spans.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==Spans.get(i).spanId){
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
        		
        		for(int i = 0; i < SmsApplicationLevelData.contactsList.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsApplicationLevelData.contactsList.get(i).groupRowId.get(j)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(i).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(i).number);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(i).image);
        					childParameters.put(Constants.CHILD_CHECK, false);//doubted
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< Spans.size(); m++){
       								if(Spans.get(m).spanId == spanIdsForGroup.get(k) && Spans.get(m).entityId ==Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(i).content_uri_id);
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
        		ArrayList<Long> spanIdsForGroup = mdba.fetchSpansForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		new BitmapFactory();
				group.put(Constants.GROUP_IMAGE, BitmapFactory.decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		Log.d("Group size in private : " + spanIdsForGroup.size());
        		group.put(Constants.GROUP_CHECK, false);
        		if(spanIdsForGroup.size()>0){
       				for(int i = 0; i< Spans.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==Spans.get(i).spanId){
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
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< Spans.size(); m++){
       								if(Spans.get(m).spanId == spanIdsForGroup.get(k) && Spans.get(m).entityId == contactIds.get(i)){
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







	public void doSmsSchedulingTask(){
		
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		String dateString = sdf.format(cal.getTime());
		
		long groupId = mdba.getNextGroupId();
		ArrayList<String> numbers = new ArrayList<String>();
		
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
							mdba.addRecentContact(Spans.get(i).entityId, "");
						}
						if(messageText.getText().toString().length() == 0){
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
						Spans.get(i).smsId = received_id;
						Spans.get(i).spanId = mdba.createSpan(Spans.get(i).displayName, Spans.get(i).entityId, Spans.get(i).type, Spans.get(i).smsId);
						for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
							mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), Spans.get(i).groupTypes.get(k));
						}
					}
				}
			}else if(Spans.get(i).type == 1){
				long received_id = mdba.scheduleSms(Spans.get(i).displayName, messageText.getText().toString(), dateString, parts.size(), groupId, cal.getTimeInMillis());
				
				if((Spans.size()==1 && Spans.get(0).displayName.equals(" ")) || messageText.toString().matches("(''|[' ']*)")){
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
				for(int k = 0; k< Spans.get(i).groupIds.size(); k++){
					mdba.addSpanGroupRel(Spans.get(i).spanId, Spans.get(i).groupIds.get(k), Spans.get(i).groupTypes.get(i));
				}
			}
		}
	}
	
	
	
	
	
	
	public void onScheduleButtonPressTasks(){
		if(Spans.size()==0 && messageText.getText().toString().matches("(''|[' ']*)")){
			final Dialog d = new Dialog(AbstractScheduleClass.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog_layout);
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
					AbstractScheduleClass.this.finish();
				}
			});
			
			d.show();
		}else
		if(Spans.size()==0){
			final Dialog d = new Dialog(AbstractScheduleClass.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.confirmation_dialog_layout);
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
				final Dialog d = new Dialog(AbstractScheduleClass.this);
				d.requestWindowFeature(Window.FEATURE_NO_TITLE);
				d.setContentView(R.layout.confirmation_dialog_layout);
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