/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities.ScheduleSms;

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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.ContactNumber;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.DisplayImage;
import com.vinsol.sms_scheduler.utils.MyGson;
import com.vinsol.sms_scheduler.activities.Contact.SelectContacts;
import com.vinsol.sms_scheduler.activities.Template.TemplateAdapter;


public abstract class AbstractScheduleSms extends Activity{

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
	protected ImageButton				repeatButton;
	//---------------------------------------------------------
	
	
	//-----------For expanded list data of contactsTabActivity------------------------
	public static ArrayList<ArrayList<HashMap<String, Object>>> nativeChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	public static ArrayList<HashMap<String, Object>> nativeGroupData = new ArrayList<HashMap<String, Object>>();
	
	public static ArrayList<ArrayList<HashMap<String, Object>>> privateChildData = new ArrayList<ArrayList<HashMap<String, Object>>>();
	public static ArrayList<HashMap<String, Object>> privateGroupData = new ArrayList<HashMap<String, Object>>();
	//--------------------------------------------------------------------------------------
	
	
	
	//---------------------------------------------------------------
	public static ArrayList<Recipient> Recipients = new ArrayList<Recipient>();
	protected static ArrayList<Recipient> originalRecipients = new ArrayList<Recipient>();
	protected static String originalMessage;
	//--------------------------------------------------------------------
	
	
	private static int TO_OPEN_YES_AUTOCOMPLETE = 2;
	private static int TO_OPEN_YES_CONTACTS = 1;
	private static int TO_OPEN_NO = 0;
	
	
	//-------------------Variables related to autocomplete implementation------------------------
	LinearLayout hostlayout;
	RelativeLayout ac_wrapper;
	
	ArrayList<Row> rows = new ArrayList<Row>();
	ArrayList<View> views = new ArrayList<View>();
	
	Row firstRow = new Row(true);
	Row currentRow;
	Row numbersTextHolder = null;
	Row tempRow;
	
	LayoutInflater inflater;
	ImageView recipientDetailsButton;
	
	float dpi;
	int widthSum = 0;
	int widthOfContainerInDp = 0;
	int widthOfacWrapper = 0;
	int widthOfExtrasInDp = 0;
	
	Paint paint;
	
	boolean oncePressed = false;
	//----------------------------------------------------------------------------------------------
	
	
	
	//-----------------Repeat mode defaults-------------------------------------------------------
	HashMap<String, Object> defaultRepeatHash = new HashMap<String, Object>();
	int defaultRepeatMode;
	//-----------------------------------------------------------------------------------------------
	
	//---------------For the list in Recipient detail Dialog----------------------------
	ImageView undoButton;
	RecipientStack recipientStack = new RecipientStack();
	ArrayList<Recipient> prunedRecipients = new ArrayList<Recipient>();
	MyAdapter detailsRecipientsAdapter;
	//----------------------------------------------------------------------------------
	
	public static final String PREFS_NAME = "MyPrefsFile";
	boolean showMessage;
	
	
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
	
	protected int toOpen = TO_OPEN_NO;
	protected Dialog dataLoadWaitDialog;
	protected IntentFilter dataloadIntentFilter;
	
	protected int mode;
	protected long editedSms;
	
	protected InputMethodManager inputMethodManager;
	
	protected AutoCompleteAdapter myAutoCompleteAdapter;
	
	private Dialog dateSelectDialog;
	private Dialog templateDialog;
	
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
		
