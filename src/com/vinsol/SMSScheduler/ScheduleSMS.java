package com.vinsol.SMSScheduler;
 
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.vinsol.SMSScheduler.ContactAppManager.ContactAccessor;
 
public class ScheduleSMS extends ListActivity implements OnClickListener {
    final int DATE_DIALOG_ID = 1;
    final int TIME_DIALOG_ID = 2;
	
    EditText contactNumberEditText;
    
    Button sendDateButton, sendTimeButton;
    	
    Button writeNewMessageButton;
    
    Button chooseMessageFromTemplateButton;
    
    Button scheduleSMSButton;
    
    TextView messageTextView;
    
    Calendar scheduledTimeCalendar = Calendar.getInstance();
   
	ListView receiverDetailListView;
	
	ReceiverListAdapter receiverListAdapter;
	
	Context context;
	
	
	//An SDK-specific instance of {@link ContactAccessor}.  The activity does not need
	//to know what SDK it is running in: all idiosyncrasies of different SDKs are
	//encapsulated in the implementations of the ContactAccessor class.
    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    
    // Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;
    
    ArrayList<Receiver> listOfReceivers = new ArrayList<Receiver>();
    ArrayList<Receiver> receiversForAdapter = new ArrayList<Receiver>();
    
    Message messageForEdit;
      
    int typeOfPage;
    
    /**=========================================================== 
     * method onCreate()
     * Called when the activity is first created. 
     * ===========================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.schedule_sms);
    
    	context = this;
    	
    	//======================== setting type of page ===========================//
    	typeOfPage = getIntent().getIntExtra(Constant.TYPE_OF_SCHEDULE_SMS_PAGE, Constant.PAGE_TYPE_ADD);
    	
    	//========================== Add phone Number Edit Text ============================//
        contactNumberEditText = (EditText)findViewById(R.id.schedule_sms_contact_number_edit_text);
        
        //========================== Add phone Number Button ============================//
        Button addPhoneNumberButton = (Button)findViewById(R.id.schedule_sms_add_number_button);
        addPhoneNumberButton.setOnClickListener(this);
        
        //========================== Add from Contact Button ==========================//
        Button addFromContactButton = (Button)findViewById(R.id.schedule_sms_add_from_contact_button);
        addFromContactButton.setOnClickListener(this);
        
        //========================== receiver Detail List View ==========================//
        receiverDetailListView = this.getListView();
        
        //================== receiver Detail List View onItemClickListener ==================//
        receiverDetailListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.performLongClick();
			}
        });
        
        //=============== receiver Detail List View  register for context menu ===================//
        registerForContextMenu(receiverDetailListView);
        
        receiverListAdapter = new ReceiverListAdapter (this, receiversForAdapter);
        
        if(!(receiverListAdapter == null)){
			setListAdapter(receiverListAdapter);
		} 
        
        //========================== write new message Button ==========================//
        writeNewMessageButton = (Button)findViewById(R.id.schedule_sms_new_message_button);
        writeNewMessageButton.setOnClickListener(this);
        
        //========================== Choose Message From Template Button ==========================//
        chooseMessageFromTemplateButton = (Button)findViewById(R.id.schedule_sms_message_from_template_button);
        chooseMessageFromTemplateButton.setOnClickListener(this);
        
        //=========================== Message TextView =============================//
        messageTextView = (TextView)findViewById(R.id.schedule_sms_message_text_view);    	
        registerForContextMenu(messageTextView);
        messageTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				v.performLongClick();
			}
        });
        
        //======================== Send Date Button ===============================//
        sendDateButton = (Button)findViewById(R.id.schedule_sms_send_date_button);
        sendDateButton.setOnClickListener(this);
        
        //========================== Send Time Button ===============================//
        sendTimeButton = (Button)findViewById(R.id.schedule_sms_send_time_button);
        sendTimeButton.setOnClickListener(this);
        
      
        //========================== Schedule SMS Button ==========================//
        scheduleSMSButton = (Button) findViewById(R.id.schedule_sms_done_button);
        scheduleSMSButton.setOnClickListener(this);
        
        //======================fill Form Data according to the pageType =======================// 
        if(typeOfPage == Constant.PAGE_TYPE_ADD) {
        	//uri will not null if we are opening app from contact screen
        	Uri uri = getIntent().getData();
        	if(uri != null) {
        		String schema = uri.getScheme();
        		if(schema.equalsIgnoreCase("smsto") || schema.equalsIgnoreCase("sms")) {
        			String schemaSpecificPart = uri.getSchemeSpecificPart();
        			Receiver contactInfoObject = mContactAccessor.loadContactFromContactNumber(context, schemaSpecificPart);
        			addToReceiverList(contactInfoObject);
        		}   	
        	}
        	fillDateAndTimeButton();
        }else if(typeOfPage == Constant.PAGE_TYPE_EDIT) {
        	fillFormWithDataForEdit();
        }
                
    }//end method onCreate
    
    /**========================================================================
	 * method filldateAndTimeButton
	 *=========================================================================*/
    void fillDateAndTimeButton(){
 
    	//========================== fill dateButton ================================//
    	String scheduledDate = CalendarDateConverter.getDateString(scheduledTimeCalendar);
        sendDateButton.setText(scheduledDate);
        
    	//========================== fill timeButton =================================//
    	String scheduledTime = CalendarDateConverter.getTimeString(scheduledTimeCalendar);
        sendTimeButton.setText(scheduledTime);
        
    }//end method fillDateAndTimeButton
    
    
	
