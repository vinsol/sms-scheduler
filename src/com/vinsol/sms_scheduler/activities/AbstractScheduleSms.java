/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler.activities;

import java.awt.font.NumericShaper;
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
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
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
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.SmsSchedulerApplication;
import com.vinsol.sms_scheduler.models.Contact;
import com.vinsol.sms_scheduler.models.Recipient;
import com.vinsol.sms_scheduler.receivers.SMSHandleReceiver;
import com.vinsol.sms_scheduler.utils.Log;


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
	protected SpannableStringBuilder ssb = new SpannableStringBuilder();
	protected int spanStartPosition = 0;
	protected static String originalMessage;
	protected ArrayList<ClickableSpan> clickableSpanArrayList = new ArrayList<ClickableSpan>();
	//--------------------------------------------------------------------
	
	
	
	//-------------------Variables related to new autocomplete implementation------------------------
	LinearLayout hll;
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
	//-----------------------------------------------------------------------------------------------
	
	ImageView undoButton;
	RecipientStack recipientStack = new RecipientStack();
	ArrayList<Recipient> prunedRecipients = new ArrayList<Recipient>();
	MyAdapter detailsRecipientsAdapter;
	
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
				}else if(toOpen==2){
					toOpen = 0;
					numbersText.requestFocus();
					if(Recipients.size()>0){
						if(Recipients.size()==1 && Recipients.get(0).displayName.equals(" ")){
							numbersText.setHint("Recipients");
						}else{
							numbersText.setHint(" ");
						}
						
					}
					inputMethodManager.toggleSoftInput(inputMethodManager.SHOW_FORCED, 0);
				}
			}
		}
	};
	
	
	@Override
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
		
