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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.vinsol.SMSScheduler.ContactAppManager.ContactAccessor;
 
public class ScheduleSMS extends ListActivity implements OnClickListener {
    final int DATE_DIALOG_ID = 1;
    final int TIME_DIALOG_ID = 2;
	
    EditText contactNumberEditText;
    
    Button sendDateButton, sendTimeButton;
    
    EditText messageEditText;
	
    Button scheduleSMSButton;
    
    Calendar scheduledTimeCalendar = Calendar.getInstance();
   
	ListView receiverDetailListView;
	
	ArrayAdapter<String> receiverDetailAdapter;
	
	Context context;
	
	
	//An SDK-specific instance of {@link ContactAccessor}.  The activity does not need
	//to know what SDK it is running in: all idiosyncrasies of different SDKs are
	//encapsulated in the implementations of the ContactAccessor class.
    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    
    // Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;
    
    ArrayList<Receiver> listOfReceivers = new ArrayList<Receiver>();

    //message used when type Of page is Edit
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
        receiverDetailListView.setDivider(getResources().getDrawable(R.drawable.divider));
        
        registerForContextMenu(receiverDetailListView);
        
        receiverDetailAdapter = new ArrayAdapter<String>(this, R.layout.schedule_sms_one_receiver_view);
        
        if(receiverDetailAdapter == null){
			Log.v("In SMSScheduler -> in ScheduleSMS -> in OnCreate","receiverDetailAdapter" );
		}else{
			setListAdapter(receiverDetailAdapter);
		}
        
        //========================== message Edit text ==========================//
        messageEditText = (EditText)findViewById(R.id.schedule_sms_message_edit_text);
        
        //========================== Choose Message From Template Button ==========================//
        Button chooseMessageFromTemplateButton = (Button)findViewById(R.id.schedule_sms_message_from_template_button);
        chooseMessageFromTemplateButton.setOnClickListener(this);
        
        
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
		
    	//=========================== fill messageEditText ============================//
		messageEditText.setText(messageForEdit.messageBody);
    	
		//=========================== fill receiversList ==============================//
    	for(int i=0; i < listOfReceivers.size(); i++ ) {
    		String contactNumber = listOfReceivers.get(i).getPhoneNumber();
    		String displayName = listOfReceivers.get(i).getDisplayName();
    		
    		if(displayName.equalsIgnoreCase(Constant.UNKNOWN_NAME)) {
    			receiverDetailAdapter.add(contactNumber);
    		}else {
    			receiverDetailAdapter.add(displayName);
    		}
    	}
    	
