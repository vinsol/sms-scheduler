package com.vinsol.SMSScheduler;
 
import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vinsol.SMSScheduler.ContactAppManager.ContactAccessor;
 
public class ScheduleSMS extends ListActivity implements OnClickListener {
    final int DATE_DIALOG_ID = 1;
    final int TIME_DIALOG_ID = 2;
	
    EditText contactNumberEditText;
    
    EditText sendDateEditText, sendTimeEditText;
	
	Calendar currentTimeCalendar, scheduledTimeCalendar;
	
	int currentDate, currentMonth, currentYear;
	int currentHour, currentMinute;
	
	ListView receiverDetailListView;
	
	ArrayAdapter<String> receiverDetailAdapter;
	
	
	//An SDK-specific instance of {@link ContactAccessor}.  The activity does not need
	//to know what SDK it is running in: all idiosyncrasies of different SDKs are
	//encapsulated in the implementations of the ContactAccessor class.
    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    
    // Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;
    
    ArrayList<Receiver> listOfReceivers = new ArrayList<Receiver>();


    
    /**=========================================================== 
     * method onCreate()
     * Called when the activity is first created. 
     * ===========================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.schedule_sms);        
        
    	//======================== setting current Date and time ===========================//
    	currentTimeCalendar = Calendar.getInstance();
    	currentDate = currentTimeCalendar.get(Calendar.DATE);
    	currentMonth = currentTimeCalendar.get(Calendar.MONTH);
    	currentYear = currentTimeCalendar.get(Calendar.YEAR);
    	//if we wants hour in 24 hour format then use HOUR_OF_DAY else use HOUR
    	currentHour = currentTimeCalendar.get(Calendar.HOUR_OF_DAY);
    	currentMinute = currentTimeCalendar.get(Calendar.MINUTE);
    	
    	
    	scheduledTimeCalendar = Calendar.getInstance();
        
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
        //receiverDetailListView.setOnItemSelectedListener(this);
        
        
        receiverDetailAdapter = new ArrayAdapter<String>(this, R.layout.schedule_sms_one_receiver_view);
        
        if(receiverDetailAdapter == null){
			Log.v("In SMSScheduler -> in ScheduleSMS -> in OnCreate","receiverDetailAdapter" );
		}else{
			setListAdapter(receiverDetailAdapter);
		}
        //========================== Choose Message From Template Button ==========================//
        Button chooseMessageFromTemplateButton = (Button)findViewById(R.id.schedule_sms_message_from_template_button);
        chooseMessageFromTemplateButton.setOnClickListener(this);
        
        
        //======================== Send Date EditText ===============================//
        sendDateEditText = (EditText)findViewById(R.id.schedule_sms_send_date_edit_text);
        sendDateEditText.setInputType(InputType.TYPE_NULL);
        sendDateEditText.setOnClickListener(this);
        
        //========================== Send Time EditText ===============================//
        sendTimeEditText = (EditText)findViewById(R.id.schedule_sms_send_time_edit_text);
        sendTimeEditText.setInputType(InputType.TYPE_NULL);
        sendTimeEditText.setOnClickListener(this);
        
      
        //========================== Schedule SMS Button ==========================//
        Button scheduleSMSButton = (Button) findViewById(R.id.schedule_sms_done_button);
        scheduleSMSButton.setOnClickListener(this);
                
    }//end method onCreate
    
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
    			addToReceiverList("Unknown Name", contactNumber);
    			break;
    		}
    		case R.id.schedule_sms_add_from_contact_button: {
    			startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    			break;
    		}
    		case R.id.schedule_sms_message_from_template_button: {
    			break;
    		}
    		case R.id.schedule_sms_done_button: {
    			doneButtonHandler();
    			break;
    		}
    		case R.id.schedule_sms_send_date_edit_text: {
    			showDialog(DATE_DIALOG_ID);
    			break;
    		}
    		case R.id.schedule_sms_send_time_edit_text: {
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
    void doneButtonHandler(){
    	if((listOfReceivers.size()) <= 0) {
    		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_no_contact_number), Toast.LENGTH_LONG).show();
    	} else {
    		EditText messageEditText = (EditText)findViewById(R.id.schedule_sms_message_edit_text);
        	String message = messageEditText.getText().toString();
        	
        	if(message == null || message.equalsIgnoreCase("")) {
        		Toast.makeText(this, getString(R.string.toast_message_schedule_sms_done_blank_message), Toast.LENGTH_LONG).show();
        	} else {
        		String scheduledTime = "" + scheduledTimeCalendar.getTimeInMillis();
            	
        		long messageID = new SMSSchedulerDBHelper(this).addMessage(message, scheduledTime);
            	if(messageID != -1){
            		new SMSSchedulerDBHelper(this).addReceivers(messageID, listOfReceivers);
            		
            		Intent intent = new Intent(this, SMSListing.class);
            		finish();
            		startActivity(intent);
            		
            	}else {
            		Toast.makeText(this, "message not added ", Toast.LENGTH_SHORT).show();
            	}
        	}
    	}
    }//end Method doneButtonHandler

  
    /**================================================================
     * method onCreateDialog
     *================================================================*/
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DATE_DIALOG_ID: {
	            return new DatePickerDialog(this, dateSetListenerObject, currentYear, currentMonth, currentDate);
	        }
	        case TIME_DIALOG_ID: {
	        	return new TimePickerDialog(this, timeSetListenerObject, currentHour, currentMinute, true);
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
            		
            	sendDateEditText.setText("" + scheduledTimeCalendar.get(Calendar.MONTH + 1)
            							+ "/" + scheduledTimeCalendar.get(Calendar.DATE)
            							+ "/" + scheduledTimeCalendar.get(Calendar.YEAR));
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
                Log.v("in onTimeSet", "hour = " + hour);
                Log.v("in onTimeSet", "minute = " + minute);
                
    			//================== setting calendar time ======================//
            	scheduledTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            	scheduledTimeCalendar.set(Calendar.MINUTE, minute);
            	scheduledTimeCalendar.set(Calendar.SECOND, 0);
            	scheduledTimeCalendar.set(Calendar.MILLISECOND, 0);
            	
            	Log.v("Current Time calendar millisecond = " , "" + currentTimeCalendar.getTimeInMillis());
            	Log.v("Scheduled Time calendar millisecond = ", "" + scheduledTimeCalendar.getTimeInMillis());
            	
            	Log.v("Current Time year = "  + currentTimeCalendar.get(Calendar.YEAR) , "Scheduled Time year = "  + scheduledTimeCalendar.get(Calendar.YEAR));
            	Log.v("Current Time month = "  + currentTimeCalendar.get(Calendar.MONTH) , "Scheduled Time month = "  + scheduledTimeCalendar.get(Calendar.MONTH));
            	Log.v("Current Time date = "  + currentTimeCalendar.get(Calendar.DATE) , "Scheduled Time date = "  + scheduledTimeCalendar.get(Calendar.DATE));
            	Log.v("Current Time hour = "  + currentTimeCalendar.get(Calendar.HOUR_OF_DAY) , "Scheduled Time hour = "  + scheduledTimeCalendar.get(Calendar.HOUR_OF_DAY));
            	Log.v("Current Time minute = "  + currentTimeCalendar.get(Calendar.MINUTE) , "Scheduled Time minute = "  + scheduledTimeCalendar.get(Calendar.MINUTE));
            	
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
            	
            	sendTimeEditText.setText("" + pad(hour)
            							+ ":" + pad(minute));
            								
    		}
	};//end object timeSetListenerObject
	
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
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