//		numbersText.setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
		detailsRecipientsAdapter = new MyAdapter();
		
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		showMessage = settings.getBoolean("SHOW_MESSAGE", true);
		
		
		//-----------------------declarations related to new autocomplete implementation-------------------------
		inflater = AbstractScheduleSms.this.getLayoutInflater();
		ac_wrapper = (RelativeLayout) findViewById(R.id.autocomplete_wrapper);
		hll = (LinearLayout) findViewById(R.id.layouts_host);
        recipientDetailsButton = (ImageView) findViewById(R.id.recipients_detail_image);
		
        
        firstRow.ll = (LinearLayout) findViewById(R.id.edit_text_host);
        firstRow.ll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				showMessagePreference();
				
				if(!SmsSchedulerApplication.isDataLoaded){
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
					toOpen = 2;
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
        
        recipientDetailsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
//					detailsRecipientsAdapter = new MyAdapter();
					detailsList.setAdapter(detailsRecipientsAdapter);
					
					confirmButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Recipients.clear();
							for(int i = 0; i< prunedRecipients.size(); i++){
								Recipients.add(prunedRecipients.get(i));
							}
							
							refreshRecipientViews();
							if(rows.size()==1){
								if(numbersText.getParent()!=null){
									((LinearLayout)numbersText.getParent()).removeView(numbersText);
								}
								currentRow.ll.addView(numbersText);
							}
							d.cancel();
						}
					});
					
					cancelButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							d.cancel();
						}
					});
					
					undoButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
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
					infoText.setText("Please select some Recipients to show details of!");
					okButton.setOnClickListener(new OnClickListener() {
						
						@Override
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
        //TODO:
        final Row tempRow = new Row(false);
        final View sampleElement = createElement(new Recipient(-1, 1, "sa", -2, -1, 0, 0));
        tempRow.ll.addView(sampleElement);
        hll.addView(tempRow.ll);
        
        
        paint = new Paint();
		final float densityMultiplier = getBaseContext().getResources().getDisplayMetrics().density;
		final float scaledPx = 14 * densityMultiplier;
		paint.setTextSize(scaledPx);
        
        DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dpi = metrics.density;
		Log.d("width of screen : " + metrics.widthPixels);
		Log.d("width of screen in DPI : " + metrics.widthPixels/dpi);
		Log.d("dpi : " + dpi);
		
		numbersText.setDropDownAnchor(R.id.autocomplete_wrapper);
		
		
		ac_wrapper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				showMessagePreference();
				numbersText.requestFocus();
				inputMethodManager.showSoftInput(numbersText, 0);
			}
		});
		
		
		hll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				showMessagePreference();
				
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
					Log.d("Width of ll : " + firstRow.ll.getWidth()/dpi);
				}else{
					toOpen = 2;
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
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				numbersText.bringToFront();
				numbersText.requestFocus();
			}
			
			@Override
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
						if(!(str.charAt(i)== '0' ||
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
							Recipient recipient = new Recipient(-1, 1, numbersText.getText().toString().trim(), -1, -1, -1, -1);
							Recipients.add(recipient);
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
					hll.addView(numbersTextHolder.ll);
					numbersText.requestFocus();
					numbersText.bringToFront();
					numbersText.showDropDown();
				}
				
				if(numbersTextHolder!=null){
					if((currentRow.elementsWidth + textWidth)<widthOfContainerInDp){
						numbersTextHolder.ll.removeView(numbersText);
						hll.removeView(numbersTextHolder.ll);
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
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				
				if(keyCode == KeyEvent.KEYCODE_DEL){
					Log.d("backspace pressed");
					if(numbersText.getText().toString().equals("") && currentRow.views.size()>0){
						if(oncePressed){
							oncePressed = false;
							removeRecipientFromGroups(Recipients.get(Recipients.size()-1).contactId, Recipients.get(Recipients.size()-1).displayName);
							Recipients.remove(Recipients.size()-1);
//							refreshRecipientViews();
							
//							removeElement(currentRow.views.get(currentRow.views.size()-1));
							currentRow.elementsWidth = currentRow.elementsWidth - currentRow.views.get(currentRow.views.size()-1).getWidth()/dpi;
							currentRow.ll.removeView(currentRow.views.get(currentRow.views.size()-1));
							currentRow.views.remove(currentRow.views.get(currentRow.views.size()-1));
							
							if(currentRow.views.size()==0 && rows.size()>1){
								
								if(numbersTextHolder!=null){
									numbersTextHolder.ll.removeView(numbersText);
//									rows.get(rows.size()-1).ll.addView(numbersText);
								}else{
									currentRow.ll.removeView(numbersText);
								}
								hll.removeView(currentRow.ll);
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
									hll.addView(numbersTextHolder.ll);
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
					
					if(hll.getChildCount()==1){
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
		
		
		final ViewTreeObserver vto = hll.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
//				Log.d("" + firstRow.ll.getWidth()/dpi);
				widthOfContainerInDp = (int) (firstRow.ll.getWidth()/dpi);
//				Log.d("width of sample element : " + sampleElement.getWidth()/dpi);
				
				int widthOfTextInDp = (int) (paint.measureText("sa")/dpi);
//				Log.d("width of extras : " + (sampleElement.getWidth()/dpi - widthOfTextInDp));
				widthOfExtrasInDp = (int) (sampleElement.getWidth()/dpi - widthOfTextInDp);
				hll.removeView(tempRow.ll);
//				if(vto.isAlive())
//					vto.removeGlobalOnLayoutListener(this);
			}
		});
		
		
//		numbersText.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if(!hasFocus){
//					inputMethodManager.hideSoftInputFromWindow(numbersText.getWindowToken(), 0);
//				}
//			}
//		});
   
		//-------------------------------------------------------------------------------------------------------
		
		
		
		
		
		//---------------- Check to see if a recognition activity is present--------
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
        
        dataLoadWaitDialog = new Dialog(AbstractScheduleSms.this);
		dataLoadWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		Recipients.clear();
		
		myAutoCompleteAdapter = (AutoCompleteAdapter) new AutoCompleteAdapter(this);
		numbersText.setAdapter(myAutoCompleteAdapter);
        
		showMessagePreference();
		
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
	protected void onDestroy() {
		super.onDestroy();
		//TODO:
//		inputMethodManager.hideSoftInputFromInputMethod(null, 0);
	}
	
	
	@Override
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
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		numbersText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				showMessagePreference();
				if(widthOfacWrapper==0){
					widthOfacWrapper = ac_wrapper.getWidth();
					numbersText.setDropDownWidth(widthOfacWrapper);
				}
				if(SmsSchedulerApplication.isDataLoaded) {
//					numbersText.setSelection(numbersText.getText().toString().length());
					inputMethodManager.restartInput(numbersText);
				} else {
					toOpen = 2;
					dataLoadWaitDialog.setContentView(R.layout.wait_dialog);
					dataLoadWaitDialog.setCancelable(false);
					dataLoadWaitDialog.show();
				}
			}
		});
		
		
		
		numbersText.setLongClickable(false);
//		numbersText.setMovementMethod(LinkMovementMethod.getInstance());
		
		
		//----------------functionality for schedule button----------------------------
		scheduleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				scheduleButtonOnClickListener();
			}
		});
		
		
		
		//------------Date Select Button set to current date--------------------
		//Date currentDate = processDate;
		dateButton.setText(sdf.format(processDate));