	/**========================================================================
	 * method fillFormWithDataForEdit 
	 * if type of page is edit fill message edit text and receivers
	 *=========================================================================*/
    void fillFormWithDataForEdit(){
    	
    	messageForEdit = MessageAndReceivers.message;
		listOfReceivers = MessageAndReceivers.receivers;
		
		scheduledTimeCalendar.setTimeInMillis(messageForEdit.scheduledTimeInMilliSecond);
    	
		//============================ fillDate And time button ========================//
		fillDateAndTimeButton();
		
    	//=========================== fill messageTextView ============================//
		fillMessageTextView(messageForEdit.messageBody);
    	
		//=========================== fill receiversList ==============================//
    	for(int i=0; i < listOfReceivers.size(); i++ ) {
    		receiverListAdapter.add(listOfReceivers.get(i));
    	}
    	
    	//====================== set the text "Update" of done button ==========================// 
    	scheduleSMSButton.setText("Update");
    }//end method fillfillFormWithDataForEdit
    
    /**========================================================================
	 * method fillMessageTextView 
	 *=========================================================================*/
    void fillMessageTextView(String message) {
    	writeNewMessageButton.setVisibility(View.GONE);
    	chooseMessageFromTemplateButton.setVisibility(View.GONE);
    	
    	messageTextView.setText(message);
    	messageTextView.setVisibility(View.VISIBLE);
    }//end method fillMessageTextView
    