		public void onReceive(Context context, Intent intent2) {
			if(dataLoadWaitDialog.isShowing()){
				dataLoadWaitDialog.cancel();
				if(toOpen == TO_OPEN_YES_CONTACTS) {
					toOpen = TO_OPEN_NO;
					Intent intent = new Intent(AbstractScheduleSms.this, SelectContacts.class);
					intent.putExtra("ORIGIN", "edit");
					startActivityForResult(intent, 2);
				} else if(toOpen==TO_OPEN_YES_AUTOCOMPLETE) {
					toOpen = TO_OPEN_NO;
					numbersText.requestFocus();
					if(Recipients.size()>0) {
						if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")) {
							numbersText.setHint("Recipients");
						}else{
							numbersText.setHint(" ");
						}
					}
					inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}
			}
		}
	};
	
	
	
	@Override
    protected void onStart() {
    	super.onStart();
    	FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_sms);
		
		numbersText 				= (AutoCompleteTextView) 	findViewById(R.id.recipients_autocomplete_text);
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
		repeatButton				= (ImageButton) 			findViewById(R.id.repeat_button);
		
		detailsRecipientsAdapter = new MyAdapter();
		
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		showMessage = settings.getBoolean("SHOW_MESSAGE", true);
		
		
		//-----------------------declarations related to new autocomplete implementation-------------------------
		inflater = AbstractScheduleSms.this.getLayoutInflater();
		ac_wrapper = (RelativeLayout) findViewById(R.id.autocomplete_wrapper);
		hostlayout = (LinearLayout) findViewById(R.id.layouts_host);
        recipientDetailsButton = (ImageView) findViewById(R.id.recipients_detail_image);
		
        
        firstRow.ll = (LinearLayout) findViewById(R.id.edit_text_host);
        firstRow.ll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(!SmsSchedulerApplication.isDataLoaded){
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
					toOpen = TO_OPEN_YES_AUTOCOMPLETE;
				}else{
					numbersText.requestFocus();
					if(Recipients.size()>0){
						if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")){
							numbersText.setHint("Recipients");
						}else{
							numbersText.setHint(" ");
						}
					}
					inputMethodManager.showSoftInput(numbersText, 0);
				}
			}
		});
        
        ac_wrapper.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				numbersText.showContextMenu();
				return true;
			}
		});
        
        hostlayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				numbersText.showContextMenu();
				return true;
			}
		});
        
        firstRow.ll.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				numbersText.showContextMenu();
				return true;
			}
		});
        
        recipientDetailsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Recipients Details Checked");
				if(Recipients.size()>0){
					recipientStack.recipients.clear();
					recipientStack.positions.clear();
					prunedRecipients.clear();
					for(int i = 0; i< Recipients.size(); i++){
						prunedRecipients.add(Recipients.get(i));
					}
					final Dialog d = new Dialog(AbstractScheduleSms.this);
					d.setTitle("Recipients");
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.recipients_detail_dialog);
					
					ListView detailsList = (ListView) d.findViewById(R.id.recipients_detail_list);
					Button confirmButton = (Button) d.findViewById(R.id.confirm_button);
					Button cancelButton = (Button) d.findViewById(R.id.cancel_button);
					undoButton = (ImageView) d.findViewById(R.id.undo_button);
					
					undoButton.setEnabled(false);
					undoButton.setBackgroundResource(R.drawable.undo_button_pressed);
					detailsList.setAdapter(detailsRecipientsAdapter);
					
					confirmButton.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							Recipients.clear();
							for(int i = 0; i< prunedRecipients.size(); i++){
								Recipients.add(prunedRecipients.get(i));
							}
							
							for(int i = 0; i< recipientStack.recipients.size(); i++){
								removeRecipientFromGroups(recipientStack.recipients.get(i).contactId, recipientStack.recipients.get(i).displayName);
							}
							
							refreshRecipientViews();
							
							d.cancel();
						}
					});
					
					cancelButton.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							d.cancel();
						}
					});
					
					undoButton.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							FlurryAgent.logEvent("Recipient Details: Undo");
							if(recipientStack.recipients.size()>0){
								Recipient r = recipientStack.popRecipient();
								int p = recipientStack.popPosition();
								prunedRecipients.add(p, r);
								detailsRecipientsAdapter.notifyDataSetChanged();
								if(recipientStack.recipients.size()==0){
									undoButton.setEnabled(false);
									undoButton.setBackgroundResource(R.drawable.undo_button_pressed);
								}
							}
						}
					});
					
					d.show();
				}else{
					final Dialog d = new Dialog(AbstractScheduleSms.this);
					d.requestWindowFeature(Window.FEATURE_NO_TITLE);
					d.setContentView(R.layout.info_dialog);
					TextView infoText = (TextView)d.findViewById(R.id.info_dialog_text);
					Button okButton = (Button)d.findViewById(R.id.ok_button);
					infoText.setText("Please select some recipients to show details of.");
					
					okButton.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							d.cancel();
						}
					});
					
					d.show();
				}
			}
		});
        
        currentRow = firstRow;
        
        rows.add(firstRow);
        final Row tempRow = new Row(false);
        final View sampleElement = createElement(new Recipient(-1, 1, "sa", -2, -1, 0, 0, null));
        tempRow.ll.addView(sampleElement);
        hostlayout.addView(tempRow.ll);
        
        
        paint = new Paint();
		final float densityMultiplier = getBaseContext().getResources().getDisplayMetrics().density;
		final float scaledPx = 14 * densityMultiplier;
		paint.setTextSize(scaledPx);
        
        DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dpi = metrics.density;
		
		numbersText.setDropDownAnchor(R.id.autocomplete_wrapper);
		
		
		ac_wrapper.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				numbersText.requestFocus();
				inputMethodManager.showSoftInput(numbersText, 0);
			}
		});
		
		
		hostlayout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(widthOfacWrapper==0){
					widthOfacWrapper = ac_wrapper.getWidth();
					numbersText.setDropDownWidth(widthOfacWrapper);
				}
				if(SmsSchedulerApplication.isDataLoaded){
					numbersText.requestFocus();
					if(Recipients.size()>0){
						if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")){
							numbersText.setHint("Recipients");
						}else{
							numbersText.setHint(" ");
						}
					}
					inputMethodManager.showSoftInput(numbersText, 0);
				}else{
					toOpen = TO_OPEN_YES_AUTOCOMPLETE;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		numbersText.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				numbersText.bringToFront();
				numbersText.requestFocus();
			}
			
			public void afterTextChanged(Editable s) {
				
				if(widthOfacWrapper==0){
					widthOfacWrapper = ac_wrapper.getWidth();
					numbersText.setDropDownWidth(widthOfacWrapper);
				}
				
				if(widthOfContainerInDp==0){
					widthOfContainerInDp = (int)(firstRow.ll.getWidth()*dpi);
				}
				
				String str = numbersText.getText().toString();
				int sizeOfS = str.length();
				if(sizeOfS>0 && (str.charAt(sizeOfS - 1))==' '){
					boolean isNumber = true;
					for(int i=0; i<sizeOfS-1; i++){
						if(!((i==0 && str.charAt(i)=='+') || 
								str.charAt(i)== '0' ||
								str.charAt(i)== '1' ||
								str.charAt(i)== '2' ||
								str.charAt(i)== '3' ||
								str.charAt(i)== '4' ||
								str.charAt(i)== '5' ||
								str.charAt(i)== '6' ||
								str.charAt(i)== '7' ||
								str.charAt(i)== '8' ||
								str.charAt(i)== '9')){
							isNumber = false;
						}
					}
					if(!(numbersText.getText().toString().matches("(''|[' ']*)")))
					if(isNumber){
						boolean isPresent = false;
						for(int i = 0; i< Recipients.size(); i++) {
							if(Recipients.get(i).displayName.equals(numbersText.getText().toString().trim())){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							Recipient recipient = new Recipient(-1, 1, numbersText.getText().toString().trim(), -1, -1, -1, -1, numbersText.getText().toString().trim());
							Recipients.add(recipient);
							FlurryAgent.logEvent("Number added");
							
							
							View view = createElement(recipient);
							addView(view);
						}else{
							Toast.makeText(AbstractScheduleSms.this, "'" + numbersText.getText().toString().trim() + "'  is already added", Toast.LENGTH_SHORT).show();
						}
						numbersText.setText("");
						numbersText.setHint(" ");
					}
				}
				
				if(numbersText.getText().toString().equals("")){
					if(rows.get(0).views.size()==0){
						numbersText.setHint("Recipients");
					}else{
						numbersText.setHint(" ");
					}
				}else{
					oncePressed = false;
				}
				
				float textWidth = paint.measureText(numbersText.getText().toString()) + 5;
				if((currentRow.elementsWidth + textWidth)>widthOfContainerInDp){
					Row newRow = new Row(false);
					((LinearLayout)numbersText.getParent()).removeView(numbersText);
					newRow.ll.addView(numbersText);
					numbersTextHolder = newRow;
					hostlayout.addView(numbersTextHolder.ll);
					numbersText.requestFocus();
					numbersText.bringToFront();
					numbersText.showDropDown();
				}
				
				if(numbersTextHolder!=null){
					if((currentRow.elementsWidth + textWidth)<widthOfContainerInDp){
						numbersTextHolder.ll.removeView(numbersText);
						hostlayout.removeView(numbersTextHolder.ll);
						numbersTextHolder = null;
						currentRow.ll.addView(numbersText);
						numbersText.requestFocus();
						numbersText.bringToFront();
						numbersText.showDropDown();
					}
				}
			}
		});
		
		
		
		
		numbersText.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL){
					if(numbersText.getText().toString().equals("") && currentRow.views.size()>0){
						if(oncePressed){
							oncePressed = false;
							removeRecipientFromGroups(Recipients.get(Recipients.size()-1).contactId, Recipients.get(Recipients.size()-1).displayName);
							Recipients.remove(Recipients.size()-1);
							
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("From", "Autocomplete Backspace");
							FlurryAgent.logEvent("Recipient Removed", params);
							
							currentRow.elementsWidth = currentRow.elementsWidth - currentRow.views.get(currentRow.views.size()-1).getWidth()/dpi;
							currentRow.ll.removeView(currentRow.views.get(currentRow.views.size()-1));
							currentRow.views.remove(currentRow.views.get(currentRow.views.size()-1));
							
							if(currentRow.views.size()==0 && rows.size()>1){
								
								if(numbersTextHolder!=null){
									numbersTextHolder.ll.removeView(numbersText);
								}else{
									currentRow.ll.removeView(numbersText);
								}
								hostlayout.removeView(currentRow.ll);
								rows.remove(currentRow);
								currentRow = rows.get(rows.size()-1);
								
								float textWidth = paint.measureText(numbersText.getText().toString()) + 1;
								
								if(numbersText.getParent()!=null){
									((LinearLayout)numbersText.getParent()).removeView(numbersText);
								}
								
								if((rows.get(rows.size()-1).elementsWidth + textWidth)<widthOfContainerInDp){
									currentRow.ll.addView(numbersText);
								}else{
									Row newRow = new Row(false);
									newRow.ll.addView(numbersText);
									numbersTextHolder = newRow;
									hostlayout.addView(numbersTextHolder.ll);
								}
							}
							
							if(rows.size()==1 && rows.get(0).views.size() == 0){
								numbersText.setHint("Recipients");
							}
							
							numbersText.bringToFront();
							numbersText.requestFocus();
						}else{
							oncePressed = true;
						}
					}
					
					if(hostlayout.getChildCount()==1){
						((LinearLayout)numbersText.getParent()).removeView(numbersText);
						firstRow = currentRow;
						firstRow.ll.addView(numbersText);
					}
					
					if(Recipients.size()==0){
						numbersText.setHint("Recipients");
					}
					
					numbersText.bringToFront();
					numbersText.requestFocus();
				}
				return false;
			}
		});
		
		
		final ViewTreeObserver vto = hostlayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				widthOfContainerInDp = (int) (firstRow.ll.getWidth()/dpi);
				int widthOfTextInDp = (int) (paint.measureText("sa")/dpi);
				widthOfExtrasInDp = (int) (sampleElement.getWidth()/dpi - widthOfTextInDp);
				hostlayout.removeView(tempRow.ll);
			}
		});

		//-------------------------------------------------------------------------------------------------------




		//---------------- Check to see if a recognition activity is present--------
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speechImageButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
        } else {
            speechImageButton.setEnabled(false);
        }
		//---------------------------------------------------------------------
        
        dataLoadWaitDialog = new Dialog(AbstractScheduleSms.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		Recipients.clear();
		
		myAutoCompleteAdapter = (AutoCompleteAdapter) new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
        
		showMessagePreference();
		
		repeatButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				showRepeatDialog(defaultRepeatMode, defaultRepeatHash);
			}
		});
		
        dataloadIntentFilter = new IntentFilter();
        dataloadIntentFilter.addAction(Constants.DIALOG_CONTROL_ACTION);
	}
	
	
	
	protected void onResume() {
		super.onResume();
		registerReceiver(mDataLoadedReceiver, dataloadIntentFilter);
	}
	
	
	
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mDataLoadedReceiver);
	}
	
	
	
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            showMatchesDialog();
        }
        else if(resultCode == 2) {
        	refreshRecipientViews();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	protected void setSuperFunctionalities(){
		numbersText.setThreshold(1);
		
		addFromContactsImgButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(SmsSchedulerApplication.isDataLoaded){
					Intent intent = new Intent(AbstractScheduleSms.this, SelectContacts.class);
					intent.putExtra("IDSARRAY", idsString);
					intent.putExtra("ORIGIN", "new");
					startActivityForResult(intent, 2);
				}else{
					toOpen = TO_OPEN_YES_CONTACTS;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		numbersText.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(widthOfacWrapper==0){
					widthOfacWrapper = ac_wrapper.getWidth();
					numbersText.setDropDownWidth(widthOfacWrapper);
				}
				if(SmsSchedulerApplication.isDataLoaded) {
					inputMethodManager.restartInput(numbersText);
				} else {
					toOpen = TO_OPEN_YES_AUTOCOMPLETE;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		numbersText.setLongClickable(false);
		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				scheduleButtonOnClickListener();
			}
		});
		
		
		
		//------------Date Select Button set to current date--------------------
		dateButton.setText(sdf.format(processDate));
		if(checkDateValidity(processDate)){
			pastTimeDateLabel.setVisibility(View.GONE);
		}else{
			pastTimeDateLabel.setVisibility(View.VISIBLE);
		}
		dateButton.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				dateSelectDialog = new Dialog(AbstractScheduleSms.this);
				dateSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dateSelectDialog.setContentView(R.layout.date_time_picker);
				
				final DatePicker datePicker   	= (DatePicker)  dateSelectDialog.findViewById(R.id.new_date_picker);
				final TimePicker timePicker   	= (TimePicker)  dateSelectDialog.findViewById(R.id.new_time_picker);
				final View dateLabel 		    = dateSelectDialog.findViewById(R.id.new_date_label);
				Button okDateButton 			= (Button) 		dateSelectDialog.findViewById(R.id.new_date_dialog_ok_button);
				Button cancelDateButton 		= (Button) 		dateSelectDialog.findViewById(R.id.new_date_dialog_cancel_button);
				
				//---Setting DatePicker value change listener--------
				
				timePicker.setCurrentHour(processDate.getHours());
				timePicker.setCurrentMinute(processDate.getMinutes());
				final int mYear = processDate.getYear() + 1900;
				final int mMonth = processDate.getMonth();
				final int mDay = processDate.getDate();
				
				datePicker.init(mYear, mMonth, mDay, new OnDateChangedListener() {
					
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
				
				

				//---Setting TimePicker value change listener--------
				timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
					
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
				
				
				refCal = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				refDate = refCal.getTime();
				if(checkDateValidity(refDate)){
					dateLabel.setVisibility(View.INVISIBLE);
					pastTimeDateLabel.setVisibility(View.GONE);
				}else{
					dateLabel.setVisibility(View.VISIBLE);
					pastTimeDateLabel.setVisibility(View.VISIBLE);
				}
				
				okDateButton.setOnClickListener(new OnClickListener() {
					
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
						FlurryAgent.logEvent("Date Changed");
					}
				});
				
				cancelDateButton.setOnClickListener(new OnClickListener() {
					
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

				dateSelectDialog.show();
			}
		});
		
		//-----------------------------------------------------------end of Date select setup---------
		
		
		
		
		//------------setting functionality of character count-------------------
		messageText.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
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

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				FlurryAgent.logEvent("Smiley Added");
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
				}else if(messageText.getText().length()==0){
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
			
			public void onClick(View v) {

				loadTemplates();
				if(templatesArray.size()>0){
					FlurryAgent.logEvent("Templates Dialog Opened");
					templateDialog = new Dialog(AbstractScheduleSms.this);
					templateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					templateDialog.setContentView(R.layout.templates_dialog);
					ListView templateList = (ListView) templateDialog.findViewById(R.id.dialog_template_list);
					TemplateAdapter templateAdapter = new TemplateAdapter(templatesArray, messageText, templateDialog, AbstractScheduleSms.this);
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
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Add Template Button Clicked");
				if(messageText.getText().toString().matches("(''|[' ']*)")){
					Toast.makeText(AbstractScheduleSms.this, "Empty message, can't add it as template", Toast.LENGTH_SHORT).show();
				}else{
					mdba.open();
					Cursor cur = mdba.fetchAllTemplates();
					boolean z = true;
					if(cur.moveToFirst()){
						do{
							if(cur.getString(cur.getColumnIndex(DBAdapter.KEY_TEMP_CONTENT)).equals(messageText.getText().toString().trim())){
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
						
						public void onClick(View v) {
							if(mode==2){
								mdba.open();
								mdba.deleteSms(editedSms, AbstractScheduleSms.this);
								mdba.close();
								HashMap<String, String> deletedGroup = new HashMap<String, String>();
								deletedGroup.put("deleted SMS", "Edited SMS");
								FlurryAgent.logEvent("Message Deleted", deletedGroup);
							}else{
								FlurryAgent.logEvent("Scheduling Cancelled");
							}
							d.cancel();
							AbstractScheduleSms.this.finish();
						}
					});
					
					noButton.setOnClickListener(new OnClickListener() {
						
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
			
			public void onClick(View v) {
				FlurryAgent.logEvent("Voice Input");
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				
		        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		        
		        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			}
		});
		
	}
	
	
	protected abstract void scheduleButtonOnClickListener(); 
	
	
	//=======================setting up voice recognition functionality============================
	/**
	 * Starts a voice recognition activity that might be present in the device.
	 */
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

			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
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
	
	
	
	
	//=======================function to handle update of Pending Intent===================================
	private void handlePiUpdate(String number, long smsId, long recipientid, long time){
		//Cancel the pi conditionally----------------------
		Cursor cur = mdba.getPiDetails();
		startManagingCursor(cur);
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
	//=============================================end of Pending Intent update function==================
	
	
	
	
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

		
		public View getView(int position, View convertView, ViewGroup parent) {
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
			
		
		public int getCount() {
			return mData.size();
		}
		
		
		public Contact getItem(int position) {
			if(mData.size()>position)
				return mData.get(position);
			else
				return null;
		}
		
		
		public Filter getFilter() {
			Filter myFilter = new Filter() {
					
				protected FilterResults performFiltering(final CharSequence constraint) {
					
					mData.clear();
					
					final FilterResults filterResults = new FilterResults();
					final Activity activity = (Activity) AbstractScheduleSms.this;
					activity.runOnUiThread(new Runnable() {
						
						
						public void run() {
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
						}
					});
					
					return filterResults;
				}

				
				protected void publishResults(CharSequence constraints, FilterResults results) {
					//if there are matches, show the autocomplete dropdown or else don't.
					if(results != null && results.count > 0) {
						final Activity activity = (Activity) AbstractScheduleSms.this;
						activity.runOnUiThread(new Runnable() {
							
							public void run() {
								((AutoCompleteAdapter)numbersText.getAdapter()).notifyDataSetChanged();
							}
						});
		            }else {
		            	notifyDataSetInvalidated();
		            }		
				}
			};
			
			return myFilter;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final AutoCompleteListHolder holder;
			if(convertView == null) {
        		convertView = getLayoutInflater().inflate(R.layout.suggestions_dropdown_row, parent, false);
        		holder = new AutoCompleteListHolder();
        		holder.nameText 			= (TextView) 	 convertView.findViewById(R.id.row_name_label);
        		holder.numberText 			= (TextView) 	 convertView.findViewById(R.id.row_number_label);
        		holder.primaryNumberLayout  = (LinearLayout) convertView.findViewById(R.id.primary_number_space);
        		convertView.setTag(holder);
    		} else {
    			holder = (AutoCompleteListHolder) convertView.getTag();
    		}
    		
			if(shortlist != null && shortlist.size() > position) {
				holder.nameText.setText(shortlist.get(position).name);
				holder.numberText.setText(shortlist.get(position).numbers.get(0).type + ": " + shortlist.get(position).numbers.get(0).number);
			}
			
			holder.extraNumbersLayout 	= (LinearLayout) convertView.findViewById(R.id.extra_number_layout);
			
			if(shortlist.get(position).numbers.size()>1){
    			holder.extraNumbersLayout.setVisibility(View.VISIBLE);
    			holder.extraNumbersLayout.removeAllViews();
    			holder.extraNumbersViews.clear();
    			ArrayList<ContactNumber> extraNumbers = new ArrayList<ContactNumber>();
        		for(int i=1; i< shortlist.get(position).numbers.size(); i++){
        			extraNumbers.add(shortlist.get(position).numbers.get(i));
        		}
        		for(int i = 0; i< extraNumbers.size(); i++){
        			View view = createView(extraNumbers.get(i), shortlist.get(position), getLayoutInflater());
        			holder.extraNumbersViews.add(view);
        		}
        		for(int i = 0; i< holder.extraNumbersViews.size(); i++){
        			holder.extraNumbersLayout.addView(holder.extraNumbersViews.get(i));
        		}
    		}else{
    			holder.extraNumbersLayout.setVisibility(View.GONE);
    		}
			
			holder.primaryNumberLayout.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					boolean isPresent = false;
					for(int i = 0; i< Recipients.size(); i++){
						if(Recipients.get(i).contactId == shortlist.get(position).content_uri_id && Recipients.get(i).number.equals(shortlist.get(position).numbers.get(0).number)) {
							isPresent = true;
							break;
						}
					}
					if(!isPresent){
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Autocomplete Dropdown");
						params.put("Is Primary Number", "yes");
						FlurryAgent.logEvent("Recipient Added", params);
						
						final Recipient recipient = new Recipient(-1, 2, shortlist.get(position).name, shortlist.get(position).content_uri_id, -1, -1, -1, shortlist.get(position).numbers.get(0).number);
						Recipients.add(recipient);
						
						View view = createElement(recipient);
						addView(view);
					}else{
						Toast.makeText(AbstractScheduleSms.this, shortlist.get(position).name + " " + shortlist.get(position).numbers.get(0).number + " is already added", Toast.LENGTH_SHORT).show();
					}
					numbersText.setText("");
					if(Recipients.size()>0)
		        		numbersText.setHint(" ");
		        	else
		        		numbersText.setHint("Recipients");
					numbersText.requestFocus();
					numbersText.dismissDropDown();
				}
			});
    		return convertView;
		}


		private View createView(final ContactNumber contactNumber, final Contact contact, LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.suggestion_list_extra_numbers, null);
			TextView tv = (TextView) view.findViewById(R.id.extra_row_number_label);
			LinearLayout rowSpace = (LinearLayout) view.findViewById(R.id.extra_row_number_space);
			tv.setText(contactNumber.type + ": " + contactNumber.number);
			
			rowSpace.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					boolean isPresent = false;
					for(int i = 0; i< Recipients.size(); i++){
						if(Recipients.get(i).contactId == contact.content_uri_id && Recipients.get(i).number.equals(contactNumber.number)) {
							isPresent = true;
							break;
						}
					}
					if(!isPresent){
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("From", "Autocomplete Dropdown");
						params.put("Is Primary Number", "no");
						FlurryAgent.logEvent("Recipient Added", params);
						
						final Recipient recipient = new Recipient(-1, 2, contact.name, contact.content_uri_id, -1, -1, -1, contactNumber.number);
						Recipients.add(recipient);
						
						View view = createElement(recipient);
						addView(view);
					}else{
						Toast.makeText(AbstractScheduleSms.this, contact.name + " " + contactNumber.number + " is already added", Toast.LENGTH_SHORT).show();
					}
					numbersText.setText("");
					if(Recipients.size()>0)
		        		numbersText.setHint(" ");
		        	else
		        		numbersText.setHint("Recipients");
					numbersText.requestFocus();
					numbersText.dismissDropDown();
				}
			});
			return view;
		}
	}
	
	
	
	private ArrayList<Contact> shortlistContacts(CharSequence constraint) {
		
		String text1 = (String) constraint;
		String text2 = "";
		for(int i = 0; i< text1.length(); i++){
			if(!(text1.charAt(i) == '.' || text1.charAt(i) == '+' || text1.charAt(i) == '*' || text1.charAt(i) == '(' || text1.charAt(i) == ')' || text1.charAt(i) == '{' ||
					text1.charAt(i) == '}' || text1.charAt(i) == '[' || text1.charAt(i) == ']' || text1.charAt(i) == '\\' )){
				text2 = text2 + text1.charAt(i);
			}
		}
		
		if(text2.length() > 0) {
	
			Pattern p = Pattern.compile(text2, Pattern.CASE_INSENSITIVE);
			for(int i = 0; i < SmsSchedulerApplication.contactsList.size(); i++) {
				SmsSchedulerApplication.contactsList.get(i).numbers.get(0).number = refineNumber(SmsSchedulerApplication.contactsList.get(i).numbers.get(0).number);
				Matcher m = p.matcher(SmsSchedulerApplication.contactsList.get(i).name);
				if(m.find()) {
					shortlist.add(SmsSchedulerApplication.contactsList.get(i));
				} else {
					for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
						SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number = AbstractScheduleSms.refineNumber(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number);
						m = p.matcher(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number);
						if(m.find()) {
							shortlist.add(SmsSchedulerApplication.contactsList.get(i));
							break;
						}
					}
				}
			}
		}
		
		ContentResolver cr = getContentResolver();
		for(int i = 0; i< shortlist.size(); i++){
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, shortlist.get(i).content_uri_id);
			
			String[] projection1 = new String[]{ContactsContract.Contacts.TIMES_CONTACTED};
	    	Cursor cur = cr.query(uri, projection1, null, null, null);
	    	if(cur.moveToFirst()){
	    		shortlist.get(i).timesContacted = Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)));
	    	}else{
	    	    shortlist.get(i).timesContacted = 0;
	    	}
		}
		
		Contact temp;
  	  	for(int i = 0; i< shortlist.size()-1; i++){
  	  		for(int j = i+1; j< shortlist.size(); j++){
  	  			if(shortlist.get(j).timesContacted > shortlist.get(i).timesContacted){
  	  				temp = shortlist.get(j);
  	  				shortlist.set(j, shortlist.get(i));
  	  				shortlist.set(i, temp);
  	  			}
  	  		}
  	  	}
  	  	
		return shortlist;
	}
	
	public static String refineNumber(String number) {
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
		number = new String();
		for(int i = 0; i< chars.size(); i++){
			number = number + chars.get(i);
		}
		return number;
	}
	
	
	
	//-----------------------------------
	//Holder for Auto-complete text
	//-----------------------------------	
	private class AutoCompleteListHolder {
		TextView nameText;
		TextView numberText;
		LinearLayout extraNumbersLayout;
		LinearLayout primaryNumberLayout;
		ArrayList<View> extraNumbersViews = new ArrayList<View>();
	}
	
	
		

	//------------------function to fetch template data from database----------------------------
	private void loadTemplates(){
		mdba.open();
		Cursor cur = mdba.fetchAllTemplates();
		startManagingCursor(cur);
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
	
	
	
	
	protected class AsyncScheduling extends AsyncTask<Void, Void, Void>{

		Dialog dialog;
		
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
		
		
		
		protected Void doInBackground(Void... params) {
			doSmsSchedulingTask();
			return null;
		}
		
		
		
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.cancel();
			AbstractScheduleSms.this.finish();
		}
	}
	
	
	protected void loadGroupsData(){
		
		DisplayImage displayImage = new DisplayImage();
		
		nativeGroupData.clear();
		nativeChildData.clear();
		
		privateGroupData.clear();
		privateChildData.clear();
		
		//------------------------ Setting up data for native groups ---------------------------
		String[] projection = new String[] {
				  Groups._ID,
	              Groups.TITLE,
	              Groups.SYSTEM_ID,
	              Groups.NOTES
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        ContentResolver cr = this.getContentResolver();
        Cursor groupCursor = cr.query(groupsUri, projection, null, null, null);
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
        		boolean hasChild = false;
        		for(int i = 0; i < SmsSchedulerApplication.contactsList.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).groupRowId.size(); j++){
        				if(groupCursor.getLong(groupCursor.getColumnIndex(Groups._ID)) == SmsSchedulerApplication.contactsList.get(i).groupRowId.get(j)){
        					hasChild = true;
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(i).content_uri_id);
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(i).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsSchedulerApplication.contactsList.get(i).numbers.get(0).number);
        					displayImage.storeImage((Long)childParameters.get(Constants.CHILD_CONTACT_ID), childParameters, AbstractScheduleSms.this);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< recipientIdsForGroup.size(); k++){
       							for(int m = 0; m< Recipients.size(); m++){
       								if(Recipients.get(m).recipientId == recipientIdsForGroup.get(k) && Recipients.get(m).contactId ==SmsSchedulerApplication.contactsList.get(i).content_uri_id){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}
        					
        					child.add(childParameters);
        				}
        			}
        		}
        		
        		
        		nativeChildData.add(child);
        		if(!hasChild){
        			nativeGroupData.remove(group);
        		}
        		
        	}while(groupCursor.moveToNext());
        	mdba.close();
        }
        groupCursor.close();
        // ---------------------------------------------------end of setting up native groups data-------------
        
        
        
        //---------------------------- Setting up private Groups data ------------------------------------
        mdba.open();
        Cursor groupsCursor = mdba.fetchAllGroups();
        startManagingCursor(groupsCursor);
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
        		ArrayList<String> contactNumbers = mdba.fetchNumbersForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		boolean hasChild = false;
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
        				if(contactIds.get(i)==SmsSchedulerApplication.contactsList.get(j).content_uri_id){
        					hasChild = true;
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsSchedulerApplication.contactsList.get(j).name);
        					ArrayList<ContactNumber> numbers = SmsSchedulerApplication.contactsList.get(j).numbers;
        					String number = "";
        					for(int m = 0; m< numbers.size(); m++){
        						if(numbers.get(m).number.equals(contactNumbers.get(i))){
        							number = numbers.get(m).number;
        							break;
        						}
        					}
        					childParameters.put(Constants.CHILD_NUMBER, number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
        					displayImage.storeImage((Long)childParameters.get(Constants.CHILD_CONTACT_ID), childParameters, AbstractScheduleSms.this);
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
        		if(!hasChild){
        			privateGroupData.remove(group);
        		}
        	}while(groupsCursor.moveToNext());
        }
        groupCursor.close();
        mdba.close();
	}




	protected void doSmsSchedulingTask(){
		Calendar cal = new GregorianCalendar(processDate.getYear() + 1900, processDate.getMonth(), processDate.getDate(), processDate.getHours(), processDate.getMinutes());
		String dateString = sdf.format(cal.getTime());

		ArrayList<String> numbers = new ArrayList<String>();
		parts = smsManager.divideMessage(messageText.getText().toString());

		String repeatHashString = new MyGson().serializeRepeatHash(defaultRepeatHash);

		mdba.open();
		long smsId = mdba.scheduleSms(messageText.getText().toString(), dateString, parts.size(), cal.getTimeInMillis(), defaultRepeatMode, repeatHashString);

		boolean isDraft = false;

		if(Recipients.size()==0 || messageText.getText().toString().matches("(''|[' ']*)")){
			mdba.setAsDraft(smsId);
			isDraft = true;
		}
		HashMap<String, String> params = new HashMap<String, String>();
		if(isDraft){
			params.put("Type", "Draft");
		}else{
			if(mode==2){
				params.put("Type", "Edited");
			}else{
				params.put("Type", "New");
			}
		}
		params.put("Recipients", String.valueOf(Recipients.size()));
		params.put("Message Size", String.valueOf(parts));
		FlurryAgent.logEvent("Message Scheduled", params);
		
		if(mode==2){
			mdba.deleteSms(editedSms, AbstractScheduleSms.this);
		}
		if(Recipients.size()==0){
			Recipient recipient = new Recipient(-1, 1, " ", -1, -1, -1, -1, null);  // for adding as a fake span to create a draft
			Recipients.add(recipient);
		}
		
		for(int i = 0; i< Recipients.size(); i++){
			if(Recipients.get(i).type == 2){
				for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
					if(Recipients.get(i).contactId == SmsSchedulerApplication.contactsList.get(j).content_uri_id){
						numbers.add(SmsSchedulerApplication.contactsList.get(j).numbers.get(0).number);
						long receivedRecipientId = mdba.addRecipient(smsId, Recipients.get(i).number, SmsSchedulerApplication.contactsList.get(j).name, 2, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
						if(!Recipients.get(i).displayName.equals(" ")){
							mdba.addRecentContact(Recipients.get(i).contactId, Recipients.get(i).number);
						}
						
						if(!(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")) && !(messageText.getText().toString().matches("(''|[' ']*)"))){
							if(mdba.getCurrentPiFiretime() == -1){
								handlePiUpdate(Recipients.get(i).number, smsId, receivedRecipientId, cal.getTimeInMillis());
							}else if(cal.getTimeInMillis() < mdba.getCurrentPiFiretime()){
								handlePiUpdate(Recipients.get(i).number, smsId, receivedRecipientId, cal.getTimeInMillis());
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
				if(!Recipients.get(i).displayName.equals(" ")){
					mdba.addRecentContact(-1, Recipients.get(i).displayName);
				}
				
				if(!((Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")) || messageText.toString().matches("(''|[' ']*)"))){
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
				
				public void onClick(View v) {
					d.cancel();
					numbersText.requestFocus();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
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
				
				public void onClick(View v) {
					d.cancel();
					new AsyncScheduling().execute();
				}
			});
			
			noButton.setOnClickListener(new OnClickListener() {
				
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
				
				public void onClick(View v) {
					d.cancel();
					new AsyncScheduling().execute();
				}
			});
		
			noButton.setOnClickListener(new OnClickListener() {
				
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
	
	
	
	
	//------------------Functions and classes related to new autocomple implementation--------------------
	class Row{
		LinearLayout ll;
		ArrayList<View> views = new ArrayList<View>();
		float elementsWidth = 0;
		
		public Row(boolean first){
			if(!first){
				ll = (LinearLayout) inflater.inflate(R.layout.linear_layout, null);
				
				ll.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						if(widthOfacWrapper==0){
							widthOfacWrapper = ac_wrapper.getWidth();
							numbersText.setDropDownWidth(widthOfacWrapper);
						}
						if(!SmsSchedulerApplication.isDataLoaded){
							dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
							dataLoadWaitDialog.setCancelable(false);
							dataLoadWaitDialog.show();
						}
						numbersText.requestFocus();
						numbersText.bringToFront();
						if(Recipients.size()>0){
							numbersText.setHint(" ");
						}
						inputMethodManager.showSoftInput(numbersText, 0);
					}
				});
				
				ll.setOnLongClickListener(new OnLongClickListener() {
					
					public boolean onLongClick(View v) {
						numbersText.showContextMenu();
						return true;
					}
				});
			}
		}
	}
	
	
	
	
	
	public void displayViews(){
		for(Recipient r : Recipients){
			if(!r.displayName.equals(" ")){
				View view = createElement(r);
				addView(view);
			}
		}
		firstRow = rows.get(0);
	}
	
	
	public View createElement(final Recipient recipient){
		final View view = inflater.inflate(R.layout.element, null);
		
		TextView tv = (TextView) view.findViewById(R.id.text);
		final LinearLayout containerLayout = (LinearLayout) view.findViewById(R.id.container_linear);
		String text = ellipsizeName(recipient.displayName, recipient.contactId);
		tv.setText(text);
		view.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("From", "Autocomplete Element Click");
				FlurryAgent.logEvent("Recipient Removed", params);
				
				removeElement(view);
				Recipients.remove(recipient);
				removeRecipientFromGroups(recipient.contactId, recipient.displayName);
			}
		});
		
		
		containerLayout.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("From", "Autocomplete Element Click");
				FlurryAgent.logEvent("Recipient Removed", params);
				
				removeElement(view);
				Recipients.remove(recipient);
				removeRecipientFromGroups(recipient.contactId, recipient.displayName);
			}
		});
		
		
		return view;
	}
	
	
	
	private String ellipsizeName(String displayName, long contactId) {
		if(contactId!=-2 && paint.measureText(displayName)>70){
			int i;
			for(i = 0; i< displayName.length(); i++){
				if(paint.measureText(displayName.substring(0, i))>65){
					break;
				}
			}
			displayName = displayName.substring(0, i-1) + "..";
		}
		return displayName;
	}

	

	public void removeElement(View view){
		if(((LinearLayout)(view.getParent()))!=currentRow.ll){
			LinearLayout ll = ((LinearLayout)(view.getParent()));
			Row row = null;
			View fromView = null;
			int i = 0, j = 0;
			for(i = 0; i< rows.size()-1; i++){
				if(rows.get(i).ll.equals(ll)){
					row = rows.get(i);
					for(j = 0; j< row.views.size(); j++){
						if(row.views.get(j).equals(view)){
							fromView = row.views.get(j);
							row.views.remove(fromView);
							row.ll.removeView(fromView);
							rearrange(i, j);
							if(numbersText.getParent()==null)
								currentRow.ll.addView(numbersText);
							break;
						}
					}
					break;
				}
			}
		}else{
			currentRow.elementsWidth = currentRow.elementsWidth - view.getWidth()/dpi;
			((LinearLayout)(view.getParent())).removeView(view);
			currentRow.views.remove(view);
			if(currentRow.views.size()==0){
				currentRow.elementsWidth = 0;
				if(rows.size()==1){
					numbersText.setHint("Recipients");
				}
			}
			
			if(numbersTextHolder!=null){
				if((currentRow.elementsWidth + numbersText.getWidth())<widthOfContainerInDp){
					numbersTextHolder.ll.removeView(numbersText);
					hostlayout.removeView(numbersTextHolder.ll);
					numbersTextHolder = null;
					currentRow.ll.addView(numbersText);
				}
			}
			if(currentRow.views.size()==0 && rows.size()>1){
				currentRow.ll.removeView(numbersText);
				hostlayout.removeView(currentRow.ll);
				rows.remove(currentRow);
				currentRow = rows.get(rows.size()-1);
				if((rows.get(rows.size()-1).elementsWidth + numbersText.getWidth())<widthOfContainerInDp){
					currentRow.ll.addView(numbersText);
				}else{
					Row newRow = new Row(false);
					newRow.ll.addView(numbersText);
					numbersTextHolder = newRow;
					hostlayout.addView(numbersTextHolder.ll);
				}
				numbersText.requestFocus();
				numbersText.bringToFront();
			}
		}
		if(numbersText.getParent()==null){
			currentRow.ll.addView(numbersText);
			numbersText.requestFocus();
			numbersText.bringToFront();
		}
			
	}
	
	
	
	public void removeRecipientFromGroups(long id, String name){
		for(int j = 0; j< nativeGroupData.size(); j++){
			for(int k = 0; k< nativeChildData.get(j).size(); k++){
				if((Long)nativeChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == id && (Boolean)nativeChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
					nativeChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
       		 	}
       	 	}
        }	
		for(int j = 0; j< privateGroupData.size(); j++){
			for(int k = 0; k< privateChildData.get(j).size(); k++){
				if((Long)privateChildData.get(j).get(k).get(Constants.CHILD_CONTACT_ID) == id && (Boolean)privateChildData.get(j).get(k).get(Constants.CHILD_CHECK)){
					privateChildData.get(j).get(k).put(Constants.CHILD_CHECK, false);
       		 	}
       	 	}
        }
	}
	
	
	public void addView(View view){
		if(widthOfContainerInDp==0){
			widthOfContainerInDp = (int)(currentRow.ll.getWidth()/dpi);
		}
		float textWidth = paint.measureText(((TextView)view.findViewById(R.id.text)).getText().toString());

		widthOfExtrasInDp = 35;
		int widthOfViewInDp = (int) Math.ceil(textWidth/dpi + widthOfExtrasInDp + 1.5);
		
		if((currentRow.elementsWidth + widthOfViewInDp)> widthOfContainerInDp){
			
			if(numbersTextHolder==null){
				Row newRow = new Row(false);
				hostlayout.addView(newRow.ll);
				currentRow.ll.removeView(numbersText);
				newRow.ll.addView(numbersText);
				rows.add(newRow);
				currentRow = newRow;
			}else{
				rows.add(numbersTextHolder);
				currentRow = numbersTextHolder;
				numbersTextHolder = null;
			}
		}else{
			if(numbersTextHolder!=null){
				numbersTextHolder.ll.removeView(numbersText);
				numbersTextHolder = null;
				currentRow.ll.addView(numbersText);
			}
		}
		
		if(currentRow.ll.getChildCount()>1){
			currentRow.ll.addView(view, currentRow.ll.getChildCount()-1);
		}else{
			currentRow.ll.addView(view, 0);
		}
		currentRow.views.add(view);
		numbersText.requestFocus();
		currentRow.elementsWidth = currentRow.elementsWidth + widthOfViewInDp;
	}
	
	
	
	public void rearrange(int i, int j){
		views.clear();
		for(int k = j; k< rows.get(i).views.size(); k++){
			views.add(rows.get(i).views.get(k));
		}
		for(int k = i + 1; k< rows.size(); k++){
			for(int l = 0; l< rows.get(k).views.size(); l++){
				views.add(rows.get(k).views.get(l));
			}
		}
		
		for(int k = j; k< rows.get(i).views.size();){
			rows.get(i).ll.removeView(rows.get(i).views.get(k));
			rows.get(i).elementsWidth = rows.get(i).elementsWidth - rows.get(i).views.get(k).getWidth();
			rows.get(i).views.remove(k);
			
		}
		for(int k = i + 1; k< rows.size(); k++){
			for(int l = 0; l< rows.get(k).views.size();){
				rows.get(k).ll.removeView(rows.get(k).views.get(l));
				rows.get(k).views.remove(l);
			}
		}
		
		for(int k = i + 1; k< rows.size(); ){
			if(rows.get(k).equals(currentRow)){
				rows.get(k).ll.removeView(numbersText);
			}
			hostlayout.removeView(rows.get(k).ll);
			rows.remove(rows.get(k));
		}
		if(numbersTextHolder!=null){
			numbersTextHolder.ll.removeView(numbersText);
			numbersTextHolder = null;
		}
		rows.get(i).ll.addView(numbersText);
		currentRow = rows.get(i);
		currentRow.elementsWidth = 0;
		for(int n = 0; n< currentRow.views.size(); n++){
			currentRow.elementsWidth = currentRow.elementsWidth + currentRow.views.get(n).getWidth()/dpi;
		}
		for(int n = 0; n< views.size(); n++){
			addView(views.get(n));
		}
		if((currentRow.elementsWidth + numbersText.getWidth()/dpi)>=widthOfContainerInDp){
			Row newRow = new Row(false);
			((LinearLayout)numbersText.getParent()).removeView(numbersText);
			newRow.ll.addView(numbersText);
			numbersTextHolder = newRow;
			hostlayout.addView(numbersTextHolder.ll);
			numbersText.requestFocus();
		}
	}
	
	
	
	public void refreshRecipientViews(){
		if(numbersTextHolder!=null){
    		numbersTextHolder.ll.removeView(numbersText);
    		hostlayout.removeView(numbersTextHolder.ll);
    		numbersTextHolder = null;
    	}else{
    		if(((LinearLayout)numbersText.getParent())!=null)
    			((LinearLayout)numbersText.getParent()).removeView(numbersText);
    	}
    	
    	for(int i = rows.size()-1; i>=0; i--){
    		hostlayout.removeView(rows.get(i).ll);
    		rows.remove(i);
    	}
    	firstRow = new Row(false);
    	hostlayout.addView(firstRow.ll);
    	rows.add(firstRow);
    	currentRow = firstRow;
    	
    	displayViews();
    	
    	numbersText.requestFocus();
    	numbersText.setText("");
    	if(Recipients.size()>0){
    		if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")){
				numbersText.setHint("Recipients");
			}else{
				numbersText.setHint(" ");
			}
    	}else{
    		currentRow.ll.addView(numbersText);
    		numbersText.setHint("Recipients");
    	}
    	
    	if(rows.size()==1){
			if(numbersText.getParent()!=null){
				((LinearLayout)numbersText.getParent()).removeView(numbersText);
			}
			currentRow.ll.addView(numbersText);
		}
	}
	//----------------------------------------------------------------------------------------------------
	
	
	
	@SuppressWarnings("rawtypes")
	public class MyAdapter extends ArrayAdapter{
    	@SuppressWarnings("unchecked")
		MyAdapter(){
    		super(AbstractScheduleSms.this, R.layout.manage_groups_list_row, prunedRecipients);
    	}
    	
    	
    	
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final TemplateViewHolder holder;
    		if(convertView==null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.recipients_detail_list_row, parent, false);
    			holder = new TemplateViewHolder();
    			holder.templateBodyLabel = (TextView)convertView.findViewById(R.id.recipient_detail_contact_name);
    			holder.deleteTemplateButton = (ImageView)convertView.findViewById(R.id.recipient_detail_delete_image);
    			holder.templateNumberLabel = (TextView) convertView.findViewById(R.id.recipient_detail_contact_number);
    			convertView.setTag(holder);
    		}else{
    			holder = (TemplateViewHolder) convertView.getTag();
    		}
    		holder.templateBodyLabel.setText(prunedRecipients.get(position).displayName);
    		String type = "";
    		for(int i = 0; i< SmsSchedulerApplication.contactsList.size(); i++){
    			if(SmsSchedulerApplication.contactsList.get(i).content_uri_id == prunedRecipients.get(position).contactId){
    				for(int j = 0; j< SmsSchedulerApplication.contactsList.get(i).numbers.size(); j++){
    					if(SmsSchedulerApplication.contactsList.get(i).numbers.get(j).number.equals(prunedRecipients.get(position).number)){
    						type = SmsSchedulerApplication.contactsList.get(i).numbers.get(j).type;
    					}
    				}
    			}
    		}
    		if(prunedRecipients.get(position).type==2){
    			holder.templateNumberLabel.setText(type + ": " + prunedRecipients.get(position).number);
    		}else if(prunedRecipients.get(position).type==1){
    			holder.templateNumberLabel.setText("");
    		}
    		
    		
    		holder.deleteTemplateButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("From", "Details Delete Button");
					FlurryAgent.logEvent("Recipient Removed", params);
					
					if(recipientStack.recipients.size()==0){
						undoButton.setEnabled(true);
						undoButton.setBackgroundDrawable(getApplication().getResources().getDrawable(R.drawable.undo_button_states));
					}
					recipientStack.push(prunedRecipients.get(position), position);
					prunedRecipients.remove(prunedRecipients.get(position));
					detailsRecipientsAdapter.notifyDataSetChanged();
				}
			});
    		
    		return convertView;
    	}
    }
	
	
	private class TemplateViewHolder{
		TextView templateBodyLabel;
		TextView templateNumberLabel;
		ImageView deleteTemplateButton;
	}
	
	
	
	public class RecipientStack{
		ArrayList<Recipient> recipients = new ArrayList<Recipient>();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		
		public Recipient popRecipient(){
			return recipients.remove(recipients.size()-1);
		}
		
		public int popPosition(){
			return positions.remove(positions.size()-1);
		}
		
		public void push(Recipient r, int p){
			recipients.add(r);
			positions.add(p);
		}
	}
	
	
	public void showMessagePreference(){
		if(showMessage){
			final Dialog d = new Dialog(AbstractScheduleSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setCancelable(false);
			d.setContentView(R.layout.show_message_dialog);
			
			final CheckBox checkBox = (CheckBox) d.findViewById(R.id.show_again_check);
			Button okButton = (Button) d.findViewById(R.id.ok_button);
			TextView tv = (TextView) d.findViewById(R.id.dont_show_msg_text);
			
			okButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					if(checkBox.isChecked()){
						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					    SharedPreferences.Editor editor = settings.edit();
					    editor.putBoolean("SHOW_MESSAGE", false);
					    editor.commit();
					}
					d.cancel();
				}
			});
			
			
			
			tv.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					if(checkBox.isChecked())
						checkBox.setChecked(false);
					else
						checkBox.setChecked(true);
				}
			});
			
			d.show();
		}
	}
	
	
	
	public void showRepeatDialog(int mode, HashMap<String, Object> values){
		final Dialog d = new Dialog(AbstractScheduleSms.this);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setCancelable(false);
		d.setContentView(R.layout.repeat_dialog);
		
		new RepeatDialogHolder(mode, values, d);
		
		d.show();
	}
	
	
	
	private class RepeatDialogHolder{
		private int mode, modeTemp;
		private int endMode = Constants.END_MODE_NEVER;
		Dialog d = new Dialog(AbstractScheduleSms.this);
		Date dateTemp = new Date();
		private HashMap<String, Object> values;
		private boolean isInitialSetup = true;
		
		private TextView summaryText;
		
		private Spinner repeatModeSpinner;
		
		private LinearLayout repeatFrequencyLayout;
		private Spinner repeatFrequencySpinner;
		private TextView repeatUnitText;
		
		private LinearLayout weekdaysLayout;
		private CheckBox cbSunday;
		private CheckBox cbMonday;
		private CheckBox cbTuesday;
		private CheckBox cbWednesday;
		private CheckBox cbThursday;
		private CheckBox cbFriday;
		private CheckBox cbSaturday;
		
		private RadioButton endNeverRadio;
		private RadioButton endAfterRadio;
		private RadioButton endOnRadio;
		
		private Spinner repeatOccurSpinner;
		private TextView dateText;
		
		private Button doneButton;
		private Button cancelButton;
		
		private ArrayAdapter<Integer> repeatFrequencyAdapter;
		
		private ArrayList<Integer> repeatOccurValues = new ArrayList<Integer>();
		private ArrayList<String> modes = new ArrayList<String>();
		
		public RepeatDialogHolder(int mode, HashMap<String, Object> values, Dialog d){
			this.mode = this.modeTemp = mode;
			this.values = values;
			if(mode==0){
				this.dateTemp = new Date(System.currentTimeMillis());
			}else{
				try{
					this.dateTemp = (Date) values.get(Constants.REPEAT_HASH_END_DATE); //TODO
				}catch(ClassCastException cce){
					this.dateTemp = new Date((String)values.get(Constants.REPEAT_HASH_END_DATE));
				}
			}
			
			this.d = d;
			
			initializeViews();
			doInitialSetup();
		}
		
		private void initializeViews() {
			summaryText 			= (TextView) 	d.findViewById(R.id.summary_text);

			repeatModeSpinner 		= (Spinner) 	d.findViewById(R.id.repeat_mode_spinner);
			
			repeatFrequencyLayout	= (LinearLayout)d.findViewById(R.id.repeat_freq_layout);
			repeatFrequencySpinner 	= (Spinner) 	d.findViewById(R.id.repeat_freq_spinner);
			repeatUnitText 			= (TextView) 	d.findViewById(R.id.repeat_unit_text);
			
			weekdaysLayout 			= (LinearLayout)d.findViewById(R.id.weekdays_layout);
			cbSunday 				= (CheckBox) 	d.findViewById(R.id.cb_sunday);
			cbMonday 				= (CheckBox) 	d.findViewById(R.id.cb_monday);
			cbTuesday 				= (CheckBox) 	d.findViewById(R.id.cb_tuesday);
			cbWednesday 			= (CheckBox) 	d.findViewById(R.id.cb_wednesday);
			cbThursday 				= (CheckBox) 	d.findViewById(R.id.cb_thursday);
			cbFriday 				= (CheckBox) 	d.findViewById(R.id.cb_friday);
			cbSaturday 				= (CheckBox) 	d.findViewById(R.id.cb_saturday);
			
			endNeverRadio			= (RadioButton) d.findViewById(R.id.end_never_radio);
			endAfterRadio			= (RadioButton) d.findViewById(R.id.end_after_radio);
			endOnRadio				= (RadioButton) d.findViewById(R.id.end_on_radio);
			
			repeatOccurSpinner		= (Spinner) 	d.findViewById(R.id.repeat_occr_spinner);
			dateText				= (TextView) 	d.findViewById(R.id.date_text);
			
			doneButton				= (Button) 		d.findViewById(R.id.repeat_ok_button);
			cancelButton			= (Button) 		d.findViewById(R.id.repeat_cancel_button);
		}
		
		private void doInitialSetup() {
			modes.add("No Repeat");
			modes.add("Daily");
			modes.add("Weekly");
			modes.add("Monthly");
			modes.add("Yearly");
			
			ArrayAdapter<String> modesAdapter = new ArrayAdapter<String>(AbstractScheduleSms.this, android.R.layout.simple_spinner_item, modes);
			modesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			repeatModeSpinner.setAdapter(modesAdapter);
			
			for(int i = 1; i<= 60; i++){
				repeatOccurValues.add(i);
			}
			
			Calendar c = new GregorianCalendar(dateTemp.getYear() + 1900, dateTemp.getMonth(), dateTemp.getDate(), dateTemp.getHours(), dateTemp.getMinutes(), dateTemp.getSeconds());
			setDateText(c);
			
			ArrayAdapter<Integer> repeatOccurAdapter = new ArrayAdapter<Integer>(AbstractScheduleSms.this, android.R.layout.simple_spinner_item, repeatOccurValues);
			repeatOccurAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			repeatOccurSpinner.setAdapter(repeatOccurAdapter);
			
			
			cancelButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					d.cancel();
				}
			});
			
			
			doneButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					doOnDoneTask();
					d.cancel();
				}
			});
			
			
			endNeverRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						selectEndNeverRadio();
					}
				}
			});
			
			
			endAfterRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						selectEndAfterRadio();
					}
				}
			});
			
			
			endOnRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						selectEndOnRadio();
					}
				}
			});
			
			
			
			dateText.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					showDatePickerDialog(Calendar.getInstance());
				}
			});
			
			dateText.setOnKeyListener(new OnKeyListener() {
				
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					showDatePickerDialog(Calendar.getInstance());
					return false;
				}
			});
			
			
			
			repeatModeSpinner.setSelection(mode);
			
			repeatModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					modeTemp = position;
					switch (position){
						case 0:
							setupNoRepeatMode(); 
							repeatOccurSpinner.setSelection(0);
							break;
						case 1:
							setupDailyRepeatMode();
							
							if(isInitialSetup){
								try{
									repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
								}catch (ClassCastException e) {
									repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
								}
							}
							else
								repeatOccurSpinner.setSelection(0);
							
							break;
						case 2:
							setupWeeklyRepeatMode();
							
							if(isInitialSetup){
								try{
									repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
								}catch (ClassCastException e) {
									repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
								}
								
								@SuppressWarnings("unchecked")
								ArrayList<Boolean> weekBool = (ArrayList<Boolean>)values.get(Constants.REPEAT_HASH_WEEK_BOOL);
								
								cbSunday.setChecked(weekBool.get(0));
								cbMonday.setChecked(weekBool.get(1));
								cbTuesday.setChecked(weekBool.get(2));
								cbWednesday.setChecked(weekBool.get(3));
								cbThursday.setChecked(weekBool.get(4));
								cbFriday.setChecked(weekBool.get(5));
								cbSaturday.setChecked(weekBool.get(6));
							}else{
								cbSunday.setChecked(false);
								cbMonday.setChecked(false);
								cbTuesday.setChecked(false);
								cbWednesday.setChecked(false);
								cbThursday.setChecked(false);
								cbFriday.setChecked(false);
								cbSaturday.setChecked(false);
								
								repeatOccurSpinner.setSelection(0);
							}
							break;
						case 3:
							setupMonthlyRepeatMode();
							
							if(isInitialSetup) {
								try{
									repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
								}catch (ClassCastException e) {
									repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
								}
							} else
								repeatOccurSpinner.setSelection(0);
							
							break;
						case 4:
							setupYearlyRepeatMode();
							break;
						default:
							break;
					}
					
					
					if(isInitialSetup) {
						if(mode>0){
							int endMode;
							try{
								endMode = ((Double)values.get(Constants.REPEAT_HASH_END_MODE)).intValue();
							}catch (ClassCastException e) {
								endMode = ((Integer)values.get(Constants.REPEAT_HASH_END_MODE));
							}
							
							switch (endMode) {
								case 0:
									selectEndNeverRadio(); break;
								case 1:
									selectEndAfterRadio();
									try{
										repeatOccurSpinner.setSelection(((Integer)values.get(Constants.REPEAT_HASH_END_FREQ)-1), true);
									}catch (ClassCastException e) {
										repeatOccurSpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_END_FREQ)).intValue()-1, true);
									}
									
									break;
								case 2:
									selectEndOnRadio();
									Date date;
									try{
										date = (Date)values.get(Constants.REPEAT_HASH_END_DATE);
									}catch (ClassCastException e) {
										date = new Date((String)values.get(Constants.REPEAT_HASH_END_DATE));
									}
									
									Calendar c = Calendar.getInstance();
									c.set(date.getYear()+1900, date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
									setDateText(c);
									break;
							}
						}
						isInitialSetup = false;
					}
					updateSummary();
				}

				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
			
			repeatFrequencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateSummary();
				}

				public void onNothingSelected(AdapterView<?> parent) {
					updateSummary();
				}
			});
			
			
			repeatOccurSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateSummary();
				}

				public void onNothingSelected(AdapterView<?> parent) {
					updateSummary();
				}
			});
		}
		
		
		private void setupNoRepeatMode() {
			weekdaysLayout.setVisibility(View.GONE);
			repeatFrequencySpinner.setSelection(0);
			repeatFrequencyLayout.setVisibility(View.VISIBLE);
			repeatFrequencySpinner.setEnabled(false);
			repeatUnitText.setText("");
			endNeverRadio.setEnabled(false);
			endAfterRadio.setEnabled(false);
			endOnRadio.setEnabled(false);
			endNeverRadio.setChecked(false);
			endAfterRadio.setChecked(false);
			endOnRadio.setChecked(false);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			updateSummary();
		}
		
		
		private void setupDailyRepeatMode() {
			weekdaysLayout.setVisibility(View.GONE);
			
			ArrayList<Integer> items = new ArrayList<Integer>();
			for(int i = 1; i<= 30; i++){
				items.add(i);
			}
			
			repeatFrequencyAdapter = new ArrayAdapter<Integer>(AbstractScheduleSms.this, android.R.layout.simple_spinner_item, items);
			repeatFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			repeatFrequencySpinner.setAdapter(repeatFrequencyAdapter);
			
			if(isInitialSetup){
				try{
					repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
				}catch (ClassCastException e) {
					repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
				}
			}
			else
				repeatFrequencySpinner.setSelection(0, true);
			
			repeatFrequencyLayout.setVisibility(View.VISIBLE);
			repeatFrequencySpinner.setEnabled(true);
			repeatUnitText.setText("Days");
			endNeverRadio.setEnabled(true);
			endAfterRadio.setEnabled(true);
			endOnRadio.setEnabled(true);
			endNeverRadio.setChecked(true);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			
			updateSummary();
		}
		
		
		private void setupWeeklyRepeatMode() {
			ArrayList<Integer> items = new ArrayList<Integer>();
			for(int i = 1; i<= 5; i++){
				items.add(i);
			}
			
			repeatFrequencyAdapter = new ArrayAdapter<Integer>(AbstractScheduleSms.this, android.R.layout.simple_spinner_item, items);
			repeatFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			repeatFrequencySpinner.setAdapter(repeatFrequencyAdapter);
			
			weekdaysLayout.setVisibility(View.VISIBLE);
			if(isInitialSetup){
				try{
					repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
				}catch (ClassCastException e) {
					repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
				}
			}
			else
				repeatFrequencySpinner.setSelection(0, true);
			repeatFrequencyLayout.setVisibility(View.VISIBLE);
			repeatFrequencySpinner.setEnabled(true);
			repeatUnitText.setText("Weeks");
			endNeverRadio.setEnabled(true);
			endAfterRadio.setEnabled(true);
			endOnRadio.setEnabled(true);
			endNeverRadio.setChecked(true);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			updateSummary();
		}
		
		
		private void setupMonthlyRepeatMode() {
			weekdaysLayout.setVisibility(View.GONE);
			
			ArrayList<Integer> items = new ArrayList<Integer>();
			for(int i = 1; i<= 12; i++){
				items.add(i);
			}
			
			repeatFrequencyAdapter = new ArrayAdapter<Integer>(AbstractScheduleSms.this, android.R.layout.simple_spinner_item, items);
			repeatFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			repeatFrequencySpinner.setAdapter(repeatFrequencyAdapter);
			
			if(isInitialSetup) {
				try{
					repeatFrequencySpinner.setSelection(((Double)values.get(Constants.REPEAT_HASH_FREQ)).intValue() - 1, true);
				}catch (ClassCastException e) {
					repeatFrequencySpinner.setSelection((Integer)values.get(Constants.REPEAT_HASH_FREQ) - 1, true);
				}
			}
			else
				repeatFrequencySpinner.setSelection(0, true);
			
			repeatFrequencyLayout.setVisibility(View.VISIBLE);
			repeatFrequencySpinner.setEnabled(true);
			repeatUnitText.setText("Months");
			endNeverRadio.setEnabled(true);
			endAfterRadio.setEnabled(true);
			endOnRadio.setEnabled(true);
			endNeverRadio.setChecked(true);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			
			updateSummary();
		}
		
		
		private void setupYearlyRepeatMode(){
			weekdaysLayout.setVisibility(View.GONE);
			repeatFrequencySpinner.setSelection(0);
			repeatFrequencyLayout.setVisibility(View.GONE);
			repeatFrequencySpinner.setEnabled(true);
			repeatUnitText.setText("Years");
			endNeverRadio.setEnabled(true);
			endAfterRadio.setEnabled(true);
			endOnRadio.setEnabled(true);
			endNeverRadio.setChecked(true);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			updateSummary();
		}
		
		
		private void showDatePickerDialog(final Calendar c){
			
			final Date date = c.getTime();
			
			DatePickerDialog datePickerDialog = new DatePickerDialog(AbstractScheduleSms.this, new OnDateSetListener() {
				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar cNew = Calendar.getInstance();
					cNew.set(year, monthOfYear, dayOfMonth);
					if(cNew.after(c)){
						setDateText(cNew);
						dateTemp = cNew.getTime();
					}
				}
			}, 1900+date.getYear(), date.getMonth(), date.getDate());
			
			datePickerDialog.show();
		}
		
		
		private void setDateText(Calendar c){
			Date date = c.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
			dateText.setText(sdf.format(date));
			updateSummary();
		}
		
		
		
		private void selectEndNeverRadio(){
			endMode = Constants.END_MODE_NEVER;
			endNeverRadio.setChecked(true);
			endAfterRadio.setChecked(false);
			endOnRadio.setChecked(false);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(false);
			updateSummary();
		}
		
		
		private void selectEndAfterRadio(){
			endMode = Constants.END_MODE_AFTER;
			endAfterRadio.setChecked(true);
			endNeverRadio.setChecked(false);
			endOnRadio.setChecked(false);
			repeatOccurSpinner.setEnabled(true);
			dateText.setEnabled(false);
			updateSummary();
		}
		
		
		private void selectEndOnRadio(){
			endMode = Constants.END_MODE_ON;
			endOnRadio.setChecked(true);
			endNeverRadio.setChecked(false);
			endAfterRadio.setChecked(false);
			repeatOccurSpinner.setEnabled(false);
			dateText.setEnabled(true);
			updateSummary();
		}



		private void updateSummary(){

			String summaryString = "";

			switch (repeatModeSpinner.getSelectedItemPosition()){
				case 0:
					summaryString += "No Repeat";
					break;
				case 1:
					if((Integer)repeatFrequencySpinner.getSelectedItem() > 1)
						summaryString += "Repeats Daily, after every " + repeatFrequencySpinner.getSelectedItem() + " days, ";
					else
						summaryString += "Repeats Daily, ";
					break;
				case 2:
					if((Integer)repeatFrequencySpinner.getSelectedItem() > 1)
						summaryString += "Repeats Weekly, after every " + repeatFrequencySpinner.getSelectedItem() + " weeks, ";
					else
						summaryString += "Repeats Weekly, ";
					break;
				case 3:
					if((Integer)repeatFrequencySpinner.getSelectedItem() > 1)
						summaryString += "Repeats Monthly, after every " + repeatFrequencySpinner.getSelectedItem() + " months, ";
					else
						summaryString += "Repeats Monthly, ";
					break;
				case 4:
					summaryString += "Repeats Yearly, ";
					break;
				default:
					break;
			}

			if(repeatModeSpinner.getSelectedItemPosition()>0){
				switch (endMode){
				case Constants.END_MODE_NEVER:
					summaryString += "never Ends.";
					break;
				case Constants.END_MODE_AFTER:
					summaryString += "ends after " + repeatOccurSpinner.getSelectedItem() + (((Integer)repeatOccurSpinner.getSelectedItem()>1)?" times.":" time.");
					break;
				case Constants.END_MODE_ON:
					summaryString += "ends on " + dateText.getText() + ".";
					break;
				default:
					break;
				}
			}

			summaryText.setText(summaryString);
		}


		private void doOnDoneTask(){
			defaultRepeatMode = modeTemp;
			defaultRepeatHash = constructHash();
		}

		private HashMap<String, Object> constructHash(){
			ArrayList<Boolean> weekBools = new ArrayList<Boolean>();
			
			int newEndMode;
			
			if(endNeverRadio.isChecked()){
				newEndMode = 0;
			}else if(endAfterRadio.isChecked()){
				newEndMode = 1;
			}else{
				newEndMode = 2;
			}
			
			weekBools.add(cbSunday.isChecked()); weekBools.add(cbMonday.isChecked());
			weekBools.add(cbTuesday.isChecked()); weekBools.add(cbWednesday.isChecked());
			weekBools.add(cbThursday.isChecked()); weekBools.add(cbFriday.isChecked());
			weekBools.add(cbSaturday.isChecked());
			
			HashMap<String, Object> newRepeatHash = new HashMap<String, Object>();
			newRepeatHash.put(Constants.REPEAT_HASH_FREQ, repeatFrequencySpinner.getSelectedItem());
			newRepeatHash.put(Constants.REPEAT_HASH_WEEK_BOOL, weekBools);
			newRepeatHash.put(Constants.REPEAT_HASH_END_MODE, newEndMode);
			newRepeatHash.put(Constants.REPEAT_HASH_END_FREQ, repeatOccurSpinner.getSelectedItem());
			newRepeatHash.put(Constants.REPEAT_HASH_END_DATE, dateTemp);
			newRepeatHash.put(Constants.REPEAT_HASH_LAST_SENT_TIME, 0);
			
			return newRepeatHash;
		}
	}
}