//		processDate = currentDate; 
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

				dateSelectDialog.show();
			}
		});
		
		//-----------------------------------------------------------end of Date select setup---------
		
		
		
		
		//------------setting functionality of character count-------------------
		messageText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
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
	
	
	protected abstract void scheduleButtonOnClickListener(); 
	
	
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
			if(mData.size()>position)
				return mData.get(position);
			else
				return null;
		}
		
		@Override
		public Filter getFilter() {
			Filter myFilter = new Filter() {
					
				@Override
				protected FilterResults performFiltering(final CharSequence constraint) {
					
					mData.clear();
					
					final FilterResults filterResults = new FilterResults();
					final Activity activity = (Activity) AbstractScheduleSms.this;
					activity.runOnUiThread(new Runnable() {
						
						@Override
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

				@Override
				protected void publishResults(CharSequence constraints, FilterResults results) {
					if(results != null && results.count > 0) {
						final Activity activity = (Activity) AbstractScheduleSms.this;
						activity.runOnUiThread(new Runnable() {
							
							@Override
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
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean isPresent = false;
					for(int i = 0; i< Recipients.size(); i++){
						if(Recipients.get(i).contactId == shortlist.get(position).content_uri_id) {
							isPresent = true;
							break;
						}
					}
					if(!isPresent){
						final Recipient recipient = new Recipient(-1, 2, shortlist.get(position).name, shortlist.get(position).content_uri_id, -1, -1, -1);
						Recipients.add(recipient);
						
						View view = createElement(recipient);
						addView(view);
					}else{
						Toast.makeText(AbstractScheduleSms.this, shortlist.get(position).name + " is already added", Toast.LENGTH_SHORT).show();
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
	              Groups.NOTES
             };
        Uri groupsUri =  ContactsContract.Groups.CONTENT_URI;
        int count = 0;
        
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
        		
        		boolean hasChild = false;
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
        				if(contactIds.get(i)==SmsSchedulerApplication.contactsList.get(j).content_uri_id){
        					hasChild = true;
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
		parts 		 	= smsManager.divideMessage(messageText.getText().toString());
		mdba.open();
		long smsId = mdba.scheduleSms(messageText.getText().toString(), dateString, parts.size(), cal.getTimeInMillis());
		
		if(Recipients.size()==0 || messageText.getText().toString().matches("(''|[' ']*)")){
			mdba.setAsDraft(smsId);
			
		}
		if(mode==2){
			mdba.deleteSms(editedSms, AbstractScheduleSms.this);
		}
		if(Recipients.size()==0){
			Recipient recipient = new Recipient(-1, 1, " ", -1, -1, -1, -1);  // for adding as a fake span to create a draft
			Recipients.add(recipient);
		}
		
		for(int i = 0; i< Recipients.size(); i++){
			if(Recipients.get(i).type == 2){
				for(int j = 0; j< SmsSchedulerApplication.contactsList.size(); j++){
					if(Recipients.get(i).contactId == SmsSchedulerApplication.contactsList.get(j).content_uri_id){
						numbers.add(SmsSchedulerApplication.contactsList.get(j).number);
						Log.d("added Display Name : " + SmsSchedulerApplication.contactsList.get(j).name);
						long receivedRecipientId = mdba.addRecipient(smsId, SmsSchedulerApplication.contactsList.get(j).number, SmsSchedulerApplication.contactsList.get(j).name, 2, SmsSchedulerApplication.contactsList.get(j).content_uri_id);
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
	
	
	
	
	//------------------Functions and classes related to new autocomple implementation--------------------
	class Row{
		LinearLayout ll;
		ArrayList<View> views = new ArrayList<View>();
		float elementsWidth = 0;
		
		public Row(boolean first){
			if(!first){
				ll = (LinearLayout) inflater.inflate(R.layout.linear_layout, null);
				
				ll.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
//						showMessagePreference();
						
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
			
			@Override
			public void onClick(View v) {
				removeElement(view);
				Recipients.remove(recipient);
				removeRecipientFromGroups(recipient.contactId, recipient.displayName);
			}
		});
		
		
		containerLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
					hll.removeView(numbersTextHolder.ll);
					numbersTextHolder = null;
					currentRow.ll.addView(numbersText);
				}
			}
			if(currentRow.views.size()==0 && rows.size()>1){
				currentRow.ll.removeView(numbersText);
				hll.removeView(currentRow.ll);
				rows.remove(currentRow);
				currentRow = rows.get(rows.size()-1);
				if((rows.get(rows.size()-1).elementsWidth + numbersText.getWidth())<widthOfContainerInDp){
					currentRow.ll.addView(numbersText);
				}else{
					Row newRow = new Row(false);
					newRow.ll.addView(numbersText);
					numbersTextHolder = newRow;
					hll.addView(numbersTextHolder.ll);
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
//		float widthOfExtras = 36*3/2;
//		float widthOfExtrasInDp = widthOfExtras/dpi;
//		float widthOfView = textWidth + widthOfExtras;
		widthOfExtrasInDp = 35;
		Log.d("width of extras : " + widthOfExtrasInDp);
		Log.d("width of text : " + textWidth);
		int widthOfViewInDp = (int) Math.ceil(textWidth/dpi + widthOfExtrasInDp + 1.5);
		
		Log.d("width of elements : " + currentRow.elementsWidth);
		Log.d("width of view : " + widthOfViewInDp);
		Log.d("width of container : " +widthOfContainerInDp);
		
		if((currentRow.elementsWidth + widthOfViewInDp)> widthOfContainerInDp){
			
			if(numbersTextHolder==null){
				Log.d("entered for new layout");
				Row newRow = new Row(false);
				hll.addView(newRow.ll);
				currentRow.ll.removeView(numbersText);
				newRow.ll.addView(numbersText);
				rows.add(newRow);
				currentRow = newRow;
			}else{
				Log.d("entered for new layout");
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
			hll.removeView(rows.get(k).ll);
			rows.remove(rows.get(k));
		}
		if(numbersTextHolder!=null){
			numbersTextHolder.ll.removeView(numbersText);
			numbersTextHolder = null;
		}
		rows.get(i).ll.addView(numbersText);
		currentRow = rows.get(i);
		currentRow.elementsWidth = 0;
		Log.d("views in currentRow : " + currentRow.views.size());
		for(int n = 0; n< currentRow.views.size(); n++){
			currentRow.elementsWidth = currentRow.elementsWidth + currentRow.views.get(n).getWidth()/dpi;
		}
		Log.d("size of currentRow : " + currentRow.elementsWidth);
		for(int n = 0; n< views.size(); n++){
			Log.d("processed view : " + n);
			addView(views.get(n));
		}
		if((currentRow.elementsWidth + numbersText.getWidth()/dpi)>=widthOfContainerInDp){
			Row newRow = new Row(false);
			((LinearLayout)numbersText.getParent()).removeView(numbersText);
			newRow.ll.addView(numbersText);
			numbersTextHolder = newRow;
			hll.addView(numbersTextHolder.ll);
			numbersText.requestFocus();
		}
	}
	
	
	
	public void refreshRecipientViews(){
		if(numbersTextHolder!=null){
    		numbersTextHolder.ll.removeView(numbersText);
    		hll.removeView(numbersTextHolder.ll);
    		numbersTextHolder = null;
    	}else{
    		if(((LinearLayout)numbersText.getParent())!=null)
    			((LinearLayout)numbersText.getParent()).removeView(numbersText);
    	}
    	
    	for(int i = rows.size()-1; i>=0; i--){
    		hll.removeView(rows.get(i).ll);
    		rows.remove(i);
    	}
    	firstRow = new Row(false);
    	hll.addView(firstRow.ll);
    	rows.add(firstRow);
    	currentRow = firstRow;
    	
//    	for(int i = firstRow.views.size()-1; i>=0; i--){
//    		firstRow.ll.removeView(firstRow.views.get(i));
//    		firstRow.views.remove(i);
//    	}
//    	firstRow.ll.addView(numbersText);
    	
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
	}
	//----------------------------------------------------------------------------------------------------
	
	
	
	@SuppressWarnings("rawtypes")
	public class MyAdapter extends ArrayAdapter{
    	@SuppressWarnings("unchecked")
		MyAdapter(){
    		super(AbstractScheduleSms.this, R.layout.manage_groups_list_row, prunedRecipients);
    	}
    	
    	
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		final TemplateViewHolder holder;
    		if(convertView==null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.manage_groups_list_row, parent, false);
    			holder = new TemplateViewHolder();
    			holder.templateBodyLabel = (TextView)convertView.findViewById(R.id.manage_groups_row_group_name);
    			holder.deleteTemplateButton = (ImageView)convertView.findViewById(R.id.manage_groups_row_group_delete_image);
    			convertView.setTag(holder);
    		}else{
    			holder = (TemplateViewHolder) convertView.getTag();
    		}
    		final int _position  = position;
    		
    		holder.templateBodyLabel.setText(prunedRecipients.get(position).displayName);
    		
    		holder.deleteTemplateButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
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
		Log.d("getting into showprefs");
		if(showMessage){
			final Dialog d = new Dialog(AbstractScheduleSms.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setCancelable(false);
			d.setContentView(R.layout.show_message_dialog);
			
			final CheckBox checkBox = (CheckBox) d.findViewById(R.id.show_again_check);
			Button okButton = (Button) d.findViewById(R.id.ok_button);
			TextView tv = (TextView) d.findViewById(R.id.dont_show_msg_text);
			
			okButton.setOnClickListener(new OnClickListener() {
				
				@Override
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
				
				@Override
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
	
}