    /**===============================================================
     * method onClick
     *================================================================*/
    @Override
	public void onClick(View clickedView) {
    	
    	int idOfClickedView = clickedView.getId();
    	
    	switch(idOfClickedView){
			case R.id.schedule_sms_add_number_button: {
				//hide the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(contactNumberEditText.getWindowToken(), 0);
				
    			String contactNumber = contactNumberEditText.getText().toString();
    			if(contactNumber == null || contactNumber.equalsIgnoreCase("")) {
    				Toast.makeText(this, getString(R.string.toast_message_schedule_sms_blank_contact_number_edit_text), Toast.LENGTH_SHORT).show();
    			} else {
	    			contactNumberEditText.setText("");
	    			Receiver receiver = new Receiver();
	    			receiver.setPhoneNumber(contactNumber);
	    			receiver.setDisplayName(Constant.UNKNOWN_NAME);
	    			receiver.setPhoneType(Constant.UNKNOWN_TYPE);
	    			receiver.setContactImage(null); 
	    			addToReceiverList(receiver);
	    			
    			}
    			break;
    		}
    		case R.id.schedule_sms_add_from_contact_button: {
    			startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    			break;
    		}
    		case R.id.schedule_sms_new_message_button: {
    			showAlertDialogForWriteOrUpdateMessage(null);
    			break;
    		}
    		
    		case R.id.schedule_sms_message_from_template_button: {
    			showAlertDialogForChooseFromTemplate();
    			break;
    		}
    		case R.id.schedule_sms_done_button: {
    			doneButtonHandler();
    			break;
    		}
    		case R.id.schedule_sms_send_date_button: {
    			showDialog(DATE_DIALOG_ID);
    			break;
    		}
    		case R.id.schedule_sms_send_time_button: {
    			showDialog(TIME_DIALOG_ID);
    			break;
    		}
    	}//end switch
    	
    }//end method onClick
    
    
    /**=========================================================================================
     * method onActivityResult
     * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
     * will contain a reference to the contact selected by the user. We will treat it as
     * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
     *==========================================================================================*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            getContactInfoFromContentProvider(data.getData());
        }
    }//end method onActivityResult

    /**=================================================================================
     * Load contact information on a background thread.
     *==================================================================================*/
    private void getContactInfoFromContentProvider(Uri contactUri) {

    	//We should always run database queries on a background thread. The database may be
        //locked by some process for a long time.  If we locked up the UI thread while waiting
        //for the query to come back, we might get an "Application Not Responding" dialog.
        AsyncTask<Uri, Void, Receiver> task = new AsyncTask<Uri, Void, Receiver>() {

            @Override
            protected Receiver doInBackground(Uri... uris) {
    			return mContactAccessor.loadContact(ScheduleSMS.this, getContentResolver(), uris[0]);
           }

            @Override
            protected void onPostExecute(final Receiver contactInfoObject) {
                final String[] phoneNumberArray = contactInfoObject.getPhoneNumberArray() != null?contactInfoObject.getPhoneNumberArray(): new String[0];
                final String[] phoneTypeArray = contactInfoObject.getPhoneTypeArray() != null?contactInfoObject.getPhoneTypeArray(): new String[0];
                
                if(phoneNumberArray.length == 0) {
                	Toast.makeText(ScheduleSMS.this, getString(R.string.toast_message_schedule_sms_receiver_has_no_contact_number), Toast.LENGTH_LONG).show();
                } else if(phoneNumberArray.length == 1) {
                	addToReceiverList(contactInfoObject);
                } else {//means phoneNumberArray.length > 1           	
            		AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleSMS.this);
            		String alertDialogHeading = getString(R.string.alert_dialog_heading_pick_a_contact_number);
            		
            		builder.setTitle(alertDialogHeading);
            		builder.setSingleChoiceItems(phoneNumberArray, 0, new DialogInterface.OnClickListener() {
            		    public void onClick(DialogInterface dialog, int position) {
            		    	contactInfoObject.setPhoneNumber(phoneNumberArray[position]);
            		    	contactInfoObject.setPhoneType(phoneTypeArray[position]);
                        	addToReceiverList(contactInfoObject);
            		    	dialog.dismiss();
            		    }
            		});
            		AlertDialog alert = builder.create();
            		alert.show();
                }
            }
        };
        task.execute(contactUri);

    }//end method getContactInfoFromContentProvider
    
    /**=====================================================================
     * method addToReceiverList
     * ====================================================================*/
    void addToReceiverList(Receiver receiver) {
    	if(listOfReceivers.contains(receiver)) {
    		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_receiver_already_exist), Toast.LENGTH_SHORT).show();
    	} else {
    		listOfReceivers.add(receiver);
    		receiverListAdapter.add(receiver);
    	}
    }//end method addToReceiverList
    
    /**=============================================================================
     * method showAlertDialogForWriteOrUpdateMessage
     * @param message
     *==============================================================================*/
    void showAlertDialogForWriteOrUpdateMessage(String message) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String alertDialogHeading = getString(R.string.alert_dialog_heading_compose_message);
		String alertDialogMessage = getString(R.string.alert_dialog_message_compose_message);
		
		LinearLayout llForMessageEditText = new LinearLayout(this);
		llForMessageEditText.setOrientation(LinearLayout.HORIZONTAL);
		llForMessageEditText.setPadding(10, 0, 10, 0);
		
		final EditText messageEditText = new EditText(this);
		LayoutParams lpForMessageEditText = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		messageEditText.setLayoutParams(lpForMessageEditText);
		messageEditText.setLines(2);
		
		llForMessageEditText.addView(messageEditText);
		
		if(!(message == null || message.equalsIgnoreCase(""))) {
			messageEditText.setText(message);
		}
		
		builder.setTitle(alertDialogHeading);
		builder.setMessage(alertDialogMessage);
		builder.setView(llForMessageEditText);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		    	String message = messageEditText.getText().toString();
		    	if(message == null || message.equalsIgnoreCase("")) {
		    		String toastMessage = getString(R.string.toast_message_schedule_sms_blank_message);
		    		Toast.makeText(ScheduleSMS.this, toastMessage, Toast.LENGTH_SHORT).show();
		    	}else {
		    		fillMessageTextView(message);
		    	}
		    	dialog.dismiss();
		    }
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		    	dialog.dismiss();
		    }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
    }//end method showAlertDialogForWriteOrUpdateMessage
    
    /**=============================================================================
     * method showAlertDialogForChooseFromTemplate
     *==============================================================================*/
    void showAlertDialogForChooseFromTemplate() {
    	
    	ArrayList<String> templateArrayList = new SMSSchedulerDBHelper(this).retrieveTemplates();
		
		final String[] templateArray = templateArrayList.toArray(new String[0]);	
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String alertDialogHeading = getString(R.string.alert_dialog_heading_pick_a_message);
		
		builder.setTitle(alertDialogHeading);
		builder.setSingleChoiceItems(templateArray, -1, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		    	fillMessageTextView(templateArray[position]);
		    	dialog.dismiss();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
    }//end method showAlertDoalogForChooseFromTemplate
    
    
    /**=====================================================================
     * method doneButtonHandler 
     * ====================================================================*/
    void doneButtonHandler() {
    	
    	//=============== common for both add and edit =======================//
		
    	if((listOfReceivers.size()) <= 0) {
    		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_no_contact_number), Toast.LENGTH_LONG).show();
    	} else {
    		final String message = (String)messageTextView.getText();
    		
        	if(message == null || message.equalsIgnoreCase("")) {
        		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_blank_message), Toast.LENGTH_LONG).show();
        	} else {	
        		final String scheduledTime = "" + scheduledTimeCalendar.getTimeInMillis();
            			
    			Calendar currentTimeCalendar = Calendar.getInstance();
        		final long currentTimeInMillis = currentTimeCalendar.getTimeInMillis();	
    			
    			if(scheduledTimeCalendar.getTimeInMillis() <= currentTimeInMillis) {
    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    				builder.setTitle("Scheduled Time")
    						.setMessage(getString(R.string.alert_dialog_message_scheduled_time_in_past))
    						.setCancelable(false)
    						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int id) {
    								final long messageId = saveMessageAndReceiversInDatabase(message, scheduledTime);
	    				        	   
    								Thread t = new Thread(){
    									@Override
    									public void run(){
    										Message newMessage = new Message();
    										newMessage.id = messageId;
    										newMessage.messageBody = message;
    	    				        	   
    										new SMSSender().sendSMS(ScheduleSMS.this, listOfReceivers, newMessage);
    									}
    								};
    				        	   
    								t.start();
    							 
    								new IntentHandler().gotoSMSListingTabActivity(context, Constant.SELECTED_TAB_SENT_SMS);
    				           }
    				       })
    				       .setNegativeButton("Reschedule", new DialogInterface.OnClickListener() {
    				           public void onClick(DialogInterface dialog, int id) {
    				                dialog.cancel();
    				           }
    				       });
    				AlertDialog scheduleTimeInPastAlert = builder.create();
    				scheduleTimeInPastAlert.show();
    			}else { //means scheduled time is in future
    				saveMessageAndReceiversInDatabase(message, scheduledTime);
    				
    				Thread t = new Thread(){
    					@Override
    					public void run() {
    						new ScheduleAlarm().scheduleAlarm(ScheduleSMS.this, currentTimeInMillis);
    					}
    				};
    				
    				t.start();
    				
    				new IntentHandler().gotoSMSListingTabActivity(context, Constant.SELECTED_TAB_SCHEDULED_SMS);
    				
    			}
        	}
    	}
    }//end Method doneButtonHandler
    
    /**====================================================================
     * saveMesageInDatabase
     *=====================================================================*/
    private long saveMessageAndReceiversInDatabase(String message, String scheduledTime){
    	long messageId;
    	//========================= for Add page =======================================//
		if(typeOfPage == Constant.PAGE_TYPE_ADD) {
			messageId = new SMSSchedulerDBHelper(this).addMessage(message, scheduledTime);
			if(messageId != -1) {
				new SMSSchedulerDBHelper(this).addReceivers(messageId, listOfReceivers);
			}else {
				Toast.makeText(this, getString(R.string.toast_message_schedule_sms_problem_in_adding_message), Toast.LENGTH_SHORT).show();
			}
			return messageId;
		}//end if(type of page is add)
		
		//========================= for Add page =======================================//
		else if(typeOfPage == Constant.PAGE_TYPE_EDIT) {
			
			messageId = messageForEdit.id;
			int numberOfAffectedRows = new SMSSchedulerDBHelper(this).updateMessage(messageId, message, scheduledTime, Constant.STATUS_SCHEDULED);
			
			if(numberOfAffectedRows != 0) {
				new SMSSchedulerDBHelper(this).updateReceivers(messageId, listOfReceivers);				
			}else {
				Toast.makeText(this, getString(R.string.toast_message_schedule_sms_problem_in_updating_message), Toast.LENGTH_SHORT).show();
			}
			return messageId;
		}//end else if(type of page is Edit)
		else {
			return -1;
		}
    }//end method saveMessageInDatabase
  
    /**================================================================
     * method onCreateDialog
     *================================================================*/
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DATE_DIALOG_ID: {
	        	int scheduledDate = scheduledTimeCalendar.get(Calendar.DATE);
	        	int scheduledMonth = scheduledTimeCalendar.get(Calendar.MONTH);
	        	int scheduledYear = scheduledTimeCalendar.get(Calendar.YEAR);
	        	return new DatePickerDialog(this, dateSetListenerObject, scheduledYear, scheduledMonth, scheduledDate);
	        }
	        case TIME_DIALOG_ID: {
	        	//if we wants hour in 24 hour format then use HOUR_OF_DAY else use HOUR
	        	int scheduledHour = scheduledTimeCalendar.get(Calendar.HOUR_OF_DAY);
	        	int scheduledMinute = scheduledTimeCalendar.get(Calendar.MINUTE);
	            
	        	return new TimePickerDialog(this, timeSetListenerObject, scheduledHour, scheduledMinute, true);
	        }
        }//end switch
		return null;
    }//end method onCreateDialog
    
    /**==================================================================
     * object Of OnDateSetListener
     * the callback received when the user "sets" the date in the dialog
     *====================================================================*/ 
    private DatePickerDialog.OnDateSetListener dateSetListenerObject =
    	new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //================== setting calendar date ======================//
            	scheduledTimeCalendar.set(Calendar.YEAR, year);
            	scheduledTimeCalendar.set(Calendar.MONTH, monthOfYear);
            	scheduledTimeCalendar.set(Calendar.DATE, dayOfMonth);
            		
            	String dateInString = CalendarDateConverter.getDateString(scheduledTimeCalendar);
            	sendDateButton.setText(dateInString);
            }
    };//end object dateSetListenerObject
    
    /**==================================================================
     * object Of OnTimeSetListener
     * the callback received when the user "sets" the time in the dialog
     *====================================================================*/ 
    private TimePickerDialog.OnTimeSetListener timeSetListenerObject =
    	new TimePickerDialog.OnTimeSetListener() {
    		
    		@Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                
    			//================== setting calendar time ======================//
            	scheduledTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            	scheduledTimeCalendar.set(Calendar.MINUTE, minute);
            	scheduledTimeCalendar.set(Calendar.SECOND, 0);
            	scheduledTimeCalendar.set(Calendar.MILLISECOND, 0);
            	
            	//String amORpm;
            	//int amORpm1 = scheduledTimeCalendar.get(Calendar.AM_PM);
            	//Log.v("amOrPm = ", "" + amORpm1);
            	
            	//if(hour < 12){
            	//	amORpm = "AM";
            	//} else {
            	//	amORpm = "PM";
            	//}
            	
            	//int extractedHour = scheduledTimeCalendar.get(Calendar.HOUR);
            	//int extractedMinute = scheduledTimeCalendar.get(Calendar.MINUTE);
            	
            	String timeInString = CalendarDateConverter.getTimeString(scheduledTimeCalendar);
            	sendTimeButton.setText(timeInString);
    		}
	};//end object timeSetListenerObject
	
	/**=============================================================================
	 * method onKeyDown
	 *==============================================================================*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	new IntentHandler().gotoSMSListingTabActivity(context, Constant.SELECTED_TAB_SCHEDULED_SMS);  
	    }
	    return super.onKeyDown(keyCode, event);
	}//end method onKeyDown

	
	String headingForContextMenu;
	/**=============================================================================
	 * method onCreateContextMenu
	 *==============================================================================*/
	public void onCreateContextMenu(ContextMenu menu, View clickedView, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, clickedView, menuInfo);
		switch(clickedView.getId()) {
			case android.R.id.list: {
				int positionOfClickedListItem = ((AdapterContextMenuInfo)menuInfo).position;
				
				String displayName = listOfReceivers.get(positionOfClickedListItem).getDisplayName();
				
				if(displayName.equalsIgnoreCase(Constant.UNKNOWN_NAME)){
					headingForContextMenu = listOfReceivers.get(positionOfClickedListItem).getPhoneNumber();
				} else{
					headingForContextMenu = displayName;
				}
			
				menu.setHeaderTitle(headingForContextMenu);
				
				menu.add(0, R.id.SCHEDULE_SMS_RECEIVER_CONTEXT_MENU_DELETE, 0,  "Delete");
				break;
			}//end case android.R.id.list:
			
			case R.id.schedule_sms_message_text_view: {
				headingForContextMenu = "Options";
				menu.setHeaderTitle(headingForContextMenu);
				
				menu.add(0, R.id.SCHEDULE_SMS_MESSAGE_CONTEXT_MENU_UPDATE, 0,  "Update");
				menu.add(0, R.id.SCHEDULE_SMS_MESSAGE_CONTEXT_MENU_CHOOSE_FROM_TEMPLATE, 1, getString(R.string.schedule_sms_choose_message_from_template));
				break;
			}//end case schedule_sms_message_text_view:	
			default: {
				Toast.makeText(this, "For Developer -> ContextMenu for this item does not exist", Toast.LENGTH_SHORT).show();
			}
		}//end switch
	}//end method onCreateContextMenu

	/**=============================================================================
	 * method onContextItemSelected
	 *==============================================================================*/
	public boolean onContextItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case R.id.SCHEDULE_SMS_RECEIVER_CONTEXT_MENU_DELETE: {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				int positionOfClickedListItem = info.position;
				receiverListAdapter.remove(listOfReceivers.get(positionOfClickedListItem));
				listOfReceivers.remove(positionOfClickedListItem);
				return true;
			}
			case R.id.SCHEDULE_SMS_MESSAGE_CONTEXT_MENU_UPDATE: {
				String messageForUpdate = (String)messageTextView.getText();
				showAlertDialogForWriteOrUpdateMessage(messageForUpdate);
				return true;
			}
			case R.id.SCHEDULE_SMS_MESSAGE_CONTEXT_MENU_CHOOSE_FROM_TEMPLATE: {
				showAlertDialogForChooseFromTemplate();
				return true;
			}
			default: {
				return super.onContextItemSelected(item);
			}
		}//end switch
	}//end method onContextItemSelected
	
	
	/**==================================================================
	 * method onCreateOptionsMenu(Menu menu)
	 * create optionMenu
	 *===================================================================*/
	public boolean onCreateOptionsMenu(Menu menu) {
	    new OptionMenuHelper().createOptionMenu(menu, Constant.OPTION_MENU_FOR_SCHEDULED_SMS);
	    return true;
	}//end method onCreateOptionsMenu

	/**==================================================================
	 * method onOptionsItemSelected(MenuItem item)
	 * called when an option item is being clicked
	 *===================================================================*/
	public boolean onOptionsItemSelected(MenuItem item) {
	    return new OptionMenuHelper().onOptionsItemSelected(this, item);
	}//end method onOptionsItemSelected
    
}//end class scheduleSMS