    	//====================== set the text "Update" of done button ==========================// 
    	scheduleSMSButton.setText("Update");
    }//end method fillfillFormWithDataForEdit
	
    
    /**===============================================================
     * method onClick
     *================================================================*/
    @Override
	public void onClick(View clickedView) {
    	
    	int idOfClickedView = clickedView.getId();
    	
    	switch(idOfClickedView){
			case R.id.schedule_sms_add_number_button: {
    			String contactNumber = contactNumberEditText.getText().toString();
    			receiverDetailAdapter.add(contactNumber);
    			contactNumberEditText.setText("");
    			addToReceiverList(Constant.UNKNOWN_NAME, contactNumber);
    			break;
    		}
    		case R.id.schedule_sms_add_from_contact_button: {
    			startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    			break;
    		}
    		case R.id.schedule_sms_message_from_template_button: {
    			Toast.makeText(this, "Under Construction :)", Toast.LENGTH_LONG).show();
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
                return mContactAccessor.loadContact(getContentResolver(), uris[0]);
            }

            @Override
            protected void onPostExecute(Receiver contactInfoObject) {
                String contactName = contactInfoObject.getDisplayName();
                String contactNumber = contactInfoObject.getPhoneNumber();
                receiverDetailAdapter.add(contactName);  
                addToReceiverList(contactName, contactNumber);
            }
        };

        task.execute(contactUri);
    }//end method getContactInfoFromContentProvider
    
    /**=====================================================================
     * method addToReceiverList
     * ====================================================================*/
    void addToReceiverList(String contactName, String contactNumber) {
    	Receiver ci = new Receiver();
    	ci.setDisplayName(contactName);
    	ci.setPhoneNumber(contactNumber);
    	
    	listOfReceivers.add(ci);
    }//end method addToReceiverList
    
    
    /**=====================================================================
     * method doneButtonHandler 
     * ====================================================================*/
    void doneButtonHandler() {
    	
    	//=============== common for both add and edit =======================//
		
    	if((listOfReceivers.size()) <= 0) {
    		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_no_contact_number), Toast.LENGTH_LONG).show();
    	} else {
        	final String message = messageEditText.getText().toString();
        	
        	if(message == null || message.equalsIgnoreCase("")) {
        		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_blank_message), Toast.LENGTH_LONG).show();
        	} else {	
        		final String scheduledTime = "" + scheduledTimeCalendar.getTimeInMillis();
            			
    			Calendar currentTimeCalendar = Calendar.getInstance();
        			
    			if(scheduledTimeCalendar.getTimeInMillis() <= currentTimeCalendar.getTimeInMillis()) {
    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    				builder.setTitle("Scheduled Time")
    						.setMessage(getString(R.string.alert_dialog_message_scheduled_time_in_past))
    						.setCancelable(false)
    						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int id) {
    				        	   saveMessageInDatabase(message, scheduledTime);
    				        	   //toDo
    				        	   //toDo
    				        	   //toDo
    				        	   //toDo
    				        	   //toDo
    				        	   //toDo
    				        	   //toDo
    				        	  // sendMessage();
    				        	   
    				        	   
    				        	   new IntentHandler().gotoSMSListingPage(context);
    				           }
    				       })
    				       .setNegativeButton("Reschedule", new DialogInterface.OnClickListener() {
    				           public void onClick(DialogInterface dialog, int id) {
    				                dialog.cancel();
    				           }
    				       });
    				AlertDialog deleteAlert = builder.create();
    				deleteAlert.show();
    			}else { //means scheduled time is in future
    				saveMessageInDatabase(message, scheduledTime);
    				
    				//toDo
    				//toDo
    				//toDo
    				//toDo
    				//toDo
    				//toDo
    				//toDo
    				
    				//rescheduleAlarmManager();
    				new IntentHandler().gotoSMSListingPage(context);
    				
    			}
        	}
    	}
    }//end Method doneButtonHandler
    
    /**====================================================================
     * saveMesageInDatabase
     *=====================================================================*/
    private boolean saveMessageInDatabase(String message, String scheduledTime){
    	//========================= for Add page =======================================//
		if(typeOfPage == Constant.PAGE_TYPE_ADD) {
			long messageID = new SMSSchedulerDBHelper(this).addMessage(message, scheduledTime);
			if(messageID != -1) {
				new SMSSchedulerDBHelper(this).addReceivers(messageID, listOfReceivers);
				return true;
			}else {
				Toast.makeText(this, getString(R.string.toast_message_schedule_sms_problem_in_adding_message), Toast.LENGTH_SHORT).show();
				return false;
			}
		}//end if(type of page is add)
		
		//========================= for Add page =======================================//
		else if(typeOfPage == Constant.PAGE_TYPE_EDIT) {
			
			long messageID = messageForEdit.id;
			int numberOfAffectedRows = new SMSSchedulerDBHelper(this).updateMessage(messageID, message, scheduledTime);
			
			if(numberOfAffectedRows != 0) {
				new SMSSchedulerDBHelper(this).updateReceivers(messageID, listOfReceivers);
				return true;
			}else {
				Toast.makeText(this, getString(R.string.toast_message_schedule_sms_problem_in_updating_message), Toast.LENGTH_SHORT).show();
				return false;
			}
		}//end else if(type of page is Edit)
		else {
			return false;
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
	

	String headingForContextMenu;
	/**=============================================================================
	 * method onCreateContextMenu
	 *==============================================================================*/
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		int positionOfClickedListItem = ((AdapterContextMenuInfo)menuInfo).position;
		
		String displayName = listOfReceivers.get(positionOfClickedListItem).getDisplayName();
		
		if(displayName.equalsIgnoreCase(Constant.UNKNOWN_NAME)){
			headingForContextMenu = listOfReceivers.get(positionOfClickedListItem).getPhoneNumber();
		} else{
			headingForContextMenu = displayName;
		}
	
		menu.setHeaderTitle(headingForContextMenu);
		
		menu.add(0, R.id.SCHEDULE_SMS_RECEIVER_CONTEXT_MENU_DELETE, 0,  "Delete");
	}

	/**=============================================================================
	 * method onContextItemSelected
	 *==============================================================================*/
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	
		int positionOfClickedListItem = info.position;
		switch (item.getItemId()) {
			case R.id.SCHEDULE_SMS_RECEIVER_CONTEXT_MENU_DELETE: {
				listOfReceivers.remove(positionOfClickedListItem);
				receiverDetailAdapter.remove(headingForContextMenu);
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
	    new OptionMenuHelper().createOptionMenu(menu);
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
